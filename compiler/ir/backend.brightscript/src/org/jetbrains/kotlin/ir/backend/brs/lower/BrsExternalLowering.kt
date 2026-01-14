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
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Lowers external declarations for BrightScript interop.
 *
 * This pass handles:
 * - External class constructors -> CreateObject() calls
 * - External function calls -> Direct BrightScript function calls
 * - @BrsName annotations for name mapping
 * - Dynamic type member access
 */
class BrsExternalLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val brsExternalFqn = FqName("kotlin.brs.BrsExternal")
    private val brsNameFqn = FqName("kotlin.brs.BrsName")
    private val brsInlineFqn = FqName("kotlin.brs.BrsInline")
    private val brsNamespaceFqn = FqName("kotlin.brs.BrsNamespace")

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(ExternalCallTransformer())
    }

    /**
     * Transforms calls to external declarations into BrightScript-compatible forms.
     */
    private inner class ExternalCallTransformer : IrElementTransformerVoid() {

        override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
            val constructor = expression.symbol.owner
            val irClass = constructor.parentAsClass

            // Check if this is an external class
            if (irClass.isExternal || irClass.hasAnnotation(brsExternalFqn)) {
                // Transform to CreateObject() call
                return transformExternalConstructorCall(expression, irClass)
            }

            return super.visitConstructorCall(expression)
        }

        override fun visitCall(expression: IrCall): IrExpression {
            val function = expression.symbol.owner

            // Check for @BrsInline annotation (inline BrightScript code)
            if (function.hasAnnotation(brsInlineFqn)) {
                return transformInlineCall(expression, function)
            }

            // Check if this is an external function
            if (function.isExternal) {
                return transformExternalCall(expression, function)
            }

            // Check for member calls on external classes
            val dispatchReceiver = expression.dispatchReceiver
            if (dispatchReceiver != null) {
                val receiverType = dispatchReceiver.type
                if (isExternalType(receiverType)) {
                    return transformExternalMemberCall(expression, function)
                }
                // Check for calls on @BrsNamespace objects
                if (isNamespaceType(receiverType)) {
                    return transformNamespaceCall(expression, function)
                }
            }

            return super.visitCall(expression)
        }

        override fun visitGetField(expression: IrGetField): IrExpression {
            val field = expression.symbol.owner
            val parentClass = field.parentClassOrNull

            // Check if field belongs to an external class
            if (parentClass != null && (parentClass.isExternal || parentClass.hasAnnotation(brsExternalFqn))) {
                return transformExternalFieldGet(expression, field, parentClass)
            }

            return super.visitGetField(expression)
        }

        override fun visitSetField(expression: IrSetField): IrExpression {
            val field = expression.symbol.owner
            val parentClass = field.parentClassOrNull

            // Check if field belongs to an external class
            if (parentClass != null && (parentClass.isExternal || parentClass.hasAnnotation(brsExternalFqn))) {
                return transformExternalFieldSet(expression, field, parentClass)
            }

            return super.visitSetField(expression)
        }

        /**
         * Transforms an external constructor call to CreateObject().
         *
         * Example:
         * ```kotlin
         * val array = RoArray() // External class
         * ```
         * Becomes:
         * ```brightscript
         * array = CreateObject("roArray")
         * ```
         */
        private fun transformExternalConstructorCall(
            expression: IrConstructorCall,
            irClass: IrClass
        ): IrExpression {
            // Get the BrightScript object type name
            val brsTypeName = getBrsTypeName(irClass)

            // Mark this call for special handling in IR-to-BRS transformation
            // The actual CreateObject() generation happens in IrToBrsTransformer
            return expression.apply {
                // Add marker attribute for the transformer
                putValueArgument(expression.valueArgumentsCount, null) // Placeholder
            }
        }

        /**
         * Transforms inline BrightScript code.
         *
         * Example:
         * ```kotlin
         * brs("m.top.visible = true")
         * ```
         */
        private fun transformInlineCall(
            expression: IrCall,
            function: IrFunction
        ): IrExpression {
            // The inline code is passed as a string argument
            // Extract it and mark for direct emission
            return expression
        }

        /**
         * Transforms a call to an external function.
         */
        private fun transformExternalCall(
            expression: IrCall,
            function: IrFunction
        ): IrExpression {
            // Get the BrightScript function name
            val brsFunctionName = getBrsFunctionName(function)

            // The actual transformation happens in IrToBrsTransformer
            // Here we just validate and prepare
            return expression
        }

        /**
         * Transforms a member call on an external object.
         *
         * Example:
         * ```kotlin
         * roArray.push(value)
         * ```
         * Becomes:
         * ```brightscript
         * roArray.push(value)
         * ```
         */
        private fun transformExternalMemberCall(
            expression: IrCall,
            function: IrFunction
        ): IrExpression {
            val brsFunctionName = getBrsFunctionName(function)
            // The actual transformation is handled in IrToBrsTransformer
            return expression
        }

        /**
         * Transforms a call on a @BrsNamespace object.
         *
         * BrighterScript namespaces compile to functions with Namespace_functionName format.
         *
         * Example:
         * ```kotlin
         * @BrsNamespace("Utils")
         * external object Utils {
         *     fun getMessage(port: Dynamic): Dynamic
         * }
         *
         * val msg = Utils.getMessage(port)
         * ```
         * Compiles to:
         * ```brightscript
         * msg = Utils_getMessage(port)
         * ```
         */
        private fun transformNamespaceCall(
            expression: IrCall,
            function: IrFunction
        ): IrExpression {
            val dispatchReceiver = expression.dispatchReceiver ?: return expression
            val receiverType = dispatchReceiver.type
            val irClass = (receiverType.classifierOrNull as? IrClassSymbol)?.owner ?: return expression

            // Get namespace name from @BrsNamespace annotation or object name
            val namespaceName = getNamespaceName(irClass)
            val functionName = getBrsFunctionName(function)

            // Store the resolved name for IrToBrsTransformer to use
            context.namespaceCallNames[expression] = "${namespaceName}_$functionName"

            return expression
        }

        /**
         * Gets the namespace name from @BrsNamespace annotation or object name.
         */
        private fun getNamespaceName(irClass: IrClass): String {
            val namespaceAnnotation = irClass.getAnnotation(brsNamespaceFqn)
            if (namespaceAnnotation != null) {
                val nameArg = namespaceAnnotation.getValueArgument(0)
                if (nameArg is IrConst) {
                    val name = nameArg.value as String
                    if (name.isNotEmpty()) {
                        return name
                    }
                }
            }
            // Default: use object name
            return irClass.name.asString()
        }

        /**
         * Transforms field access on an external object.
         *
         * Example:
         * ```kotlin
         * val name = node.id
         * ```
         * Becomes:
         * ```brightscript
         * name = node.id
         * ```
         */
        private fun transformExternalFieldGet(
            expression: IrGetField,
            field: IrField,
            parentClass: IrClass
        ): IrExpression {
            val brsFieldName = getBrsFieldName(field)
            // Field access on external objects is direct property access in BrightScript
            return expression
        }

        /**
         * Transforms field set on an external object.
         */
        private fun transformExternalFieldSet(
            expression: IrSetField,
            field: IrField,
            parentClass: IrClass
        ): IrExpression {
            val brsFieldName = getBrsFieldName(field)
            return expression
        }

        /**
         * Checks if a type is an external type.
         */
        private fun isExternalType(type: IrType): Boolean {
            val classifier = type.classifierOrNull ?: return false
            if (classifier !is IrClassSymbol) return false
            val irClass = classifier.owner
            return irClass.isExternal || irClass.hasAnnotation(brsExternalFqn)
        }

        /**
         * Checks if a type is a @BrsNamespace object.
         */
        private fun isNamespaceType(type: IrType): Boolean {
            val classifier = type.classifierOrNull ?: return false
            if (classifier !is IrClassSymbol) return false
            val irClass = classifier.owner
            return irClass.hasAnnotation(brsNamespaceFqn)
        }

        /**
         * Gets the BrightScript type name for a class.
         * Checks for @BrsName annotation, otherwise uses the class name.
         */
        private fun getBrsTypeName(irClass: IrClass): String {
            // Check for @BrsName annotation
            val brsNameAnnotation = irClass.getAnnotation(brsNameFqn)
            if (brsNameAnnotation != null) {
                val nameArg = brsNameAnnotation.getValueArgument(0)
                if (nameArg is IrConst) {
                    return nameArg.value as String
                }
            }

            // Default: use class name with "ro" prefix lowercased
            val className = irClass.name.asString()
            return if (className.startsWith("Ro")) {
                "ro${className.substring(2)}"
            } else {
                className.lowercase()
            }
        }

        /**
         * Gets the BrightScript function name.
         */
        private fun getBrsFunctionName(function: IrFunction): String {
            // Check for @BrsName annotation
            val brsNameAnnotation = function.getAnnotation(brsNameFqn)
            if (brsNameAnnotation != null) {
                val nameArg = brsNameAnnotation.getValueArgument(0)
                if (nameArg is IrConst) {
                    return nameArg.value as String
                }
            }

            // BrightScript is case-insensitive, so we use the original name
            return function.name.asString()
        }

        /**
         * Gets the BrightScript field name.
         */
        private fun getBrsFieldName(field: IrField): String {
            // Check for @BrsName annotation
            val brsNameAnnotation = field.getAnnotation(brsNameFqn)
            if (brsNameAnnotation != null) {
                val nameArg = brsNameAnnotation.getValueArgument(0)
                if (nameArg is IrConst) {
                    return nameArg.value as String
                }
            }

            return field.name.asString()
        }
    }
}

