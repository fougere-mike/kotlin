/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Detects shared variables (mutable variables captured by closures) BEFORE local class extraction.
 *
 * This lowering runs AFTER LocalDeclarationsLowering but BEFORE BrsLocalClassExtractionLowering.
 * It detects which variables need to be boxed in {value: x} AAs to support closure mutation,
 * and stores this information in the BrsIrBackendContext for use during transformation.
 *
 * The Problem:
 * The IrToBrsTransformer's detectSharedVariables() runs at transformation time, but by then
 * local classes have been extracted to file level and are no longer nested in their original
 * functions. This means the transformer can't detect that a variable is shared.
 *
 * The Solution:
 * Detect shared variables during lowering when the local classes are still in place,
 * and store the information for later use during transformation.
 */
class BrsSharedVariableDetectionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            when (declaration) {
                is IrFunction -> processFunction(declaration)
                is IrClass -> processClassMembers(declaration)
            }
        }
    }

    private fun processClassMembers(irClass: IrClass) {
        irClass.declarations.forEach { member ->
            when (member) {
                is IrFunction -> processFunction(member)
                is IrClass -> processClassMembers(member)
            }
        }
    }

    private fun processFunction(irFunction: IrFunction) {
        val body = irFunction.body ?: return
        val sharedVars = detectSharedVariables(body)

        if (sharedVars.isNotEmpty()) {
            // Store the shared variables for this function
            context.sharedVariablesByFunction[irFunction.symbol] = sharedVars
        }
    }

    /**
     * Detect mutable variables that are captured by closures and modified.
     * These variables need to be boxed in {value: x} AAs so that modifications
     * are visible outside and vice versa.
     */
    private fun detectSharedVariables(body: IrBody): Set<IrValueSymbol> {
        val sharedVars = mutableSetOf<IrValueSymbol>()

        body.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunctionExpression(expression: IrFunctionExpression) {
                // Detect captured variables for this lambda closure
                val capturedVars = detectCapturedVariables(expression.function)
                for (captured in capturedVars) {
                    if (captured.isMutable) {
                        sharedVars.add(captured.symbol)
                    }
                }
                expression.function.body?.acceptVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Handle local classes (including anonymous object expressions)
                if (declaration.visibility == org.jetbrains.kotlin.descriptors.DescriptorVisibilities.LOCAL) {
                    val capturedVars = detectCapturedVariablesInClass(declaration)
                    for (captured in capturedVars) {
                        if (captured.isMutable) {
                            sharedVars.add(captured.symbol)

                            // Track that this class will have a field holding a shared variable box.
                            // The field name is the variable name (after LocalDeclarationsLowering adds $ prefix
                            // which gets sanitized to _). We track by variable name since the field
                            // doesn't exist yet at this lowering phase.
                            val varName = captured.symbol.owner.name.asString()
                            val className = declaration.name.asString()
                            context.sharedVariableFields.add("$className._$varName")
                        }
                    }
                }
                declaration.acceptChildrenVoid(this)
            }
        })

        return sharedVars
    }

    /**
     * Detect variables captured by a lambda from outer scopes.
     */
    private fun detectCapturedVariables(function: IrSimpleFunction): List<CapturedVariable> {
        val declaredSymbols = mutableSetOf<IrValueSymbol>()
        val referencedSymbols = mutableMapOf<IrValueSymbol, Boolean>() // symbol -> isMutated

        // Add function parameters as declared
        function.valueParameters.forEach { declaredSymbols.add(it.symbol) }
        function.extensionReceiverParameter?.let { declaredSymbols.add(it.symbol) }
        function.dispatchReceiverParameter?.let { declaredSymbols.add(it.symbol) }

        function.body?.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                declaredSymbols.add(declaration.symbol)
                declaration.acceptChildrenVoid(this)
            }

            override fun visitGetValue(expression: IrGetValue) {
                val symbol = expression.symbol
                if (symbol !in declaredSymbols) {
                    val owner = symbol.owner
                    if (owner is IrVariable || owner is IrValueParameter) {
                        if (symbol !in referencedSymbols) {
                            referencedSymbols[symbol] = false
                        }
                    }
                }
                expression.acceptChildrenVoid(this)
            }

            override fun visitSetValue(expression: IrSetValue) {
                val symbol = expression.symbol
                if (symbol !in declaredSymbols) {
                    val owner = symbol.owner
                    if (owner is IrVariable || owner is IrValueParameter) {
                        referencedSymbols[symbol] = true
                    }
                }
                expression.acceptChildrenVoid(this)
            }
        })

        return referencedSymbols.map { (symbol, isMutated) ->
            CapturedVariable(symbol, isMutated)
        }
    }

    /**
     * Detect variables captured by a local class from outer scopes.
     */
    private fun detectCapturedVariablesInClass(irClass: IrClass): List<CapturedVariable> {
        val declaredSymbols = mutableSetOf<IrValueSymbol>()
        val referencedSymbols = mutableMapOf<IrValueSymbol, Boolean>() // symbol -> isMutated

        irClass.declarations.forEach { declaration ->
            when (declaration) {
                is IrSimpleFunction -> {
                    // Add function parameters as declared
                    declaration.valueParameters.forEach { declaredSymbols.add(it.symbol) }
                    declaration.extensionReceiverParameter?.let { declaredSymbols.add(it.symbol) }
                    declaration.dispatchReceiverParameter?.let { declaredSymbols.add(it.symbol) }

                    declaration.body?.acceptVoid(object : IrVisitorVoid() {
                        override fun visitElement(element: IrElement) {
                            element.acceptChildrenVoid(this)
                        }

                        override fun visitVariable(declaration: IrVariable) {
                            declaredSymbols.add(declaration.symbol)
                            declaration.acceptChildrenVoid(this)
                        }

                        override fun visitGetValue(expression: IrGetValue) {
                            val symbol = expression.symbol
                            if (symbol !in declaredSymbols) {
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    // Skip parameters added by LocalDeclarationsLowering
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    if (symbol !in referencedSymbols) {
                                        referencedSymbols[symbol] = false
                                    }
                                }
                            }
                            expression.acceptChildrenVoid(this)
                        }

                        override fun visitSetValue(expression: IrSetValue) {
                            val symbol = expression.symbol
                            if (symbol !in declaredSymbols) {
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    // Skip parameters added by LocalDeclarationsLowering
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    referencedSymbols[symbol] = true
                                }
                            }
                            expression.acceptChildrenVoid(this)
                        }
                    })
                }
                is IrConstructor -> {
                    // Analyze constructor bodies too
                    declaration.valueParameters.forEach { declaredSymbols.add(it.symbol) }
                    declaration.body?.acceptVoid(object : IrVisitorVoid() {
                        override fun visitElement(element: IrElement) {
                            element.acceptChildrenVoid(this)
                        }

                        override fun visitVariable(declaration: IrVariable) {
                            declaredSymbols.add(declaration.symbol)
                            declaration.acceptChildrenVoid(this)
                        }

                        override fun visitGetValue(expression: IrGetValue) {
                            val symbol = expression.symbol
                            if (symbol !in declaredSymbols) {
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    if (symbol !in referencedSymbols) {
                                        referencedSymbols[symbol] = false
                                    }
                                }
                            }
                            expression.acceptChildrenVoid(this)
                        }

                        override fun visitSetValue(expression: IrSetValue) {
                            val symbol = expression.symbol
                            if (symbol !in declaredSymbols) {
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    referencedSymbols[symbol] = true
                                }
                            }
                            expression.acceptChildrenVoid(this)
                        }
                    })
                }
            }
        }

        return referencedSymbols.map { (symbol, isMutated) ->
            CapturedVariable(symbol, isMutated)
        }
    }

    data class CapturedVariable(val symbol: IrValueSymbol, val isMutable: Boolean)
}
