/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

/**
 * Marks a declaration as externally implemented in BrightScript.
 *
 * Use this annotation on `external` declarations that are implemented
 * in BrightScript code or are part of the Roku SDK.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsExternal

/**
 * Specifies a custom BrightScript name for a declaration.
 *
 * By default, Kotlin names are used directly. Use this annotation
 * to specify a different name in the generated BrightScript code.
 *
 * @property name The BrightScript name to use.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsName(val name: String)

/**
 * Marks a function to be exported in the SceneGraph component interface.
 *
 * Functions marked with this annotation will be accessible from the
 * component's public interface and can be called via `callFunc`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsExport

/**
 * Marks a property as a SceneGraph component field.
 *
 * Properties marked with this annotation will be generated as
 * `<field>` elements in the component's XML interface.
 *
 * @property type The BrightScript field type (e.g., "string", "integer", "node").
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsField(
    val type: String = "",
    val alwaysNotify: Boolean = false
)

/**
 * Specifies the onChange handler for a field.
 *
 * @property handler The name of the function to call when the field changes.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsOnChange(val handler: String)

/**
 * Marks a class as a SceneGraph component.
 *
 * Classes marked with this annotation will generate both a .brs file
 * and a corresponding .xml component definition.
 *
 * @property name The component name (defaults to the class name).
 * @property extends The parent component to extend (defaults to "Group").
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsComponent(
    val name: String = "",
    val extends: String = "Group"
)

/**
 * Marks code to be inlined as raw BrightScript.
 *
 * The function body will be replaced with the provided BrightScript code.
 * Use with caution as no type checking is performed on the inline code.
 *
 * @property code The raw BrightScript code to inline.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsInline(val code: String)

/**
 * Marks a function as a BrightScript intrinsic.
 *
 * The compiler will replace calls to this function with the specified
 * intrinsic implementation. The function must be declared as `external`.
 *
 * @property name The intrinsic name (e.g., "brsIntrinsicIsAA").
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsIntrinsic(val name: String)

/**
 * Specifies the minimum Roku OS version required for a declaration.
 *
 * The compiler will generate appropriate fallbacks or errors for
 * features not supported on older Roku OS versions.
 *
 * @property major The major version number.
 * @property minor The minor version number.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsMinRokuOS(val major: Int, val minor: Int = 0)

/**
 * Suppresses specific BrightScript compiler warnings.
 *
 * @property warnings The warning codes to suppress.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.EXPRESSION
)
@Retention(AnnotationRetention.SOURCE)
public annotation class BrsSuppress(vararg val warnings: String)

/**
 * Marks a companion object function as a CreateObject factory.
 *
 * The function compiles to `CreateObject("<typeName>", args...)`.
 * Use this on companion object functions to provide type-safe factories
 * for native BrightScript objects.
 *
 * Example:
 * ```kotlin
 * external interface RoArray {
 *     companion object {
 *         @BrsCreateObject("roArray")
 *         fun create(size: Int = 0, resize: Boolean = true): RoArray = definedExternally
 *     }
 * }
 * ```
 *
 * @property typeName The BrightScript object type name (e.g., "roArray", "roAssociativeArray").
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsCreateObject(val typeName: String)

/**
 * Marks a class as a SceneGraph component base class.
 *
 * Classes annotated with this annotation (and their subclasses) are compiled
 * differently from regular Kotlin classes:
 * - No constructor function is generated (component lifecycle is managed by SceneGraph)
 * - The `init { }` block compiles to BrightScript's `sub init()`
 * - Property accesses to inherited members like `top`, `global` compile to `m.top`, `m.global`
 *
 * This annotation is typically used on abstract base classes like [SceneComponent].
 * User code should extend those base classes rather than using this annotation directly.
 *
 * @property extends The BrightScript component type this extends (e.g., "Group", "Task", "Scene").
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsSceneGraphComponent(
    val extends: String = "Group"
)