/**
 * Handles Dynamic type member access.
 *
 * The Dynamic type allows unchecked access to any member.
 * This pass transforms Dynamic member access into appropriate BrightScript patterns.
 */
class DynamicAccessLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val dynamicFqn = FqName("kotlin.brs.Dynamic")

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(DynamicAccessTransformer())
    }

    private inner class DynamicAccessTransformer : IrElementTransformerVoid() {

        override fun visitCall(expression: IrCall): IrExpression {
            val dispatchReceiver = expression.dispatchReceiver

            // Check if calling on Dynamic type
            if (dispatchReceiver != null && isDynamicType(dispatchReceiver.type)) {
                // Dynamic calls are emitted directly in BrightScript
                // The member name is used as-is without type checking
                return expression
            }

            return super.visitCall(expression)
        }

        override fun visitGetField(expression: IrGetField): IrExpression {
            val receiver = expression.receiver

            if (receiver != null && isDynamicType(receiver.type)) {
                // Dynamic field access is direct property access
                return expression
            }

            return super.visitGetField(expression)
        }

        override fun visitSetField(expression: IrSetField): IrExpression {
            val receiver = expression.receiver

            if (receiver != null && isDynamicType(receiver.type)) {
                // Dynamic field set is direct property assignment
                return expression
            }

            return super.visitSetField(expression)
        }

        private fun isDynamicType(type: IrType): Boolean {
            val classifier = type.classifierOrNull
            if (classifier !is IrClassSymbol) return false
            return classifier.owner.fqNameWhenAvailable == dynamicFqn
        }
    }
}

