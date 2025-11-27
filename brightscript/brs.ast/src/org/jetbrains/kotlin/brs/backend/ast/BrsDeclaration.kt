/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * BrightScript type annotations.
 */
enum class BrsType(val typeName: String) {
    INTEGER("Integer"),
    LONG_INTEGER("LongInteger"),
    FLOAT("Float"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    STRING("String"),
    OBJECT("Object"),
    DYNAMIC("Dynamic"),
    VOID("Void"),
    INVALID("Invalid"),
    FUNCTION("Function")
}

/**
 * Base interface for all BrightScript declarations.
 */
sealed interface BrsDeclaration : BrsNode {
    override fun deepCopy(): BrsDeclaration
}

/**
 * A function parameter.
 *
 * ```brightscript
 * paramName as Type = defaultValue
 * ```
 */
class BrsParameter(
    var name: String,
    var type: BrsType? = null,
    var defaultValue: BrsExpression? = null
) : BrsNodeBase() {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitParameter(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        defaultValue?.accept(visitor, data)
    }

    override fun deepCopy(): BrsParameter = BrsParameter(name, type, defaultValue?.deepCopy()).also { it.source = source }
}

/**
 * A function declaration (returns a value).
 *
 * ```brightscript
 * function FunctionName(param1 as Type, param2) as ReturnType
 *     ' statements
 *     return value
 * end function
 * ```
 */
class BrsFunction(
    var name: String,
    val parameters: MutableList<BrsParameter> = mutableListOf(),
    var returnType: BrsType? = null,
    var body: BrsBlock
) : BrsNodeBase(), BrsDeclaration {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitFunction(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        parameters.forEach { it.accept(visitor, data) }
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsFunction = BrsFunction(
        name,
        parameters.map { it.deepCopy() }.toMutableList(),
        returnType,
        body.deepCopy()
    ).also { it.source = source }
}

/**
 * A sub declaration (no return value).
 *
 * ```brightscript
 * sub SubName(param1 as Type, param2)
 *     ' statements
 * end sub
 * ```
 */
class BrsSub(
    var name: String,
    val parameters: MutableList<BrsParameter> = mutableListOf(),
    var body: BrsBlock
) : BrsNodeBase(), BrsDeclaration {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitSub(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        parameters.forEach { it.accept(visitor, data) }
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsSub = BrsSub(
        name,
        parameters.map { it.deepCopy() }.toMutableList(),
        body.deepCopy()
    ).also { it.source = source }
}

/**
 * A field declaration in a component interface.
 *
 * ```brightscript
 * <field id="myField" type="string" value="" onChange="onFieldChanged" />
 * ```
 */
class BrsField(
    var name: String,
    var type: String,
    var defaultValue: String? = null,
    var onChange: String? = null,
    var alwaysNotify: Boolean = false
) : BrsNodeBase(), BrsDeclaration {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitField(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsField = BrsField(name, type, defaultValue, onChange, alwaysNotify).also { it.source = source }
}

/**
 * A complete BrightScript program (a .brs file).
 */
class BrsProgram(
    val declarations: MutableList<BrsDeclaration> = mutableListOf(),
    val statements: MutableList<BrsStatement> = mutableListOf()
) : BrsNodeBase() {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitProgram(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        declarations.forEach { it.accept(visitor, data) }
        statements.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsProgram = BrsProgram(
        declarations.map { it.deepCopy() }.toMutableList(),
        statements.map { it.deepCopy() }.toMutableList()
    ).also { it.source = source }
}

/**
 * A SceneGraph component interface definition.
 */
class BrsComponentInterface(
    val fields: MutableList<BrsField> = mutableListOf(),
    val functions: MutableList<String> = mutableListOf()
) : BrsNodeBase() {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitNode(visitor, data)

    private fun <R, D> visitNode(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitNode(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        fields.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsComponentInterface = BrsComponentInterface(
        fields.map { it.deepCopy() }.toMutableList(),
        functions.toMutableList()
    ).also { it.source = source }
}

/**
 * A SceneGraph component definition.
 *
 * This represents both the XML structure and the associated BrightScript code.
 */
class BrsComponent(
    var name: String,
    var extends: String = "Group",
    var componentInterface: BrsComponentInterface = BrsComponentInterface(),
    var script: BrsProgram = BrsProgram()
) : BrsNodeBase() {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitComponent(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        componentInterface.accept(visitor, data)
        script.accept(visitor, data)
    }

    override fun deepCopy(): BrsComponent = BrsComponent(
        name,
        extends,
        componentInterface.deepCopy(),
        script.deepCopy()
    ).also { it.source = source }
}
