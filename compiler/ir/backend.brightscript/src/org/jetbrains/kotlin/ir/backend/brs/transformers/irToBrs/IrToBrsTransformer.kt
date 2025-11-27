/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * Transforms Kotlin IR to BrightScript AST.
 *
 * This is the core transformation that converts lowered Kotlin IR
 * into BrightScript-specific AST nodes that can then be rendered
 * to text.
 */
class IrToBrsTransformer(
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsNode?, Unit> {

    private val statementTransformer = IrStatementToBrsTransformer(this, context)
    private val expressionTransformer = IrExpressionToBrsTransformer(this, context)

    // ==================== Entry Points ====================

    /**
     * Transform an IR file to a BrightScript program.
     */
    fun transformFile(irFile: IrFile): BrsProgram {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()

        for (declaration in irFile.declarations) {
            when (declaration) {
                is IrFunction -> {
                    transformFunction(declaration)?.let { declarations.add(it) }
                }
                is IrClass -> {
                    // Classes are expanded into functions and statements
                    transformClassDeclarations(declaration, declarations, statements)
                }
                is IrProperty -> {
                    // Top-level properties become variables or functions
                    transformProperty(declaration, declarations, statements)
                }
                else -> {
                    // Other declarations (type aliases, etc.) are typically not emitted
                }
            }
        }

        return BrsProgram(declarations, statements)
    }

    /**
     * Transform an IR class to its BrightScript representation.
     */
    fun transformClass(irClass: IrClass): List<BrsDeclaration> {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()
        transformClassDeclarations(irClass, declarations, statements)
        return declarations
    }

    // ==================== Declarations ====================

    /**
     * Transform an IR function to a BrightScript function or sub.
     */
    fun transformFunction(irFunction: IrFunction): BrsDeclaration? {
        if (irFunction is IrSimpleFunction && irFunction.isFakeOverride) return null
        if (irFunction.isExternal) return null

        val name = context.getBrsName(irFunction)
        val parameters = irFunction.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }

        val body = irFunction.body?.let { transformBody(it) } ?: BrsBlock()

        val returnType = mapTypeToBrs(irFunction.returnType)

        // Functions with Unit return type become subs
        return if (irFunction.returnType.isUnit() || irFunction.returnType.isNothing()) {
            BrsSub(name, parameters.toMutableList(), body)
        } else {
            BrsFunction(name, parameters.toMutableList(), returnType, body)
        }
    }

    /**
     * Transform class declarations into top-level functions and statements.
     */
    private fun transformClassDeclarations(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Generate constructor function
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate member functions
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride) {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }

        // Generate property accessors
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            transformProperty(property, declarations, statements)
        }

        // Process nested classes
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform a constructor to a BrightScript function.
     */
    private fun transformConstructor(irClass: IrClass, constructor: IrConstructor): BrsFunction? {
        val className = context.getBrsName(irClass)
        val name = "${className}_create"

        val parameters = constructor.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // Create the object (AA)
        bodyStatements.add(
            BrsVariable(
                name = "this",
                initializer = BrsAALiteral()
            )
        )

        // Initialize fields
        for (field in irClass.declarations.filterIsInstance<IrField>()) {
            field.initializer?.expression?.let { initializer ->
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), field.name.asString()),
                            BrsBinaryOperator.EQ,
                            transformExpression(initializer)
                        )
                    )
                )
            }
        }

        // Execute constructor body
        constructor.body?.let { body ->
            val transformed = transformBody(body)
            bodyStatements.addAll(transformed.statements)
        }

        // Return the constructed object
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name,
            parameters.toMutableList(),
            BrsType.OBJECT,
            BrsBlock(bodyStatements)
        )
    }

    /**
     * Transform a property to BrightScript representation.
     */
    private fun transformProperty(
        property: IrProperty,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Generate getter function if present
        property.getter?.let { getter ->
            if (!getter.isFakeOverride) {
                transformFunction(getter)?.let { declarations.add(it) }
            }
        }

        // Generate setter function if present
        property.setter?.let { setter ->
            if (!setter.isFakeOverride) {
                transformFunction(setter)?.let { declarations.add(it) }
            }
        }

        // For top-level properties with backing fields, generate initialization
        property.backingField?.let { field ->
            if (field.parent is IrFile) {
                field.initializer?.expression?.let { initializer ->
                    statements.add(
                        BrsVariable(
                            name = property.name.asString(),
                            type = mapTypeToBrs(field.type),
                            initializer = transformExpression(initializer)
                        )
                    )
                }
            }
        }
    }

    // ==================== Body and Statements ====================

    /**
     * Transform an IR body to a BrightScript block.
     */
    fun transformBody(body: IrBody): BrsBlock {
        return when (body) {
            is IrBlockBody -> {
                val statements = body.statements.mapNotNull { statement ->
                    transformStatement(statement)
                }
                BrsBlock(statements.toMutableList())
            }
            is IrExpressionBody -> {
                BrsBlock(mutableListOf(BrsReturn(transformExpression(body.expression))))
            }
            is IrSyntheticBody -> {
                BrsBlock()
            }
        }
    }

    /**
     * Transform an IR statement to a BrightScript statement.
     */
    fun transformStatement(statement: IrStatement): BrsStatement? {
        return statement.accept(statementTransformer, Unit)
    }

    // ==================== Expressions ====================

    /**
     * Transform an IR expression to a BrightScript expression.
     */
    fun transformExpression(expression: IrExpression): BrsExpression {
        return expression.accept(expressionTransformer, Unit)
    }

    // ==================== Type Mapping ====================

    /**
     * Map a Kotlin IR type to a BrightScript type.
     */
    fun mapTypeToBrs(type: IrType): BrsType? {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> BrsType.INTEGER
            type.isLong() -> BrsType.LONG_INTEGER
            type.isFloat() -> BrsType.FLOAT
            type.isDouble() -> BrsType.DOUBLE
            type.isBoolean() -> BrsType.BOOLEAN
            type.isString() -> BrsType.STRING
            type.isUnit() -> BrsType.VOID
            type.isNothing() -> BrsType.VOID
            type.isNullable() -> BrsType.DYNAMIC
            type.isFunction() -> BrsType.FUNCTION
            else -> BrsType.OBJECT
        }
    }

    // ==================== Visitor Implementation ====================

    override fun visitElement(element: IrElement, data: Unit): BrsNode? {
        // Default implementation - should be handled by specialized transformers
        return null
    }
}