/**
 * Lowers SceneGraph component field observers.
 *
 * The @BrsOnChange annotation creates field observers in SceneGraph components.
 */
class FieldObserverLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val brsOnChangeFqn = FqName("kotlin.brs.BrsOnChange")
    private val brsComponentFqn = FqName("kotlin.brs.BrsComponent")

    override fun lower(irFile: IrFile) {
        // Find all @BrsComponent classes
        for (declaration in irFile.declarations) {
            if (declaration is IrClass && declaration.hasAnnotation(brsComponentFqn)) {
                processComponent(declaration)
            }
        }
    }

    private fun processComponent(irClass: IrClass) {
        // Find all @BrsOnChange methods
        val observers = mutableMapOf<String, IrFunction>()

        for (declaration in irClass.declarations) {
            if (declaration is IrFunction) {
                val onChangeAnnotation = declaration.getAnnotation(brsOnChangeFqn)
                if (onChangeAnnotation != null) {
                    // Get the field name from the annotation
                    val fieldNameArg = onChangeAnnotation.getValueArgument(0)
                    if (fieldNameArg is IrConst) {
                        val fieldName = fieldNameArg.value as String
                        observers[fieldName] = declaration
                    }
                }
            }
        }

        // Store observers for code generation
        if (observers.isNotEmpty()) {
            context.componentFieldObservers[irClass.symbol] = observers
        }
    }
}
