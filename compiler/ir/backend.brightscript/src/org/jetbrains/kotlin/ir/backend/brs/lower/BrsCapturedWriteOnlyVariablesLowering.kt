/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Ensures that captured variables which are ONLY written (never read) in local classes
 * still get fields created by LocalDeclarationsLowering.
 *
 * The Problem:
 * LocalDeclarationsLowering uses a lazy "PotentiallyUnusedField" mechanism that only creates
 * a field when it's accessed via irGet() (i.e., when the captured variable is READ).
 * If a captured variable is only written (never read) from inside a local class,
 * no field is created, and BrsCapturedVariableWriteLowering has nothing to rewrite.
 *
 * The Solution:
 * This lowering runs BEFORE LocalDeclarationsLowering and adds synthetic reads to the
 * constructor body for any captured variable that's written inside a local class method.
 * This triggers LocalDeclarationsLowering's irGet() path, ensuring a field is created.
 *
 * Example:
 * ```kotlin
 * fun testCapture() {
 *     var result: Int = 0
 *     val callback = object : Callback {
 *         override fun onResult(value: Int) {
 *             result = value  // WRITE to captured variable, but no READ
 *         }
 *     }
 * }
 * ```
 *
 * Without this lowering: No field created for `result`, write is left as local variable access
 * With this lowering: Field created, BrsCapturedVariableWriteLowering can rewrite the write
 */
class BrsCapturedWriteOnlyVariablesLowering(
    @Suppress("unused")
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            if (declaration is IrFunction) {
                declaration.body?.let { body ->
                    processFunction(body, declaration)
                }
            } else if (declaration is IrClass) {
                processClassMembers(declaration)
            }
        }
    }

    private fun processClassMembers(irClass: IrClass) {
        irClass.declarations.forEach { member ->
            when (member) {
                is IrFunction -> {
                    member.body?.let { body ->
                        processFunction(body, member)
                    }
                }
                is IrClass -> {
                    processClassMembers(member)
                }
            }
        }
    }

    /**
     * Process a function body to find local classes that write to captured variables.
     */
    private fun processFunction(body: IrBody, function: IrFunction) {
        // Collect all local variables defined in this function
        val localVariables = mutableSetOf<IrValueSymbol>()
        body.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                localVariables.add(declaration.symbol)
                declaration.acceptChildrenVoid(this)
            }
        })

        // Also include function parameters
        function.valueParameters.forEach { param ->
            localVariables.add(param.symbol)
        }
        function.dispatchReceiverParameter?.let { localVariables.add(it.symbol) }
        function.extensionReceiverParameter?.let { localVariables.add(it.symbol) }

        // Find all local classes in this function body
        body.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Check if this is a local class (anonymous object or local class)
                if (declaration.visibility == org.jetbrains.kotlin.descriptors.DescriptorVisibilities.LOCAL) {
                    processLocalClass(declaration, localVariables)
                }
                // Continue visiting nested elements
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    /**
     * Process a local class to find captured variables that are written but not read.
     */
    private fun processLocalClass(localClass: IrClass, outerVariables: Set<IrValueSymbol>) {
        // Collect variables that are written in this class's methods
        val writtenVariables = mutableSetOf<IrValueSymbol>()
        // Collect variables that are read in this class's methods
        val readVariables = mutableSetOf<IrValueSymbol>()

        localClass.declarations.forEach { member ->
            if (member is IrFunction) {
                member.body?.acceptVoid(object : IrVisitorVoid() {
                    override fun visitElement(element: IrElement) {
                        element.acceptChildrenVoid(this)
                    }

                    override fun visitSetValue(expression: IrSetValue) {
                        if (expression.symbol in outerVariables) {
                            writtenVariables.add(expression.symbol)
                        }
                        expression.acceptChildrenVoid(this)
                    }

                    override fun visitGetValue(expression: IrGetValue) {
                        if (expression.symbol in outerVariables) {
                            readVariables.add(expression.symbol)
                        }
                        expression.acceptChildrenVoid(this)
                    }
                })
            }
        }

        // Find variables that are written but not read (write-only captured variables)
        val writeOnlyVariables = writtenVariables - readVariables

        if (writeOnlyVariables.isEmpty()) {
            return
        }

        // Add synthetic reads to a METHOD body (not constructor body) to trigger field creation
        // LocalDeclarationsLowering's irGet() in constructor context uses constructor parameters directly,
        // but irGet() in method context triggers field creation via PotentiallyUnusedField.symbol access.

        // Find the first method that writes to these variables and add synthetic reads there
        for (member in localClass.declarations) {
            if (member is IrSimpleFunction) {
                val body = member.body as? IrBlockBody ?: continue

                // Check which write-only variables this method writes to
                val writtenHere = mutableSetOf<IrValueSymbol>()
                body.acceptVoid(object : IrVisitorVoid() {
                    override fun visitElement(element: IrElement) {
                        element.acceptChildrenVoid(this)
                    }
                    override fun visitSetValue(expression: IrSetValue) {
                        if (expression.symbol in writeOnlyVariables) {
                            writtenHere.add(expression.symbol)
                        }
                        expression.acceptChildrenVoid(this)
                    }
                })

                if (writtenHere.isEmpty()) continue

                // Add synthetic reads for these variables at the start of the method
                // These reads will be processed by LocalDeclarationsLowering, triggering field creation
                val syntheticReads = writtenHere.map { symbol ->
                    IrGetValueImpl(
                        member.startOffset,
                        member.endOffset,
                        symbol.owner.type,
                        symbol
                    )
                }

                // Wrap synthetic reads in a composite that evaluates to Unit
                val composite = IrCompositeImpl(
                    member.startOffset,
                    member.endOffset,
                    context.irBuiltIns.unitType,
                    origin = null,
                    statements = syntheticReads
                )
                // Add at the beginning of the method body
                body.statements.add(0, composite)
            }
        }
    }
}
