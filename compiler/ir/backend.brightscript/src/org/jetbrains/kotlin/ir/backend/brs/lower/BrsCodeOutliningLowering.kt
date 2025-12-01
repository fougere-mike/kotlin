/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName

/**
 * Parses @BrsInline code strings into BrightScript AST.
 *
 * This lowering pass processes functions annotated with @BrsInline and:
 * 1. Extracts the BrightScript code string from the annotation
 * 2. Parses it into proper BrightScript AST nodes
 * 3. Validates the code syntax
 * 4. Identifies and captures local variables from Kotlin scope
 * 5. Stores the parsed AST for code generation
 *
 * This follows the same architecture as Kotlin/JS's JsCodeOutliningLowering.
 */
class BrsCodeOutliningLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val brsInlineFqn = FqName("kotlin.brs.BrsInline")

    override fun lower(irFile: IrFile) {
        irFile.acceptVoid(BrsInlineProcessor())
    }

    /**
     * Information about parsed @BrsInline code for a function.
     */
    data class BrsInlineInfo(
        val irFunction: IrFunction,
        val code: String,
        val parsedStatements: List<BrsStatement>,
        val capturedVariables: Set<String>,
        val errors: List<String>
    )

    /**
     * Processes functions with @BrsInline annotations.
     */
    private inner class BrsInlineProcessor : IrElementVisitorVoid {

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitFunction(declaration: IrFunction) {
            val inlineAnnotation = declaration.getAnnotation(brsInlineFqn)
            if (inlineAnnotation != null) {
                processInlineFunction(declaration, inlineAnnotation)
            }
            declaration.acceptChildrenVoid(this)
        }

        private fun processInlineFunction(
            irFunction: IrFunction,
            annotation: IrConstructorCall
        ) {
            // Extract the code string from the annotation
            val codeArg = annotation.getValueArgument(0)
            if (codeArg !is IrConst || codeArg.kind != IrConstKind.String) {
                context.reportError(
                    irFunction,
                    "@BrsInline code must be a compile-time constant string"
                )
                return
            }

            val code = codeArg.value as String

            // Parse the BrightScript code
            val parseResult = parseBrightScriptStatements(code)

            if (parseResult.hasErrors) {
                for (error in parseResult.errors) {
                    context.reportError(irFunction, "Error parsing @BrsInline code: $error")
                }
                return
            }

            // Find captured variables - identifiers in the BrightScript code
            // that correspond to parameters or local variables in the Kotlin function
            val capturedVariables = findCapturedVariables(
                parseResult.result,
                irFunction
            )

            // Store the parsed info for code generation
            val info = BrsInlineInfo(
                irFunction = irFunction,
                code = code,
                parsedStatements = parseResult.result,
                capturedVariables = capturedVariables,
                errors = parseResult.errors
            )

            context.inlineFunctionInfo[irFunction.symbol] = info
        }

        /**
         * Find identifiers in the BrightScript code that reference Kotlin parameters.
         *
         * For example:
         * ```kotlin
         * @BrsInline("return a + b")
         * fun add(a: Int, b: Int): Int = TODO()
         * ```
         *
         * The identifiers 'a' and 'b' are captured from the Kotlin function parameters.
         */
        private fun findCapturedVariables(
            statements: List<BrsStatement>,
            irFunction: IrFunction
        ): Set<String> {
            val kotlinNames = irFunction.valueParameters.map { it.name.asString() }.toSet()
            val foundNames = mutableSetOf<String>()

            val visitor = object : BrsVisitorVoid() {
                override fun visitNode(node: BrsNode, data: Unit) {
                    node.acceptChildren(this, Unit)
                }

                override fun visitIdentifier(identifier: BrsIdentifier, data: Unit) {
                    if (identifier.name in kotlinNames) {
                        foundNames.add(identifier.name)
                    }
                }
            }

            for (stmt in statements) {
                stmt.accept(visitor, Unit)
            }

            return foundNames
        }
    }
}

/**
 * Transforms @BrsInline calls in the IR to use the parsed BrightScript code.
 *
 * When a function marked with @BrsInline is called, this transformer
 * replaces the call with the inline BrightScript code, substituting
 * captured variables with the actual arguments.
 */
