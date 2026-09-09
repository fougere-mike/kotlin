/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * BrightScript-specific intrinsic functions and types.
 *
 * These represent BrightScript built-in operations that have special handling
 * during code generation.
 */
@OptIn(InternalSymbolFinderAPI::class)
class BrsIntrinsics(
    private val irBuiltIns: IrBuiltIns
) {
    private val symbolFinder = irBuiltIns.symbolFinder

    /**
     * The roArray class symbol for BrightScript arrays.
     */
    val roArrayClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.roArray
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The roAssociativeArray class symbol for BrightScript associative arrays.
     */
    val roAssociativeArrayClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.roAssociativeArray
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The roSGNode class symbol for SceneGraph nodes.
     */
    val roSGNodeClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.roSGNode
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    // =============================================================================
    // Native Iteration Support
    // =============================================================================

    /**
     * The NativeArrayIterator marker interface symbol.
     * When iterator() returns this type, the compiler emits native for-each.
     */
    val nativeArrayIteratorClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.nativeArrayIterator
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The NativeIterable interface symbol.
     * Types implementing this support native BrightScript for-each iteration.
     */
    val nativeIterableClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.nativeIterable
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The IEnumNative interface symbol.
     * External native types that support BrightScript enumeration.
     */
    val iEnumNativeClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.iEnumNative
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The RoArray interface symbol.
     */
    val roArrayInterfaceClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.roArrayInterface
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The RoAssociativeArray interface symbol.
     */
    val roAssociativeArrayInterfaceClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.BuiltIns.roAssociativeArrayInterface
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    // =============================================================================
    // SharedService (reference-shared classes)
    // =============================================================================

    /**
     * The kotlin.brs.SharedService class symbol — the SOLE machinery root for
     * reference-shared classes ([BrsStandardClassIds.Shared.sharedService]).
     */
    val sharedServiceClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Shared.sharedService
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * Whether [irClass] is SharedService or reaches it through the transitive
     * superclass chain. Drives the SharedService emission switch (extension-shaped
     * methods + forwarding wrapper slots, IrToBrsTransformer) and the static
     * dispatch lowering (BrsSharedDispatchLowering).
     *
     * MIRROR NOTE: this is the IR-side mirror of the FIR predicate in
     * checkers.brs (`BrsSharedServiceTypes.isSharedServiceClass`) — the two
     * modules share no type-system source, so the classification is mirrored
     * rather than imported; the shared root is the ClassId in
     * [BrsStandardClassIds.Shared]. Contract (keep both sides identical, change
     * them in the same commit):
     * - the walk follows the SUPERCLASS chain only (SharedService is a class —
     *   it is unreachable through interfaces; FIR uses
     *   `lookupSuperTypes(lookupInterfaces = false, deep = true)`),
     * - type aliases are expanded at each hop (already true structurally in IR:
     *   supertypes arrive alias-expanded from FIR),
     * - the root itself classifies as shared,
     * - unclassifiable inputs answer false/null (under-approximate, never
     *   false-positive — BrsScopeMarshallability precedent).
     */
    fun isSharedServiceClass(irClass: IrClass): Boolean {
        val sharedSymbol = sharedServiceClass ?: return false
        var current: IrClass? = irClass
        val visited = mutableSetOf<IrClass>()
        while (current != null && visited.add(current)) {
            if (current.symbol == sharedSymbol) return true
            current = current.superTypes.firstNotNullOfOrNull { superType ->
                superType.classOrNull?.owner?.takeIf { !it.isInterface }
            }
        }
        return false
    }

    /**
     * The SharedService-reaching class of [type], or null when the type does not
     * reach SharedService. Nullability never affects the answer (classifier
     * extraction ignores it); type parameters, error types, and non-class types
     * answer null — the same under-approximation as the FIR mirror's
     * `sharedClassSymbolOrNull` (see the MIRROR NOTE on [isSharedServiceClass]).
     */
    fun sharedServiceClassOrNull(type: IrType): IrClass? {
        val irClass = type.classOrNull?.owner ?: return null
        return irClass.takeIf { isSharedServiceClass(it) }
    }

    // =============================================================================
    // SceneGraph Component Support
    // =============================================================================

    /**
     * FqName for the @BrsSceneGraphComponent annotation. Internal so the
     * expression transformer's super-call redirect can tell a STDLIB component
     * base (annotated abstract base such as GroupComponent) from a user base.
     */
    internal val brsSceneGraphComponentFqn = FqName("kotlin.brs.BrsSceneGraphComponent")

    /**
     * The ComponentBase abstract class symbol.
     * This is the shared base for all component types.
     */
    val componentBaseClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.ComponentBase
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The GroupComponent base class symbol (extends Group).
     */
    val groupComponentClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.GroupComponent
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The LayoutComponent base class symbol (extends LayoutGroup).
     */
    val layoutComponentClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.LayoutComponent
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The SceneComponent base class symbol (extends Scene).
     */
    val sceneComponentClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.SceneComponent
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The TaskComponent base class symbol (extends Task).
     */
    val taskComponentClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.TaskComponent
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * The ContentNodeComponent base class symbol (extends ContentNode).
     */
    val contentNodeComponentClass: IrClassSymbol? by lazy {
        val classId = BrsStandardClassIds.Components.ContentNodeComponent
        symbolFinder.findClass(classId.shortClassName, classId.packageFqName)
    }

    /**
     * @deprecated Use [sceneComponentClass] instead.
     */
    @Deprecated("Use sceneComponentClass instead", ReplaceWith("sceneComponentClass"))
    val sceneNodeComponentClass: IrClassSymbol? by lazy {
        sceneComponentClass
    }

    /**
     * Checks if a class is a SceneGraph component.
     *
     * A class is a component if it or any of its supertypes has @BrsSceneGraphComponent.
     * This includes user classes that extend GroupComponent, SceneComponent, TaskComponent, etc.
     */
    fun isSceneGraphComponent(irClass: IrClass): Boolean {
        // Traverse class hierarchy looking for @BrsSceneGraphComponent
        val visited = mutableSetOf<IrClass>()
        val queue = ArrayDeque<IrClass>()
        queue.add(irClass)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            // Check for @BrsSceneGraphComponent annotation
            if (current.hasAnnotation(brsSceneGraphComponentFqn)) {
                return true
            }

            // Check supertypes
            for (superType in current.superTypes) {
                val superClass = superType.classOrNull?.owner ?: continue
                queue.add(superClass)
            }
        }
        return false
    }

    /**
     * Checks if a class is one of the abstract component base declarations
     * (GroupComponent, SceneComponent, TaskComponent, ...): directly annotated
     * with @BrsSceneGraphComponent and abstract.
     *
     * These bases carry their contract (extends value, inherited protocol
     * fields) through the component extractor's inheritance pass; they must
     * not emit any BrightScript of their own — their source file is a shared
     * pkg:/source script, so e.g. a `sub init()` there would collide with
     * every component's own init().
     */
    fun isComponentBaseDeclaration(irClass: IrClass): Boolean {
        return irClass.modality == Modality.ABSTRACT &&
                irClass.hasAnnotation(brsSceneGraphComponentFqn)
    }

    /**
     * Checks if a class is a Task component.
     *
     * Task components don't support onKeyEvent as they run on a separate thread.
     */
    fun isTaskComponent(irClass: IrClass): Boolean {
        val taskSym = taskComponentClass ?: return false
        return checkClassHierarchy(irClass) { it.symbol == taskSym }
    }

    /**
     * Checks if a class is a ContentNode component.
     *
     * ContentNode components don't support onKeyEvent as they're data containers.
     */
    fun isContentNodeComponent(irClass: IrClass): Boolean {
        val contentNodeSym = contentNodeComponentClass ?: return false
        return checkClassHierarchy(irClass) { it.symbol == contentNodeSym }
    }

    /**
     * Checks if a component needs an onKeyEvent function.
     *
     * Returns true for GroupComponent, LayoutComponent, and SceneComponent.
     * Returns false for TaskComponent and ContentNodeComponent (they don't receive key events).
     */
    fun componentNeedsOnKeyEvent(irClass: IrClass): Boolean {
        if (!isSceneGraphComponent(irClass)) return false
        if (isTaskComponent(irClass)) return false
        if (isContentNodeComponent(irClass)) return false
        return true
    }

    /**
     * Whether [irClass] gets the bare-named lifecycle entries
     * `__kotlinRetire`/`__kotlinRevive` (spec §5.6): a CONCRETE render
     * component. The ONE predicate behind both the script emission
     * (IrToBrsTransformer.generateLifecycleEntryFunctions) and the XML
     * `<function>` lines (BrsCompiler.generateComponentXmlContent), so the
     * two can never disagree. A "not task, not ContentNode, not abstract"
     * test alone is NOT enough on the XML side: a legacy `@BrsComponent`
     * plain class is extracted for XML but never reaches the component
     * transformer (transformClass gates it on isSceneGraphComponent), and
     * would advertise functions its script does not define.
     */
    fun emitsLifecycleEntries(irClass: IrClass): Boolean {
        return componentNeedsOnKeyEvent(irClass) && irClass.modality != Modality.ABSTRACT
    }

    /**
     * The nearest REAL (non-fake, non-abstract) override of member [name] that
     * also satisfies [signature], walking [irClass] and its USER component
     * supertypes. Stops — answering null — at the first stdlib base: a class
     * directly annotated @BrsSceneGraphComponent, or kotlin.brs.ComponentBase.
     * Null means nothing in the user hierarchy overrides the member and the
     * stdlib default applies.
     *
     * Why a hierarchy walk: SceneGraph runs base init() before derived init()
     * over a shared m, so an override declared in a concrete user BASE is
     * attached as a slot the leaf inherits — the leaf's generated wrappers and
     * lifecycle entries must consult the whole chain, not just the leaf
     * (the onKeyEvent leaf-wrapper shadowing defect, spec §5.5).
     */
    fun hierarchyOverrides(
        irClass: IrClass,
        name: String,
        signature: (IrSimpleFunction) -> Boolean = { true },
    ): IrSimpleFunction? {
        var current: IrClass? = irClass
        while (current != null) {
            if (current.hasAnnotation(brsSceneGraphComponentFqn)) return null
            if (current.fqNameWhenAvailable?.asString() == "kotlin.brs.ComponentBase") return null
            val found = current.declarations.filterIsInstance<IrSimpleFunction>().find {
                it.name.asString() == name &&
                    !it.isFakeOverride &&
                    it.modality != Modality.ABSTRACT &&
                    signature(it)
            }
            if (found != null) return found
            current = current.superTypes
                .mapNotNull { it.classOrNull?.owner }
                .firstOrNull { !it.isInterface }
        }
        return null
    }

    /**
     * Helper to check class hierarchy.
     */
    private fun checkClassHierarchy(irClass: IrClass, predicate: (IrClass) -> Boolean): Boolean {
        val visited = mutableSetOf<IrClass>()
        val queue = ArrayDeque<IrClass>()
        queue.add(irClass)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            if (predicate(current)) return true

            for (superType in current.superTypes) {
                val superClass = superType.classOrNull?.owner ?: continue
                queue.add(superClass)
            }
        }
        return false
    }

    /**
     * Checks if a property name is a component scope property.
     *
     * These properties compile to m.<name> instead of generating accessor calls:
     * - top -> m.top
     * - global -> m.global
     * - m -> m
     */
    fun isComponentScopeProperty(propertyName: String): Boolean {
        return propertyName in setOf("top", "global", "m")
    }

    /**
     * True for a property whose backing field is initialized from a PRIMARY
     * CONSTRUCTOR value parameter — the `class X(@SGStringField val a: String)`
     * shape. SceneGraph components are runtime-instantiated, so such a property
     * is a REQUIRED INPUT: its value arrives through the lowered constructor
     * call's field write (or a static-layout constant), never through init().
     */
    fun isConstructorParameterProperty(property: IrProperty): Boolean {
        val init = property.backingField?.initializer?.expression as? IrGetValue ?: return false
        val owner = init.symbol.owner as? IrValueParameter ?: return false
        val constructor = owner.parent as? IrConstructor ?: return false
        return constructor.isPrimary
    }

    /**
     * The class's OWN constructor inputs, in declaration order: primary-constructor-parameter
     * properties ([isConstructorParameterProperty]) that ALSO carry an interface-field
     * annotation ([hasInterfaceFieldAnnotation] — `@SG*Field` or `@BrsField`, the set that
     * gets an XML `<field>`). This is THE definition of "constructor input", shared by the
     * extractor's `requiredInputs` (XML field list + ready-marker field), the constructor-call
     * lowering's input writes (BrsComponentConstructorCallLowering), and the layout-builder
     * validation — one predicate, so a parameter can never be an input on one side and not
     * the other (a write to an undeclared node field is a silent drop on device). A plain
     * `val` parameter has no node field and is NOT an input (the init-time SKIP stays
     * annotation-agnostic on purpose: there is no local to read inside sub init() either way).
     *
     * MIRROR: checkers.brs `BrsComponentTypes.constructorInputs` / `isConstructorInput` is the
     * FIR-side copy (module boundary) — a change to what counts as an input lands in both in
     * the same commit.
     */
    fun constructorInputs(irClass: IrClass): List<IrProperty> =
        irClass.declarations.filterIsInstance<IrProperty>()
            .filter { !it.isFakeOverride && isConstructorParameterProperty(it) && hasInterfaceFieldAnnotation(it) }

    /**
     * Checks if a type implements NativeIterable.
     * Types implementing this interface support native BrightScript for-each iteration.
     */
    fun isNativeIterable(type: IrType): Boolean {
        val nativeIterableSym = nativeIterableClass ?: return false
        val classifier = type.classOrNull ?: return false

        // Check direct match
        if (classifier == nativeIterableSym) return true

        // Check supertypes recursively
        return checkSuperTypesFor(classifier, nativeIterableSym)
    }

    /**
     * Checks if a function's return type is NativeArrayIterator.
     * This signals that the type supports native BrightScript for-each iteration.
     */
    fun returnsNativeArrayIterator(function: IrSimpleFunction): Boolean {
        val nativeArrayIteratorSym = nativeArrayIteratorClass ?: return false
        val returnClassifier = function.returnType.classOrNull ?: return false
        return returnClassifier == nativeArrayIteratorSym
    }

    /**
     * Helper to check if a class symbol has a specific interface in its supertype hierarchy.
     */
    private fun checkSuperTypesFor(classSymbol: IrClassSymbol, targetInterface: IrClassSymbol): Boolean {
        val visited = mutableSetOf<IrClassSymbol>()
        val queue = ArrayDeque<IrClassSymbol>()
        queue.add(classSymbol)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            if (current == targetInterface) return true

            // Add supertypes to queue
            for (superType in current.owner.superTypes) {
                val superClassifier = superType.classOrNull ?: continue
                queue.add(superClassifier)
            }
        }
        return false
    }

    /**
     * CreateObject function for instantiating BrightScript objects.
     */
    val createObject: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            BrsStandardClassIds.Callables.createObject.callableName,
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Type() function for runtime type checking.
     */
    val typeOfFunction: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            BrsStandardClassIds.Callables.typeOf.callableName,
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Print function for debug output.
     */
    val printFunction: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            BrsStandardClassIds.Callables.print.callableName,
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Inline BrightScript code function.
     */
    val brsInline: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            BrsStandardClassIds.Callables.brs.callableName,
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Mapping from Kotlin primitive types to their BrightScript boxing constructors.
     */
    val primitiveToBoxConstructor: Map<IrClassSymbol, IrSimpleFunctionSymbol> by lazy {
        buildMap {
            // These will be populated when the stdlib is available
        }
    }

    /**
     * Mapping from Kotlin primitive types to their BrightScript array constructors.
     */
    val primitiveArrayConstructors: Map<IrClassSymbol, IrSimpleFunctionSymbol> by lazy {
        buildMap {
            // These will be populated when the stdlib is available
        }
    }

    /**
     * Check if a function is a BrightScript intrinsic that should be inlined.
     */
    fun isIntrinsic(symbol: IrSimpleFunctionSymbol): Boolean {
        return symbol == createObject ||
                symbol == typeOfFunction ||
                symbol == printFunction ||
                symbol == brsInline ||
                isStdlibIntrinsic(symbol)
    }

    private val brsIntrinsicFqn = FqName("kotlin.brs.BrsIntrinsic")

    /**
     * Check if a function is a stdlib intrinsic.
     * These are either:
     * 1. External functions with brsIntrinsic* prefix
     * 2. External functions with @BrsIntrinsic annotation
     */
    fun isStdlibIntrinsic(symbol: IrSimpleFunctionSymbol): Boolean {
        val function = symbol.owner
        if (!function.isExternal) return false
        // Check by name prefix
        if (function.name.asString().startsWith("brsIntrinsic")) return true
        // Check for @BrsIntrinsic annotation
        return function.getAnnotation(brsIntrinsicFqn) != null
    }

    /**
     * Get the intrinsic name for a function.
     * Returns the function name, or the value from @BrsIntrinsic annotation if present.
     */
    fun getIntrinsicName(symbol: IrSimpleFunctionSymbol): String {
        val function = symbol.owner
        // Check for @BrsIntrinsic annotation
        val annotation = function.getAnnotation(brsIntrinsicFqn)
        if (annotation != null) {
            val nameArg = annotation.getValueArgument(0)
            if (nameArg is IrConst) {
                return nameArg.value as String
            }
        }
        // Fall back to function name
        return function.name.asString()
    }

    /**
     * Mapping of stdlib intrinsic names to their BrightScript equivalents.
     */
    val stdlibIntrinsicMapping: Map<String, StdlibIntrinsic> = mapOf(
        // Math intrinsics
        "brsIntrinsicSin" to StdlibIntrinsic.SimpleCall("Sin"),
        "brsIntrinsicCos" to StdlibIntrinsic.SimpleCall("Cos"),
        "brsIntrinsicTan" to StdlibIntrinsic.SimpleCall("Tan"),
        "brsIntrinsicAsin" to StdlibIntrinsic.Asin,               // asin(x) = atan(x / sqrt(1 - x*x))
        "brsIntrinsicAcos" to StdlibIntrinsic.Acos,               // acos(x) = atan(sqrt(1 - x*x) / x)
        "brsIntrinsicAtan" to StdlibIntrinsic.SimpleCall("Atn"),
        "brsIntrinsicAtan2" to StdlibIntrinsic.Atan2,             // Custom handling for atan2
        "brsIntrinsicSqrt" to StdlibIntrinsic.SimpleCall("Sqr"),
        "brsIntrinsicExp" to StdlibIntrinsic.SimpleCall("Exp"),
        "brsIntrinsicLog" to StdlibIntrinsic.SimpleCall("Log"),
        "brsIntrinsicCeil" to StdlibIntrinsic.Ceil,               // Int(x) + 1 if not integer
        "brsIntrinsicFloor" to StdlibIntrinsic.Floor,             // Int(x)
        "brsIntrinsicRound" to StdlibIntrinsic.Round,             // Int(x + 0.5)
        "brsIntrinsicAbs" to StdlibIntrinsic.SimpleCall("Abs"),
        "brsIntrinsicAbsInt" to StdlibIntrinsic.SimpleCall("Abs"),
        "brsIntrinsicTrunc" to StdlibIntrinsic.SimpleCall("Fix"),  // Fix(x) truncates toward zero
        "brsIntrinsicPow" to StdlibIntrinsic.Pow,                 // x ^ y
        "brsIntrinsicSinh" to StdlibIntrinsic.Sinh,               // (Exp(x) - Exp(-x)) / 2
        "brsIntrinsicCosh" to StdlibIntrinsic.Cosh,               // (Exp(x) + Exp(-x)) / 2
        "brsIntrinsicTanh" to StdlibIntrinsic.Tanh,               // sinh/cosh

        // String intrinsics
        "brsIntrinsicUCase" to StdlibIntrinsic.SimpleCall("UCase"),
        "brsIntrinsicLCase" to StdlibIntrinsic.SimpleCall("LCase"),
        "brsIntrinsicInstr" to StdlibIntrinsic.Instr,
        "brsIntrinsicLen" to StdlibIntrinsic.SimpleCall("Len"),
        "brsIntrinsicLeft" to StdlibIntrinsic.SimpleCall("Left"),
        "brsIntrinsicRight" to StdlibIntrinsic.SimpleCall("Right"),
        "brsIntrinsicMid" to StdlibIntrinsic.SimpleCall("Mid"),
        "brsIntrinsicAsc" to StdlibIntrinsic.SimpleCall("Asc"),
        "brsIntrinsicCharCode" to StdlibIntrinsic.SimpleCall("Asc"),
        "brsIntrinsicChr" to StdlibIntrinsic.SimpleCall("Chr"),
        "brsIntrinsicVal" to StdlibIntrinsic.SimpleCall("Val"),
        "brsIntrinsicStr" to StdlibIntrinsic.SimpleCall("Str"),
        "brsIntrinsicStringI" to StdlibIntrinsic.SimpleCall("String"),
        "brsIntrinsicSubstitute" to StdlibIntrinsic.SimpleCall("Substitute"),

        // IO intrinsics
        "brsIntrinsicPrint" to StdlibIntrinsic.Print(newline = true),
        "brsIntrinsicPrintNoNewline" to StdlibIntrinsic.Print(newline = false),

        // Time intrinsics
        "brsIntrinsicCurrentTimeMillis" to StdlibIntrinsic.CurrentTimeMillis,

        // Random intrinsics
        "brsIntrinsicRnd" to StdlibIntrinsic.SimpleCall("Rnd"),
        "brsIntrinsicRandom" to StdlibIntrinsic.SimpleCall("Rnd"),
        "brsIntrinsicRandomSeed" to StdlibIntrinsic.RandomSeed,

        // JSON intrinsics
        "brsIntrinsicParseJson" to StdlibIntrinsic.SimpleCall("ParseJson"),
        "brsIntrinsicFormatJson" to StdlibIntrinsic.SimpleCall("FormatJson"),

        // Type intrinsics
        "brsIntrinsicType" to StdlibIntrinsic.SimpleCall("Type"),
        "brsIntrinsicGetGlobalAA" to StdlibIntrinsic.SimpleCall("GetGlobalAA"),

        // Node intrinsics
        "brsIntrinsicCreateObject" to StdlibIntrinsic.CreateObject,

        // Conversion intrinsics
        "brsIntrinsicToString" to StdlibIntrinsic.ToString,

        // Structural equality intrinsics
        "brsIntrinsicIsAA" to StdlibIntrinsic.IsAssociativeArray,
        "brsIntrinsicIsSGNode" to StdlibIntrinsic.IsSGNode,
        "brsIntrinsicCallEquals" to StdlibIntrinsic.CallEquals,
        "brsIntrinsicNativeEquals" to StdlibIntrinsic.NativeEquals,

        // Comparison intrinsics
        "brsIntrinsicNativeCompare" to StdlibIntrinsic.NativeCompare,
        "brsIntrinsicCallCompareTo" to StdlibIntrinsic.CallCompareTo,

        // Comparator intrinsic
        "brsIntrinsicCallComparator" to StdlibIntrinsic.CallComparator,

        // Direct method call intrinsic
        "brsIntrinsicGetLength" to StdlibIntrinsic.GetLength,

        // Function name intrinsic
        "brsIntrinsicFunctionName" to StdlibIntrinsic.FunctionName,

        // Core utility intrinsics
        "brsIntrinsicSleep" to StdlibIntrinsic.SimpleCall("Sleep"),
        "brsIntrinsicWait" to StdlibIntrinsic.SimpleCall("Wait"),
        "brsIntrinsicUpTime" to StdlibIntrinsic.UpTime,
        "brsIntrinsicGetInterface" to StdlibIntrinsic.SimpleCall("GetInterface"),
        "brsIntrinsicFindMemberFunction" to StdlibIntrinsic.SimpleCall("FindMemberFunction"),

        // File system intrinsics
        "brsIntrinsicListDir" to StdlibIntrinsic.SimpleCall("ListDir"),
        "brsIntrinsicReadAsciiFile" to StdlibIntrinsic.SimpleCall("ReadAsciiFile"),
        "brsIntrinsicWriteAsciiFile" to StdlibIntrinsic.SimpleCall("WriteAsciiFile"),
        "brsIntrinsicCopyFile" to StdlibIntrinsic.SimpleCall("CopyFile"),
        "brsIntrinsicMoveFile" to StdlibIntrinsic.SimpleCall("MoveFile"),
        "brsIntrinsicDeleteFile" to StdlibIntrinsic.SimpleCall("DeleteFile"),
        "brsIntrinsicDeleteDirectory" to StdlibIntrinsic.SimpleCall("DeleteDirectory"),
        "brsIntrinsicCreateDirectory" to StdlibIntrinsic.SimpleCall("CreateDirectory"),
        "brsIntrinsicMatchFiles" to StdlibIntrinsic.SimpleCall("MatchFiles"),
        "brsIntrinsicFormatDrive" to StdlibIntrinsic.SimpleCall("FormatDrive"),

        // String conversion intrinsics
        "brsIntrinsicStrToI" to StdlibIntrinsic.SimpleCall("StrToI"),

        // System intrinsics
        "brsIntrinsicRunGarbageCollector" to StdlibIntrinsic.SimpleCall("RunGarbageCollector"),
        "brsIntrinsicTr" to StdlibIntrinsic.SimpleCall("Tr"),
        "brsIntrinsicRebootSystem" to StdlibIntrinsic.SimpleCall("RebootSystem")
    )

    /**
     * Represents different types of stdlib intrinsics and how they should be generated.
     */
    sealed class StdlibIntrinsic {
        /** Simple function call: brsIntrinsicSin(x) -> Sin(x) */
        data class SimpleCall(val brsName: String) : StdlibIntrinsic()

        /** Print with optional newline */
        data class Print(val newline: Boolean) : StdlibIntrinsic()

        /** Power operation: x ^ y */
        data object Pow : StdlibIntrinsic()

        /** Atan2 operation */
        data object Atan2 : StdlibIntrinsic()

        /** Asin: asin(x) = atan(x / sqrt(1 - x*x)) */
        data object Asin : StdlibIntrinsic()

        /** Acos: acos(x) = atan(sqrt(1 - x*x) / x) with quadrant adjustment */
        data object Acos : StdlibIntrinsic()

        /** Ceiling: Int(x) + (1 if x > Int(x) else 0) */
        data object Ceil : StdlibIntrinsic()

        /** Floor: Int(x) */
        data object Floor : StdlibIntrinsic()

        /** Round: Int(x + 0.5) */
        data object Round : StdlibIntrinsic()

        /** Sinh: (Exp(x) - Exp(-x)) / 2 */
        data object Sinh : StdlibIntrinsic()

        /** Cosh: (Exp(x) + Exp(-x)) / 2 */
        data object Cosh : StdlibIntrinsic()

        /** Tanh: sinh/cosh formula */
        data object Tanh : StdlibIntrinsic()

        /** Get current time in milliseconds using roTimespan */
        data object CurrentTimeMillis : StdlibIntrinsic()

        /** Seed the random generator */
        data object RandomSeed : StdlibIntrinsic()

        /** CreateObject with type and optional args */
        data object CreateObject : StdlibIntrinsic()

        /** Type-aware toString conversion for Any? */
        data object ToString : StdlibIntrinsic()

        /** Check if value is roAssociativeArray: Type(a) = "roAssociativeArray" */
        data object IsAssociativeArray : StdlibIntrinsic()

        /** Check if value is roSGNode: Type(a) = "roSGNode" */
        data object IsSGNode : StdlibIntrinsic()

        /** Call equals method on object: a.equals(b) */
        data object CallEquals : StdlibIntrinsic()

        /** Native BrightScript equals: a = b */
        data object NativeEquals : StdlibIntrinsic()

        /** Native BrightScript comparison: returns -1, 0, or 1 using < and > */
        data object NativeCompare : StdlibIntrinsic()

        /** Call compareTo method on object: a.compareTo(b) */
        data object CallCompareTo : StdlibIntrinsic()

        /** Call Comparator's compare method: comparator.compare_AnyN_AnyN_k_(a, b) */
        data object CallComparator : StdlibIntrinsic()

        /** Instr with index conversion: 0-based Kotlin to 1-based BrightScript */
        data object Instr : StdlibIntrinsic()

        /** Call get_length() directly on an object: obj.get_length() */
        data object GetLength : StdlibIntrinsic()

        /** Extract the mangled BrightScript name from a function reference */
        data object FunctionName : StdlibIntrinsic()

        /** UpTime requires a dummy integer argument per BrightScript docs */
        data object UpTime : StdlibIntrinsic()
    }

    /**
     * BrightScript equals function for structural equality.
     */
    val brsEquals: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            org.jetbrains.kotlin.name.Name.identifier("brsEquals"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * BrightScript hashCode function.
     */
    val brsHashCode: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            org.jetbrains.kotlin.name.Name.identifier("brsHashCode"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * BrightScript toString function.
     */
    val brsToString: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            org.jetbrains.kotlin.name.Name.identifier("brsToString"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * BrightScript array literal constructor.
     */
    val arrayLiteral: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            org.jetbrains.kotlin.name.Name.identifier("arrayOf"),
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * BrightScript associative array literal constructor.
     */
    val aaLiteral: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            org.jetbrains.kotlin.name.Name.identifier("aaOf"),
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    // =============================================================================
    // SceneGraph Layout DSL Support
    // =============================================================================

    /**
     * FqName for the @SGLayout annotation.
     */
    private val sgLayoutFqn = FqName("kotlin.brs.scenegraph.SGLayout")

    /**
     * Check if a property has the @SGLayout annotation.
     */
    fun hasSGLayoutAnnotation(property: org.jetbrains.kotlin.ir.declarations.IrProperty): Boolean {
        return property.hasAnnotation(sgLayoutFqn)
    }

    /**
     * Check if a function has the @SGLayout annotation.
     */
    fun hasSGLayoutAnnotation(function: IrSimpleFunction): Boolean {
        return function.hasAnnotation(sgLayoutFqn)
    }

    companion object {
        /**
         * The interface-field annotations: the `@SG*Field` family
         * ([BrsStandardClassIds.Annotations.sgFieldAnnotationTypes]) plus legacy `@BrsField` —
         * exactly the properties BrsComponentExtractor turns into XML `<field>` declarations,
         * and therefore the properties whose writes live on the NODE (`m.top.<field>`).
         *
         * MIRROR: checkers.brs `BrsComponentTypes.fieldAnnotationIds` is the FIR-side copy of
         * this set (module boundary) — keep them identical in the same commit.
         */
        val interfaceFieldAnnotationIds: Set<ClassId> =
            BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys + BrsStandardClassIds.Annotations.BrsField

        /**
         * True for a property carrying one of [interfaceFieldAnnotationIds] — a SceneGraph
         * interface field (it has a node field; its backing-field writes route to `.top`).
         */
        fun hasInterfaceFieldAnnotation(property: IrProperty): Boolean =
            property.annotations.any { annotation ->
                val id = annotation.type.classOrNull?.owner?.classId
                id != null && id in interfaceFieldAnnotationIds
            }
    }
}
