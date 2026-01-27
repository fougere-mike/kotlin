/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * BrightScript-specific SharedVariablesLowering that boxes ALL mutable variables captured by closures.
 *
 * Unlike the standard SharedVariablesLowering, this version does NOT skip inline lambdas.
 * This is necessary because BrightScript cannot inline lambda functions - they always become
 * closure objects at runtime. Therefore, mutable variables captured by inline lambdas must
 * also be boxed.
 *
 * Example:
 * ```kotlin
 * var counter = 0
 * list.forEach { counter++ }  // forEach is inline, but counter must still be boxed
 * ```
 *
 * On JVM, `forEach` would be inlined and `counter++` would directly access the local variable.
 * On BrightScript, `forEach` creates a closure object, so `counter` must be boxed in {value: x}.
 */
class BrsSharedVariablesLowering(
    private val context: BrsIrBackendContext
) : BodyLoweringPass {

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        SharedVariablesTransformer(context.sharedVariablesManager, irBody, container).transform()
    }

    private class SharedVariablesTransformer(
        private val sharedVariablesManager: SharedVariablesManager,
        private val irBody: IrBody,
        private val irDeclaration: IrDeclaration
    ) {
        private val sharedVariables = mutableSetOf<IrVariable>()

        fun transform() {
            collectSharedVariables()
            if (sharedVariables.isEmpty()) return
            rewriteSharedVariables()
        }

        /**
         * Collect mutable variables that are accessed from a different scope than where they're declared.
         *
         * IMPORTANT: Unlike the standard implementation, this does NOT skip inline lambdas.
         * All lambdas (inline or not) that capture a mutable variable cause that variable to be boxed.
         */
        private fun collectSharedVariables() {
            irBody.accept(object : IrVisitor<Unit, IrDeclarationParent?>() {
                val relevantVars = HashSet<IrVariable>()

                override fun visitElement(element: IrElement, data: IrDeclarationParent?) {
                    element.acceptChildren(this, data)
                }

                // Process calls, but DON'T skip inline lambdas like the standard implementation does.
                // This ensures that variables captured by inline lambdas are also boxed.
                override fun visitCall(expression: IrCall, data: IrDeclarationParent?) {
                    // Just visit all children without special handling for inline functions
                    super.visitCall(expression, data)
                }

                override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclarationParent?) {
                    // Update the parent context when entering a new declaration
                    super.visitDeclaration(declaration, declaration as? IrDeclarationParent ?: data)
                }

                // Handle IrRichFunctionReference - visit the invoke function with proper parent tracking
                override fun visitRichFunctionReference(expression: IrRichFunctionReference, data: IrDeclarationParent?) {
                    // Visit bound values (captured variables)
                    expression.boundValues.forEach { it.accept(this, data) }

                    // Visit the invoke function - this is where variable accesses occur
                    // Update parent to the invoke function so we can detect cross-boundary access
                    val invokeFunction = expression.invokeFunction
                    invokeFunction.accept(this, invokeFunction as IrDeclarationParent)
                }

                override fun visitVariable(declaration: IrVariable, data: IrDeclarationParent?) {
                    declaration.acceptChildren(this, data)

                    // Track mutable local variables (but not lateinit)
                    if (declaration.isVar && !declaration.isLateinit) {
                        relevantVars.add(declaration)
                    }
                }

                override fun visitValueAccess(expression: IrValueAccessExpression, data: IrDeclarationParent?) {
                    expression.acceptChildren(this, data)

                    val value = expression.symbol.owner
                    // Check if this is a variable declared in a different scope
                    if (value in relevantVars && value is IrVariable && value.parent != data) {
                        sharedVariables.add(value)
                    }
                }
            }, irDeclaration as? IrDeclarationParent ?: irDeclaration.parent)
        }

        private fun rewriteSharedVariables() {
            val transformedSymbols = HashMap<IrVariableSymbol, IrVariableSymbol>()

            irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
                override fun visitVariable(declaration: IrVariable): IrStatement {
                    declaration.transformChildrenVoid(this)

                    if (declaration !in sharedVariables) return declaration

                    val newDeclaration = sharedVariablesManager.declareSharedVariable(declaration)
                    transformedSymbols[declaration.symbol] = newDeclaration.symbol

                    return newDeclaration
                }

                override fun visitGetValue(expression: IrGetValue): IrExpression {
                    val newSymbol = transformedSymbols[expression.symbol]
                        ?: return super.visitGetValue(expression)

                    return sharedVariablesManager.getSharedValue(newSymbol, expression)
                }

                override fun visitSetValue(expression: IrSetValue): IrExpression {
                    expression.transformChildrenVoid(this)

                    val newSymbol = transformedSymbols[expression.symbol]
                        ?: return expression

                    return sharedVariablesManager.setSharedValue(newSymbol, expression)
                }
            })
        }
    }
}

/**
 * File lowering pass wrapper for BrsSharedVariablesLowering.
 */
class BrsSharedVariablesLoweringPass(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = BrsSharedVariablesLowering(context)

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            when (declaration) {
                is IrFunction -> {
                    declaration.body?.let { body ->
                        delegate.lower(body, declaration)
                    }
                }
                is IrClass -> lowerClass(declaration)
            }
        }
    }

    private fun lowerClass(irClass: IrClass) {
        irClass.declarations.forEach { member ->
            when (member) {
                is IrFunction -> {
                    member.body?.let { body ->
                        delegate.lower(body, member)
                    }
                }
                is IrClass -> lowerClass(member)
            }
        }
    }
}