class BrsInlineCallTransformer(
    private val context: BrsIrBackendContext
) {

    /**
     * Check if a function call should be inlined.
     */
    fun shouldInline(call: IrCall): Boolean {
        return context.inlineFunctionInfo.containsKey(call.symbol)
    }

    /**
     * Get the inline info for a function call.
     */
    fun getInlineInfo(call: IrCall): BrsCodeOutliningLowering.BrsInlineInfo? {
        return context.inlineFunctionInfo[call.symbol] as? BrsCodeOutliningLowering.BrsInlineInfo
    }

    /**
     * Transform the @BrsInline code for a specific call site,
     * substituting captured variables with the actual arguments.
     *
     * @param info The parsed inline info
     * @param arguments Map of parameter names to their BrightScript expression values
     * @return The transformed BrightScript statements
     */
    fun transformInlineCode(
        info: BrsCodeOutliningLowering.BrsInlineInfo,
        arguments: Map<String, BrsExpression>
    ): List<BrsStatement> {
        val substituter = VariableSubstituter(arguments)
        return info.parsedStatements.map { stmt ->
            substituteStatement(stmt, substituter)
        }
    }

    private fun substituteStatement(
        stmt: BrsStatement,
        substituter: VariableSubstituter
    ): BrsStatement {
        return when (stmt) {
            is BrsBlock -> BrsBlock(
                stmt.statements.map { substituteStatement(it, substituter) }.toMutableList()
            )
            is BrsIf -> BrsIf(
                substituter.substitute(stmt.condition),
                substituteStatement(stmt.thenBranch, substituter),
                stmt.elseBranch?.let { substituteStatement(it, substituter) }
            )
            is BrsWhile -> BrsWhile(
                substituter.substitute(stmt.condition),
                substituteStatement(stmt.body, substituter)
            )
            is BrsFor -> BrsFor(
                stmt.variable,
                substituter.substitute(stmt.start),
                substituter.substitute(stmt.end),
                stmt.step?.let { substituter.substitute(it) },
                substituteStatement(stmt.body, substituter)
            )
            is BrsForEach -> BrsForEach(
                stmt.variable,
                substituter.substitute(stmt.iterable),
                substituteStatement(stmt.body, substituter)
            )
            is BrsReturn -> BrsReturn(stmt.value?.let { substituter.substitute(it) })
            is BrsExpressionStatement -> BrsExpressionStatement(
                substituter.substitute(stmt.expression)
            )
            is BrsVariable -> BrsVariable(
                stmt.name,
                stmt.type,
                stmt.initializer?.let { substituter.substitute(it) }
            )
            is BrsTry -> BrsTry(
                substituteStatement(stmt.tryBlock, substituter),
                stmt.catchVariable,
                stmt.catchBlock?.let { substituteStatement(it, substituter) }
            )
            is BrsThrow -> BrsThrow(substituter.substitute(stmt.message))
            is BrsPrint -> BrsPrint(
                stmt.expressions.map { substituter.substitute(it) }.toMutableList()
            )
            else -> stmt
        }
    }

    /**
     * Substitutes identifiers with their replacement expressions.
     */
    private class VariableSubstituter(
        private val substitutions: Map<String, BrsExpression>
    ) {
        fun substitute(expr: BrsExpression): BrsExpression {
            return when (expr) {
                is BrsIdentifier -> substitutions[expr.name] ?: expr
                is BrsBinaryOp -> BrsBinaryOp(
                    substitute(expr.left),
                    expr.operator,
                    substitute(expr.right)
                )
                is BrsUnaryOp -> BrsUnaryOp(expr.operator, substitute(expr.operand))
                is BrsFunctionCall -> BrsFunctionCall(
                    substitute(expr.target),
                    expr.arguments.map { substitute(it) }.toMutableList()
                )
                is BrsMethodCall -> BrsMethodCall(
                    substitute(expr.receiver),
                    expr.method,
                    expr.arguments.map { substitute(it) }.toMutableList()
                )
                is BrsIndexAccess -> BrsIndexAccess(
                    substitute(expr.target),
                    substitute(expr.index)
                )
                is BrsDotAccess -> BrsDotAccess(
                    substitute(expr.target),
                    expr.field
                )
                is BrsArrayLiteral -> BrsArrayLiteral(
                    expr.elements.map { substitute(it) }.toMutableList()
                )
                is BrsAALiteral -> BrsAALiteral(
                    expr.entries.map { BrsAAEntry(it.key, substitute(it.value)) }.toMutableList()
                )
                is BrsConditional -> BrsConditional(
                    substitute(expr.condition),
                    substitute(expr.thenExpr),
                    substitute(expr.elseExpr)
                )
                is BrsTypeOf -> BrsTypeOf(substitute(expr.value))
                is BrsCreateObject -> BrsCreateObject(
                    expr.objectType,
                    expr.arguments.map { substitute(it) }.toMutableList()
                )
                is BrsAnonymousFunction -> BrsAnonymousFunction(
                    expr.parameters,
                    expr.returnType,
                    BrsBlock(expr.body.statements.map {
                        substituteStatementHelper(it)
                    }.toMutableList())
                )
                // Literals don't need substitution
                else -> expr
            }
        }

        private fun substituteStatementHelper(stmt: BrsStatement): BrsStatement {
            // This is a simplified version - full implementation would be recursive
            return when (stmt) {
                is BrsExpressionStatement -> BrsExpressionStatement(substitute(stmt.expression))
                is BrsReturn -> BrsReturn(stmt.value?.let { substitute(it) })
                is BrsVariable -> BrsVariable(
                    stmt.name,
                    stmt.type,
                    stmt.initializer?.let { substitute(it) }
                )
                else -> stmt
            }
        }
    }
}

/**
 * Extension function to report errors during lowering.
 */
private fun BrsIrBackendContext.reportError(element: IrElement, message: String) {
    // In a real implementation, this would use the proper diagnostic reporting mechanism
    System.err.println("BrightScript compiler error: $message")
}