/**
 * Transforms IR statements to BrightScript statements.
 */
class IrStatementToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsStatement?, Unit> {

    override fun visitElement(element: IrElement, data: Unit): BrsStatement? = null

    override fun visitVariable(declaration: IrVariable, data: Unit): BrsStatement {
        return BrsVariable(
            name = declaration.name.asString(),
            type = parent.mapTypeToBrs(declaration.type),
            initializer = declaration.initializer?.let { parent.transformExpression(it) }
        )
    }

    override fun visitReturn(expression: IrReturn, data: Unit): BrsStatement {
        return BrsReturn(
            value = if (expression.value.type.isUnit()) null
            else parent.transformExpression(expression.value)
        )
    }

    override fun visitThrow(expression: IrThrow, data: Unit): BrsStatement {
        return if (context.supportsExceptions) {
            BrsThrow(parent.transformExpression(expression.value))
        } else {
            // Fallback for older Roku OS versions - use stop
            BrsExpressionStatement(
                BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(
                        BrsStringLiteral("Error: "),
                        parent.transformExpression(expression.value)
                    )
                )
            )
        }
    }

    override fun visitTry(aTry: IrTry, data: Unit): BrsStatement {
        return if (context.supportsExceptions) {
            val tryBlock = parent.transformBody(aTry.tryResult as IrBody)
            val catchBlock = aTry.catches.firstOrNull()?.let { catch ->
                parent.transformBody(catch.result as IrBody)
            }
            val catchVar = aTry.catches.firstOrNull()?.catchParameter?.name?.asString()

            BrsTry(tryBlock, catchVar, catchBlock)
        } else {
            // Without exception support, just execute the try block
            parent.transformBody(aTry.tryResult as IrBody)
        }
    }

    override fun visitWhen(expression: IrWhen, data: Unit): BrsStatement {
        // Transform to if/else chain
        var result: BrsIf? = null
        var current: BrsIf? = null

        for (branch in expression.branches) {
            val condition = parent.transformExpression(branch.condition)
            val body = parent.transformExpression(branch.result)
            val bodyStatement = BrsExpressionStatement(body)

            val ifStmt = BrsIf(
                condition = condition,
                thenBranch = bodyStatement
            )

            if (result == null) {
                result = ifStmt
                current = ifStmt
            } else {
                current?.elseBranch = ifStmt
                current = ifStmt
            }
        }

        return result ?: BrsEmpty()
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsStatement {
        return BrsWhile(
            condition = parent.transformExpression(loop.condition),
            body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
        )
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsStatement {
        // BrightScript doesn't have do-while, transform to while with entry guard
        val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
        val condition = parent.transformExpression(loop.condition)

        return BrsBlock(mutableListOf(
            body,
            BrsWhile(condition, body.deepCopy())
        ))
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsStatement {
        return BrsExit(BrsExitKind.WHILE) // or FOR depending on context
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsStatement {
        return if (context.supportsContinue) {
            BrsContinue(BrsContinueKind.WHILE)
        } else {
            // For older Roku OS, we'd need to restructure the loop
            BrsComment("continue not supported on target Roku OS")
        }
    }

    override fun visitBlockBody(body: IrBlockBody, data: Unit): BrsStatement {
        return parent.transformBody(body)
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsStatement {
        val statements = expression.statements.mapNotNull { stmt ->
            when (stmt) {
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
        }
        return BrsBlock(statements.toMutableList())
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsStatement {
        return BrsExpressionStatement(
            BrsBinaryOp(
                BrsIdentifier(expression.symbol.owner.name.asString()),
                BrsBinaryOperator.EQ,
                parent.transformExpression(expression.value)
            )
        )
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsStatement {
        val receiver = expression.receiver?.let { parent.transformExpression(it) }
            ?: BrsMRef()

        return BrsExpressionStatement(
            BrsBinaryOp(
                BrsDotAccess(receiver, expression.symbol.owner.name.asString()),
                BrsBinaryOperator.EQ,
                parent.transformExpression(expression.value)
            )
        )
    }

    private fun transformBlockOrStatement(element: IrElement): BrsStatement {
        return when (element) {
            is IrBlock -> visitBlock(element, Unit)
            is IrBlockBody -> visitBlockBody(element, Unit)
            is IrExpression -> BrsExpressionStatement(parent.transformExpression(element))
            is IrStatement -> parent.transformStatement(element) ?: BrsEmpty()
            else -> BrsEmpty()
        }
    }
}

/**
 * Transforms IR expressions to BrightScript expressions.
 */
class IrExpressionToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsExpression, Unit> {

    override fun visitElement(element: IrElement, data: Unit): BrsExpression {
        return BrsStringLiteral("/* Unsupported: ${element::class.simpleName} */")
    }

    // ==================== Literals ====================

    override fun visitConst(expression: IrConst, data: Unit): BrsExpression {
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

    // ==================== References ====================

    override fun visitGetValue(expression: IrGetValue, data: Unit): BrsExpression {
        val name = expression.symbol.owner.name.asString()
        return if (name == "<this>") {
            BrsMRef()
        } else {
            BrsIdentifier(name)
        }
    }

    override fun visitGetField(expression: IrGetField, data: Unit): BrsExpression {
        val receiver = expression.receiver?.let { visitElement(it, data) }
            ?: BrsMRef()

        return BrsDotAccess(receiver, expression.symbol.owner.name.asString())
    }

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Unit): BrsExpression {
        // Object singletons are represented as function calls that create/return the instance
        val name = context.getBrsName(expression.symbol.owner)
        return BrsFunctionCall(BrsIdentifier("${name}_getInstance"), mutableListOf())
    }

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Unit): BrsExpression {
        val enumClass = expression.symbol.owner.parentAsClass
        val className = context.getBrsName(enumClass)
        val entryName = expression.symbol.owner.name.asString()
        return BrsIdentifier("${className}_${entryName}")
    }

    // ==================== Function Calls ====================

    override fun visitCall(expression: IrCall, data: Unit): BrsExpression {
        val function = expression.symbol.owner

        // Check for intrinsics
        if (context.intrinsics.isIntrinsic(expression.symbol)) {
            return transformIntrinsic(expression)
        }

        val functionName = context.getBrsName(function)
        val arguments = mutableListOf<BrsExpression>()

        // Add dispatch receiver if present
        expression.dispatchReceiver?.let { receiver ->
            val receiverExpr = visitElement(receiver, data)
            // For method calls, transform to dot notation
            if (function.dispatchReceiverParameter != null) {
                val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { visitElement(it, data) }
                }
                return BrsMethodCall(receiverExpr, function.name.asString(), args.toMutableList())
            }
            arguments.add(receiverExpr)
        }

        // Add extension receiver if present
        expression.extensionReceiver?.let { receiver ->
            arguments.add(visitElement(receiver, data))
        }

        // Add value arguments
        for (i in 0 until expression.valueArgumentsCount) {
            expression.getValueArgument(i)?.let { arg ->
                arguments.add(visitElement(arg, data))
            }
        }

        return BrsFunctionCall(BrsIdentifier(functionName), arguments)
    }

    private fun transformIntrinsic(expression: IrCall): BrsExpression {
        val function = expression.symbol.owner
        val name = function.name.asString()

        return when (name) {
            "createObject" -> {
                val typeArg = expression.getValueArgument(0)
                val objectType = (typeArg as? IrConst)?.value?.toString() ?: "Object"
                val args = (1 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { visitElement(it, Unit) }
                }
                BrsCreateObject(objectType, args.toMutableList())
            }
            "typeOf" -> {
                val arg = expression.getValueArgument(0)?.let { visitElement(it, Unit) }
                    ?: BrsInvalidLiteral()
                BrsTypeOf(arg)
            }
            "print" -> {
                val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { visitElement(it, Unit) }
                }
                BrsFunctionCall(BrsIdentifier("print"), args.toMutableList())
            }
            else -> BrsFunctionCall(BrsIdentifier(name), mutableListOf())
        }
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass
        val className = context.getBrsName(irClass)

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { visitElement(it, data) }
        }

        // Check if this is an external class (Roku SDK type)
        if (irClass.isExternal || isExternalClass(irClass)) {
            // External classes use CreateObject()
            val brsTypeName = getBrsExternalTypeName(irClass)
            return BrsCreateObject(brsTypeName, arguments.toMutableList())
        }

        return BrsFunctionCall(
            BrsIdentifier("${className}_create"),
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
        val argument = visitElement(expression.argument, data)

        return when (expression.operator) {
            IrTypeOperator.CAST, IrTypeOperator.IMPLICIT_CAST -> argument
            IrTypeOperator.SAFE_CAST -> {
                // Safe cast returns invalid if type doesn't match
                BrsConditional(
                    BrsBinaryOp(
                        BrsTypeOf(argument.deepCopy()),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(mapTypeToString(expression.typeOperand))
                    ),
                    argument,
                    BrsInvalidLiteral()
                )
            }
            IrTypeOperator.INSTANCEOF -> {
                BrsBinaryOp(
                    BrsTypeOf(argument),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(mapTypeToString(expression.typeOperand))
                )
            }
            IrTypeOperator.NOT_INSTANCEOF -> {
                BrsBinaryOp(
                    BrsTypeOf(argument),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral(mapTypeToString(expression.typeOperand))
                )
            }
            else -> argument
        }
    }

    private fun mapTypeToString(type: IrType): String {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> "roInt"
            type.isLong() -> "LongInteger"
            type.isFloat() -> "roFloat"
            type.isDouble() -> "roDouble"
            type.isBoolean() -> "roBoolean"
            type.isString() -> "roString"
            type.isArray() -> "roArray"
            else -> "roAssociativeArray"
        }
    }

    // ==================== Binary Operations ====================

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Unit): BrsExpression {
        val parts = expression.arguments.map { visitElement(it, data) }
        return parts.reduce { acc, expr ->
            BrsBinaryOp(acc, BrsBinaryOperator.CONCAT, expr)
        }
    }

    // ==================== Collections ====================

    override fun visitVararg(expression: IrVararg, data: Unit): BrsExpression {
        val elements = expression.elements.map { element ->
            when (element) {
                is IrExpression -> visitElement(element, data)
                is IrSpreadElement -> visitElement(element.expression, data)
                else -> BrsInvalidLiteral()
            }
        }
        return BrsArrayLiteral(elements.toMutableList())
    }

    // ==================== Control Flow ====================

    override fun visitWhen(expression: IrWhen, data: Unit): BrsExpression {
        // Transform to nested conditional expressions
        val branches = expression.branches

        if (branches.isEmpty()) {
            return BrsInvalidLiteral()
        }

        // Build from the end
        var result: BrsExpression = BrsInvalidLiteral()

        for (i in branches.indices.reversed()) {
            val branch = branches[i]
            val condition = visitElement(branch.condition, data)
            val value = visitElement(branch.result, data)

            result = if (i == branches.lastIndex && branch.isElse()) {
                value
            } else {
                BrsConditional(condition, value, result)
            }
        }

        return result
    }

    private fun IrBranch.isElse(): Boolean {
        return condition is IrConst && (condition as IrConst).value == true
    }

    // ==================== Function Expressions ====================

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Unit): BrsExpression {
        val function = expression.function

        val parameters = function.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = parent.mapTypeToBrs(param.type)
            )
        }

        val body = function.body?.let { parent.transformBody(it) } ?: BrsBlock()
        val returnType = parent.mapTypeToBrs(function.returnType)

        return BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
    }

    // ==================== Composite ====================

    override fun visitComposite(expression: IrComposite, data: Unit): BrsExpression {
        // Composite expressions are sequences - return the last one
        val statements = expression.statements
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            val last = statements.last()
            if (last is IrExpression) {
                visitElement(last, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsExpression {
        // Block expressions return the last statement's value
        val statements = expression.statements
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            val last = statements.last()
            if (last is IrExpression) {
                visitElement(last, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }
}
