/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName

/**
 * Lowering pass that evaluates @BrsConstant object properties at compile time.
 *
 * This pass:
 * 1. Identifies all @BrsConstant object declarations
 * 2. Evaluates all property initializers to constant values
 * 3. Stores evaluated values in context.mapping for use during code generation
 * 4. Reports errors for non-constant expressions
 *
 * Supported expressions:
 * - Primitive literals (Int, Long, Float, Double, String, Boolean)
 * - String concatenation
 * - Arithmetic operators (+, -, *, /)
 * - Boolean operators (&&, ||, !)
 * - References to other @BrsConstant properties
 * - Enum ordinal and name values
 *
 * Must run after BrsEnumOrdinalPrecomputeLowering so enum ordinals are available.
 */
class BrsConstantEvaluationLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val brsConstantFqn = FqName("kotlin.brs.BrsConstant")

    // Evaluation context: maps property symbols to their evaluated values
    // Used for transitive constant resolution within and across @BrsConstant objects
    private val evaluatedConstants = mutableMapOf<IrProperty, IrConst>()

    // Track objects being processed to detect circular dependencies
    private val processingObjects = mutableSetOf<IrClass>()

    override fun lower(irFile: IrFile) {
        // First pass: identify all @BrsConstant objects in the file
        val constantObjects = mutableListOf<IrClass>()

        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                if (declaration.kind == ClassKind.OBJECT &&
                    declaration.hasAnnotation(brsConstantFqn)) {
                    constantObjects.add(declaration)
                }
                super.visitClass(declaration)
            }
        })

        // Evaluate each constant object's properties
        for (obj in constantObjects) {
            evaluateConstantObject(obj)
        }
    }

    /**
     * Evaluates all properties in a @BrsConstant object.
     */
    private fun evaluateConstantObject(irClass: IrClass) {
        // Check for circular dependencies
        if (irClass in processingObjects) {
            context.reportError(
                irClass,
                "@BrsConstant object '${irClass.name}' has circular dependency."
            )
            return
        }

        // Skip if already processed
        if (irClass in context.mapping.constantObjectClasses) {
            return
        }

        processingObjects.add(irClass)

        val propertyValues = mutableMapOf<String, IrConst>()

        for (declaration in irClass.declarations) {
            when (declaration) {
                is IrProperty -> {
                    if (!declaration.isVar) { // Only val properties
                        val backingField = declaration.backingField
                        val initializer = backingField?.initializer?.expression

                        if (initializer != null) {
                            val evaluated = evaluateExpression(initializer, irClass)
                            if (evaluated != null) {
                                propertyValues[declaration.name.asString()] = evaluated
                                evaluatedConstants[declaration] = evaluated
                            } else {
                                reportNonConstantError(declaration, initializer)
                            }
                        }
                    } else {
                        // Defense-in-depth: FIR's BRS_BRSCONSTANT_VAR is the primary diagnostic for
                        // user source. This IR branch fires for klib-deserialized callers that bypass
                        // FIR, or when the FIR diagnostic is @Suppress-ed at the property. The "[IR] "
                        // prefix lets AbstractBrsDiagnosticTest's ignoredMessagePatterns filter this
                        // message during fixture verification.
                        context.reportError(
                            declaration,
                            "[IR] @BrsConstant objects cannot contain var properties. " +
                            "Use val instead: ${declaration.name}"
                        )
                    }
                }
            }
        }

        // Store evaluated constants in context for code generation
        context.mapping.constantObjectProperties[irClass] = propertyValues
        context.mapping.constantObjectClasses.add(irClass)

        processingObjects.remove(irClass)
    }

    /**
     * Evaluate an expression to a constant value.
     * Returns null if the expression cannot be evaluated at compile time.
     */
    private fun evaluateExpression(expression: IrExpression, containingClass: IrClass): IrConst? {
        return when (expression) {
            is IrConst -> expression

            is IrStringConcatenation -> evaluateStringConcatenation(expression, containingClass)

            is IrCall -> evaluateCall(expression, containingClass)

            is IrGetField -> evaluateGetField(expression, containingClass)

            is IrGetEnumValue -> {
                // Enum values themselves aren't scalar constants, but ordinal/name are
                // Return null here - property access is handled in evaluateCall
                null
            }

            is IrWhen -> evaluateWhen(expression, containingClass)

            is IrTypeOperatorCall -> {
                // Handle implicit coercion, casts, etc.
                when (expression.operator) {
                    IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                    IrTypeOperator.IMPLICIT_CAST,
                    IrTypeOperator.CAST,
                    IrTypeOperator.SAFE_CAST -> evaluateExpression(expression.argument, containingClass)
                    else -> null
                }
            }

            else -> null
        }
    }

    /**
     * Evaluates IrWhen expressions that represent short-circuit boolean operations.
     * In IR, `a && b` is lowered to `when { a -> b; else -> false }`
     * and `a || b` is lowered to `when { a -> true; else -> b }`
     */
    private fun evaluateWhen(expression: IrWhen, containingClass: IrClass): IrConst? {
        val origin = expression.origin

        // Handle && (ANDAND) operator
        if (origin == IrStatementOrigin.ANDAND && expression.branches.size == 2) {
            val condition = evaluateExpression(expression.branches[0].condition, containingClass) ?: return null
            if (condition.kind != IrConstKind.Boolean) return null

            return if (condition.value as Boolean) {
                // If first operand is true, result is second operand
                evaluateExpression(expression.branches[0].result, containingClass)
            } else {
                // If first operand is false, result is false
                IrConstImpl.boolean(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.booleanType,
                    false
                )
            }
        }

        // Handle || (OROR) operator
        if (origin == IrStatementOrigin.OROR && expression.branches.size == 2) {
            val condition = evaluateExpression(expression.branches[0].condition, containingClass) ?: return null
            if (condition.kind != IrConstKind.Boolean) return null

            return if (condition.value as Boolean) {
                // If first operand is true, result is true
                IrConstImpl.boolean(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.booleanType,
                    true
                )
            } else {
                // If first operand is false, result is second operand
                evaluateExpression(expression.branches[1].result, containingClass)
            }
        }

        return null
    }

    /**
     * Evaluates string concatenation by evaluating each part and joining them.
     */
    private fun evaluateStringConcatenation(
        expression: IrStringConcatenation,
        containingClass: IrClass
    ): IrConst? {
        val parts = expression.arguments.map { arg ->
            val evaluated = evaluateExpression(arg, containingClass) ?: return null
            evaluated.value.toString()
        }
        return IrConstImpl.string(
            expression.startOffset,
            expression.endOffset,
            context.irBuiltIns.stringType,
            parts.joinToString("")
        )
    }

    /**
     * Evaluates function calls including property getters and arithmetic operations.
     */
    private fun evaluateCall(expression: IrCall, containingClass: IrClass): IrConst? {
        val function = expression.symbol.owner
        val functionName = function.name.asString()

        // Handle property getters on @BrsConstant objects
        if (functionName.startsWith("<get-")) {
            val propName = functionName.removePrefix("<get-").removeSuffix(">")
            val receiver = expression.dispatchReceiver

            // Reference to property in same @BrsConstant object
            // Receiver could be:
            // - IrGetObjectValue (explicit object access: Math.A)
            // - null (implicit this access, sometimes)
            // - IrGetValue referencing 'this' (implicit this access inside object)
            val isSameObjectAccess = when {
                receiver == null -> true
                receiver is IrGetObjectValue && receiver.symbol.owner == containingClass -> true
                receiver is IrGetValue -> {
                    // IrGetValue with name "<this>" indicates implicit this access
                    val valueName = receiver.symbol.owner.name.asString()
                    valueName == "<this>" || valueName == "this"
                }
                else -> false
            }

            if (isSameObjectAccess) {
                val prop = containingClass.declarations
                    .filterIsInstance<IrProperty>()
                    .find { it.name.asString() == propName }
                if (prop != null) {
                    return evaluatedConstants[prop]
                        ?: prop.backingField?.initializer?.expression?.let {
                            evaluateExpression(it, containingClass)
                        }
                }
            }

            // Reference to property in another @BrsConstant object
            if (receiver is IrGetObjectValue) {
                val otherClass = receiver.symbol.owner
                if (otherClass.hasAnnotation(brsConstantFqn)) {
                    // Ensure the other object is evaluated first
                    if (otherClass !in context.mapping.constantObjectClasses) {
                        evaluateConstantObject(otherClass)
                    }
                    return context.getConstantObjectProperty(otherClass, propName)
                }
            }

            // Handle enum ordinal/name access
            if (receiver is IrGetEnumValue) {
                val entry = receiver.symbol.owner
                when (propName) {
                    "ordinal" -> {
                        context.getEnumOrdinal(entry)?.let { ordinal ->
                            return IrConstImpl.int(
                                expression.startOffset,
                                expression.endOffset,
                                context.irBuiltIns.intType,
                                ordinal
                            )
                        }
                    }
                    "name" -> {
                        context.getEnumName(entry)?.let { name ->
                            return IrConstImpl.string(
                                expression.startOffset,
                                expression.endOffset,
                                context.irBuiltIns.stringType,
                                name
                            )
                        }
                    }
                    else -> {
                        // Check for custom enum properties
                        context.getEnumConstantProperties(entry)?.get(propName)?.let { value ->
                            return valueToIrConst(value, expression)
                        }
                    }
                }
            }
        }

        // Handle arithmetic and boolean operations
        val origin = expression.origin
        if (origin != null) {
            return evaluateArithmetic(expression, origin, containingClass)
        }

        // Handle arithmetic operations by function name (when no origin marker)
        return evaluateArithmeticByName(expression, functionName, containingClass)
    }

    /**
     * Evaluates arithmetic operations by function name when IrStatementOrigin is not available.
     * This handles cases like `val SUM = A + B` within the same @BrsConstant object.
     */
    private fun evaluateArithmeticByName(
        expression: IrCall,
        functionName: String,
        containingClass: IrClass
    ): IrConst? {
        val dispatchReceiver = expression.dispatchReceiver ?: return null

        // Binary operations
        if (expression.valueArgumentsCount > 0) {
            val argument = expression.getValueArgument(0) ?: return null
            val left = evaluateExpression(dispatchReceiver, containingClass) ?: return null
            val right = evaluateExpression(argument, containingClass) ?: return null

            return when (functionName) {
                "plus" -> evaluateBinaryPlus(left, right, expression)
                "minus" -> evaluateBinaryMinus(left, right, expression)
                "times" -> evaluateBinaryMul(left, right, expression)
                "div" -> evaluateBinaryDiv(left, right, expression)
                "and" -> evaluateBooleanAnd(left, right, expression)
                "or" -> evaluateBooleanOr(left, right, expression)
                else -> null
            }
        }

        // Unary operations
        val operand = evaluateExpression(dispatchReceiver, containingClass) ?: return null

        return when (functionName) {
            "unaryMinus" -> evaluateUnaryMinus(operand, expression)
            "not" -> evaluateNot(operand, expression)
            else -> null
        }
    }

    /**
     * Evaluates arithmetic and boolean operations.
     */
    private fun evaluateArithmetic(
        expression: IrCall,
        origin: IrStatementOrigin,
        containingClass: IrClass
    ): IrConst? {
        val dispatchReceiver = expression.dispatchReceiver
        val argument = if (expression.valueArgumentsCount > 0) expression.getValueArgument(0) else null

        // Binary operations
        if (dispatchReceiver != null && argument != null) {
            val left = evaluateExpression(dispatchReceiver, containingClass) ?: return null
            val right = evaluateExpression(argument, containingClass) ?: return null

            return when (origin) {
                IrStatementOrigin.PLUS -> evaluateBinaryPlus(left, right, expression)
                IrStatementOrigin.MINUS -> evaluateBinaryMinus(left, right, expression)
                IrStatementOrigin.MUL -> evaluateBinaryMul(left, right, expression)
                IrStatementOrigin.DIV -> evaluateBinaryDiv(left, right, expression)
                IrStatementOrigin.ANDAND -> evaluateBooleanAnd(left, right, expression)
                IrStatementOrigin.OROR -> evaluateBooleanOr(left, right, expression)
                else -> null
            }
        }

        // Unary operations
        if (dispatchReceiver != null && argument == null) {
            val operand = evaluateExpression(dispatchReceiver, containingClass) ?: return null

            return when (origin) {
                IrStatementOrigin.UMINUS -> evaluateUnaryMinus(operand, expression)
                IrStatementOrigin.EXCL -> evaluateNot(operand, expression)
                else -> null
            }
        }

        return null
    }

    // ==================== Binary Operation Implementations ====================

    private fun evaluateBinaryPlus(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        return when {
            left.kind == IrConstKind.Int && right.kind == IrConstKind.Int ->
                IrConstImpl.int(expr.startOffset, expr.endOffset, context.irBuiltIns.intType,
                    (left.value as Int) + (right.value as Int))
            left.kind == IrConstKind.Long && right.kind == IrConstKind.Long ->
                IrConstImpl.long(expr.startOffset, expr.endOffset, context.irBuiltIns.longType,
                    (left.value as Long) + (right.value as Long))
            left.kind == IrConstKind.Float && right.kind == IrConstKind.Float ->
                IrConstImpl.float(expr.startOffset, expr.endOffset, context.irBuiltIns.floatType,
                    (left.value as Float) + (right.value as Float))
            left.kind == IrConstKind.Double && right.kind == IrConstKind.Double ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    (left.value as Double) + (right.value as Double))
            // Mixed numeric types - promote to Double
            isNumeric(left) && isNumeric(right) ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    toDouble(left) + toDouble(right))
            // String concatenation
            left.kind == IrConstKind.String || right.kind == IrConstKind.String ->
                IrConstImpl.string(expr.startOffset, expr.endOffset, context.irBuiltIns.stringType,
                    left.value.toString() + right.value.toString())
            else -> null
        }
    }

    private fun evaluateBinaryMinus(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        return when {
            left.kind == IrConstKind.Int && right.kind == IrConstKind.Int ->
                IrConstImpl.int(expr.startOffset, expr.endOffset, context.irBuiltIns.intType,
                    (left.value as Int) - (right.value as Int))
            left.kind == IrConstKind.Long && right.kind == IrConstKind.Long ->
                IrConstImpl.long(expr.startOffset, expr.endOffset, context.irBuiltIns.longType,
                    (left.value as Long) - (right.value as Long))
            left.kind == IrConstKind.Float && right.kind == IrConstKind.Float ->
                IrConstImpl.float(expr.startOffset, expr.endOffset, context.irBuiltIns.floatType,
                    (left.value as Float) - (right.value as Float))
            left.kind == IrConstKind.Double && right.kind == IrConstKind.Double ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    (left.value as Double) - (right.value as Double))
            isNumeric(left) && isNumeric(right) ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    toDouble(left) - toDouble(right))
            else -> null
        }
    }

    private fun evaluateBinaryMul(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        return when {
            left.kind == IrConstKind.Int && right.kind == IrConstKind.Int ->
                IrConstImpl.int(expr.startOffset, expr.endOffset, context.irBuiltIns.intType,
                    (left.value as Int) * (right.value as Int))
            left.kind == IrConstKind.Long && right.kind == IrConstKind.Long ->
                IrConstImpl.long(expr.startOffset, expr.endOffset, context.irBuiltIns.longType,
                    (left.value as Long) * (right.value as Long))
            left.kind == IrConstKind.Float && right.kind == IrConstKind.Float ->
                IrConstImpl.float(expr.startOffset, expr.endOffset, context.irBuiltIns.floatType,
                    (left.value as Float) * (right.value as Float))
            left.kind == IrConstKind.Double && right.kind == IrConstKind.Double ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    (left.value as Double) * (right.value as Double))
            isNumeric(left) && isNumeric(right) ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    toDouble(left) * toDouble(right))
            else -> null
        }
    }

    private fun evaluateBinaryDiv(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        return when {
            left.kind == IrConstKind.Int && right.kind == IrConstKind.Int -> {
                val rightVal = right.value as Int
                if (rightVal == 0) null
                else IrConstImpl.int(expr.startOffset, expr.endOffset, context.irBuiltIns.intType,
                    (left.value as Int) / rightVal)
            }
            left.kind == IrConstKind.Long && right.kind == IrConstKind.Long -> {
                val rightVal = right.value as Long
                if (rightVal == 0L) null
                else IrConstImpl.long(expr.startOffset, expr.endOffset, context.irBuiltIns.longType,
                    (left.value as Long) / rightVal)
            }
            left.kind == IrConstKind.Float && right.kind == IrConstKind.Float ->
                IrConstImpl.float(expr.startOffset, expr.endOffset, context.irBuiltIns.floatType,
                    (left.value as Float) / (right.value as Float))
            left.kind == IrConstKind.Double && right.kind == IrConstKind.Double ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    (left.value as Double) / (right.value as Double))
            isNumeric(left) && isNumeric(right) ->
                IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType,
                    toDouble(left) / toDouble(right))
            else -> null
        }
    }

    private fun evaluateBooleanAnd(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        if (left.kind != IrConstKind.Boolean || right.kind != IrConstKind.Boolean) return null
        return IrConstImpl.boolean(expr.startOffset, expr.endOffset, context.irBuiltIns.booleanType,
            (left.value as Boolean) && (right.value as Boolean))
    }

    private fun evaluateBooleanOr(left: IrConst, right: IrConst, expr: IrCall): IrConst? {
        if (left.kind != IrConstKind.Boolean || right.kind != IrConstKind.Boolean) return null
        return IrConstImpl.boolean(expr.startOffset, expr.endOffset, context.irBuiltIns.booleanType,
            (left.value as Boolean) || (right.value as Boolean))
    }

    // ==================== Unary Operation Implementations ====================

    private fun evaluateUnaryMinus(operand: IrConst, expr: IrCall): IrConst? {
        return when (operand.kind) {
            IrConstKind.Int -> IrConstImpl.int(expr.startOffset, expr.endOffset,
                context.irBuiltIns.intType, -(operand.value as Int))
            IrConstKind.Long -> IrConstImpl.long(expr.startOffset, expr.endOffset,
                context.irBuiltIns.longType, -(operand.value as Long))
            IrConstKind.Float -> IrConstImpl.float(expr.startOffset, expr.endOffset,
                context.irBuiltIns.floatType, -(operand.value as Float))
            IrConstKind.Double -> IrConstImpl.double(expr.startOffset, expr.endOffset,
                context.irBuiltIns.doubleType, -(operand.value as Double))
            else -> null
        }
    }

    private fun evaluateNot(operand: IrConst, expr: IrCall): IrConst? {
        if (operand.kind != IrConstKind.Boolean) return null
        return IrConstImpl.boolean(expr.startOffset, expr.endOffset, context.irBuiltIns.booleanType,
            !(operand.value as Boolean))
    }

    // ==================== Field Access ====================

    private fun evaluateGetField(expression: IrGetField, containingClass: IrClass): IrConst? {
        val field = expression.symbol.owner
        val property = field.correspondingPropertySymbol?.owner

        if (property != null) {
            // Check if this is from a @BrsConstant object
            val parentClass = field.parent as? IrClass
            if (parentClass?.hasAnnotation(brsConstantFqn) == true) {
                // Ensure the parent object is evaluated first
                if (parentClass !in context.mapping.constantObjectClasses) {
                    evaluateConstantObject(parentClass)
                }
                return context.getConstantObjectProperty(parentClass, property.name.asString())
            }

            // Check if it's already evaluated in the current context
            return evaluatedConstants[property]
        }

        return null
    }

    // ==================== Helper Methods ====================

    private fun isNumeric(const: IrConst): Boolean {
        return const.kind == IrConstKind.Int ||
               const.kind == IrConstKind.Long ||
               const.kind == IrConstKind.Float ||
               const.kind == IrConstKind.Double
    }

    private fun toDouble(const: IrConst): Double {
        return when (const.kind) {
            IrConstKind.Int -> (const.value as Int).toDouble()
            IrConstKind.Long -> (const.value as Long).toDouble()
            IrConstKind.Float -> (const.value as Float).toDouble()
            IrConstKind.Double -> const.value as Double
            else -> 0.0
        }
    }

    private fun valueToIrConst(value: Any?, expr: IrExpression): IrConst? {
        return when (value) {
            is Int -> IrConstImpl.int(expr.startOffset, expr.endOffset, context.irBuiltIns.intType, value)
            is Long -> IrConstImpl.long(expr.startOffset, expr.endOffset, context.irBuiltIns.longType, value)
            is Float -> IrConstImpl.float(expr.startOffset, expr.endOffset, context.irBuiltIns.floatType, value)
            is Double -> IrConstImpl.double(expr.startOffset, expr.endOffset, context.irBuiltIns.doubleType, value)
            is Boolean -> IrConstImpl.boolean(expr.startOffset, expr.endOffset, context.irBuiltIns.booleanType, value)
            is String -> IrConstImpl.string(expr.startOffset, expr.endOffset, context.irBuiltIns.stringType, value)
            is Char -> IrConstImpl.char(expr.startOffset, expr.endOffset, context.irBuiltIns.charType, value)
            else -> null
        }
    }

    // ==================== Error Reporting ====================

    private fun reportNonConstantError(declaration: IrProperty, expression: IrExpression) {
        val exprDescription = when (expression) {
            is IrCall -> {
                val funcName = expression.symbol.owner.name.asString()
                if (funcName.startsWith("<get-")) {
                    "property access '${funcName.removePrefix("<get-").removeSuffix(">")}'"
                } else {
                    "function call '$funcName'"
                }
            }
            is IrGetValue -> "variable reference '${expression.symbol.owner.name}'"
            is IrGetField -> "field access '${expression.symbol.owner.name}'"
            is IrConstructorCall -> "constructor call '${expression.symbol.owner.parentAsClass.name}'"
            is IrGetObjectValue -> "object reference '${expression.symbol.owner.name}'"
            else -> expression.javaClass.simpleName
        }

        context.reportError(
            declaration,
            "@BrsConstant property '${declaration.name}' has non-constant initializer: $exprDescription. " +
            "Only compile-time constant expressions are allowed: " +
            "literals, string concatenation, arithmetic (+,-,*,/), boolean (&&,||,!), " +
            "and references to other @BrsConstant properties or enum ordinals/names."
        )
    }
}
