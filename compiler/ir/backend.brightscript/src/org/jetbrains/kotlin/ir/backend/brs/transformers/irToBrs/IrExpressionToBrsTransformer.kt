/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.backend.common.capturedFields
import org.jetbrains.kotlin.backend.common.lower.AbstractSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.parseBrightScriptStatements
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsCodeOutliningLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInlineCallTransformer
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.io.File

/**
 * Transforms IR expressions to BrightScript expressions.
 */
class IrExpressionToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext,
    private val genCtx: BrsGenerationContext,
) : IrVisitor<BrsExpression, Unit>() {

    override fun visitElement(element: IrElement, data: Unit): BrsExpression {
        return BrsStringLiteral("/* Unsupported: ${element::class.simpleName} */")
    }

    // ==================== Loops in Expression Context ====================

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsExpression {
        // While loops in expression context - wrap in BrsStatementAsExpression
        // so they render as statements inline rather than being treated as expressions
        val whileStmt = parent.statementVisitor.visitWhileLoop(loop, Unit)
        return BrsStatementAsExpression(whileStmt)
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsExpression {
        // Do-while loops in expression context - wrap in BrsStatementAsExpression
        val doWhileStmt = parent.statementVisitor.visitDoWhileLoop(loop, Unit)
        return BrsStatementAsExpression(doWhileStmt)
    }

    // ==================== Control Flow in Expression Context ====================

    override fun visitReturn(expression: IrReturn, data: Unit): BrsExpression {
        // Return statements in expression context (e.g., from inlined functions)
        //
        // After BrsReturnableBlockLowering, returns to returnable blocks become return@block Unit,
        // which the statement visitor converts to "exit while" to break out of the wrapper loop.
        // Returns to functions remain as BrsReturn statements.
        //
        // Route all returns through the statement visitor.
        val returnStmt = parent.statementVisitor.visitReturn(expression, Unit)
        return BrsStatementAsExpression(returnStmt)
    }

    override fun visitThrow(expression: IrThrow, data: Unit): BrsExpression {
        // Throw statements in expression context
        val throwStmt = parent.statementVisitor.visitThrow(expression, Unit)
        return BrsStatementAsExpression(throwStmt)
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsExpression {
        // Break statements in expression context
        val breakStmt = parent.statementVisitor.visitBreak(jump, Unit)
        return BrsStatementAsExpression(breakStmt)
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsExpression {
        // Continue statements in expression context
        val continueStmt = parent.statementVisitor.visitContinue(jump, Unit)
        return BrsStatementAsExpression(continueStmt)
    }

    override fun visitTry(aTry: IrTry, data: Unit): BrsExpression {
        // Try statements in expression context
        val tryStmt = parent.statementVisitor.visitTry(aTry, Unit)
        return BrsStatementAsExpression(tryStmt)
    }

    // ==================== Literals ====================

    override fun visitConst(expression: IrConst, data: Unit): BrsExpression {
        // Handle unsigned types by wrapping in constructor calls
        // UInt, ULong, UByte, UShort are value classes that need explicit boxing in BrightScript
        if (expression.type.isUnsigned() && expression.kind != IrConstKind.Null) {
            val className = expression.type.classOrNull?.owner?.name?.asString()
            return when (className) {
                "UInt" -> createFunctionCall(
                    "UInt_create_I_k_",
                    mutableListOf(BrsIntLiteral(expression.value as Int)),
                    context
                )
                "ULong" -> createFunctionCall(
                    "ULong_create_J_k_",
                    mutableListOf(BrsLongIntLiteral(expression.value as Long)),
                    context
                )
                "UByte" -> createFunctionCall(
                    "UByte_create_B_k_",
                    mutableListOf(BrsIntLiteral((expression.value as Byte).toInt())),
                    context
                )
                "UShort" -> createFunctionCall(
                    "UShort_create_S_k_",
                    mutableListOf(BrsIntLiteral((expression.value as Short).toInt())),
                    context
                )
                else -> {
                    // Unknown unsigned type, fall through to regular handling
                    visitConstPrimitive(expression)
                }
            }
        }
        return visitConstPrimitive(expression)
    }

    private fun visitConstPrimitive(expression: IrConst): BrsExpression {
        return when (expression.kind) {
            IrConstKind.Int -> BrsIntLiteral(expression.value as Int)
            IrConstKind.Long -> BrsLongIntLiteral(expression.value as Long)
            IrConstKind.Float -> BrsFloatLiteral(expression.value as Float)
            IrConstKind.Double -> BrsDoubleLiteral(expression.value as Double)
            IrConstKind.Boolean -> BrsBooleanLiteral(expression.value as Boolean)
            IrConstKind.String -> BrsStringLiteral(expression.value as String)
            IrConstKind.Char -> BrsStringLiteral((expression.value as Char).toString())
            IrConstKind.Byte -> BrsIntLiteral((expression.value as Byte).toInt())
            IrConstKind.Short -> BrsIntLiteral((expression.value as Short).toInt())
            IrConstKind.Null -> BrsInvalidLiteral()
        }
    }

    /**
     * Transform an IrConst to a BrightScript literal expression.
     * Used for @BrsConstant property inlining.
     */
    fun transformConstToLiteral(const: IrConst): BrsExpression {
        return when (const.kind) {
            IrConstKind.Int -> BrsIntLiteral(const.value as Int)
            IrConstKind.Long -> BrsLongIntLiteral(const.value as Long)
            IrConstKind.Float -> BrsFloatLiteral(const.value as Float)
            IrConstKind.Double -> BrsDoubleLiteral(const.value as Double)
            IrConstKind.Boolean -> BrsBooleanLiteral(const.value as Boolean)
            IrConstKind.String -> BrsStringLiteral(const.value as String)
            IrConstKind.Char -> BrsStringLiteral((const.value as Char).toString())
            IrConstKind.Byte -> BrsIntLiteral((const.value as Byte).toInt())
            IrConstKind.Short -> BrsIntLiteral((const.value as Short).toInt())
            IrConstKind.Null -> BrsInvalidLiteral()
        }
    }

    // ==================== References ====================

    override fun visitGetValue(expression: IrGetValue, data: Unit): BrsExpression {
        // Check if this is a temp variable that should be inlined (increment/decrement)
        val substitution = genCtx.getTempVarSubstitution(expression.symbol)
        if (substitution != null) {
            // Inline the initializer expression instead of outputting the temp var reference
            return substitution.accept(this, data)
        }

        // Check if there's a temp var name mapping (used when increment is an expression)
        val tempVarName = genCtx.getTempVarName(expression.symbol)
        if (tempVarName != null) {
            return BrsIdentifier(tempVarName)
        }

        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = genCtx.getCapturedVariable(expression.symbol)
        if (capturedVar != null) {
            // Rewrite to access via m (the closure object)
            return if (capturedVar.isMutable) {
                // Mutable captures: m.varName.value
                BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value")
            } else {
                // Read-only captures: m.varName
                BrsDotAccess(BrsMRef(), capturedVar.name)
            }
        }

        // Check if this is a reference to a lambda's extension receiver
        // The receiver parameter is named '__receiver' in the generated BrightScript
        // (not 'm', to avoid collision with closure's m reference for captured variables)
        if (expression.symbol == genCtx.currentLambdaExtensionReceiver) {
            return BrsIdentifier("__receiver")
        }

        // Check if this is a shared variable (mutable var captured by closure) accessed outside the closure
        // These are boxed in {value: x} and need .value access
        // Note: capturedVar is null here because we returned early above if it wasn't
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val owner = expression.symbol.owner
        val isSharedVariable = (owner is IrVariable && owner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                               expression.symbol in genCtx.sharedVariables
        if (isSharedVariable) {
            val varName = sanitizeParameterName(rawName)
            return BrsDotAccess(BrsIdentifier(varName), "value")
        }

        return when {
            // In constructor bodies, '<this>' refers to the local 'this' variable being constructed
            // In regular methods, '<this>' refers to 'm' (the object the method was called on)
            rawName == "<this>" -> if (genCtx.isInConstructorBody) BrsIdentifier("this") else BrsMRef()
            // Sanitize setter parameter names
            rawName.startsWith("<set-") && rawName.endsWith(">") -> BrsIdentifier("value")
            // Sanitize other special names
            rawName.startsWith("<") && rawName.endsWith(">") ->
                BrsIdentifier(rawName.removePrefix("<").removeSuffix(">").replace("-", "_"))
            // For local variables, use the unique name to avoid collisions from inline expansion
            owner is IrVariable -> {
                val baseName = sanitizeParameterName(rawName)
                BrsIdentifier(genCtx.getVariableName(expression.symbol, baseName))
            }
            // Apply full sanitization including reserved keyword escaping
            else -> BrsIdentifier(sanitizeParameterName(rawName))
        }
    }

    override fun visitGetField(expression: IrGetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner

        // For top-level fields with constant initializers, inline the value directly
        // This avoids the need for initialization statements that can't appear at file level
        if (field.parent is IrFile && field.isFinal) {
            field.initializer?.expression?.let { initializer ->
                if (isConstantExpression(initializer, context)) {
                    return parent.transformExpression(initializer)
                }
            }
        }

        // For top-level fields (parent is IrFile), use GetGlobalAA()
        val receiver = if (field.parent is IrFile) {
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf())
        } else {
            expression.receiver?.let { it.accept(this, data) } ?: BrsMRef()
        }

        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // Check if accessing outer class field from inner class
        // The receiver will be the outer class reference
        val fieldParentClass = field.parent as? IrClass
        if (fieldParentClass != null) {
            // If the receiver references an outer class instance (not the current class),
            // it means we're accessing an outer class field
            val receiverSymbol = (expression.receiver as? IrGetValue)?.symbol?.owner
            if (receiverSymbol != null && receiverSymbol.name.asString().contains("\$this")) {
                // This is an outer class reference - access through _outer
                return BrsDotAccess(
                    BrsDotAccess(BrsMRef(), "_outer"),
                    fieldName
                )
            }
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // If so, we need to read field.value instead of field
        val className = fieldParentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields

        return if (isSharedVariableField) {
            // Read the box's value: m.fieldName.value
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner

        // For top-level fields (parent is IrFile), use GetGlobalAA()
        val receiver = if (field.parent is IrFile) {
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf())
        } else {
            expression.receiver?.let { it.accept(this, data) } ?: BrsMRef()
        }

        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // EXCEPTION: In constructor body, we're initializing the field with the box itself
        val parentClass = field.parent as? IrClass
        val className = parentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields
        val isInConstructor = genCtx.isInConstructorBody

        // A shared variable moved to a coroutine field keeps its SHARED_BOX_INIT-tagged
        // initializing assignment - the box is created there; other writes go through .value
        val isBoxInit = isSharedVariableField && expression.origin == BrsStatementOrigins.SHARED_BOX_INIT

        // Generate assignment expression: receiver.field = value (or receiver.field.value = value for shared vars)
        val target = if (isSharedVariableField && !isInConstructor && !isBoxInit) {
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }

        val transformedValue = expression.value.accept(this, data)
        val finalValue = if (isBoxInit) {
            BrsAALiteral(mutableListOf(BrsAAEntry("value", transformedValue)))
        } else {
            transformedValue
        }

        return BrsBinaryOp(
            target,
            BrsBinaryOperator.EQ,
            finalValue
        )
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsExpression {
        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = genCtx.getCapturedVariable(expression.symbol)
        if (capturedVar != null && capturedVar.isMutable) {
            // Rewrite to assign via m (the closure object): m.varName.value = newValue
            return BrsBinaryOp(
                BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value"),
                BrsBinaryOperator.EQ,
                expression.value.accept(this, data)
            )
        }

        val sanitizedName = when {
            rawName.startsWith("<set-") && rawName.endsWith(">") -> "value"
            rawName.startsWith("<") && rawName.endsWith(">") ->
                rawName.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> sanitizeParameterName(rawName)
        }

        // Check if this is a shared variable accessed outside closure
        // Note: sharedVariables only contains mutable vars, and mutable captures returned above
        // Two detection mechanisms (same as the statement transformer's visitSetValue):
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val sharedOwner = expression.symbol.owner
        val isSharedVariable = (sharedOwner is IrVariable && sharedOwner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                               expression.symbol in genCtx.sharedVariables
        if (isSharedVariable) {
            // The state machine re-emits a hoisted shared declaration's initialization as an
            // assignment tagged SHARED_BOX_INIT - that assignment creates the {value: ...} box;
            // every other write assigns through .value
            if (expression.origin == BrsStatementOrigins.SHARED_BOX_INIT) {
                return BrsBinaryOp(
                    BrsIdentifier(sanitizeParameterName(sanitizedName)),
                    BrsBinaryOperator.EQ,
                    BrsAALiteral(mutableListOf(BrsAAEntry("value", expression.value.accept(this, data))))
                )
            }
            return BrsBinaryOp(
                BrsDotAccess(BrsIdentifier(sanitizeParameterName(sanitizedName)), "value"),
                BrsBinaryOperator.EQ,
                expression.value.accept(this, data)
            )
        }

        // For local variables, use the unique name to avoid collisions from inline expansion
        val owner = expression.symbol.owner
        val targetName = if (owner is IrVariable) {
            genCtx.getVariableName(expression.symbol, sanitizedName)
        } else {
            sanitizedName
        }

        // Generate assignment expression: varName = value
        return BrsBinaryOp(
            BrsIdentifier(targetName),
            BrsBinaryOperator.EQ,
            expression.value.accept(this, data)
        )
    }

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Unit): BrsExpression {
        // Special case: Unit doesn't need a getInstance call in BrightScript
        // Unit represents "void" which is just invalid in BrightScript
        if (expression.type.isUnit()) {
            return BrsIdentifier("invalid")
        }

        val objectClass = expression.symbol.owner

        // Object singletons are represented as function calls that create/return the instance
        val name = context.getBrsName(objectClass)

        // Record dependency for the singleton/companion object
        // The getInstance function is tracked via the function manifest
        val getInstanceFuncName = "${name}_getInstance"
        context.recordFunctionDependency(getInstanceFuncName)

        return BrsFunctionCall(BrsIdentifier("${name}_getInstance"), mutableListOf())
    }

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Unit): BrsExpression {
        val enumClass = expression.symbol.owner.parentAsClass
        val className = context.getBrsName(enumClass)
        val entryName = expression.symbol.owner.name.asString()

        // Ensure enum entries are initialized before accessing.
        // BrightScript enum entries are stored in module scope (m.ClassName_EntryName) and are lazily initialized.
        // We must call initEntries() to ensure the entry exists before returning it.
        // Generate: [ClassName_initEntries(), m.ClassName_EntryName][1]
        // This uses BrightScript's array literal with index [1] to execute init and return the value.
        return BrsIndexAccess(
            BrsArrayLiteral(
                mutableListOf(
                    createFunctionCall("${className}_initEntries", context),
                    BrsDotAccess(BrsIdentifier("m"), "${className}_${entryName}")
                )
            ),
            BrsIntLiteral(1)
        )
    }

    // ==================== KClass / Reflection ====================

    /**
     * Handles class literal references like `Person::class`.
     *
     * For Kotlin classes: generates `__kotlin_KClass_create("ClassName")`
     * For external interfaces (native Roku types): generates `__kotlin_KClass_create("roTypeName")`
     *   where roTypeName matches BrightScript's Type() return value (e.g., "roArray", "roSGScreenEvent")
     */
    override fun visitClassReference(expression: IrClassReference, data: Unit): BrsExpression {
        val classType = expression.classType
        val typeName = getTypeNameForKClass(classType)

        // Generate: __kotlin_KClass_create("TypeName")
        context.recordFunctionDependency("__kotlin_KClass_create")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_KClass_create"),
            mutableListOf(BrsStringLiteral(typeName))
        )
    }

    /**
     * Handles getting the class of an instance like `dog::class` or `animal::class`.
     *
     * At runtime, this checks if the object is a Kotlin-created object (has __type field)
     * or a native BrightScript object (uses Type() function).
     */
    override fun visitGetClass(expression: IrGetClass, data: Unit): BrsExpression {
        val argument = expression.argument.accept(this, data)

        // Generate: __kotlin_getClass(argument)
        context.recordFunctionDependency("__kotlin_getClass")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_getClass"),
            mutableListOf(argument)
        )
    }

    /**
     * Gets the type name string for a KClass based on the IR type.
     *
     * For native Roku types (external interfaces), the name should match
     * what BrightScript's Type() function returns (e.g., "roArray", "roSGScreenEvent").
     * For Kotlin classes, uses the BRS-mangled class name.
     */
    private fun getTypeNameForKClass(type: IrType): String {
        val classifier = type.classifierOrNull ?: return "Object"

        // Handle type parameters - use "Object" as a fallback
        if (classifier is IrTypeParameterSymbol) {
            return "Object"
        }

        val irClass = type.classOrNull?.owner ?: return "Object"

        // For native Roku types (external interfaces), use the BrightScript type name
        // that matches what Type() returns at runtime
        if (irClass.isExternal) {
            val className = irClass.name.asString()
            // External interfaces in kotlin.brs.roku use PascalCase (e.g., RoArray, RoSGScreenEvent)
            // BrightScript Type() returns "roArray", "roSGScreenEvent", etc.
            // Convert: RoArray -> roArray, RoSGScreenEvent -> roSGScreenEvent
            if (className.startsWith("Ro") && className.length > 2) {
                return "ro${className.substring(2)}"
            }
            // For interfaces like IEnumNative, IArray, etc., use the raw name
            // These typically won't be used as map keys directly
            return className
        }

        // For Kotlin classes, use the BRS-mangled class name
        return context.getBrsName(irClass)
    }

    // ==================== Function Calls ====================

    override fun visitCall(expression: IrCall, data: Unit): BrsExpression {
        val function = expression.symbol.owner

        // Check for operator expressions based on origin
        expression.origin?.let { origin ->
            val operatorResult = transformOperator(expression, origin)
            if (operatorResult != null) return operatorResult
        }

        // Handle identity comparison (===) even when origin is null
        // This happens when coroutine lowering creates calls via local buildCall() with null origin
        if (expression.symbol == context.irBuiltIns.eqeqeqSymbol) {
            val left = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
            val right = expression.getValueArgument(1)?.accept(this, data) ?: return BrsInvalidLiteral()
            context.recordFunctionDependency("__kotlin_identityEquals")
            return BrsFunctionCall(
                BrsIdentifier("__kotlin_identityEquals"),
                mutableListOf(left, right)
            )
        }

        // Handle builtin comparison functions (less, lessOrEqual, greater, greaterOrEqual)
        // These come from irBuiltIns and may not have an origin when introduced by lowering passes
        val builtinComparisonResult = transformBuiltinComparison(expression)
        if (builtinComparisonResult != null) return builtinComparisonResult

        // Handle kotlin.internal.ir intrinsics like CHECK_NOT_NULL, THROW_CCE, etc.
        // These are built-in operators that need to be lowered to BrightScript
        val intrinsicFunctionName = function.name.asString()
        val intrinsicPackageFqName = function.getPackageFragment()?.packageFqName?.asString() ?: ""
        if (intrinsicPackageFqName.startsWith("kotlin.internal")) {
            when (intrinsicFunctionName) {
                "CHECK_NOT_NULL" -> {
                    // In BrightScript we don't have strict null checking,
                    // so just return the value argument directly
                    return expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                }
                "THROW_CCE" -> {
                    // Class cast exception - generate a stop statement for debugging
                    return BrsIdentifier("invalid")
                }
                "THROW_ISE" -> {
                    // Illegal state exception - generate a stop statement for debugging
                    return BrsIdentifier("invalid")
                }
            }
        }

        // Handle BrightScript runtime intrinsics (brsFormatJson, brsParseJson, etc.)
        // These are defined in kotlin.brs.runtime but are not marked external
        val runtimeFunctionName = function.name.asString()
        val runtimePackageFqName = function.getPackageFragment()?.packageFqName?.asString() ?: ""
        if (runtimePackageFqName == "kotlin.brs.runtime") {
            when (runtimeFunctionName) {
                "brsFormatJson" -> {
                    // Convert Kotlin collections to plain BrightScript types before JSON serialization
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    context.recordFunctionDependency("__kotlin_toJsonValue_AnyN_k_")
                    val converted = BrsFunctionCall(BrsIdentifier("__kotlin_toJsonValue_AnyN_k_"), mutableListOf(arg))
                    return BrsFunctionCall(BrsIdentifier("FormatJson"), mutableListOf(converted))
                }
                "brsParseJson" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsFunctionCall(BrsIdentifier("ParseJson"), mutableListOf(arg))
                }
                "brsTypeOf" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsTypeOf(arg)
                }
                "brsIsInvalid" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsBinaryOp(arg, BrsBinaryOperator.EQ, BrsInvalidLiteral())
                }
                "brsCreateObject" -> {
                    val typeArg = expression.getValueArgument(0)
                    val objectType = (typeArg as? IrConst)?.let { it.value.toString() } ?: "Object"
                    val args = (1 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.accept(this, data)
                    }
                    return BrsCreateObject(objectType, args.toMutableList())
                }
                "brsCreateArray" -> {
                    val sizeArg = expression.getValueArgument(0)?.accept(this, data) ?: BrsIntLiteral(0)
                    return BrsCreateObject("roArray", mutableListOf(sizeArg, BrsBooleanLiteral(true)))
                }
                "brsArrayLength" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(arg, "count", mutableListOf())
                }
                "brsCreateAssociativeArray" -> {
                    return BrsAALiteral()
                }
                "brsCreatePlainAA" -> {
                    // Create a plain roAssociativeArray (not a Kotlin class instance)
                    return BrsCreateObject("roAssociativeArray", mutableListOf())
                }
                "brsAAAddReplace" -> {
                    // aa.AddReplace(key, value)
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val key = expression.getValueArgument(1)?.accept(this, data) ?: BrsInvalidLiteral()
                    val value = expression.getValueArgument(2)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "AddReplace", mutableListOf(key, value))
                }
                "brsPrint" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsFunctionCall(BrsIdentifier("print"), mutableListOf(arg))
                }
                // AA/Array intrinsics for JSON serialization
                "brsHasField" -> {
                    // DoesExist on AA
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "DoesExist", mutableListOf(field))
                }
                "brsGetField" -> {
                    // Direct field access via Lookup
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "Lookup", mutableListOf(field))
                }
                "brsIntrinsicCreateObject" -> {
                    val typeArg = expression.getValueArgument(0)
                    val objectType = (typeArg as? IrConst)?.let { it.value.toString() } ?: "roAssociativeArray"
                    // roArray needs size and resize flag arguments
                    val args = if (objectType == "roArray") {
                        mutableListOf<BrsExpression>(BrsIntLiteral(0), BrsBooleanLiteral(true))
                    } else {
                        mutableListOf()
                    }
                    return BrsCreateObject(objectType, args)
                }
                "brsIntrinsicKeys" -> {
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "Keys", mutableListOf())
                }
                "brsIntrinsicGetField" -> {
                    // Same as brsGetField but named differently
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "Lookup", mutableListOf(field))
                }
                "brsIntrinsicAddReplace" -> {
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val key = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val value = expression.getValueArgument(2)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "AddReplace", mutableListOf(key, value))
                }
                "brsIntrinsicCallGetArray" -> {
                    val collection = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(collection, "__get_array", mutableListOf())
                }
                "brsIntrinsicCount" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(array, "count", mutableListOf())
                }
                "brsIntrinsicArrayGet" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val index = expression.getValueArgument(1)?.accept(this, data) ?: BrsIntLiteral(0)
                    return BrsIndexAccess(array, index)
                }
                "brsIntrinsicArrayPush" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val value = expression.getValueArgument(1)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(array, "push", mutableListOf(value))
                }
                "brsIntrinsicCallMethod" -> {
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val methodArg = expression.getValueArgument(1)
                    val methodName = (methodArg as? IrConst)?.let { it.value.toString() } ?: "unknown"
                    return BrsMethodCall(obj, methodName, mutableListOf())
                }
                "brsStringEquals" -> {
                    // Simple string comparison: (a = b)
                    val a = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val b = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsBinaryOp(a, BrsBinaryOperator.EQ, b)
                }
                "brsToString" -> {
                    // Convert value to string using the existing runtime function
                    val value = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    context.recordFunctionDependency("toString_AnyN_k_")
                    return BrsFunctionCall(BrsIdentifier("toString_AnyN_k_"), mutableListOf(value))
                }
                "brsIntrinsicStartsWith" -> {
                    // Left(str, Len(prefix)) = prefix
                    val str = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val prefix = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val lenCall = BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(prefix))
                    val leftCall = BrsFunctionCall(BrsIdentifier("Left"), mutableListOf(str, lenCall))
                    return BrsBinaryOp(leftCall, BrsBinaryOperator.EQ, prefix)
                }
                "brsIntrinsicEndsWith" -> {
                    // Right(str, Len(suffix)) = suffix
                    val str = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val suffix = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val lenCall = BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(suffix))
                    val rightCall = BrsFunctionCall(BrsIdentifier("Right"), mutableListOf(str, lenCall))
                    return BrsBinaryOp(rightCall, BrsBinaryOperator.EQ, suffix)
                }
            }
        }

        // createComponent<T>() intrinsic (kotlin.brs.ComponentFactory): the reified call
        // site carries the concrete component class as its type argument and lowers to
        // CreateObject("roSGNode", name). createComponent itself is matched (not just the
        // underlying brsCreateComponent) because BrsInlineFunctionResolver cannot inline
        // klib functions — user call sites reach codegen un-inlined. brsCreateComponent
        // is matched too for call sites that DO get inlined (stdlib-internal callers).
        if (runtimePackageFqName == "kotlin.brs" &&
            (runtimeFunctionName == "createComponent" || runtimeFunctionName == "brsCreateComponent")
        ) {
            val typeArgument = expression.typeArguments.getOrNull(0)
            val componentClass = typeArgument?.classOrNull?.owner
            if (componentClass != null) {
                if (!context.intrinsics.isSceneGraphComponent(componentClass) ||
                    componentClass.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
                ) {
                    context.reportError(
                        expression,
                        "[IR] createComponent type argument must be a concrete SceneGraph component class, " +
                            "got '${componentClass.name.asString()}'"
                    )
                    return BrsInvalidLiteral()
                }
                return BrsCreateObject(
                    "roSGNode",
                    mutableListOf(BrsStringLiteral(context.getBrsName(componentClass)))
                )
            }
            // Unsubstituted type parameter: this is the inline wrapper's own body being
            // emitted (declaration codegen, not a user call site) — keep the stub call.
        }

        // Check for @BrsCreateObject annotation - compile to CreateObject(typeName, args...)
        val brsCreateObjectAnnotation = function.getAnnotation(BrsStandardClassIds.Annotations.BrsCreateObject.asSingleFqName())
        if (brsCreateObjectAnnotation != null) {
            val typeNameArg = brsCreateObjectAnnotation.getValueArgument(0)
            val typeName = if (typeNameArg is IrConst) {
                (typeNameArg as IrConst).value as String
            } else {
                // Fallback if annotation value is not a simple constant
                "Object"
            }
            val args = mutableListOf<BrsExpression>(BrsStringLiteral(typeName))
            // Add all call arguments
            for (i in 0 until expression.valueArgumentsCount) {
                expression.getValueArgument(i)?.let { args.add(it.accept(this, data)) }
            }
            return BrsFunctionCall(BrsIdentifier("CreateObject"), args)
        }

        // Check for @BrsInline functions - inline at call site
        if (parent.inlineCallTransformer.shouldInline(expression)) {
            return transformBrsInlineCall(expression)
        }

        // Check for @BrsNamespace object calls - these were preprocessed by BrsExternalLowering
        // and the resolved name is stored in context.namespaceCallNames
        context.namespaceCallNames[expression]?.let { resolvedName ->
            val args = mutableListOf<BrsExpression>()
            for (i in 0 until expression.valueArgumentsCount) {
                expression.getValueArgument(i)?.let { args.add(it.accept(this, data)) }
            }
            return BrsFunctionCall(BrsIdentifier(resolvedName), args)
        }

        // ==================== @BrsConstant Property Inlining Optimization ====================
        // When accessing properties on @BrsConstant objects, inline the evaluated constant value
        // directly to avoid runtime getInstance() calls and property lookups.
        val constDispatchReceiver = expression.dispatchReceiver
        if (constDispatchReceiver is IrGetObjectValue) {
            val objectClass = constDispatchReceiver.symbol.owner
            if (context.isConstantObject(objectClass)) {
                val functionName = function.name.asString()
                if (functionName.startsWith("<get-")) {
                    val propName = functionName.removePrefix("<get-").removeSuffix(">")
                    context.getConstantObjectProperty(objectClass, propName)?.let { constValue ->
                        return transformConstToLiteral(constValue)
                    }
                }
            }
        }
        // ==================== End @BrsConstant Property Inlining ====================

        // ==================== Enum Property Inlining Optimization ====================
        // When accessing ordinal, name, or constant properties on a compile-time-known
        // enum value, inline the value directly to avoid runtime lookup overhead.
        val dispatchReceiver = expression.dispatchReceiver
        if (dispatchReceiver is IrGetEnumValue) {
            val entry = dispatchReceiver.symbol.owner
            val functionName = function.name.asString()

            when {
                // Inline .ordinal -> integer literal
                functionName == "<get-ordinal>" || functionName == "ordinal" -> {
                    context.getEnumOrdinal(entry)?.let { ordinal ->
                        return BrsIntLiteral(ordinal)
                    }
                }
                // Inline .name -> string literal
                functionName == "<get-name>" || functionName == "name" -> {
                    context.getEnumName(entry)?.let { name ->
                        return BrsStringLiteral(name)
                    }
                }
                // Inline custom val properties with constant values
                functionName.startsWith("<get-") -> {
                    val propName = functionName.removePrefix("<get-").removeSuffix(">")
                    context.getEnumConstantProperties(entry)?.get(propName)?.let { value ->
                        when (value) {
                            is Int -> return BrsIntLiteral(value)
                            is Long -> return BrsLongIntLiteral(value)
                            is String -> return BrsStringLiteral(value)
                            is Boolean -> return BrsBooleanLiteral(value)
                            is Double -> return BrsDoubleLiteral(value)
                            is Float -> return BrsFloatLiteral(value)
                            is Byte -> return BrsIntLiteral(value.toInt())
                            is Short -> return BrsIntLiteral(value.toInt())
                            is Char -> return BrsStringLiteral(value.toString())
                            // Fall through to default handling for other types
                        }
                    }
                }
            }
        }
        // ==================== End Enum Property Inlining ====================

        // Handle primitive type conversion methods and unary operators
        // These are Kotlin methods that don't exist in BrightScript - we need to transform them
        val methodName = function.name.asString()
        val receiver = expression.dispatchReceiver ?: expression.extensionReceiver
        if (receiver != null) {
            // Handle Array.size - BrightScript arrays use .count() not .size property
            if (methodName == "<get-size>" && receiver.type.isArray()) {
                val receiverExpr = receiver.accept(this, data)
                return BrsMethodCall(receiverExpr, "count", mutableListOf())
            }

            // Handle String.length - BrightScript strings use Len() function
            // Native BrightScript strings don't have methods, so str.get_length() won't work.
            // IMPORTANT: Use isStringClassType() to handle both String and String? types.
            // CharSequence could be implemented by StringBuilder or other classes that have
            // a get_length property, so we need polymorphic dispatch for those cases.
            if (methodName == "<get-length>" || methodName == "length") {
                if (receiver.type.isStringClassType()) {
                    val receiverExpr = receiver.accept(this, data)
                    return BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(receiverExpr))
                }
                // For CharSequence (interface that String implements), we need runtime polymorphism.
                // Native BrightScript strings use Len(), while StringBuilder uses get_length().
                // Check if the receiver type is CharSequence by checking the fqName
                val receiverClass = receiver.type.classOrNull?.owner
                val isCharSequence = receiverClass?.fqNameWhenAvailable?.asString() == "kotlin.CharSequence"
                if (isCharSequence) {
                    val receiverExpr = receiver.accept(this, data)
                    // Generate polymorphic dispatch using __kotlin_charSequenceLength helper
                    // Note: The mangled name includes the parameter type (_CharSequenceN_k_)
                    return createFunctionCall(
                        "__kotlin_charSequenceLength_CharSequenceN_k_",
                        mutableListOf(receiverExpr),
                        context
                    )
                }
                // For other types, fall through to normal property access
                // which will generate m.get_length() for proper polymorphic dispatch
            }

            // Handle Array.get(index) - BrightScript arrays use [] indexing, not .get() method
            if (methodName == "get" && receiver.type.isArray() && expression.valueArgumentsCount == 1) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                return BrsIndexAccess(receiverExpr, indexExpr)
            }

            // Handle String.get(index) - BrightScript uses Mid(string, index+1, 1) to get a character
            // Note: BrightScript Mid() is 1-indexed, so we add 1 to the index
            if (methodName == "get" && receiver.type.isString() && expression.valueArgumentsCount == 1) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                // Mid(string, index + 1, 1) - BrightScript is 1-indexed
                return BrsFunctionCall(
                    BrsIdentifier("Mid"),
                    mutableListOf(
                        receiverExpr,
                        BrsBinaryOp(indexExpr, BrsBinaryOperator.ADD, BrsIntLiteral(1)),
                        BrsIntLiteral(1)
                    )
                )
            }

            // Handle Array.set(index, value) - BrightScript arrays use [] indexing, not .set() method
            if (methodName == "set" && receiver.type.isArray() && expression.valueArgumentsCount == 2) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                val valueExpr = expression.getValueArgument(1)?.accept(this, data) ?: return BrsInvalidLiteral()
                return BrsBinaryOp(BrsIndexAccess(receiverExpr, indexExpr), BrsBinaryOperator.EQ, valueExpr)
            }

            when (methodName) {
                // Type conversions - BrightScript handles most implicitly
                "toDouble", "toFloat", "toInt", "toLong", "toShort", "toByte" -> {
                    return receiver.accept(this, data)
                }
                // Int.toChar() needs Chr() in BrightScript to convert integer code to character string
                "toChar" -> {
                    val receiverType = receiver.type
                    if (receiverType.isInt() || receiverType.isLong() || receiverType.isShort() || receiverType.isByte()) {
                        return BrsFunctionCall(
                            BrsIdentifier("Chr"),
                            mutableListOf(receiver.accept(this, data))
                        )
                    }
                    // For Char.toChar(), it's a no-op
                    return receiver.accept(this, data)
                }
                // Unary minus/plus as method calls (when they don't have UMINUS/UPLUS origin)
                // Only convert to unary operators if receiver is a primitive type.
                // Non-primitive types (UInt, ULong, custom classes) need method calls.
                "unaryMinus" -> {
                    if (receiver.type.isPrimitiveForArithmetic()) {
                        return BrsUnaryOp(BrsUnaryOperator.NEG, receiver.accept(this, data))
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                "unaryPlus" -> {
                    if (receiver.type.isPrimitiveForArithmetic()) {
                        return receiver.accept(this, data)  // No-op for primitives
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                // Boolean.not() - BrightScript uses prefix 'not' operator, not a method call
                "not" -> {
                    if (receiver.type.isBoolean()) {
                        return BrsUnaryOp(BrsUnaryOperator.NOT, receiver.accept(this, data))
                    }
                }
                // Binary plus as method call (String.plus, etc.)
                // Only convert to binary operator if receiver is a primitive type that supports +.
                // Non-primitive types (UInt, ULong, custom classes) need method calls.
                // Note: For shared variables (boxed in {value: x}), the receiver.type may be anyNType
                // but the function's declared receiver type (dispatchReceiverParameter.type) preserves
                // the original type. We use the function's declared type to detect primitives.
                "plus" -> {
                    val arg = expression.getValueArgument(0)
                    val functionReceiverType = function.dispatchReceiverParameter?.type ?: receiver.type
                    if (arg != null && functionReceiverType.isPrimitiveForArithmetic()) {
                        var left = receiver.accept(this, data)
                        var right = arg.accept(this, data)

                        // For PLUS operations involving strings, convert non-string operands to strings
                        // BrightScript's + operator cannot mix Integer/Float with String
                        val leftIsString = receiver.type.isString()
                        val rightIsString = arg.type.isString()

                        if (leftIsString && !rightIsString) {
                            // Right operand needs string conversion
                            right = transformToString(right, arg.type, context)
                        } else if (!leftIsString && rightIsString) {
                            // Left operand needs string conversion
                            left = transformToString(left, receiver.type, context)
                        }

                        return BrsBinaryOp(left, BrsBinaryOperator.ADD, right)
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                // toString - handle primitives specially since BrightScript primitives don't have methods
                "toString" -> {
                    val receiverType = receiver.type
                    val receiverExpr = receiver.accept(this, data)
                    return transformToString(receiverExpr, receiverType, context)
                }
            }
        }

        // Check for intrinsics
        if (context.intrinsics.isIntrinsic(expression.symbol)) {
            return transformIntrinsic(expression)
        }

        // kotlin.arrayOf / kotlin.arrayOfNulls resolve from builtins metadata and have no BRS
        // implementation anywhere (no stdlib source, no emitted definition) - a plain mangled
        // call would be UNDEFINED at runtime. Intrinsify them instead:
        //   arrayOf(a, b, c)  -> [a, b, c]        (the vararg argument already IS the array)
        //   arrayOf()         -> []               (empty vararg arrives as a null argument)
        //   arrayOfNulls(n)   -> __kotlin_arrayOfNulls(n)   (runtime helper, count = n)
        val callFqName = function.fqNameWhenAvailable?.asString()
        if (callFqName == "kotlin.arrayOf") {
            val varargArg = expression.getValueArgument(0)
            return varargArg?.accept(this, data) ?: BrsArrayLiteral(mutableListOf())
        }
        if (callFqName == "kotlin.arrayOfNulls") {
            val sizeArg = expression.getValueArgument(0)?.accept(this, data) ?: BrsIntLiteral(0)
            return createFunctionCall("__kotlin_arrayOfNulls", mutableListOf(sizeArg), context)
        }

        // Handle invoke calls on function-typed variables
        // In Kotlin, calling a function-typed variable like `ref(5)` generates an invoke call.
        // In BrightScript, lambdas are closure objects with an invoke method, so we call .invoke()
        if (function.name.asString() == "invoke") {
            val receiver = expression.dispatchReceiver ?: expression.extensionReceiver
            if (receiver != null) {
                // Check if receiver is a function type (Function0, Function1, etc.) or KFunction
                val receiverTypeName = receiver.type.classFqName?.asString() ?: ""
                val isFunctionType = receiver.type.isFunction() ||
                    receiverTypeName.startsWith("kotlin.Function") ||
                    receiverTypeName.startsWith("kotlin.reflect.KFunction")
                if (isFunctionType) {
                    val receiverExpr = receiver.accept(this, data)
                    val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.accept(this, data)
                    }
                    // Generate mangled invoke method name based on function parameters
                    // Lambda classes from callable reference lowering use mangled names like invoke_AnyN_k_
                    // The mangling suffix is based on the parameter types
                    val paramTypes = (0 until function.valueParameters.size).map { i ->
                        val param = function.valueParameters[i]
                        context.typeToMangledString(param.type)
                    }
                    val mangledSuffix = if (paramTypes.isEmpty()) "k_" else "${paramTypes.joinToString("_")}_k_"
                    val invokeMethodName = "invoke_$mangledSuffix"
                    return BrsMethodCall(receiverExpr, invokeMethodName, args.toMutableList())
                }
            }
        }

        // Handle calls to local functions (parent is another function, not a class or file)
        // Local functions are now closure objects, so we need to call .invoke()
        val isLocalFunction = function.parent is IrFunction
        if (isLocalFunction) {
            val localFunctionName = function.name.asString()
            val args = mutableListOf<BrsExpression>()
            for (i in 0 until expression.valueArgumentsCount) {
                val arg = expression.getValueArgument(i)
                if (arg != null) {
                    args.add(arg.accept(this, data))
                } else {
                    // Parameter uses default value - add invalid to preserve position
                    args.add(BrsInvalidLiteral())
                }
            }
            // Generate mangled invoke method name based on function parameters
            val paramTypes = function.valueParameters.map { param ->
                context.typeToMangledString(param.type)
            }
            val mangledSuffix = if (paramTypes.isEmpty()) "k_" else "${paramTypes.joinToString("_")}_k_"
            val invokeMethodName = "invoke_$mangledSuffix"
            // Call .invoke_*() method on the local function closure object
            return BrsMethodCall(BrsIdentifier(localFunctionName), invokeMethodName, args)
        }

        val functionName = context.getBrsName(function)

        // NOTE: the dependency for this call is recorded at the emission points below,
        // NOT here. Recording here would capture the wrong (class-prefixed) name for
        // primitive-extension calls, whose emitted name is rebuilt later (the "corrected"
        // name), and would record names for paths that emit method calls (no global
        // dependency at all).

        val arguments = mutableListOf<BrsExpression>()

        // Add dispatch receiver if present
        expression.dispatchReceiver?.let { receiver ->
            // Check if this is an outer class reference (for inner class access)
            // We detect this by checking if the function belongs to the outer class
            // while the current receiver would resolve to a different class
            val functionParentClass = function.parent as? IrClass
            val receiverValue = receiver as? IrGetValue
            val receiverParamOwner = receiverValue?.symbol?.owner

            // If the function belongs to an outer class and the receiver is a dispatch receiver
            // from the outer class (not the current inner class), we access through _outer
            val isOuterClassAccess = if (functionParentClass != null && receiverParamOwner != null) {
                // Check if the receiver is a dispatch receiver that belongs to the function's parent class
                // When we're in an inner class and accessing outer class members,
                // the receiver's parent will be the outer class itself
                val receiverParentClass = receiverParamOwner.parent as? IrClass
                receiverParentClass == functionParentClass && functionParentClass.declarations.any {
                    it is IrClass && it.isInner
                }
            } else {
                false
            }

            val receiverExpr = if (isOuterClassAccess) {
                // Access through _outer for inner class outer reference
                BrsDotAccess(BrsMRef(), "_outer")
            } else {
                receiver.accept(this, data)
            }

            // For outer class property access, use direct field access instead of getter call
            if (isOuterClassAccess) {
                val rawName = function.name.asString()
                if (rawName.startsWith("<get-") && rawName.endsWith(">")) {
                    // This is a property getter - use direct field access
                    val fieldName = rawName.removePrefix("<get-").removeSuffix(">")
                    return BrsDotAccess(receiverExpr, fieldName)
                }
                if (rawName.startsWith("<set-") && rawName.endsWith(">")) {
                    // This is a property setter - transform to assignment
                    val fieldName = rawName.removePrefix("<set-").removeSuffix(">")
                    val value = expression.getValueArgument(0)?.accept(this, data)
                        ?: BrsInvalidLiteral()
                    return BrsBinaryOp(
                        BrsDotAccess(receiverExpr, fieldName),
                        BrsBinaryOperator.EQ,
                        value
                    )
                }
            }

            // For method calls, transform to dot notation
            // Use expression.dispatchReceiver to detect dispatch calls (more reliable than
            // function.dispatchReceiverParameter which may be null for interface methods)
            if (expression.dispatchReceiver != null) {
                // Check if the receiver type is a primitive.
                // BrightScript primitives don't support method calls, so we must use function calls
                // with the receiver as the first argument.
                val actualReceiverType = receiver.type
                val isPrimitiveReceiver = actualReceiverType.isInt() || actualReceiverType.isLong() ||
                    actualReceiverType.isFloat() || actualReceiverType.isDouble() ||
                    actualReceiverType.isShort() || actualReceiverType.isByte() ||
                    actualReceiverType.isBoolean() || actualReceiverType.isChar() ||
                    actualReceiverType.isStringClassType()

                // For any call on a primitive receiver, use a function call instead of method call
                if (isPrimitiveReceiver) {
                    val rawName = function.name.asString()

                    // Handle primitive bitwise, comparison, and arithmetic operations as intrinsics
                    // These operations should be compiled inline, not as function calls
                    when (rawName) {
                        // Arithmetic operators - compile to native BrightScript operators
                        "plus" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.ADD, right)
                        }
                        "minus" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.SUB, right)
                        }
                        "times" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.MUL, right)
                        }
                        "div" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.DIV, right)
                        }
                        "rem" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.MOD, right)
                        }
                        "xor" -> {
                            // BrightScript has NO native XOR operator
                            // Implement as: (a or b) and not (a and b)
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            // (left or right) and not (left and right)
                            val orPart = BrsBinaryOp(left, BrsBinaryOperator.OR, right)
                            val andPart = BrsBinaryOp(left.deepCopy(), BrsBinaryOperator.AND, right.deepCopy())
                            val notAndPart = BrsUnaryOp(BrsUnaryOperator.NOT, andPart)
                            return BrsBinaryOp(orPart, BrsBinaryOperator.AND, notAndPart)
                        }
                        "and" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.AND, right)
                        }
                        "or" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.OR, right)
                        }
                        "inv" -> {
                            return BrsUnaryOp(BrsUnaryOperator.NOT, receiverExpr)
                        }
                        "compareTo" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            // String/Char comparison uses string comparison; numeric types use __kotlin_intCompare
                            return if (actualReceiverType.isChar() || actualReceiverType.isStringClassType()) {
                                // For Char and String (strings in BrightScript), use __kotlin_stringCompare
                                // which compares strings lexicographically and returns -1, 0, or 1
                                createFunctionCall("__kotlin_stringCompare", mutableListOf(left, right), context)
                            } else {
                                // Use runtime helper function for numeric compareTo
                                createFunctionCall("__kotlin_intCompare", mutableListOf(left, right), context)
                            }
                        }
                        "equals" -> {
                            // String.equals(Any?) or primitive.equals(Any?) should be compiled to native =
                            // Only for the single-argument version (not the ignoreCase extension)
                            if (expression.valueArgumentsCount == 1) {
                                val left = receiverExpr
                                val right = expression.getValueArgument(0)!!.accept(this, data)
                                return BrsBinaryOp(left, BrsBinaryOperator.EQ, right)
                            }
                            // For multi-argument equals (extension function), fall through
                        }
                        "hashCode" -> {
                            // For strings, use a hash function; for primitives, the value itself
                            return if (actualReceiverType.isStringClassType()) {
                                // String hashCode needs a runtime helper
                                createFunctionCall("__kotlin_stringHashCode", mutableListOf(receiverExpr), context)
                            } else {
                                // For numeric primitives, the value is the hash
                                receiverExpr
                            }
                        }
                        "toString" -> {
                            // For strings, just return the string; for primitives, convert
                            return if (actualReceiverType.isStringClassType()) {
                                receiverExpr
                            } else {
                                BrsFunctionCall(
                                    BrsIdentifier("Str"),
                                    mutableListOf(receiverExpr)
                                )
                            }
                        }
                        "shl" -> {
                            // Left shift: a * (2 ^ b)
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            val powerOf2 = BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, right)
                            return BrsBinaryOp(left, BrsBinaryOperator.MUL, powerOf2)
                        }
                        "shr" -> {
                            // Signed right shift: a \ (2 ^ b) using integer division
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            val powerOf2 = BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, right)
                            return BrsBinaryOp(left, BrsBinaryOperator.INT_DIV, powerOf2)
                        }
                        "ushr" -> {
                            // Unsigned right shift - use helper function
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return createFunctionCall("__kotlin_ushr", mutableListOf(left, right), context)
                        }
                    }

                    val args = mutableListOf<BrsExpression>()
                    args.add(receiverExpr)
                    for (i in 0 until expression.valueArgumentsCount) {
                        expression.getValueArgument(i)?.let { arg ->
                            args.add(arg.accept(this, data))
                        }
                    }
                    // For extension functions on primitives, the function name should NOT have
                    // a class prefix. Extension functions are defined as top-level functions with
                    // the receiver type encoded in the signature (e.g., rangeTo_rI_I_IntRange_k_).
                    // The issue is getBrsName returns a name based on the call-site parent (Int class),
                    // not the definition-site parent (package). We need to reconstruct the correct name.
                    // Check if functionName starts with a primitive class prefix that needs stripping
                    val primitiveClassPrefixes = listOf("Int_", "Long_", "Float_", "Double_", "Short_", "Byte_", "Boolean_", "Char_")
                    val hasWrongClassPrefix = primitiveClassPrefixes.any { functionName.startsWith(it) }

                    val correctedFunctionName = if (hasWrongClassPrefix) {
                        // Rebuild the function name with correct format:
                        // functionName_rReceiverType_ParamTypes_k_
                        // Note: Return types are NOT included (like Java) to support polymorphism
                        val rawName = function.name.asString()

                        // Build signature parts
                        val signatureParts = mutableListOf<String>()

                        // Add receiver type with 'r' prefix - use the actual receiver type
                        signatureParts.add("r" + context.typeToMangledString(actualReceiverType))

                        // Add parameter types
                        function.valueParameters.forEach { param ->
                            signatureParts.add(context.typeToMangledString(param.type))
                        }

                        // Build the full name (no return type)
                        val signature = signatureParts.joinToString("_")
                        "${rawName}_${signature}_k_"
                    } else {
                        functionName
                    }
                    return createFunctionCall(correctedFunctionName, args, context)
                }

                // Check if this is a call on a singleton object
                // Singleton methods that reference `m` (like property getters) need to be called
                // as methods on the singleton instance, not as standalone functions.
                val parentClass = function.parent as? IrClass
                if (parentClass?.kind == ClassKind.OBJECT) {
                    val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.let { it.accept(this, data) }
                    }
                    val singletonName = context.getBrsName(parentClass)
                    context.recordFunctionDependency("${singletonName}_getInstance")
                    val singletonInstance = BrsFunctionCall(BrsIdentifier("${singletonName}_getInstance"), mutableListOf())
                    // Property accessors are attached with simple names (__get_X, __set_X)
                    // Regular methods are attached with mangled names
                    val rawFuncName = function.name.asString()
                    val methodName = if (rawFuncName.startsWith("<get-") && rawFuncName.endsWith(">")) {
                        "__get_" + rawFuncName.removePrefix("<get-").removeSuffix(">")
                    } else if (rawFuncName.startsWith("<set-") && rawFuncName.endsWith(">")) {
                        "__set_" + rawFuncName.removePrefix("<set-").removeSuffix(">")
                    } else {
                        // Use mangled name for regular methods
                        val fullMethodName = context.getBrsName(function)
                        fullMethodName.removePrefix("${singletonName}_")
                    }
                    return BrsMethodCall(singletonInstance, methodName, args.toMutableList())
                }

                // Handle property getters and setters
                // Check if the property has a simple backing field that can be accessed directly.
                // For computed properties (custom getter), overridden properties (interface impl),
                // or properties from interfaces, we need to call the getter method.
                val rawName = function.name.asString()
                if (rawName.startsWith("<get-") && rawName.endsWith(">")) {
                    val fieldName = rawName.removePrefix("<get-").removeSuffix(">")
                    val property = function.correspondingPropertySymbol?.owner
                    val backingField = property?.backingField

                    // Special handling for SceneGraph component scope properties (top, global, m)
                    // When in component context, these compile to m.top, m.global, m
                    // When inside a lambda, use getComponentMRef() to get the captured component reference
                    if (genCtx.isInComponentContext && context.intrinsics.isComponentScopeProperty(fieldName)) {
                        val componentM = genCtx.getComponentMRef()
                        return when (fieldName) {
                            "m" -> componentM  // Just m (or m._componentM in lambda)
                            else -> BrsDotAccess(componentM, fieldName)  // m.top, m.global
                        }
                    }

                    // Handle properties of SceneGraph component classes (receiver-aware).
                    // Fake overrides are resolved so properties inherited from a klib base
                    // (e.g. TaskComponent's kotlinTask* protocol fields) expose their
                    // annotations and backing field.
                    // - receiver is `this` of the current component: interface fields
                    //   (@SG*Field / @BrsField) compile to m.top.fieldName, internal state
                    //   to m.fieldName (m.top/m via getComponentMRef() inside lambdas)
                    // - any other receiver: interface fields compile to direct node field
                    //   access on the receiver handle (<receiverExpr>.fieldName)
                    run {
                        val resolvedGetter = (if (function.isFakeOverride) function.resolveFakeOverride() else null) ?: function
                        val componentProperty = resolvedGetter.correspondingPropertySymbol?.owner
                        val componentPropertyClass = resolvedGetter.parent as? IrClass
                        if (componentProperty != null && componentPropertyClass != null &&
                            context.intrinsics.isSceneGraphComponent(componentPropertyClass)
                        ) {
                            val isSelfAccess = genCtx.isInComponentContext && isCurrentComponentSelfReceiver(receiver)
                            if (isSelfAccess && componentProperty.backingField != null) {
                                val componentM = genCtx.getComponentMRef()

                                // Delegated properties must call the getter to unwrap the delegate
                                if (componentProperty.isDelegated) {
                                    return BrsMethodCall(componentM, "__get_${fieldName}_k_", mutableListOf())
                                }

                                return if (hasInterfaceFieldAnnotation(componentProperty)) {
                                    // Interface field - access via m.top
                                    BrsDotAccess(BrsDotAccess(componentM, "top"), fieldName)
                                } else {
                                    // Internal state - access via m
                                    // Layout properties are stored with underscore prefix (see layout initialization code)
                                    val actualFieldName = if (isLayoutClassProperty(componentProperty, componentPropertyClass)) {
                                        "_$fieldName"
                                    } else {
                                        fieldName
                                    }
                                    BrsDotAccess(componentM, actualFieldName)
                                }
                            }
                            if (!isSelfAccess && hasInterfaceFieldAnnotation(componentProperty) && !componentProperty.isDelegated) {
                                // Another instance's interface field: direct node field access
                                // works on the raw roSGNode handle
                                return BrsDotAccess(receiverExpr, fieldName)
                            }
                            // Computed self properties and un-annotated state on another
                            // instance fall through to the accessor-call path below.
                        }
                    }

                    // Determine if we should use direct field access or call the getter method
                    // Use direct access ONLY if:
                    // 1. There is a backing field
                    // 2. The getter is not overridden (interface implementation)
                    // 3. The parent class is a data class (simple properties)
                    val isOverridden = function.overriddenSymbols.isNotEmpty()
                    val parentClass = function.parent as? IrClass
                    val isDataClass = parentClass?.isData == true
                    val hasSimpleBackingField = backingField != null && !isOverridden && isDataClass

                    return if (hasSimpleBackingField) {
                        // Direct field access for simple backing field properties
                        BrsDotAccess(receiverExpr, fieldName)
                    } else {
                        // Call getter for computed properties, overridden properties, etc.
                        // SceneGraph component property accessors use mangled names (with _k_ suffix)
                        // Regular class property accessors use simple names (no suffix)
                        val isComponentProperty = parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)
                        val getterName = if (isComponentProperty) "__get_${fieldName}_k_" else "__get_$fieldName"
                        BrsMethodCall(receiverExpr, getterName, mutableListOf())
                    }
                }

                // Handle property setters
                if (rawName.startsWith("<set-") && rawName.endsWith(">")) {
                    val fieldName = rawName.removePrefix("<set-").removeSuffix(">")
                    val property = function.correspondingPropertySymbol?.owner
                    val backingField = property?.backingField
                    val value = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()

                    // Handle properties of SceneGraph component classes (receiver-aware).
                    // Mirrors the getter logic above: fake overrides are resolved so
                    // inherited klib properties expose their annotations/backing field;
                    // `this` of the current component writes m.top.fieldName / m.fieldName,
                    // any other receiver writes the interface field directly on the handle.
                    run {
                        val resolvedSetter = (if (function.isFakeOverride) function.resolveFakeOverride() else null) ?: function
                        val componentProperty = resolvedSetter.correspondingPropertySymbol?.owner
                        val componentPropertyClass = resolvedSetter.parent as? IrClass
                        if (componentProperty != null && componentPropertyClass != null &&
                            context.intrinsics.isSceneGraphComponent(componentPropertyClass)
                        ) {
                            val isSelfAccess = genCtx.isInComponentContext && isCurrentComponentSelfReceiver(receiver)
                            if (isSelfAccess && componentProperty.backingField != null) {
                                val componentM = genCtx.getComponentMRef()

                                // Delegated properties must call the setter
                                if (componentProperty.isDelegated) {
                                    return BrsMethodCall(componentM, "__set_${fieldName}_k_", mutableListOf(value))
                                }

                                val target = if (hasInterfaceFieldAnnotation(componentProperty)) {
                                    // Interface field - access via m.top
                                    BrsDotAccess(BrsDotAccess(componentM, "top"), fieldName)
                                } else {
                                    // Internal state - access via m
                                    // Layout properties are stored with underscore prefix (see layout initialization code)
                                    val actualFieldName = if (isLayoutClassProperty(componentProperty, componentPropertyClass)) {
                                        "_$fieldName"
                                    } else {
                                        fieldName
                                    }
                                    BrsDotAccess(componentM, actualFieldName)
                                }
                                return BrsBinaryOp(target, BrsBinaryOperator.EQ, value)
                            }
                            if (!isSelfAccess && hasInterfaceFieldAnnotation(componentProperty) && !componentProperty.isDelegated) {
                                // Another instance's interface field: direct node field access
                                // works on the raw roSGNode handle
                                return BrsBinaryOp(BrsDotAccess(receiverExpr, fieldName), BrsBinaryOperator.EQ, value)
                            }
                            // Computed self properties and un-annotated state on another
                            // instance fall through to the accessor-call path below.
                        }
                    }

                    // Mirror the getter logic: use direct field assignment for data classes
                    // with simple backing fields, call setter method otherwise
                    val isOverridden = function.overriddenSymbols.isNotEmpty()
                    val parentClass = function.parent as? IrClass
                    val isDataClass = parentClass?.isData == true
                    val hasSimpleBackingField = backingField != null && !isOverridden && isDataClass

                    return if (hasSimpleBackingField) {
                        // Direct field assignment for simple backing field properties
                        BrsBinaryOp(
                            BrsDotAccess(receiverExpr, fieldName),
                            BrsBinaryOperator.EQ,
                            value
                        )
                    } else {
                        // Call setter for computed properties, overridden properties, etc.
                        // SceneGraph component property accessors use mangled names (with _k_ suffix)
                        // Regular class property accessors use simple names (no suffix)
                        val isComponentProperty = parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)
                        val setterName = if (isComponentProperty) "__set_${fieldName}_k_" else "__set_$fieldName"
                        BrsMethodCall(receiverExpr, setterName, mutableListOf(value))
                    }
                }

                // For regular method calls, determine the method name
                val rawMethodName = function.name.asString()

                // Check if the method belongs to an external interface (native BrightScript type)
                // External interface methods use their simple names without mangling
                // Note: parentClass is already defined above at line 4101
                val isExternalInterfaceMethod = parentClass != null &&
                    (parentClass.isExternal || isExternalClass(parentClass))

                // Data class synthetic methods (componentN, copy, equals, hashCode, toString)
                // are attached with simple names, so we should not mangle them
                val isDataClassSyntheticMethod = rawMethodName.startsWith("component") ||
                    rawMethodName in listOf("copy", "equals", "hashCode", "toString")

                val methodName = if (isExternalInterfaceMethod || isDataClassSyntheticMethod) {
                    // Use simple name for external interface methods and data class synthetic
                    // methods. @BrsName overrides the simple name so distinct Kotlin declarations
                    // can target the same native method (e.g. the port form of observeField);
                    // fake overrides are resolved to the declaration carrying the annotation.
                    val resolved = function.resolveFakeOverride() ?: function
                    context.getBrsNameOverride(resolved) ?: rawMethodName
                } else {
                    // Use mangled name for other methods (to match how methods are attached)
                    val fullMethodName = context.getBrsName(function)
                    val className = parentClass?.let { context.getBrsName(it) } ?: ""
                    if (className.isNotEmpty()) {
                        fullMethodName.removePrefix("${className}_")
                    } else {
                        fullMethodName
                    }
                }
                val args = mutableListOf<BrsExpression>()
                for (i in 0 until expression.valueArgumentsCount) {
                    val arg = expression.getValueArgument(i)
                    if (arg != null) {
                        args.add(arg.accept(this, data))
                    } else {
                        // Parameter uses default value - add invalid to preserve position
                        args.add(BrsInvalidLiteral())
                    }
                }
                return BrsMethodCall(receiverExpr, methodName, args)
            }
            arguments.add(receiverExpr)
        }

        // Add extension receiver if present
        expression.extensionReceiver?.let { receiver ->
            val receiverType = receiver.type
            // Check if extension receiver is a primitive type
            val isPrimitiveExtension = receiverType.isInt() || receiverType.isLong() ||
                receiverType.isFloat() || receiverType.isDouble() ||
                receiverType.isShort() || receiverType.isByte() ||
                receiverType.isBoolean() || receiverType.isChar()

            if (isPrimitiveExtension) {
                // For extension functions on primitives, we need to fix the function name.
                // When functions are loaded from klib, the parent might be the class (e.g., Int)
                // instead of the package, causing getBrsName to prepend "Int_" to the name.
                // The correct format is: functionName_rReceiverType_ParamTypes_k_
                // Note: Return types are NOT included (like Java) to support polymorphism

                // Check if functionName starts with a primitive class prefix that needs stripping
                val primitiveClassPrefixes = listOf("Int_", "Long_", "Float_", "Double_", "Short_", "Byte_", "Boolean_", "Char_")
                val hasWrongClassPrefix = primitiveClassPrefixes.any { functionName.startsWith(it) }

                val correctedName = if (hasWrongClassPrefix) {
                    // Strip the class prefix and rebuild the correct name
                    val rawName = function.name.asString()
                    val signatureParts = mutableListOf<String>()

                    // Add receiver type with 'r' prefix - use the expression's receiver type
                    signatureParts.add("r" + context.typeToMangledString(receiverType))

                    // Add parameter types
                    function.valueParameters.forEach { param ->
                        signatureParts.add(context.typeToMangledString(param.type))
                    }

                    // Build the full name (no return type)
                    val signature = signatureParts.joinToString("_")
                    "${rawName}_${signature}_k_"
                } else {
                    // Name is already correct (e.g., top-level extension function)
                    functionName
                }

                val args = mutableListOf<BrsExpression>()
                args.add(receiver.accept(this, data))
                for (i in 0 until expression.valueArgumentsCount) {
                    val arg = expression.getValueArgument(i)
                    if (arg != null) {
                        args.add(arg.accept(this, data))
                    } else {
                        // Parameter uses default value - add invalid to preserve position
                        args.add(BrsInvalidLiteral())
                    }
                }
                return createFunctionCall(correctedName, args, context)
            }

            arguments.add(receiver.accept(this, data))
        }

        // Add value arguments, using invalid for default parameters to preserve positional alignment
        for (i in 0 until expression.valueArgumentsCount) {
            val arg = expression.getValueArgument(i)
            if (arg != null) {
                arguments.add(arg.accept(this, data))
            } else {
                // Parameter uses default value - add invalid to preserve position
                arguments.add(BrsInvalidLiteral())
            }
        }

        // Handle companion object/singleton function calls without a dispatch receiver.
        // When calling a method on a companion object statically (e.g., UInt.MIN_VALUE),
        // the IR doesn't provide a dispatch receiver, but the generated getter function
        // expects `m` to be the companion instance. We need to call the function as a method
        // on the singleton instance.
        val parentClass = function.parent as? IrClass
        if (expression.dispatchReceiver == null && parentClass?.kind == ClassKind.OBJECT) {
            // This is a call on a singleton/companion object without a receiver.
            // Call it as a method on the singleton instance.
            val singletonName = context.getBrsName(parentClass)
            context.recordFunctionDependency("${singletonName}_getInstance")
            val singletonInstance = BrsFunctionCall(BrsIdentifier("${singletonName}_getInstance"), mutableListOf())

            // Property accessors are attached with simple names (__get_X, __set_X)
            // Regular methods are attached with mangled names
            val rawFuncName = function.name.asString()
            val methodName = if (rawFuncName.startsWith("<get-") && rawFuncName.endsWith(">")) {
                "__get_" + rawFuncName.removePrefix("<get-").removeSuffix(">")
            } else if (rawFuncName.startsWith("<set-") && rawFuncName.endsWith(">")) {
                "__set_" + rawFuncName.removePrefix("<set-").removeSuffix(">")
            } else {
                // Use mangled name for regular methods
                val fullMethodName = context.getBrsName(function)
                fullMethodName.removePrefix("${singletonName}_")
            }

            return BrsMethodCall(singletonInstance, methodName, arguments)
        }

        // Plain global function call - record the dependency unless the function is
        // external (external functions are native BrightScript, they have no .brs file)
        if (function.isExternal) {
            return BrsFunctionCall(BrsIdentifier(functionName), arguments)
        }
        return createFunctionCall(functionName, arguments, context)
    }

    /**
     * Transform a call to a @BrsInline function by inlining the parsed BrightScript code.
     */
    private fun transformBrsInlineCall(expression: IrCall): BrsExpression {
        val info = parent.inlineCallTransformer.getInlineInfo(expression)
            ?: return BrsInvalidLiteral()

        // Build argument map: parameter name -> BrsExpression
        val function = expression.symbol.owner
        val arguments = mutableMapOf<String, BrsExpression>()
        function.valueParameters.forEachIndexed { index, param ->
            expression.getValueArgument(index)?.let { arg ->
                arguments[param.name.asString()] = arg.accept(this, Unit)
            }
        }

        // Transform inline code with substitutions
        val statements = parent.inlineCallTransformer.transformInlineCode(info, arguments)

        // Extract expression from statements (most @BrsInline is "return expr")
        return when {
            statements.size == 1 && statements[0] is BrsReturn ->
                (statements[0] as BrsReturn).value ?: BrsInvalidLiteral()
            statements.size == 1 && statements[0] is BrsExpressionStatement ->
                (statements[0] as BrsExpressionStatement).expression
            else -> {
                val lastStmt = statements.lastOrNull()
                if (lastStmt is BrsReturn) lastStmt.value ?: BrsInvalidLiteral()
                else BrsInvalidLiteral()
            }
        }
    }

    private fun transformIntrinsic(expression: IrCall): BrsExpression {
        val function = expression.symbol.owner
        val name = function.name.asString()

        // Check if this is a stdlib intrinsic
        if (context.intrinsics.isStdlibIntrinsic(expression.symbol)) {
            val intrinsicName = context.intrinsics.getIntrinsicName(expression.symbol)
            return transformStdlibIntrinsic(expression, intrinsicName)
        }

        return when (name) {
            "createObject" -> {
                val typeArg = expression.getValueArgument(0)
                val objectType = (typeArg as? IrConst)?.value?.toString() ?: "Object"
                val args = (1 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { it.accept(this, Unit) }
                }
                BrsCreateObject(objectType, args.toMutableList())
            }
            "typeOf" -> {
                val arg = expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: BrsInvalidLiteral()
                BrsTypeOf(arg)
            }
            "print" -> {
                val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { it.accept(this, Unit) }
                }
                BrsFunctionCall(BrsIdentifier("print"), args.toMutableList())
            }
            "brs" -> {
                // Handle inline BrightScript code: brs("code here")
                val codeArg = expression.getValueArgument(0)
                val codeString = foldBrsCodeString(codeArg)
                if (codeString == null) {
                    context.reportError(expression, "brs() argument must be a compile-time constant string")
                    return BrsInvalidLiteral()
                }

                // Parse the BrightScript code
                val parseResult = parseBrightScriptStatements(codeString)
                if (parseResult.hasErrors) {
                    for (error in parseResult.errors) {
                        context.reportError(expression, "Error in brs() code: $error")
                    }
                    return BrsInvalidLiteral()
                }

                // Extract expression from parsed statements.
                // Contract: exactly one statement or expression. See
                // compiler/ir/backend.brightscript/docs/brs-intrinsic.md.
                val statements = parseResult.result
                when {
                    statements.isEmpty() -> {
                        context.reportError(
                            expression,
                            "brs() argument parsed to no statements; expected a single expression or statement"
                        )
                        BrsInvalidLiteral()
                    }
                    statements.size == 1 && statements[0] is BrsReturn ->
                        (statements[0] as BrsReturn).value ?: BrsInvalidLiteral()
                    statements.size == 1 && statements[0] is BrsExpressionStatement ->
                        (statements[0] as BrsExpressionStatement).expression
                    statements.size == 1 -> BrsInvalidLiteral()
                    else -> {
                        context.reportError(
                            expression,
                            "brs() must contain exactly one expression or statement; got ${statements.size}. " +
                                "Split into multiple brs() calls instead."
                        )
                        BrsInvalidLiteral()
                    }
                }
            }
            else -> BrsFunctionCall(BrsIdentifier(name), mutableListOf())
        }
    }

    /**
     * Constant-fold a string expression for brs() inline code.
     * Returns null if the expression is not a compile-time constant string.
     *
     * `const val` references are followed to their initializer so that
     * `brs("print ${CONST}")` folds identically to `brs("print literal")`.
     * This matches the FIR checker's `canBeEvaluatedAtCompileTime` acceptance.
     */
    private fun foldBrsCodeString(expression: IrExpression?): String? {
        if (expression == null) return null
        return when (expression) {
            is IrConst -> {
                if (expression.kind == IrConstKind.String) expression.value as String
                else null
            }
            is IrStringConcatenation -> {
                // Support string templates like brs("print ${someConstant}")
                val builder = StringBuilder()
                for (arg in expression.arguments) {
                    val part = foldBrsCodeString(arg) ?: return null
                    builder.append(part)
                }
                builder.toString()
            }
            is IrGetValue -> {
                val owner = expression.symbol.owner
                if (owner is IrVariable && owner.isConst) {
                    foldBrsCodeString(owner.initializer)
                } else null
            }
            is IrGetField -> {
                // Mirrors visitGetField's inline-at-visit logic: a final field
                // with a compile-time-constant initializer can be spliced at fold time.
                val field = expression.symbol.owner
                if (field.isFinal) {
                    foldBrsCodeString(field.initializer?.expression)
                } else null
            }
            is IrCall -> {
                // Property getter for a `const val`. `property.isConst` is the
                // Kotlin source-level guarantee that this is a const, not a
                // user-written getter with a body.
                val getter = expression.symbol.owner
                val property = getter.correspondingPropertySymbol?.owner
                if (property?.isConst == true) {
                    foldBrsCodeString(property.backingField?.initializer?.expression)
                } else null
            }
            else -> null
        }
    }

    /**
     * Transform a stdlib intrinsic call to its BrightScript equivalent.
     */
    private fun transformStdlibIntrinsic(expression: IrCall, name: String): BrsExpression {
        val intrinsic = context.intrinsics.stdlibIntrinsicMapping[name]

        // Get all arguments
        val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, Unit) }
        }

        return when (intrinsic) {
            is BrsIntrinsics.StdlibIntrinsic.SimpleCall -> {
                // Simple function call: brsIntrinsicSin(x) -> Sin(x)
                BrsFunctionCall(BrsIdentifier(intrinsic.brsName), args.toMutableList())
            }

            is BrsIntrinsics.StdlibIntrinsic.Pow -> {
                // Power: x ^ y
                if (args.size >= 2) {
                    BrsBinaryOp(args[0], BrsBinaryOperator.POW, args[1])
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Atan2 -> {
                // atan2(y, x) in BrightScript can be computed as Atn(y/x) with quadrant adjustment
                // For now, use a simplified version (BrightScript doesn't have native atan2)
                if (args.size >= 2) {
                    // Generate: Atn(y / x)
                    // Note: This is simplified and doesn't handle all quadrants correctly
                    // A proper implementation would need runtime checks
                    BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(args[0], BrsBinaryOperator.DIV, args[1]))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Asin -> {
                // asin(x) = atan(x / sqrt(1 - x*x))
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: Atn(x / Sqr(1 - x * x))
                    val xSquared = BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.MUL, x.deepCopy())
                    val oneMinusXSquared = BrsBinaryOp(BrsDoubleLiteral(1.0), BrsBinaryOperator.SUB, xSquared)
                    val sqrtPart = BrsFunctionCall(BrsIdentifier("Sqr"), mutableListOf(oneMinusXSquared))
                    BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.DIV, sqrtPart))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Acos -> {
                // acos(x) = atan(sqrt(1 - x*x) / x) + adjustment for x < 0
                // Simplified: acos(x) = pi/2 - asin(x)
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: Atn(Sqr(1 - x * x) / x)
                    // Note: This doesn't handle negative x correctly (needs pi adjustment)
                    // For full correctness, use: pi/2 - Atn(x / Sqr(1 - x * x))
                    val xSquared = BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.MUL, x.deepCopy())
                    val oneMinusXSquared = BrsBinaryOp(BrsDoubleLiteral(1.0), BrsBinaryOperator.SUB, xSquared)
                    val sqrtPart = BrsFunctionCall(BrsIdentifier("Sqr"), mutableListOf(oneMinusXSquared))
                    // pi/2 - asin(x) = 1.5707963267948966 - Atn(x / Sqr(1 - x*x))
                    val asinPart = BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.DIV, sqrtPart))
                    )
                    BrsBinaryOp(BrsDoubleLiteral(1.5707963267948966), BrsBinaryOperator.SUB, asinPart)
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Ceil -> {
                // ceil(x): Int(x) + (1 if x > Int(x) else 0)
                // Simplified: use conditional expression
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: if x > Int(x) then Int(x) + 1 else Int(x)
                    val intX = BrsFunctionCall(BrsIdentifier("Int"), mutableListOf(x.deepCopy()))
                    BrsConditional(
                        BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.GT, intX.deepCopy()),
                        BrsBinaryOp(intX.deepCopy(), BrsBinaryOperator.ADD, BrsIntLiteral(1)),
                        intX
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Floor -> {
                // floor(x): Int(x)
                if (args.isNotEmpty()) {
                    BrsFunctionCall(BrsIdentifier("Int"), mutableListOf(args[0]))
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Round -> {
                // round(x): Int(x + 0.5)
                if (args.isNotEmpty()) {
                    BrsFunctionCall(
                        BrsIdentifier("Int"),
                        mutableListOf(BrsBinaryOp(args[0], BrsBinaryOperator.ADD, BrsDoubleLiteral(0.5)))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Sinh -> {
                // sinh(x): (Exp(x) - Exp(-x)) / 2
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val expX = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(x.deepCopy()))
                    val expNegX = BrsFunctionCall(
                        BrsIdentifier("Exp"),
                        mutableListOf(BrsUnaryOp(BrsUnaryOperator.NEG, x.deepCopy()))
                    )
                    BrsBinaryOp(
                        BrsBinaryOp(expX, BrsBinaryOperator.SUB, expNegX),
                        BrsBinaryOperator.DIV,
                        BrsDoubleLiteral(2.0)
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Cosh -> {
                // cosh(x): (Exp(x) + Exp(-x)) / 2
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val expX = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(x.deepCopy()))
                    val expNegX = BrsFunctionCall(
                        BrsIdentifier("Exp"),
                        mutableListOf(BrsUnaryOp(BrsUnaryOperator.NEG, x.deepCopy()))
                    )
                    BrsBinaryOp(
                        BrsBinaryOp(expX, BrsBinaryOperator.ADD, expNegX),
                        BrsBinaryOperator.DIV,
                        BrsDoubleLiteral(2.0)
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Tanh -> {
                // tanh(x): (Exp(2x) - 1) / (Exp(2x) + 1)
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val twoX = BrsBinaryOp(BrsDoubleLiteral(2.0), BrsBinaryOperator.MUL, x.deepCopy())
                    val exp2X = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(twoX))
                    BrsBinaryOp(
                        BrsBinaryOp(exp2X.deepCopy(), BrsBinaryOperator.SUB, BrsDoubleLiteral(1.0)),
                        BrsBinaryOperator.DIV,
                        BrsBinaryOp(exp2X, BrsBinaryOperator.ADD, BrsDoubleLiteral(1.0))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Print -> {
                // print statement
                if (intrinsic.newline) {
                    BrsFunctionCall(BrsIdentifier("print"), args.toMutableList())
                } else {
                    // Print without newline: print arg;
                    // BrightScript uses semicolon to suppress newline
                    BrsPrintNoNewline(args.firstOrNull() ?: BrsStringLiteral(""))
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CurrentTimeMillis -> {
                // CreateObject("roTimespan").TotalMilliseconds()
                BrsMethodCall(
                    BrsCreateObject("roTimespan", mutableListOf()),
                    "TotalMilliseconds",
                    mutableListOf()
                )
            }

            is BrsIntrinsics.StdlibIntrinsic.RandomSeed -> {
                // Rnd(0) to seed from system time
                BrsFunctionCall(BrsIdentifier("Rnd"), mutableListOf(BrsIntLiteral(0)))
            }

            is BrsIntrinsics.StdlibIntrinsic.CreateObject -> {
                // CreateObject(type, ...)
                if (args.isNotEmpty()) {
                    val typeArg = args[0]
                    val objectType = if (typeArg is BrsStringLiteral) typeArg.value else "Object"
                    BrsCreateObject(objectType, args.drop(1).toMutableList())
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.ToString -> {
                // Type-aware toString for Any? values
                // Generate runtime type checking to handle primitives properly
                if (args.isNotEmpty()) {
                    generateRuntimeToString(args[0], context)
                } else {
                    BrsStringLiteral("null")
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.IsAssociativeArray -> {
                // Type(a) = "roAssociativeArray"
                if (args.isNotEmpty()) {
                    BrsBinaryOp(
                        BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(args[0])),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral("roAssociativeArray")
                    )
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.IsSGNode -> {
                // Type(a) = "roSGNode"
                // roSGNode cannot be compared with = operator - causes Type Mismatch error
                if (args.isNotEmpty()) {
                    BrsBinaryOp(
                        BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(args[0])),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral("roSGNode")
                    )
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallEquals -> {
                // a.equals(b) - direct method call on roAssociativeArray
                if (args.size >= 2) {
                    BrsMethodCall(args[0], "equals", mutableListOf(args[1]))
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.NativeEquals -> {
                // a = b - native BrightScript comparison
                if (args.size >= 2) {
                    BrsBinaryOp(args[0], BrsBinaryOperator.EQ, args[1])
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.NativeCompare -> {
                // Native comparison returning -1, 0, or 1
                // Generates: if a < b then -1 else if a > b then 1 else 0
                if (args.size >= 2) {
                    BrsConditional(
                        BrsBinaryOp(args[0], BrsBinaryOperator.LT, args[1]),
                        BrsIntLiteral(-1),
                        BrsConditional(
                            BrsBinaryOp(args[0], BrsBinaryOperator.GT, args[1]),
                            BrsIntLiteral(1),
                            BrsIntLiteral(0)
                        )
                    )
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallCompareTo -> {
                // a.compareTo(b) - direct method call on roAssociativeArray
                if (args.size >= 2) {
                    BrsMethodCall(args[0], "compareTo", mutableListOf(args[1]))
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallComparator -> {
                // comparator.compare_AnyN_AnyN_k_(a, b) - call Comparator's compare method directly
                if (args.size >= 3) {
                    BrsMethodCall(args[0], "compare_AnyN_AnyN_k_", mutableListOf(args[1], args[2]))
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Instr -> {
                // Instr(start, source, substring) with index conversion
                // BrightScript Instr: 1-based start, returns 0 if not found, 1-based position otherwise
                // Kotlin indexOf: 0-based start, returns -1 if not found, 0-based position otherwise
                // Generate: if Instr(start+1, source, substring) = 0 then -1 else Instr(start+1, source, substring) - 1
                if (args.size >= 3) {
                    val start = args[0]  // 0-based
                    val source = args[1]
                    val substring = args[2]
                    val startPlus1 = BrsBinaryOp(start, BrsBinaryOperator.ADD, BrsIntLiteral(1))
                    val instrCall = BrsFunctionCall(BrsIdentifier("Instr"), mutableListOf(startPlus1, source, substring))
                    BrsConditional(
                        BrsBinaryOp(instrCall.deepCopy(), BrsBinaryOperator.EQ, BrsIntLiteral(0)),
                        BrsIntLiteral(-1),
                        BrsBinaryOp(instrCall, BrsBinaryOperator.SUB, BrsIntLiteral(1))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.GetLength -> {
                // Call __get_length() directly on an object: obj.__get_length()
                // This is used by __kotlin_charSequenceLength to avoid recursion
                if (args.isNotEmpty()) {
                    BrsFunctionCall(
                        BrsDotAccess(args[0], "__get_length"),
                        mutableListOf()
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.FunctionName -> {
                // Extract the mangled BrightScript name from a function reference
                // brsName(::myFunction) -> "ClassName_myFunction_k_"
                //
                // IMPORTANT: Always return the FULL mangled name (with class prefix).
                // Roku's observeFieldScoped looks for callback functions at module/script level,
                // NOT in m scope. The function exists at module level with its full name:
                //   sub TestLayout_onButtonPressed_RoSGNodeEvent_k_(message as Object)
                // So the observer must use that full name for Roku to find it.
                val arg = expression.getValueArgument(0)
                when (arg) {
                    is IrFunctionReference -> {
                        val function = arg.symbol.owner
                        val mangledName = context.getBrsName(function)
                        // The returned name is used as a callback (observeField / functionName
                        // fields), so the defining file must be in the component's includes.
                        // Skip external functions (native, no .brs file) and local functions
                        // (emitted inline in the current file, never in a manifest).
                        if (!function.isExternal && function.parent !is IrFunction) {
                            context.recordFunctionDependency(mangledName)
                        }
                        BrsStringLiteral(mangledName)
                    }
                    else -> {
                        context.reportError(
                            expression,
                            "brsName() requires a function reference (::functionName), got ${arg?.javaClass?.simpleName}"
                        )
                        BrsStringLiteral("invalid_function_reference")
                    }
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.UpTime -> {
                // UpTime(dummy) - requires an integer argument per BrightScript docs
                BrsFunctionCall(BrsIdentifier("UpTime"), mutableListOf(BrsIntLiteral(0)))
            }

            null -> {
                // Unknown intrinsic - generate as function call with the name stripped of prefix
                val simpleName = name.removePrefix("brsIntrinsic")
                BrsFunctionCall(BrsIdentifier(simpleName), args.toMutableList())
            }
        }
    }

    /**
     * Transform operator calls to BrightScript binary/unary operations.
     * Returns null if the origin is not an operator origin.
     */
    private fun transformOperator(expression: IrCall, origin: IrStatementOrigin): BrsExpression? {
        val function = expression.symbol.owner

        // Special case: EXCLEQ on Boolean.not() call - this means `!=` was lowered to `(a == b).not()`
        // The inner equality call also has EXCLEQ origin, so we must extract operands directly
        // and generate NE to avoid double-negation (NOT of <> would give =).
        if (origin == IrStatementOrigin.EXCLEQ && function.name.asString() == "not") {
            val dispatchReceiver = expression.dispatchReceiver
            if (dispatchReceiver != null && function.valueParameters.isEmpty()) {
                if (dispatchReceiver is IrCall) {
                    val innerCall = dispatchReceiver
                    val (leftIr, rightIr) = when {
                        innerCall.dispatchReceiver != null -> {
                            Pair(innerCall.dispatchReceiver!!, innerCall.getValueArgument(0)!!)
                        }
                        innerCall.extensionReceiver != null -> {
                            Pair(innerCall.extensionReceiver!!, innerCall.getValueArgument(0)!!)
                        }
                        else -> {
                            Pair(innerCall.getValueArgument(0)!!, innerCall.getValueArgument(1)!!)
                        }
                    }

                    // Check if we need structural comparison for non-primitive types
                    // Exclude null comparisons - BrightScript's = and <> work correctly with invalid
                    val leftType = leftIr.type
                    val rightType = rightIr.type
                    val isNullComparison = (leftIr is IrConst && (leftIr as IrConst).value == null) ||
                                          (rightIr is IrConst && (rightIr as IrConst).value == null)
                    val needsStructuralEquals = !isNullComparison &&
                        (!leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison())

                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)

                    return if (needsStructuralEquals) {
                        // Use NOT brsStructuralEquals() for non-primitive types
                        context.recordFunctionDependency("brsStructuralEquals_AnyN_AnyN_k_")
                        val structuralCall = BrsFunctionCall(
                            BrsIdentifier("brsStructuralEquals_AnyN_AnyN_k_"),
                            mutableListOf(left, right)
                        )
                        BrsUnaryOp(BrsUnaryOperator.NOT, structuralCall)
                    } else {
                        BrsBinaryOp(left, BrsBinaryOperator.NE, right)
                    }
                }
                // Fallback for non-call receivers
                val operand = dispatchReceiver.accept(this, Unit)
                return BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
        }

        // Special case: EXCLEQEQ on Boolean.not() call - this means `!==` was lowered to `(a === b).not()`
        // Extract operands from inner call and generate NOT __kotlin_identityEquals(a, b)
        if (origin == IrStatementOrigin.EXCLEQEQ && function.name.asString() == "not") {
            val dispatchReceiver = expression.dispatchReceiver
            if (dispatchReceiver != null && function.valueParameters.isEmpty()) {
                if (dispatchReceiver is IrCall) {
                    val innerCall = dispatchReceiver
                    val (left, right) = when {
                        innerCall.dispatchReceiver != null -> {
                            val l = innerCall.dispatchReceiver!!.accept(this, Unit)
                            val r = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                        innerCall.extensionReceiver != null -> {
                            val l = innerCall.extensionReceiver!!.accept(this, Unit)
                            val r = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                        else -> {
                            val l = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            val r = innerCall.getValueArgument(1)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                    }
                    // Generate NOT __kotlin_identityEquals(a, b)
                    context.recordFunctionDependency("__kotlin_identityEquals")
                    val identityCall = BrsFunctionCall(
                        BrsIdentifier("__kotlin_identityEquals"),
                        mutableListOf(left, right)
                    )
                    return BrsUnaryOp(BrsUnaryOperator.NOT, identityCall)
                }
                // Fallback for non-call receivers
                val operand = dispatchReceiver.accept(this, Unit)
                return BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
        }

        // Binary operators
        val binaryOp = when (origin) {
            IrStatementOrigin.PLUS -> BrsBinaryOperator.ADD
            IrStatementOrigin.MINUS -> BrsBinaryOperator.SUB
            IrStatementOrigin.MUL -> BrsBinaryOperator.MUL
            IrStatementOrigin.DIV -> BrsBinaryOperator.DIV
            IrStatementOrigin.PERC -> BrsBinaryOperator.MOD
            IrStatementOrigin.LT -> BrsBinaryOperator.LT
            IrStatementOrigin.GT -> BrsBinaryOperator.GT
            IrStatementOrigin.LTEQ -> BrsBinaryOperator.LE
            IrStatementOrigin.GTEQ -> BrsBinaryOperator.GE
            IrStatementOrigin.EQEQ -> BrsBinaryOperator.EQ
            IrStatementOrigin.EXCLEQ -> BrsBinaryOperator.NE
            else -> null
        }

        if (binaryOp != null) {
            // For binary operators, determine left and right operands based on structure:
            // 1. If dispatchReceiver exists: left = dispatchReceiver, right = valueArgument(0)
            // 2. If extensionReceiver exists: left = extensionReceiver, right = valueArgument(0)
            // 3. Otherwise: left = valueArgument(0), right = valueArgument(1)
            //
            // Special case: If this is EXCLEQ but valueArgumentsCount is 0, it means the
            // != was converted to (a == b).not() and we're on the outer call. Skip binary handling.
            if (binaryOp == BrsBinaryOperator.NE && expression.valueArgumentsCount == 0) {
                // This case is handled above in the special EXCLEQ handling, but just in case
                return null
            }

            // ==================== Handle compareTo with comparison origin ====================
            // When FIR lowers `a <= b` for types implementing Comparable (like Char), it generates:
            //   a.compareTo(b) with origin LTEQ
            // This gets wrapped in an outer `<= 0` comparison.
            // We must NOT convert this to a native comparison, or we'll get `(a <= b) <= 0`.
            // Instead, let it fall through to method call handling, which will generate
            // an integer comparison that the outer `<= 0` can properly use.
            if (function.name.asString() == "compareTo" &&
                (binaryOp == BrsBinaryOperator.LT || binaryOp == BrsBinaryOperator.GT ||
                 binaryOp == BrsBinaryOperator.LE || binaryOp == BrsBinaryOperator.GE)) {
                // Skip binary operator handling for compareTo - let it be a method call
                // that returns an Int for the outer comparison
                return null
            }

            // ==================== Enum Comparison Optimization ====================
            // When comparing enum values of the same type, use ordinal comparison for performance.
            // This avoids object comparison overhead and is semantically equivalent since
            // enum entries are singletons with unique ordinals.
            if (binaryOp == BrsBinaryOperator.EQ || binaryOp == BrsBinaryOperator.NE) {
                val (leftIr, rightIr) = when {
                    expression.dispatchReceiver != null -> Pair(expression.dispatchReceiver!!, expression.getValueArgument(0))
                    expression.extensionReceiver != null -> Pair(expression.extensionReceiver!!, expression.getValueArgument(0))
                    else -> Pair(expression.getValueArgument(0), expression.getValueArgument(1))
                }

                if (leftIr != null && rightIr != null) {
                    val leftType = leftIr.type
                    val rightType = rightIr.type

                    // Check if both are the same enum type (non-nullable)
                    val leftClass = leftType.classifierOrNull?.owner as? IrClass
                    val rightClass = rightType.classifierOrNull?.owner as? IrClass

                    if (leftClass != null && rightClass != null &&
                        leftClass == rightClass &&
                        leftClass.kind == ClassKind.ENUM_CLASS &&
                        !leftType.isNullable() && !rightType.isNullable()) {

                        // At least one side should be a compile-time constant for optimization benefit
                        val leftIsConst = leftIr is IrGetEnumValue
                        val rightIsConst = rightIr is IrGetEnumValue

                        if (leftIsConst || rightIsConst) {
                            // Transform to ordinal comparison
                            val leftOrdinal = if (leftIsConst) {
                                val entry = (leftIr as IrGetEnumValue).symbol.owner
                                context.getEnumOrdinal(entry)?.let { BrsIntLiteral(it) }
                            } else {
                                // Access .ordinal on the dynamic enum value
                                BrsDotAccess(leftIr.accept(this, Unit), "ordinal")
                            }

                            val rightOrdinal = if (rightIsConst) {
                                val entry = (rightIr as IrGetEnumValue).symbol.owner
                                context.getEnumOrdinal(entry)?.let { BrsIntLiteral(it) }
                            } else {
                                // Access .ordinal on the dynamic enum value
                                BrsDotAccess(rightIr.accept(this, Unit), "ordinal")
                            }

                            if (leftOrdinal != null && rightOrdinal != null) {
                                return BrsBinaryOp(leftOrdinal, binaryOp, rightOrdinal)
                            }
                        }
                    }
                }
            }
            // ==================== End Enum Comparison Optimization ====================

            val (leftIr, rightIr) = when {
                expression.dispatchReceiver != null -> {
                    Pair(expression.dispatchReceiver!!, expression.getValueArgument(0)!!)
                }
                expression.extensionReceiver != null -> {
                    Pair(expression.extensionReceiver!!, expression.getValueArgument(0)!!)
                }
                else -> {
                    Pair(expression.getValueArgument(0)!!, expression.getValueArgument(1)!!)
                }
            }

            // ==================== Structural Equals for Non-Primitive Types ====================
            // BrightScript's = and <> operators can't compare roAssociativeArray objects (Kotlin classes).
            // For non-primitive types (generics, Any, class instances), use brsStructuralEquals.
            // Exclude null comparisons - BrightScript's = and <> work correctly with invalid.
            if (binaryOp == BrsBinaryOperator.EQ || binaryOp == BrsBinaryOperator.NE) {
                val leftType = leftIr.type
                val rightType = rightIr.type

                // Check if this is a null comparison - these don't need structural equals
                val isNullComparison = (leftIr is IrConst && (leftIr as IrConst).value == null) ||
                                       (rightIr is IrConst && (rightIr as IrConst).value == null)

                // Check if either operand is a non-primitive type that needs structural comparison
                val needsStructuralEquals = !isNullComparison &&
                    (!leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison())

                if (needsStructuralEquals) {
                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)
                    context.recordFunctionDependency("brsStructuralEquals_AnyN_AnyN_k_")
                    val structuralCall = BrsFunctionCall(
                        BrsIdentifier("brsStructuralEquals_AnyN_AnyN_k_"),
                        mutableListOf(left, right)
                    )
                    return if (binaryOp == BrsBinaryOperator.NE) {
                        BrsUnaryOp(BrsUnaryOperator.NOT, structuralCall)
                    } else {
                        structuralCall
                    }
                }
            }
            // ==================== End Structural Equals ====================

            // ==================== Comparable Comparison for Non-Primitive Types ====================
            // BrightScript's <, >, <=, >= operators can't compare roAssociativeArray objects.
            // For non-primitive types implementing Comparable, call compareTo and compare with 0.
            //
            // When Kotlin FIR lowers `a > b` for Comparable types, it creates:
            //   IrCall(origin=GT, function=compareTo, receiver=a, arg=b)
            // The function being called is `compareTo`, and the origin indicates the comparison intent.
            // The outer FIR lowering wraps this with `> 0` comparison.
            //
            // So when we see a compareTo call with GT/LT origin:
            // - We should generate just the method call, NOT add `> 0` (the outer wrapper does that)
            // When we see a non-compareTo function with GT/LT origin on non-primitives:
            // - We should generate `a.compareTo(b) > 0`
            if (binaryOp == BrsBinaryOperator.LT || binaryOp == BrsBinaryOperator.GT ||
                binaryOp == BrsBinaryOperator.LE || binaryOp == BrsBinaryOperator.GE) {
                val leftType = leftIr.type
                val rightType = rightIr.type

                val needsCompareTo = !leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison()

                if (needsCompareTo) {
                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)

                    // Find the compareTo method in the left operand's class that matches the right operand type
                    val leftClass = leftType.classifierOrNull?.owner as? IrClass
                    val rightClassifier = rightType.classifierOrNull
                    val compareToMethod = leftClass?.declarations?.filterIsInstance<IrSimpleFunction>()
                        ?.find { method ->
                            method.name.asString() == "compareTo" &&
                            method.valueParameters.size == 1 &&
                            // Match the parameter type with the right operand type
                            method.valueParameters[0].type.classifierOrNull == rightClassifier
                        }
                        // If no exact match, fall back to the first compareTo with the same receiver type
                        ?: leftClass?.declarations?.filterIsInstance<IrSimpleFunction>()
                            ?.find { method ->
                                method.name.asString() == "compareTo" &&
                                method.valueParameters.size == 1 &&
                                method.valueParameters[0].type.classifierOrNull == leftType.classifierOrNull
                            }

                    val methodName = if (compareToMethod != null) {
                        val fullMethodName = context.getBrsName(compareToMethod)
                        val className = leftClass?.let { context.getBrsName(it) } ?: ""
                        if (className.isNotEmpty()) {
                            fullMethodName.removePrefix("${className}_")
                        } else {
                            fullMethodName
                        }
                    } else {
                        // Fallback: try generic compareTo (won't be mangled correctly)
                        "compareTo"
                    }

                    val compareToCall = BrsMethodCall(left, methodName, mutableListOf(right))

                    // Check if this call is already to compareTo - if so, the outer wrapper adds > 0
                    // If not (e.g., operator overloading on non-Comparable), we need to add > 0 ourselves
                    val isAlreadyCompareTo = function.name.asString() == "compareTo"
                    return if (isAlreadyCompareTo) {
                        // Just return the compareTo call - outer wrapper adds > 0
                        compareToCall
                    } else {
                        // Non-compareTo function on non-primitives - add comparison
                        BrsBinaryOp(compareToCall, binaryOp, BrsIntLiteral(0))
                    }
                }
            }
            // ==================== End Comparable Comparison ====================

            // ==================== Arithmetic Operators for Non-Primitive Types ====================
            // BrightScript's +, -, *, /, MOD operators can't operate on roAssociativeArray objects.
            // For non-primitive types (like UInt, ULong), fall back to method calls (plus, minus, times, div, rem).
            if (binaryOp == BrsBinaryOperator.ADD || binaryOp == BrsBinaryOperator.SUB ||
                binaryOp == BrsBinaryOperator.MUL || binaryOp == BrsBinaryOperator.DIV ||
                binaryOp == BrsBinaryOperator.MOD) {
                // Use the function's declared types instead of the expression's operand types.
                // This is important for shared variables (boxed in {value: x}) where the operand
                // may have anyNType but the function's declared receiver type preserves the original type.
                val functionReceiverType = function.dispatchReceiverParameter?.type
                    ?: function.extensionReceiverParameter?.type
                val functionArgType = function.valueParameters.firstOrNull()?.type

                val leftType = functionReceiverType ?: leftIr.type
                val rightType = functionArgType ?: rightIr.type

                // If either operand is non-primitive, let normal method call handling take over
                if (!leftType.isPrimitiveForArithmetic() || !rightType.isPrimitiveForArithmetic()) {
                    return null  // Fall through to method call handling
                }
            }
            // ==================== End Arithmetic Operators ====================

            var left = leftIr.accept(this, Unit)
            var right = rightIr.accept(this, Unit)

            // For PLUS operations involving strings, convert non-string operands to strings
            // BrightScript's + operator cannot mix Integer/Float with String
            if (binaryOp == BrsBinaryOperator.ADD) {
                val leftIsString = leftIr.type.isString()
                val rightIsString = rightIr.type.isString()

                if (leftIsString && !rightIsString) {
                    // Right operand needs string conversion
                    right = transformToString(right, rightIr.type, context)
                } else if (!leftIsString && rightIsString) {
                    // Left operand needs string conversion
                    left = transformToString(left, leftIr.type, context)
                }
            }

            return BrsBinaryOp(left, binaryOp, right)
        }

        // Identity operators (=== and !==)
        // These need special handling because BrightScript's = operator doesn't work for associative arrays
        // Check both origin (for user code) and symbol (for synthesized calls from coroutine lowering)
        val isIdentityOp = origin == IrStatementOrigin.EQEQEQ ||
                           origin == IrStatementOrigin.EXCLEQEQ ||
                           expression.symbol == context.irBuiltIns.eqeqeqSymbol
        if (isIdentityOp) {
            // Determine if this is === or !== based on origin (symbol is always eqeqeqSymbol)
            val isNegated = origin == IrStatementOrigin.EXCLEQEQ
            val (left, right) = when {
                expression.dispatchReceiver != null -> {
                    val l = expression.dispatchReceiver!!.accept(this, Unit)
                    val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
                expression.extensionReceiver != null -> {
                    val l = expression.extensionReceiver!!.accept(this, Unit)
                    val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
                else -> {
                    val l = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    val r = expression.getValueArgument(1)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
            }
            context.recordFunctionDependency("__kotlin_identityEquals")
            val identityCall = BrsFunctionCall(
                BrsIdentifier("__kotlin_identityEquals"),
                mutableListOf(left, right)
            )
            return if (isNegated) {
                BrsUnaryOp(BrsUnaryOperator.NOT, identityCall)
            } else {
                identityCall
            }
        }

        // Unary operators
        return when (origin) {
            IrStatementOrigin.UMINUS -> {
                val operand = expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsUnaryOp(BrsUnaryOperator.NEG, operand)
            }
            IrStatementOrigin.UPLUS -> {
                // Unary plus is a no-op in most cases
                expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
            }
            IrStatementOrigin.EXCL -> {
                val operand = expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
            IrStatementOrigin.GET_ARRAY_ELEMENT -> {
                // Array access: arr[index]
                // Only use BrsIndexAccess for native Kotlin arrays.
                // For other types (like ArrayList), fall through to method call handling.
                val receiver = expression.dispatchReceiver ?: return null
                if (!receiver.type.isArray()) {
                    // Not a native array - return null to let the normal method call handling take over
                    return null
                }
                val array = receiver.accept(this, Unit)
                val index = expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsIndexAccess(array, index)
            }
            else -> null
        }
    }

    /**
     * Transform builtin comparison functions from irBuiltIns to binary operators.
     * These functions (less, lessOrEqual, greater, greaterOrEqual) may not have an
     * IrStatementOrigin when introduced by lowering passes, so we check the function
     * name directly.
     */
    private fun transformBuiltinComparison(expression: IrCall): BrsExpression? {
        val function = expression.symbol.owner
        val functionName = function.name.asString()

        // Check if this is a builtin comparison function
        val binaryOp = when (functionName) {
            "less" -> BrsBinaryOperator.LT
            "lessOrEqual" -> BrsBinaryOperator.LE
            "greater" -> BrsBinaryOperator.GT
            "greaterOrEqual" -> BrsBinaryOperator.GE
            else -> return null
        }

        // Verify this is from kotlin.internal (builtins) not a user-defined function
        val packageFqName = function.getPackageFragment().packageFqName.asString()
        if (packageFqName.startsWith("kotlin.internal") || packageFqName.startsWith("kotlin")) {
            // This is a builtin comparison function
        } else {
            return null
        }

        // Extract left and right operands
        val (left, right) = when {
            expression.valueArgumentsCount >= 2 -> {
                val l = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                val r = expression.getValueArgument(1)?.accept(this, Unit) ?: return null
                Pair(l, r)
            }
            expression.dispatchReceiver != null -> {
                val l = expression.dispatchReceiver!!.accept(this, Unit)
                val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                Pair(l, r)
            }
            expression.extensionReceiver != null -> {
                val l = expression.extensionReceiver!!.accept(this, Unit)
                val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                Pair(l, r)
            }
            else -> return null
        }

        return BrsBinaryOp(left, binaryOp, right)
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass

        val arguments = mutableListOf<BrsExpression>()

        // For inner classes, pass the outer instance as the first argument
        if (irClass.isInner) {
            expression.dispatchReceiver?.let { outer ->
                arguments.add(outer.accept(this, data))
            } ?: run {
                // If no explicit dispatch receiver, use 'm' (the current instance)
                arguments.add(BrsMRef())
            }
        }

        // Add regular constructor arguments
        // For local class constructors, check if arguments are for captured variables
        // If a captured variable is a shared (boxed) variable, pass the box, not .value
        arguments.addAll((0 until expression.valueArgumentsCount).mapNotNull { i ->
            val arg = expression.getValueArgument(i) ?: return@mapNotNull null

            // Check if this argument is for a bound (captured) value parameter
            val param = constructor.valueParameters.getOrNull(i)
            val isCapturedValueParam = param?.origin == BOUND_VALUE_PARAMETER ||
                                        param?.origin == BOUND_RECEIVER_PARAMETER

            // If it's a captured parameter and the argument is a GetValue for a shared variable,
            // pass the box directly instead of dereferencing with .value
            // Two detection mechanisms:
            // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
            // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
            val isSharedVar = arg is IrGetValue && (
                (arg.symbol.owner is IrVariable && (arg.symbol.owner as IrVariable).origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                arg.symbol in genCtx.sharedVariables
            )
            if (isCapturedValueParam && isSharedVar) {
                // Pass the box itself, not the dereferenced value
                val varName = sanitizeParameterName((arg as IrGetValue).symbol.owner.name.asString())
                BrsIdentifier(varName)
            } else if (arg is IrGetField) {
                // Check if this field holds a shared variable box (e.g., in coroutine create method)
                // When passing captured fields to a coroutine constructor, we want the box, not .value
                val field = arg.symbol.owner
                val fieldParentClass = field.parent as? IrClass
                val rawFieldName = field.name.asString()
                val fieldName = rawFieldName.replace("$", "_")
                val className = fieldParentClass?.name?.asString() ?: ""
                val fieldKey = "$className.$fieldName"
                val isSharedVariableField = fieldKey in context.sharedVariableFields

                if (isSharedVariableField) {
                    // Pass the box itself, not the dereferenced .value
                    // Generate m.fieldName (without .value)
                    val receiver = arg.receiver?.accept(this, data) ?: BrsMRef()
                    BrsDotAccess(receiver, fieldName)
                } else {
                    arg.accept(this, data)
                }
            } else {
                arg.accept(this, data)
            }
        })

        // Check if this is an external class (Roku SDK type)
        if (irClass.isExternal || isExternalClass(irClass)) {
            // External classes use CreateObject()
            val brsTypeName = getBrsExternalTypeName(irClass)
            return BrsCreateObject(brsTypeName, arguments.toMutableList())
        }

        // Record dependency for this constructor call via function manifest
        val constructorName = context.getBrsName(constructor)
        context.recordFunctionDependency(constructorName)

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
            arguments
        )
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Record dependency for delegating constructor call (e.g., calling super constructor)
        val constructorName = context.getBrsName(constructor)
        if (!isExternalClass(irClass)) {
            context.recordFunctionDependency(constructorName)
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
            arguments.toMutableList()
        )
    }

    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Unit): BrsExpression {
        // Instance initializers are handled during constructor generation
        // Return invalid as a no-op placeholder
        return BrsInvalidLiteral()
    }

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Record dependency for enum constructor call
        val constructorName = context.getBrsName(constructor)
        if (!isExternalClass(irClass)) {
            context.recordFunctionDependency(constructorName)
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
            arguments.toMutableList()
        )
    }

    /**
     * Checks if a class is marked with @BrsExternal annotation.
     */
    private fun isExternalClass(irClass: IrClass): Boolean {
        return irClass.annotations.any { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() == "BrsExternal"
        }
    }

    /**
     * True when [receiver] is `this` of the component class currently being
     * transformed — i.e. the access targets the current component's own node.
     *
     * Only an IrGetValue of a dispatch/class `<this>` parameter typed as the
     * current component class (or one of its supertypes, for accessors declared
     * in a base) qualifies. Everything else — locals, ordinary parameters, call
     * results, extension-lambda receivers — is a handle to some OTHER instance
     * and must compile to direct node field access, not m/m.top.
     */
    private fun isCurrentComponentSelfReceiver(receiver: IrExpression): Boolean {
        var unwrapped: IrExpression = receiver
        while (unwrapped is IrTypeOperatorCall &&
            (unwrapped.operator == IrTypeOperator.IMPLICIT_CAST ||
                unwrapped.operator == IrTypeOperator.CAST ||
                unwrapped.operator == IrTypeOperator.IMPLICIT_NOTNULL)
        ) {
            unwrapped = unwrapped.argument
        }
        if (unwrapped !is IrGetValue) return false
        // Extension-lambda receivers (e.g. a T.() -> Unit block) are values, not the component's this
        if (unwrapped.symbol == genCtx.currentLambdaExtensionReceiver) return false
        val parameter = unwrapped.symbol.owner as? IrValueParameter ?: return false
        if (parameter.name.asString() != "<this>") return false
        val componentClass = genCtx.currentComponentClass ?: return false
        val parameterClass = parameter.type.classOrNull?.owner ?: return false
        return parameterClass == componentClass || componentClass.isSubclassOf(parameterClass)
    }

    /**
     * Checks if a property is a SceneGraph interface field.
     *
     * Interface fields are accessed via m.top.fieldName instead of m.fieldName.
     * This includes properties annotated with any @SG*Field annotation or @BrsField.
     */
    internal fun hasInterfaceFieldAnnotation(property: IrProperty): Boolean {
        val sgFieldAnnotations = setOf(
            "SGStringField", "SGIntegerField", "SGLongIntegerField", "SGFloatField",
            "SGDoubleField", "SGBooleanField", "SGArrayField", "SGAssocArrayField",
            "SGNodeField", "SGFunctionField", "SGUriField", "SGTimeField",
            "SGVector2DField", "SGColorField", "BrsField"
        )
        return property.annotations.any { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() in sgFieldAnnotations
        }
    }

    /**
     * Gets the BrightScript object type name for CreateObject().
     * E.g., RoArray -> "roArray", RoAssociativeArray -> "roAssociativeArray"
     */
    private fun getBrsExternalTypeName(irClass: IrClass): String {
        // Check for @BrsName annotation first
        val brsNameAnnotation = irClass.annotations.find { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() == "BrsName"
        }

        if (brsNameAnnotation != null) {
            val nameArg = brsNameAnnotation.getValueArgument(0)
            if (nameArg is IrConst) {
                return nameArg.value as String
            }
        }

        // Default: convert class name to BrightScript type name
        // RoArray -> roArray, RoDeviceInfo -> roDeviceInfo
        val className = irClass.name.asString()
        return if (className.startsWith("Ro")) {
            "ro${className.substring(2)}"
        } else {
            className.lowercase()
        }
    }

    // ==================== Operators ====================

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Unit): BrsExpression {
        // Handle IMPLICIT_COERCION_TO_UNIT specially - the inner expression's value is discarded
        // For IrWhen inside IMPLICIT_COERCION_TO_UNIT, use the statement transformer to generate
        // proper if-then-else blocks instead of trying to treat it as an expression
        if (expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
            val innerArg = expression.argument
            if (innerArg is IrWhen) {
                // Use statement transformer to generate BrsIf
                val brsIf = parent.statementVisitor.visitWhen(innerArg, data)
                return BrsStatementAsExpression(brsIf)
            }
            // For other inner expressions wrapped in IMPLICIT_COERCION_TO_UNIT,
            // just process the inner expression - its value is discarded anyway
            return innerArg.accept(this, data)
        }

        val argument = expression.argument.accept(this, data)

        return when (expression.operator) {
            IrTypeOperator.CAST, IrTypeOperator.IMPLICIT_CAST -> argument
            IrTypeOperator.SAFE_CAST -> {
                // Safe cast returns invalid if type doesn't match
                // For trivial expressions (identifiers, literals), it's safe to evaluate twice.
                // For non-trivial expressions (function calls, etc.), hoist to a temp var
                // to avoid double evaluation of side effects.
                if (isTrivialExpression(argument)) {
                    BrsConditional(
                        generateInstanceCheck(argument.deepCopy(), expression.typeOperand),
                        argument,
                        BrsInvalidLiteral()
                    )
                } else {
                    // Hoist non-trivial expression to temp variable to avoid double evaluation
                    val tempName = "__safeCast_tmp${genCtx.nextTempId()}"
                    genCtx.addHoistedStatement(BrsVariable(tempName, null, argument))
                    val tempRef = BrsIdentifier(tempName)
                    BrsConditional(
                        generateInstanceCheck(tempRef.deepCopy(), expression.typeOperand),
                        tempRef,
                        BrsInvalidLiteral()
                    )
                }
            }
            IrTypeOperator.INSTANCEOF -> {
                generateInstanceCheck(argument, expression.typeOperand)
            }
            IrTypeOperator.NOT_INSTANCEOF -> {
                BrsUnaryOp(
                    BrsUnaryOperator.NOT,
                    generateInstanceCheck(argument, expression.typeOperand)
                )
            }
            IrTypeOperator.SAM_CONVERSION -> {
                // For SAM conversions, we need to add the interface method name in addition to invoke
                // This allows both lambda-style calls (.invoke()) and interface-style calls (.methodName())
                if (argument is BrsAALiteral) {
                    val targetClass = expression.typeOperand.classOrNull?.owner
                    if (targetClass != null && targetClass.isFun) {
                        // Find the single abstract method
                        val samMethod = targetClass.declarations
                            .filterIsInstance<org.jetbrains.kotlin.ir.declarations.IrSimpleFunction>()
                            .firstOrNull { it.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT }
                        if (samMethod != null) {
                            // Get the mangled method name and strip the class prefix
                            // to match how call sites generate method names
                            val fullMethodName = context.getBrsName(samMethod)
                            val className = context.getBrsName(targetClass)
                            val methodName = fullMethodName.removePrefix("${className}_")
                            // Find the invoke entry and duplicate it with the SAM method name
                            val invokeEntry = argument.entries.find { it.key == "invoke" }
                            if (invokeEntry != null) {
                                argument.entries.add(BrsAAEntry(methodName, invokeEntry.value.deepCopy()))
                            }
                        }
                    }
                }
                argument
            }
            else -> argument
        }
    }

    /**
     * Check if a BrsExpression is trivial (safe to evaluate multiple times without side effects).
     * Trivial expressions include identifiers, 'm' reference, and all literal types.
     */
    private fun isTrivialExpression(expr: BrsExpression): Boolean = when (expr) {
        is BrsIdentifier, is BrsMRef,
        is BrsIntLiteral, is BrsLongIntLiteral, is BrsFloatLiteral, is BrsDoubleLiteral,
        is BrsStringLiteral, is BrsBooleanLiteral, is BrsInvalidLiteral -> true
        else -> false
    }

    /**
     * Generate a type check expression for the given argument and target type.
     *
     * For primitive types, uses the __kotlin_isPrimitiveType helper which handles
     * both boxed and unboxed BrightScript type names (e.g., "String" vs "roString").
     * For class types, checks the __proto chain for the class name.
     */
    private fun generateInstanceCheck(argument: BrsExpression, targetType: IrType): BrsExpression {
        // For primitive types, use __kotlin_isPrimitiveType helper which handles
        // both boxed and unboxed type names (e.g., "String" vs "roString")
        when {
            targetType.isInt() || targetType.isShort() || targetType.isByte() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Integer"))
                )
            }
            targetType.isLong() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("LongInteger"))
                )
            }
            targetType.isFloat() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Float"))
                )
            }
            targetType.isDouble() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Double"))
                )
            }
            targetType.isBoolean() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Boolean"))
                )
            }
            targetType.isString() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("String"))
                )
            }
            targetType.isArray() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roArray"))
        }

        // For class types, check type or __proto chain
        val classType = targetType.classOrNull?.owner

        if (classType != null && (classType.isExternal || isExternalClass(classType))) {
            // For external classes (native BrightScript types like RoArray, RoAssociativeArray),
            // use Type() comparison directly since they don't have __proto chains
            val brsTypeName = getBrsExternalTypeName(classType)
            return BrsBinaryOp(
                BrsTypeOf(argument),
                BrsBinaryOperator.EQ,
                BrsStringLiteral(brsTypeName)
            )
        }

        // For Kotlin classes, check the __proto chain
        val className = if (classType != null) {
            context.getBrsName(classType)
        } else {
            targetType.classFqName?.shortName()?.asString() ?: "Object"
        }

        // Generate: __kotlin_isInstanceOf(argument, "ClassName")
        // This requires the __kotlin_isInstanceOf helper function to be generated
        context.recordFunctionDependency("__kotlin_isInstanceOf")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_isInstanceOf"),
            mutableListOf(argument, BrsStringLiteral(className))
        )
    }

    /**
     * Maps Kotlin IR types to canonical BrightScript type names.
     * These names are used with __kotlin_isPrimitiveType for type checking.
     */
    private fun mapTypeToString(type: IrType): String {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> "Integer"
            type.isLong() -> "LongInteger"
            type.isFloat() -> "Float"
            type.isDouble() -> "Double"
            type.isBoolean() -> "Boolean"
            type.isString() -> "String"
            type.isArray() -> "roArray"
            else -> "roAssociativeArray"
        }
    }

    // ==================== Binary Operations ====================

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Unit): BrsExpression {
        val parts = expression.arguments.mapIndexed { index, arg ->
            val expr = arg.accept(this, data)
            // Convert non-string arguments to strings for BrightScript string concatenation
            if (!arg.type.isString()) {
                transformToString(expr, arg.type, context)
            } else {
                expr
            }
        }
        return parts.reduce { acc, expr ->
            BrsBinaryOp(acc, BrsBinaryOperator.CONCAT, expr)
        }
    }

    // ==================== Collections ====================

    override fun visitVararg(expression: IrVararg, data: Unit): BrsExpression {
        // If vararg has a single spread element, don't wrap it in another array
        // e.g., hashMapOf(*pairs) should just pass pairs, not [pairs]
        if (expression.elements.size == 1 && expression.elements[0] is IrSpreadElement) {
            val spreadElement = expression.elements[0] as IrSpreadElement
            return spreadElement.expression.accept(this, data)
        }

        val elements = expression.elements.map { element ->
            when (element) {
                is IrExpression -> element.accept(this, data)
                is IrSpreadElement -> element.expression.accept(this, data)
                else -> BrsInvalidLiteral()
            }
        }
        return BrsArrayLiteral(elements.toMutableList())
    }

    // ==================== Control Flow ====================

    override fun visitWhen(expression: IrWhen, data: Unit): BrsExpression {
        // Check for ANDAND and OROR origins (logical operators)
        when (expression.origin) {
            IrStatementOrigin.ANDAND -> {
                // a && b is represented as: if (a) b else false
                val left = expression.branches[0].condition.accept(this, data)
                val right = expression.branches[0].result.accept(this, data)
                return BrsBinaryOp(left, BrsBinaryOperator.AND, right)
            }
            IrStatementOrigin.OROR -> {
                // a || b is represented as: if (a) true else b
                val left = expression.branches[0].condition.accept(this, data)
                val right = expression.branches[1].result.accept(this, data)
                return BrsBinaryOp(left, BrsBinaryOperator.OR, right)
            }
            else -> { /* fall through to default handling */ }
        }

        // After BrsWhenExpressionLowering, when expressions in expression context
        // should have been transformed to blocks with temp variables.
        // If we get here with a non-Unit type, fall back to statement transformation
        // wrapped in an immediately-invoked function (IIFE pattern).

        // For Unit-returning when (statement-like), return invalid
        // This shouldn't normally happen in expression context, but handle gracefully
        if (expression.type.isUnit()) {
            return BrsInvalidLiteral() // Unit expressions don't have a value
        }

        // For non-Unit when expressions that weren't lowered, we need to handle them.
        // This happens when a when expression in statement context (value discarded) has
        // a non-Unit result type (e.g., StringBuilder.append() which returns StringBuilder).
        // Instead of generating an IIFE pattern (which BrightScript doesn't support),
        // delegate to the statement transformer to generate proper if-then-else,
        // and wrap it in BrsStatementAsExpression so it renders as a statement.

        // Use the statement transformer to generate a proper BrsIf
        val brsIf = parent.statementVisitor.visitWhen(expression, data)

        // Wrap the if statement in BrsStatementAsExpression so it renders as a statement
        // rather than attempting to use it as a value
        return BrsStatementAsExpression(brsIf)
    }

    private fun IrBranch.isElse(): Boolean {
        return condition is IrConst && (condition as IrConst).value == true
    }

    // ==================== Function Expressions ====================

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Unit): BrsExpression {
        val function = expression.function

        // Detect captured variables from outer scope
        val capturedVars = genCtx.detectCapturedVariables(function)

        // Build parameter list, starting with extension receiver if present
        val allParameters = mutableListOf<BrsParameter>()

        // Add extension receiver as first parameter if present (matches regular function handling)
        // Use "__receiver" instead of "m" to avoid collision with closure's m reference
        // This allows closure body to use m._componentM for component state access
        function.extensionReceiverParameter?.let { receiver ->
            allParameters.add(BrsParameter(
                name = "__receiver",
                type = mapTypeToBrs(receiver.type)
            ))
        }

        // Add value parameters
        val rawParameters = function.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type)
            )
        }
        allParameters.addAll(rawParameters)

        val parameters = normalizeParametersForBrs(deduplicateParameterNames(allParameters))

        val returnType = mapTypeToBrs(function.returnType)

        // Generate closure object for ALL function expressions
        // BrightScript anonymous functions cannot access outer scope variables,
        // so we create an object with captured variable fields and an invoke method.
        // The invoke method accesses captured variables via m.fieldName
        // For consistency, even lambdas without captures use this pattern so that
        // all lambdas can be invoked uniformly with .invoke()

        // Save previous closure context and lambda extension receiver
        val previousContext = genCtx.currentClosureContext
        val previousLambdaReceiver = genCtx.currentLambdaExtensionReceiver
        val previousInComponentLambda = genCtx.isInComponentLambda

        // Check if we're in component context - if so, we need to capture the component's m
        val needsComponentCapture = genCtx.isInComponentContext && !genCtx.isInComponentLambda

        // Set closure context for body transformation (if there are captures)
        if (capturedVars.isNotEmpty()) {
            genCtx.currentClosureContext = capturedVars
        }

        // Set lambda extension receiver for body transformation
        // This allows visitGetValue to rewrite receiver references to 'm'
        function.extensionReceiverParameter?.let {
            genCtx.currentLambdaExtensionReceiver = it.symbol
        }

        // If we're in component context, mark that we're now inside a component lambda
        // so that component state accesses use m._componentM instead of bare m
        if (needsComponentCapture) {
            genCtx.isInComponentLambda = true
        }

        // Transform body with closure context active (variable accesses will be rewritten)
        val body = function.body?.let { parent.transformBody(it) } ?: BrsBlock()

        // Restore previous context and lambda receiver
        genCtx.currentClosureContext = previousContext
        genCtx.currentLambdaExtensionReceiver = previousLambdaReceiver
        genCtx.isInComponentLambda = previousInComponentLambda

        // Build closure object fields for captured variables
        val entries = mutableListOf<BrsAAEntry>()

        // If we're in component context, capture the component's m reference
        if (needsComponentCapture) {
            entries.add(BrsAAEntry("_componentM", BrsMRef()))
        }

        for (capturedVar in capturedVars) {
            // When capturing 'this' from an outer method, use 'm' (BrightScript's object reference)
            // instead of 'this' (which doesn't exist in BrightScript methods)
            // Note: sanitizeParameterName converts <this> to __this
            val varValue = if (capturedVar.name == "__this") {
                BrsIdentifier("m")
            } else {
                BrsIdentifier(capturedVar.name)
            }

            // Check if this mutable capture is already boxed (part of sharedVariables)
            // If so, pass the box directly instead of re-boxing
            // Two detection mechanisms:
            // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
            // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
            val varOwner = capturedVar.symbol.owner
            val hasSharedVarOrigin = varOwner is IrVariable && varOwner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER
            val isAlreadyBoxed = capturedVar.isMutable && (hasSharedVarOrigin || capturedVar.symbol in genCtx.sharedVariables)
            val fieldValue = if (capturedVar.isMutable && !isAlreadyBoxed) {
                // Wrap mutable captures in { value: x } for mutation to propagate
                // (This case shouldn't happen anymore since all mutable captures should be in sharedVariables)
                BrsAALiteral(mutableListOf(BrsAAEntry("value", varValue)))
            } else {
                // Read-only captures or already-boxed mutable captures: store directly
                varValue
            }
            entries.add(BrsAAEntry(capturedVar.name, fieldValue))
        }

        // Add the invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
        entries.add(BrsAAEntry("invoke", invokeFunction))

        return BrsAALiteral(entries)
    }

    // ==================== Function References ====================

    /**
     * Transform a function reference (::functionName) to a BrightScript closure object.
     *
     * Kotlin: ::myFunction
     * BrightScript: { invoke: function(a, b) return myFunction(a, b) end function }
     *
     * All function references use closure objects for consistent .invoke() calling convention.
     */
    override fun visitFunctionReference(expression: IrFunctionReference, data: Unit): BrsExpression {
        val function = expression.symbol.owner
        val functionName = context.getBrsName(function)

        // Create wrapper function parameters matching the referenced function
        val rawParameters = function.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type)
            )
        }
        val parameters = normalizeParametersForBrs(deduplicateParameterNames(rawParameters))

        // Build the call arguments from the wrapper parameters
        val callArgs: MutableList<BrsExpression> = parameters.map { BrsIdentifier(it.name) as BrsExpression }.toMutableList()

        // Handle bound receiver if present (obj::method)
        val boundReceiver = expression.dispatchReceiver ?: expression.extensionReceiver
        val call = if (boundReceiver != null) {
            // For bound method references, call as method on the receiver
            val receiverExpr = boundReceiver.accept(this, data)
            val methodName = sanitizeMethodName(function.name.asString())
            BrsMethodCall(receiverExpr, methodName, callArgs)
        } else {
            // For unbound function references, call the global function.
            // The wrapper's body calls the referenced function, so the reference is a
            // dependency of this file even though the call happens later.
            // Skip external functions (native, no .brs file) and local functions
            // (emitted inline in the current file, never in a manifest).
            if (function.isExternal || function.parent is IrFunction) {
                BrsFunctionCall(BrsIdentifier(functionName), callArgs)
            } else {
                createFunctionCall(functionName, callArgs, context)
            }
        }

        // Determine return type
        val returnType = if (function.returnType.isUnit() || function.returnType.isNothing()) {
            BrsType.VOID
        } else {
            mapTypeToBrs(function.returnType)
        }

        // Build the wrapper body
        val body = if (returnType == BrsType.VOID) {
            // For void functions, just call without return
            BrsBlock(mutableListOf(BrsExpressionStatement(call)))
        } else {
            // For non-void functions, return the result
            BrsBlock(mutableListOf(BrsReturn(call)))
        }

        // Build closure object with invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
        val entries = mutableListOf(BrsAAEntry("invoke", invokeFunction))

        return BrsAALiteral(entries)
    }

    /**
     * Transform a property reference (::propertyName) to a BrightScript anonymous function wrapper.
     *
     * Kotlin: ::propertyName or obj::propertyName
     * BrightScript: function() return propertyName end function
     *             or function() return obj.propertyName end function
     */
    override fun visitPropertyReference(expression: IrPropertyReference, data: Unit): BrsExpression {
        val property = expression.symbol.owner
        val propertyName = property.name.asString()

        // Handle bound receiver if present (obj::property)
        val boundReceiver = expression.dispatchReceiver ?: expression.extensionReceiver

        // Build the property access expression
        val propertyAccess = if (boundReceiver != null) {
            val receiverExpr = boundReceiver.accept(this, data)
            BrsDotAccess(receiverExpr, propertyName)
        } else {
            // For top-level properties, access directly
            BrsIdentifier(propertyName)
        }

        // Get the property type for the return type
        val returnType = property.getter?.returnType?.let { mapTypeToBrs(it) }
            ?: mapTypeToBrs(property.backingField?.type ?: context.irBuiltIns.anyType)

        // Create a getter wrapper function
        val body = BrsBlock(mutableListOf(BrsReturn(propertyAccess)))

        return BrsAnonymousFunction(mutableListOf(), returnType, body)
    }

    // ==================== Composite ====================

    override fun visitComposite(expression: IrComposite, data: Unit): BrsExpression {
        // Composite expressions are sequences where all statements must execute
        // and the last expression is the result value.
        //
        // After BrsReturnableBlockLowering, composites may contain:
        // - A result variable declaration
        // - A returnable block (wrapped in while true { ... exit while })
        // - A get of the result variable (the return value)
        //
        // For nested returnable blocks (e.g., suspendCoroutineUninterceptedOrReturn
        // inside another inline function), each composite hoists only its OWN direct
        // variable declarations. Nested composites handle their own variables when
        // they are processed.
        //
        // This ensures correct ordering: when an assignment like `tmp_ret_2 = <composite>`
        // is processed, the inner composite's hoisted statements (including its variable
        // declaration and while loop) are placed BEFORE the assignment statement.
        val statements = expression.statements
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] visitComposite: ${statements.size} statements\n")
        statements.forEachIndexed { idx, s ->
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   [$idx] ${s::class.simpleName}\n")
        }
        if (statements.isEmpty()) {
            return BrsInvalidLiteral()
        }

        // PASS 1: Hoist only DIRECT variable declarations from this composite
        // Nested composites will hoist their own variables when processed
        val hoistedVars = mutableSetOf<IrVariable>()
        hoistDirectVariables(statements, hoistedVars)
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] PASS1: hoisted ${hoistedVars.size} vars\n")

        // PASS 2: Process other statements (skip variables, already done)
        for (i in 0 until statements.size - 1) {
            val stmt = statements[i]
            if (stmt is IrVariable) continue  // Already hoisted in pass 1

            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] PASS2: processing stmt[$i] ${stmt::class.simpleName}\n")
            genCtx.pushHoistedScope()
            val transformed = when (stmt) {
                is IrWhen -> parent.statementVisitor.visitWhen(stmt, Unit)
                is IrWhileLoop -> parent.statementVisitor.visitWhileLoop(stmt, Unit)
                is IrDoWhileLoop -> parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                is IrReturnableBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                is IrBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                else -> parent.transformStatement(stmt)
            }
            val nestedHoisted = genCtx.popHoistedScope()
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   nestedHoisted: ${nestedHoisted.size}, adding each to parent\n")
            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
            transformed?.let {
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   adding transformed: ${it::class.simpleName}\n")
                genCtx.addHoistedStatement(it)
            }
        }

        // Return the last statement's value
        val last = statements.last()
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] returning last: ${last::class.simpleName}\n")
        return if (last is IrExpression) {
            last.accept(this, data)
        } else {
            BrsInvalidLiteral()
        }
    }

    /**
     * Hoists only DIRECT variable declarations from the composite's statements.
     *
     * IMPORTANT: We do NOT recurse into nested composites/blocks. Each composite
     * handles its own variable hoisting when it is processed. Deep traversal
     * caused variables from inner composites to be hoisted too early, resulting
     * in wrong statement ordering (e.g., `tmp_ret_2 = tmp_ret_1` appearing before
     * the inner while loop that assigns to `tmp_ret_1`).
     *
     * The correct flow:
     * 1. Outer composite hoists only its direct variable (tmp_ret_2)
     * 2. Outer composite processes its returnable block
     * 3. Inside the block, when IrSetValue(tmp_ret_2, <inner_composite>) is processed,
     *    the inner composite hoists its own variables (tmp_ret_1) at that point
     * 4. The inner composite's hoisted statements go BEFORE the assignment
     */
    private fun hoistDirectVariables(statements: List<IrStatement>, hoistedVars: MutableSet<IrVariable>) {
        for (stmt in statements) {
            if (stmt is IrVariable && stmt !in hoistedVars) {
                hoistedVars.add(stmt)
                val varName = stmt.name.asString()
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-HOIST] hoistDirectVariables: hoisting '$varName'\n")
                val transformed = parent.statementVisitor.visitVariable(stmt, Unit)
                genCtx.addHoistedStatement(transformed)
            }
        }
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsExpression {
        // Handle IrReturnableBlock (from inlined functions).
        // If BrsReturnableBlockLowering wrapped this block (because returns target it), we would
        // see an IrComposite here instead, not the IrReturnableBlock directly.
        // So if we see IrReturnableBlock in expression context, it means no returns targeted it,
        // and we should execute it for side effects only and return Unit.
        if (expression is IrReturnableBlock) {
            // Transform the block as a statement (wraps in while true { ... exit while })
            val stmt = parent.statementVisitor.visitBlock(expression, Unit)
            // Hoist the statement for side effects
            genCtx.addHoistedStatement(stmt)
            // Return Unit (the block has no meaningful return value since no returns target it)
            return BrsInvalidLiteral()
        }

        // Handle IO_WORKER_CALL blocks from BrsIOWorkerExtractionLowering.
        // Structure: [IrConst(workerName), IrComposite(captures: [value, name, value, name, ...])]
        // Transform to: runIOWorker_T_k_("workerName", { "name": value, ... })
        if (expression.origin == BrsStatementOrigins.IO_WORKER_CALL) {
            return transformIOWorkerCall(expression)
        }

        // Handle increment/decrement blocks - when used as expression (value needed),
        // we need to hoist the temp variable and setter, then return the appropriate value
        // Block structure: [IrVariable(<unary>) = getter(), setter(<unary>+1) or IrSetValue, IrGetValue(<unary>)]
        if (expression.origin == IrStatementOrigin.POSTFIX_INCR ||
            expression.origin == IrStatementOrigin.POSTFIX_DECR ||
            expression.origin == IrStatementOrigin.PREFIX_INCR ||
            expression.origin == IrStatementOrigin.PREFIX_DECR) {

            val statements = expression.statements
            val isPostfix = expression.origin == IrStatementOrigin.POSTFIX_INCR ||
                           expression.origin == IrStatementOrigin.POSTFIX_DECR

            // Find the temp variable and its initializer
            val tempVar = statements.filterIsInstance<IrVariable>().firstOrNull {
                it.name.asString().startsWith("<") && it.name.asString().endsWith(">")
            }
            val tempVarInitializer = tempVar?.initializer

            if (tempVar != null && tempVarInitializer != null) {
                // Generate a unique name for the hoisted temp variable
                val tempVarName = "__incr_tmp_${genCtx.nextTempId()}"

                // 1. Hoist the temp variable declaration with the OLD value
                val hoistedTempVar = BrsVariable(
                    name = tempVarName,
                    type = mapTypeToBrs(tempVar.type),
                    initializer = parent.transformExpression(tempVarInitializer)
                )
                genCtx.addHoistedStatement(hoistedTempVar)

                // 2. Find and hoist the setter/assignment
                for (stmt in statements) {
                    if (stmt !== statements.last()) {
                        when (stmt) {
                            is IrCall -> {
                                // Property setter - use temp var in the call
                                genCtx.pushTempVarSubstitution(tempVar.symbol, null) // Signal to use tempVarName
                                genCtx.setTempVarName(tempVar.symbol, tempVarName)
                                try {
                                    val setterCall = stmt.accept(this, data)
                                    genCtx.addHoistedStatement(BrsExpressionStatement(setterCall))
                                } finally {
                                    genCtx.popTempVarSubstitution(tempVar.symbol)
                                }
                            }
                            is IrSetValue, is IrSetField -> {
                                // Local variable assignment - use temp var
                                genCtx.pushTempVarSubstitution(tempVar.symbol, null)
                                genCtx.setTempVarName(tempVar.symbol, tempVarName)
                                try {
                                    val assignment = parent.transformStatement(stmt)
                                    if (assignment != null) {
                                        genCtx.addHoistedStatement(assignment)
                                    }
                                } finally {
                                    genCtx.popTempVarSubstitution(tempVar.symbol)
                                }
                            }
                            is IrVariable -> {
                                // Skip the temp variable declaration itself
                            }
                        }
                    }
                }

                // 3. Return the appropriate value
                // For postfix: return the OLD value (temp var)
                // For prefix: return the NEW value (temp var +/- 1)
                return if (isPostfix) {
                    BrsIdentifier(tempVarName)
                } else {
                    // For prefix, the result is the new value, which is what the variable
                    // was set to. Since we hoisted the assignment, we can read the variable.
                    // But actually, for prefix, the last statement is also an IrGetValue
                    // which would give us the OLD value. We need to compute NEW value.
                    val isIncrement = expression.origin == IrStatementOrigin.PREFIX_INCR
                    BrsBinaryOp(
                        BrsIdentifier(tempVarName),
                        if (isIncrement) BrsBinaryOperator.ADD else BrsBinaryOperator.SUB,
                        BrsIntLiteral(1)
                    )
                }
            }

            // Fallback: transform and return the last statement (should be the value)
            val lastStmt = statements.lastOrNull()
            if (lastStmt != null && lastStmt is IrExpression) {
                return lastStmt.accept(this, data)
            }
        }

        // Handle for-loop blocks in expression context
        // For-loop blocks need to be transformed as statements and wrapped
        if (expression.origin == IrStatementOrigin.FOR_LOOP) {
            val forLoopStmt = parent.statementVisitor.visitBlock(expression, Unit)
            return BrsStatementAsExpression(forLoopStmt)
        }

        // Handle ELVIS and SAFE_CALL blocks: { val tmp = expr; when { ... } }
        // These are generated by FIR2IR for elvis (a ?: b) and safe call (a?.b) expressions.
        // The variable declaration must be hoisted so it's available in the when condition.
        // Note: After BrsWhenExpressionLowering, the when expression may be wrapped in a
        // when-lowered block { var __when_tmp; when { ... }; __when_tmp }.
        if (expression.origin == IrStatementOrigin.ELVIS || expression.origin == IrStatementOrigin.SAFE_CALL) {
            val statements = expression.statements
            if (statements.size == 2) {
                val firstStmt = statements[0]
                val secondStmt = statements[1]

                // Check if second statement is either:
                // 1. An IrWhen (not yet lowered), or
                // 2. An IrBlock that is a when-lowered block (after BrsWhenExpressionLowering)
                val isWhenOrWhenLoweredBlock = secondStmt is IrWhen ||
                    (secondStmt is IrBlock && secondStmt.statements.firstOrNull()?.let {
                        it is IrVariable && it.name.asString().startsWith("__when_tmp")
                    } == true)

                if (firstStmt is IrVariable && isWhenOrWhenLoweredBlock) {
                    // Transform and hoist the variable declaration
                    val varInit = firstStmt.initializer?.let { parent.transformExpression(it) }

                    // Take any hoisted statements from initializer transformation
                    val initHoisted = genCtx.takeHoistedStatements()
                    initHoisted.forEach { genCtx.addHoistedStatement(it) }

                    // Add the temporary variable declaration
                    genCtx.addHoistedStatement(
                        BrsVariable(
                            firstStmt.name.asString(),
                            mapTypeToBrs(firstStmt.type),
                            varInit
                        )
                    )

                    // Transform the when expression (or when-lowered block) - it will use the now-hoisted variable
                    return (secondStmt as IrExpression).accept(this, data)
                }
            }
        }

        // Check for when-lowered blocks: { var __when_tmp; when { ... -> __when_tmp = ... }; __when_tmp }
        // Or simpler: { var __when_tmp = if (...) a else b; __when_tmp }
        // These need special handling because preceding statements must be hoisted
        val statements = expression.statements
        if (statements.size >= 2) {
            val firstStmt = statements.firstOrNull()
            val lastStmt = statements.lastOrNull()

            // Detect when-lowered block by checking for __when_tmp variable and get
            val isWhenLoweredBlock = firstStmt is IrVariable &&
                firstStmt.name.asString().startsWith("__when_tmp") &&
                lastStmt is IrGetValue &&
                lastStmt.symbol.owner.name.asString().startsWith("__when_tmp")

            if (isWhenLoweredBlock) {
                // When-lowered blocks transform to: hoist all statements except last, return last as expression.
                // The hoisted statements will be consumed by the caller (e.g., flatMap for IrVariable).
                //
                // We push a new hoisting scope so nested transformations don't interfere with
                // outer scopes. After processing, we pop and add our statements to the parent scope.

                genCtx.pushHoistedScope()

                for (i in 0 until statements.size - 1) {
                    val stmt = statements[i]
                    when (stmt) {
                        is IrVariable -> {
                            // Transform the initializer - if it's a when-lowered block, its statements
                            // will be added to the current hoisted scope
                            val init = stmt.initializer?.let { parent.transformExpression(it) }
                            // Add the variable declaration after any hoisted statements from the initializer
                            genCtx.addHoistedStatement(BrsVariable(stmt.name.asString(), mapTypeToBrs(stmt.type), init))
                        }
                        is IrWhen -> {
                            // Push a scope to capture any nested when-lowered block hoisting
                            genCtx.pushHoistedScope()
                            val whenStmt = parent.statementVisitor.visitWhen(stmt, Unit)
                            // Take any statements added during transformation and add to our scope
                            val nestedHoisted = genCtx.popHoistedScope()
                            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                            genCtx.addHoistedStatement(whenStmt)
                        }
                        is IrWhileLoop -> {
                            genCtx.pushHoistedScope()
                            val loopStmt = parent.statementVisitor.visitWhileLoop(stmt, Unit)
                            val nestedHoisted = genCtx.popHoistedScope()
                            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                            genCtx.addHoistedStatement(loopStmt)
                        }
                        is IrDoWhileLoop -> {
                            genCtx.pushHoistedScope()
                            val loopStmt = parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                            val nestedHoisted = genCtx.popHoistedScope()
                            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                            genCtx.addHoistedStatement(loopStmt)
                        }
                        is IrBlock -> {
                            genCtx.pushHoistedScope()
                            val blockStmt = parent.statementVisitor.visitBlock(stmt, Unit)
                            val nestedHoisted = genCtx.popHoistedScope()
                            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                            genCtx.addHoistedStatement(blockStmt)
                        }
                        else -> {
                            genCtx.pushHoistedScope()
                            val transformed = parent.transformStatement(stmt)
                            val nestedHoisted = genCtx.popHoistedScope()
                            nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                            transformed?.let { genCtx.addHoistedStatement(it) }
                        }
                    }
                }

                // Pop our scope and add all statements to the parent scope
                val blockStatements = genCtx.popHoistedScope()
                blockStatements.forEach { genCtx.addHoistedStatement(it) }

                // Return just the temp var reference
                return (lastStmt as IrExpression).accept(this, data)
            }
        }

        // Handle when-with-subject blocks: { val tmp0_subject = expr; when { ... } }
        // These are generated by FIR2IR for when expressions with subjects like `when (x)`.
        // The subject variable declaration must be hoisted so it's available in the when conditions.
        // The second statement can be either:
        // 1. IrWhen directly (when expression not yet lowered)
        // 2. IrBlock (when-lowered block containing __when_tmp, when, __when_tmp get)
        if (statements.size == 2) {
            val firstStmt = statements[0]
            val secondStmt = statements[1]

            // Detect when-with-subject block: first is subject variable, second is when or when-lowered block
            val isSubjectVariable = firstStmt is IrVariable &&
                firstStmt.name.asString().contains("subject")

            val isWhenOrWhenLoweredBlock = secondStmt is IrWhen ||
                (secondStmt is IrBlock && secondStmt.statements.firstOrNull()?.let {
                    it is IrVariable && it.name.asString().startsWith("__when_tmp")
                } == true)

            if (isSubjectVariable && isWhenOrWhenLoweredBlock) {
                val subjectVar = firstStmt as IrVariable
                // Transform and hoist the subject variable declaration
                val varInit = subjectVar.initializer?.let { parent.transformExpression(it) }

                // Take any hoisted statements from initializer transformation
                val initHoisted = genCtx.takeHoistedStatements()
                initHoisted.forEach { genCtx.addHoistedStatement(it) }

                // Add the subject variable declaration
                genCtx.addHoistedStatement(
                    BrsVariable(
                        subjectVar.name.asString(),
                        mapTypeToBrs(subjectVar.type),
                        varInit
                    )
                )

                // Transform the when expression or when-lowered block - it will use the now-hoisted subject variable
                return (secondStmt as IrExpression).accept(this, data)
            }
        }

        // Default: Block expressions return the last statement's value
        // But we must also execute any preceding statements (side effects)!
        // This is critical for inlined lambda bodies like:
        //   { sideEffect(); returnValue }
        //
        // Note: After BrsReturnableBlockLowering runs, returnable blocks are converted
        // to composites with temp variables, so returns targeting the block become
        // assignments. This handles most cases from inline function expansion.
        //
        // For remaining cases (blocks that weren't from returnable blocks), we hoist
        // the side effect statements so they execute, then return the value.
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            // Hoist all statements except the last one (which is the return value)
            for (i in 0 until statements.size - 1) {
                val stmt = statements[i]
                genCtx.pushHoistedScope()
                val transformed = when (stmt) {
                    is IrWhen -> parent.statementVisitor.visitWhen(stmt, Unit)
                    is IrWhileLoop -> parent.statementVisitor.visitWhileLoop(stmt, Unit)
                    is IrDoWhileLoop -> parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                    is IrBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                    else -> parent.transformStatement(stmt)
                }
                val nestedHoisted = genCtx.popHoistedScope()
                nestedHoisted.forEach { genCtx.addHoistedStatement(it) }
                transformed?.let { genCtx.addHoistedStatement(it) }
            }

            // Return the last statement's value
            val last = statements.last()
            if (last is IrExpression) {
                last.accept(this, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }

    /**
     * Transform an IO_WORKER_CALL block into a runIOWorker function call.
     *
     * Block structure from BrsIOWorkerExtractionLowering:
     * - statements[0]: IrConst<String> with the worker name
     * - statements[1]: IrComposite with IO_WORKER_CAPTURES origin containing alternating
     *                  [value, name, value, name, ...] pairs
     *
     * Emits: runIOWorker_T_k_("workerName", { "name": value, ... })
     */
    private fun transformIOWorkerCall(expression: IrBlock): BrsExpression {
        val statements = expression.statements

        // Extract worker name from first statement
        val workerNameConst = statements.firstOrNull() as? IrConst
            ?: return BrsInvalidLiteral()
        val workerName = workerNameConst.value as? String
            ?: return BrsInvalidLiteral()

        // Build the captures AA literal
        val capturesEntries = mutableListOf<BrsAAEntry>()

        // If there's a second statement with captures, extract them
        if (statements.size > 1) {
            val capturesBlock = statements[1]
            if (capturesBlock is IrComposite && capturesBlock.origin == BrsStatementOrigins.IO_WORKER_CAPTURES) {
                // Pairs are: [value, name, value, name, ...]
                val captureStatements = capturesBlock.statements
                var i = 0
                while (i < captureStatements.size - 1) {
                    val valueExpr = captureStatements[i] as? IrExpression
                    val nameConst = captureStatements[i + 1] as? IrConst
                    if (valueExpr != null && nameConst != null) {
                        val captureName = nameConst.value as? String ?: "unknown"
                        val captureValue = parent.transformExpression(valueExpr)
                        capturesEntries.add(BrsAAEntry(captureName, captureValue))
                    }
                    i += 2
                }
            }
        }

        val capturesAA = BrsAALiteral(capturesEntries)

        // Emit: runIOWorker_T_k_("workerName", capturesAA)
        // For now, we use a generic name since type erasure makes all instances the same
        val functionName = "runIOWorker_Str_k_" // Simplified - actual type handling TBD
        return createFunctionCall(functionName, mutableListOf(
            BrsStringLiteral(workerName),
            capturesAA
        ), context)
    }


}

// ==================== Type Helper Extensions ====================

/**
 * Check if a type is primitive for comparison purposes.
 * Primitive types can use BrightScript's native = and <> operators.
 * Non-primitive types (classes, Any, type parameters) need brsStructuralEquals.
 */
private fun IrType.isPrimitiveForComparison(): Boolean {
    // Get the non-nullable version for checking
    val baseType = this.makeNotNull()

    // Primitive types that BrightScript can compare natively
    if (baseType.isInt() || baseType.isLong() || baseType.isFloat() || baseType.isDouble() ||
        baseType.isShort() || baseType.isByte() || baseType.isBoolean() || baseType.isChar() ||
        baseType.isString()) {
        return true
    }

    // Type parameters (generics) need structural comparison
    if (baseType.classifierOrNull is IrTypeParameterSymbol) {
        return false
    }

    // Any and Nothing types need structural comparison
    if (baseType.isAny() || baseType.isNothing()) {
        return false
    }

    // Class types (non-primitive) need structural comparison
    val classifier = baseType.classifierOrNull?.owner
    if (classifier is IrClass) {
        // Enum comparisons are handled separately above, but regular classes need structural
        return false
    }

    // Default to structural comparison for safety
    return false
}

/**
 * Check if a type is primitive for arithmetic operations.
 * Primitive types can use BrightScript's native +, -, *, /, MOD operators.
 * Non-primitive types (wrapper classes like UInt, ULong) need method calls.
 */
private fun IrType.isPrimitiveForArithmetic(): Boolean {
    val baseType = this.makeNotNull()

    // Primitive numeric types that BrightScript can operate on natively
    if (baseType.isInt() || baseType.isLong() || baseType.isFloat() || baseType.isDouble() ||
        baseType.isShort() || baseType.isByte()) {
        return true
    }

    // String uses + for concatenation
    if (baseType.isString()) {
        return true
    }

    // Everything else (UInt, ULong, UByte, UShort, custom classes) needs method calls
    return false
}
