/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Extracts local classes (including lambda classes from BrsCallableReferenceLowering)
 * from function bodies and moves them to the file level so they can be properly emitted
 * as BrightScript functions.
 *
 * This is necessary because:
 * 1. Lambda classes created by BrsCallableReferenceLowering are inside IrBlock nodes
 *    within function bodies (as statements alongside the constructor call)
 * 2. Anonymous object expressions like `object : Sequence<T> { ... }` create IrClass nodes
 *    nested inside function bodies
 * 3. The main file transformation only iterates over top-level declarations
 * 4. Without extraction, these classes would be referenced but never defined in BrightScript
 *
 * This pass should run AFTER BrsCallableReferenceLowering and LocalDeclarationsLowering
 * but BEFORE the code generation phase.
 */
class BrsLocalClassExtractionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val localClasses = mutableListOf<IrClass>()

        // Collect all local classes from function bodies using a deep traversal
        // that explicitly handles IrBlock containers (where lambda classes are stored)
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Visit children first to find nested local classes
                declaration.acceptChildrenVoid(this)

                // Note: We DON'T add classes here based on parent checks.
                // Instead, we add classes when we find them directly in statement lists
                // (see visitStatements below). This handles the case where the parent
                // is set to a class even though the lambda is embedded in a block.
            }

            // Explicitly handle block bodies to traverse function bodies
            override fun visitBlockBody(body: IrBlockBody) {
                visitStatements(body.statements)
            }

            // Explicitly handle IrBlock to find classes inside blocks
            // BrsCallableReferenceLowering produces: irBlock { +clazz; +constructorCall }
            override fun visitBlock(expression: IrBlock) {
                visitStatements(expression.statements)
            }

            // Handle function calls - their arguments might contain lambda classes
            override fun visitCall(expression: IrCall) {
                expression.dispatchReceiver?.accept(this, null)
                expression.extensionReceiver?.accept(this, null)
                for (i in 0 until expression.valueArgumentsCount) {
                    expression.getValueArgument(i)?.accept(this, null)
                }
            }

            // Handle constructor calls - their arguments might contain lambda classes
            override fun visitConstructorCall(expression: IrConstructorCall) {
                for (i in 0 until expression.valueArgumentsCount) {
                    expression.getValueArgument(i)?.accept(this, null)
                }
            }

            // Handle when expressions
            override fun visitWhen(expression: IrWhen) {
                for (branch in expression.branches) {
                    branch.condition.accept(this, null)
                    branch.result.accept(this, null)
                }
            }

            // Handle try-catch
            override fun visitTry(aTry: IrTry) {
                aTry.tryResult.accept(this, null)
                for (catch in aTry.catches) {
                    catch.result.accept(this, null)
                }
                aTry.finallyExpression?.accept(this, null)
            }

            // Handle variable initializers
            override fun visitVariable(declaration: IrVariable, data: Nothing?) {
                declaration.initializer?.accept(this, null)
            }

            // Handle return statements
            override fun visitReturn(expression: IrReturn) {
                expression.value.accept(this, null)
            }

            private fun visitStatements(statements: List<IrStatement>) {
                for (stmt in statements) {
                    when (stmt) {
                        is IrClass -> {
                            // Found a class inside a block (e.g., lambda class)
                            // This class needs extraction regardless of its parent property,
                            // because the fact that it's in a statement list means it's local
                            localClasses.add(stmt)
                            // Still visit its children to find nested local classes
                            stmt.acceptChildrenVoid(this)
                        }
                        else -> {
                            // Continue traversing
                            stmt.accept(this, null)
                        }
                    }
                }
            }
        })

        // Move local classes to file level
        for (localClass in localClasses) {
            // Change parent to file
            localClass.parent = irFile

            // Add to file declarations if not already there
            if (localClass !in irFile.declarations) {
                irFile.declarations.add(localClass)
            }
        }

        // Remove local classes from their original locations in function bodies
        // This is critical for coroutine lowering which uses LivenessAnalysis,
        // which errors if it encounters local class declarations in function bodies
        if (localClasses.isNotEmpty()) {
            val classesToRemove = localClasses.toSet()
            irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
                override fun visitBlock(expression: IrBlock): IrExpression {
                    // Transform children first
                    expression.transformChildrenVoid(this)

                    // Remove class declarations from the block's statements
                    val filteredStatements = expression.statements.filter { it !in classesToRemove }
                    if (filteredStatements.size != expression.statements.size) {
                        // Statements were removed - create a new block without the class
                        if (filteredStatements.isEmpty()) {
                            // Block is now empty, return a unit expression
                            return IrCompositeImpl(
                                expression.startOffset,
                                expression.endOffset,
                                expression.type,
                                expression.origin,
                                emptyList()
                            )
                        } else if (filteredStatements.size == 1) {
                            // Block has only one statement, return it directly if it's an expression
                            val single = filteredStatements.single()
                            if (single is IrExpression) {
                                return single
                            }
                        }
                        // Update the block's statements in place
                        expression.statements.clear()
                        expression.statements.addAll(filteredStatements)
                    }
                    return expression
                }
            })
        }
    }
}
