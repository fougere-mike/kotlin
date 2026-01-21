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
 * Marks an external object as a BrighterScript namespace.
 *
 * BrighterScript namespaces compile to functions with a naming convention
 * of `Namespace_functionName`. This annotation tells the Kotlin compiler
 * to generate calls in that format.
 *
 * Example:
 * ```kotlin
 * @BrsNamespace("Utils")
 * external object Utils {
 *     fun getMessage(port: Dynamic, sleepInterval: Int = 20): Dynamic
 *     fun toString(value: Dynamic): String
 * }
 *
 * // Usage:
 * val msg = Utils.getMessage(port, 100)
 * // Compiles to: Utils_getMessage(port, 100)
 * ```
 *
 * @property name The BrighterScript namespace name. If empty, uses the object name.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsNamespace(val name: String = "")

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
 *                   Specify the Kotlin function name; the compiler will resolve
 *                   it to the mangled BrightScript name automatically.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsOnChange(val handler: String)

// ==================== Type-Safe SceneGraph Field Annotations ====================
//
// These annotations provide type-safe alternatives to @BrsField for declaring
// SceneGraph component interface fields. Each annotation corresponds to a
// BrightScript field type and enforces the correct Kotlin property type.
//
// Example usage:
// ```kotlin
// class MyComponent : SceneComponent() {
//     @SGStringField(defaultValue = "Hello")
//     var title: String = ""
//
//     @SGBooleanField(alwaysNotify = true)
//     @BrsOnChange("onFocusedChanged")
//     var focused: Boolean = false
// }
// ```

/**
 * Marks a property as a SceneGraph string field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGStringField(
    val alwaysNotify: Boolean = false,
    val defaultValue: String = ""
)

/**
 * Marks a property as a SceneGraph integer field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGIntegerField(
    val alwaysNotify: Boolean = false,
    val defaultValue: Int = 0
)

/**
 * Marks a property as a SceneGraph long integer field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGLongIntegerField(
    val alwaysNotify: Boolean = false,
    val defaultValue: Long = 0L
)

/**
 * Marks a property as a SceneGraph float field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGFloatField(
    val alwaysNotify: Boolean = false,
    val defaultValue: Float = 0.0f
)

/**
 * Marks a property as a SceneGraph double field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGDoubleField(
    val alwaysNotify: Boolean = false,
    val defaultValue: Double = 0.0
)

/**
 * Marks a property as a SceneGraph boolean field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGBooleanField(
    val alwaysNotify: Boolean = false,
    val defaultValue: Boolean = false
)

/**
 * Marks a property as a SceneGraph array field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGArrayField(
    val alwaysNotify: Boolean = false
)

/**
 * Marks a property as a SceneGraph associative array field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGAssocArrayField(
    val alwaysNotify: Boolean = false
)

/**
 * Marks a property as a SceneGraph node field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property nodeType Optional: the specific node type (e.g., "Label", "Poster", "ContentNode").
 *                    If empty, accepts any node type.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGNodeField(
    val alwaysNotify: Boolean = false,
    val nodeType: String = ""
)

/**
 * Marks a property as a SceneGraph function reference field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGFunctionField(
    val alwaysNotify: Boolean = false
)

/**
 * Marks a property as a SceneGraph URI field.
 *
 * URI fields are used for resource paths like images, fonts, and other assets.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default URI value for this field in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGUriField(
    val alwaysNotify: Boolean = false,
    val defaultValue: String = ""
)

/**
 * Marks a property as a SceneGraph time field.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGTimeField(
    val alwaysNotify: Boolean = false
)

/**
 * Marks a property as a SceneGraph 2D vector field.
 *
 * Vector2D fields contain [x, y] coordinates.
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGVector2DField(
    val alwaysNotify: Boolean = false
)

/**
 * Marks a property as a SceneGraph color field.
 *
 * Color fields use RGBA hex format (e.g., "0xFF0000FF" for red with full opacity).
 *
 * @property alwaysNotify If true, observers are notified even when the value doesn't change.
 * @property defaultValue The default color in hex format (e.g., "0xFFFFFFFF").
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class SGColorField(
    val alwaysNotify: Boolean = false,
    val defaultValue: String = ""
)

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

/**
 * Returns the mangled BrightScript name of a function reference.
 *
 * This intrinsic is resolved at compile time to produce the actual
 * BrightScript function name, accounting for name mangling.
 *
 * Example:
 * ```kotlin
 * class MyComponent : SceneComponent() {
 *     init {
 *         button.observeField("buttonSelected", brsName(::onButtonPressed))
 *     }
 *
 *     private fun onButtonPressed(event: RoSGNodeEvent) { ... }
 * }
 * ```
 *
 * The call `brsName(::onButtonPressed)` compiles to the string literal
 * `"MyComponent_onButtonPressed_RoSGNodeEvent_k_"`.
 *
 * @param function A function reference (e.g., `::myFunction`, `this::myMethod`)
 * @return The mangled BrightScript function name as a String
 */
@BrsIntrinsic("brsIntrinsicFunctionName")
public external fun <T : Function<*>> brsName(function: T): String

/**
 * Marks a function to use its unmangled name in generated BrightScript.
 *
 * Use this annotation for functions that need stable, predictable names
 * for interoperability with BrightScript code. The function name will be
 * used without any signature suffix or `_k_` suffix.
 *
 * Valid targets:
 * - Top-level functions: compiled as `functionName`
 * - Functions in object declarations (singletons): compiled as `ObjectName_functionName`
 * - Functions in companion objects: compiled as `ClassName_functionName`
 *
 * Example:
 * ```kotlin
 * @BrsStatic
 * fun myHelper() { }
 * // Compiles to: sub myHelper()
 *
 * object Utils {
 *     @BrsStatic
 *     fun format(value: String): String { ... }
 * }
 * // Compiles to: function Utils_format(value as String)
 * ```
 *
 * Restrictions:
 * - Functions marked `@BrsStatic` cannot have overloads (same name, different signatures)
 * - Not valid on member functions of regular classes (only top-level, object, or companion object)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsStatic

/**
 * Marks an object declaration as containing compile-time constants.
 *
 * All properties in a @BrsConstant object must be val properties with
 * constant initializers. The compiler will:
 * 1. Evaluate all property values at compile time
 * 2. Inline the values at all usage sites
 * 3. Not emit the object declaration to BrightScript
 *
 * Supported expressions:
 * - Primitive literals (Int, Long, Float, Double, String, Boolean)
 * - String concatenation
 * - Arithmetic operations (+, -, *, /)
 * - Boolean operations (&&, ||, !)
 * - References to other @BrsConstant properties
 * - Enum ordinal and name values
 *
 * Example:
 * ```kotlin
 * @BrsConstant
 * object Config {
 *     val API_VERSION = 1
 *     val API_URL = "https://api.example.com/v" + API_VERSION
 *     val TIMEOUT_MS = 30 * 1000
 * }
 *
 * // Usage compiles to literal values:
 * print(Config.API_URL)  // → print "https://api.example.com/v1"
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class BrsConstant
