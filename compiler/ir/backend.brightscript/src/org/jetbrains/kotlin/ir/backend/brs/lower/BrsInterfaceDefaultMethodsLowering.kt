/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.VariableRemapper
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Generates implementations for interface default methods in classes.
 *
 * BrightScript has no prototype chain or virtual method dispatch - all methods must be
 * explicitly attached to each object instance. When a class implements an interface with
 * default methods, the Kotlin IR includes "fake overrides" - synthetic declarations that
 * represent the inherited methods but have no body.
 *
 * This lowering finds fake overrides that have concrete implementations in interfaces
 * and creates real function declarations with copied bodies. The new functions have
 * `isFakeOverride = false`, so the code generator will emit them as BrightScript functions.
 *
 * Example:
 * ```kotlin
 * interface CoroutineContext.Element {
 *     fun get(key: Key<*>): Element? = if (this.key == key) this else null  // default impl
 * }
 *
 * class JobImpl : CoroutineContext.Element { ... }  // fake override for get()
 * ```
 *
 * After this lowering, JobImpl has a real `get()` method with the copied body.
 *
 * This must run BEFORE code generation since BrightScript has no virtual dispatch.
 */
class BrsInterfaceDefaultMethodsLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        // Collect all classes (including nested ones) that need processing
        val classesToProcess = mutableListOf<IrClass>()

        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Visit children first to find nested classes
                declaration.acceptChildrenVoid(this)

                // Process classes (including abstract classes) but not interfaces.
                // Abstract classes also need interface default methods because BrightScript
                // has no prototype chain - all methods must be attached to each object.
                // When a concrete class inherits from an abstract class via super_create_k_(),
                // it needs the abstract class to already have all interface methods attached.
                if (declaration.kind == ClassKind.CLASS && !declaration.isInterface) {
                    classesToProcess.add(declaration)
                }
            }
        })

        // Process each class
        for (irClass in classesToProcess) {
            processClass(irClass)
        }
    }

    private fun processClass(irClass: IrClass) {
        // Find fake overrides that have default implementations in interfaces
        val fakeOverrides = irClass.declarations.filterIsInstance<IrSimpleFunction>()
            .filter { it.isFakeOverride }

        val replacements = mutableListOf<Pair<IrSimpleFunction, IrSimpleFunction>>()

        for (fakeOverride in fakeOverrides) {
            // Find the real implementation by resolving the fake override
            val resolved = fakeOverride.resolveFakeOverride()
            if (resolved != null && resolved != fakeOverride) {
                // Check if the resolved function is from an interface (default method)
                val parent = resolved.parent
                if (parent is IrClass && parent.isInterface && resolved.body != null) {
                    // Create a real implementation
                    val realFunction = createRealImplementation(fakeOverride, resolved, irClass)
                    if (realFunction != null) {
                        replacements.add(fakeOverride to realFunction)
                    }
                }
            }
        }

        // Replace fake overrides with real functions
        for ((fakeOverride, realFunction) in replacements) {
            val index = irClass.declarations.indexOf(fakeOverride)
            if (index >= 0) {
                irClass.declarations[index] = realFunction
            }
        }
    }

    private fun createRealImplementation(
        fakeOverride: IrSimpleFunction,
        interfaceMethod: IrSimpleFunction,
        targetClass: IrClass
    ): IrSimpleFunction? {
        val interfaceBody = interfaceMethod.body ?: return null

        // Create a new function declaration based on the fake override
        val newFunction = context.irFactory.createSimpleFunction(
            startOffset = fakeOverride.startOffset,
            endOffset = fakeOverride.endOffset,
            origin = IrDeclarationOrigin.DEFINED,
            name = fakeOverride.name,
            visibility = fakeOverride.visibility,
            isInline = fakeOverride.isInline,
            isExpect = false,
            returnType = fakeOverride.returnType,
            modality = fakeOverride.modality,
            symbol = IrSimpleFunctionSymbolImpl(),
            isTailrec = fakeOverride.isTailrec,
            isSuspend = fakeOverride.isSuspend,
            isOperator = fakeOverride.isOperator,
            isInfix = fakeOverride.isInfix,
            isExternal = false,
            containerSource = null,
            isFakeOverride = false
        )

        newFunction.parent = targetClass

        // Copy dispatch receiver parameter with the target class's type
        fakeOverride.dispatchReceiverParameter?.let { originalDispatch ->
            newFunction.dispatchReceiverParameter = context.irFactory.createValueParameter(
                startOffset = originalDispatch.startOffset,
                endOffset = originalDispatch.endOffset,
                origin = originalDispatch.origin,
                kind = IrParameterKind.DispatchReceiver,
                name = originalDispatch.name,
                type = targetClass.defaultType,
                isAssignable = originalDispatch.isAssignable,
                symbol = IrValueParameterSymbolImpl(),
                varargElementType = null,
                isCrossinline = false,
                isNoinline = false,
                isHidden = false
            ).also {
                it.parent = newFunction
            }
        }

        // Copy extension receiver parameter if any
        fakeOverride.extensionReceiverParameter?.let { originalExt ->
            newFunction.extensionReceiverParameter = context.irFactory.createValueParameter(
                startOffset = originalExt.startOffset,
                endOffset = originalExt.endOffset,
                origin = originalExt.origin,
                kind = IrParameterKind.ExtensionReceiver,
                name = originalExt.name,
                type = originalExt.type,
                isAssignable = originalExt.isAssignable,
                symbol = IrValueParameterSymbolImpl(),
                varargElementType = originalExt.varargElementType,
                isCrossinline = originalExt.isCrossinline,
                isNoinline = originalExt.isNoinline,
                isHidden = originalExt.isHidden
            ).also {
                it.parent = newFunction
            }
        }

        // Copy value parameters
        newFunction.valueParameters = fakeOverride.valueParameters.map { originalParam ->
            context.irFactory.createValueParameter(
                startOffset = originalParam.startOffset,
                endOffset = originalParam.endOffset,
                origin = originalParam.origin,
                kind = IrParameterKind.Regular,
                name = originalParam.name,
                type = originalParam.type,
                isAssignable = originalParam.isAssignable,
                symbol = IrValueParameterSymbolImpl(),
                varargElementType = originalParam.varargElementType,
                isCrossinline = originalParam.isCrossinline,
                isNoinline = originalParam.isNoinline,
                isHidden = originalParam.isHidden
            ).also {
                it.parent = newFunction
                // Copy default value if present
                originalParam.defaultValue?.let { dv ->
                    it.defaultValue = dv.expression.deepCopyWithSymbols(newFunction).let { expr ->
                        context.irFactory.createExpressionBody(dv.startOffset, dv.endOffset, expr)
                    }
                }
            }
        }

        // Copy type parameters
        newFunction.typeParameters = fakeOverride.typeParameters.map { originalTypeParam ->
            context.irFactory.createTypeParameter(
                startOffset = originalTypeParam.startOffset,
                endOffset = originalTypeParam.endOffset,
                origin = originalTypeParam.origin,
                name = originalTypeParam.name,
                symbol = IrTypeParameterSymbolImpl(),
                variance = originalTypeParam.variance,
                index = originalTypeParam.index,
                isReified = originalTypeParam.isReified
            ).also {
                it.parent = newFunction
                it.superTypes = originalTypeParam.superTypes.toList()
            }
        }

        // Copy overridden symbols
        newFunction.overriddenSymbols = fakeOverride.overriddenSymbols.toList()

        // Copy annotations
        newFunction.annotations = fakeOverride.annotations.toList()

        // Deep copy the body from the interface method
        val copiedBody = interfaceBody.deepCopyWithSymbols(newFunction)

        // Build parameter remapping from interface method parameters to new function parameters
        val parameterMapping = mutableMapOf<IrValueParameter, IrValueDeclaration>()

        // Map dispatch receiver (interface's `this` -> class's `this`)
        interfaceMethod.dispatchReceiverParameter?.let { interfaceDispatch ->
            newFunction.dispatchReceiverParameter?.let { newDispatch ->
                parameterMapping[interfaceDispatch] = newDispatch
            }
        }

        // Map extension receiver if present
        interfaceMethod.extensionReceiverParameter?.let { interfaceExt ->
            newFunction.extensionReceiverParameter?.let { newExt ->
                parameterMapping[interfaceExt] = newExt
            }
        }

        // Map value parameters
        interfaceMethod.valueParameters.forEachIndexed { index, interfaceParam ->
            if (index < newFunction.valueParameters.size) {
                parameterMapping[interfaceParam] = newFunction.valueParameters[index]
            }
        }

        // Apply parameter remapping to the copied body
        if (parameterMapping.isNotEmpty()) {
            val remapper = VariableRemapper(parameterMapping)
            copiedBody.transform(remapper, null)
        }

        newFunction.body = copiedBody

        return newFunction
    }
}
