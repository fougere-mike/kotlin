/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLowerings
import org.jetbrains.kotlin.backend.common.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.backend.common.lower.InnerClassesSupport
import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.RokuOSVersion
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInnerClassesSupport
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsSharedVariablesManager
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.FqName
import java.util.*
import kotlin.math.abs

/**
 * Backend context for BrightScript code generation.
 *
 * This class provides access to all the infrastructure needed for lowering
 * Kotlin IR to BrightScript AST.
 *
 * @param isStdlibCompilation When true, the compiler is compiling the stdlib itself.
 *        This affects symbol resolution - missing stdlib symbols are handled gracefully
 *        instead of throwing errors, since they're being defined rather than linked.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
class BrsIrBackendContext(
    val module: ModuleDescriptor,
    override val irBuiltIns: IrBuiltIns,
    val symbolTable: SymbolTable,
    override val configuration: CompilerConfiguration,
    val targetConfig: BrsTargetConfig = BrsTargetConfig.DEFAULT,
    val isStdlibCompilation: Boolean = false,
    /**
     * Function-to-file manifest loaded from klib dependencies.
     * Maps BrightScript function names to their .brs output files.
     * Used by IrToBrsTransformer to resolve dependencies accurately.
     */
    val dependencyFunctionManifest: Map<String, String> = emptyMap(),
    /**
     * File-to-file dependency graph loaded from klib dependencies.
     * Maps .brs file names to the set of .brs files they depend on.
     * Used by BrsCompiler to resolve transitive dependencies.
     */
    val dependencyFileDeps: Map<String, Set<String>> = emptyMap()
) : CommonBackendContext {

    // ==================== Type System ====================

    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override val irFactory: IrFactory = symbolTable.irFactory

    // ==================== Verbose Mode ====================

    override var inVerbosePhase: Boolean = false

    // ==================== Intrinsics and Symbols ====================

    val intrinsics: BrsIntrinsics = BrsIntrinsics(irBuiltIns)

    val brsSymbols: BrsSymbols = BrsSymbols(irBuiltIns, intrinsics, isStdlibCompilation)

    override val symbols: BrsSymbols = brsSymbols

    override val shouldGenerateHandlerParameterForDefaultBodyFun: Boolean
        get() = true

    // ==================== Package Fragments ====================

    val externalPackageFragment: MutableMap<org.jetbrains.kotlin.ir.symbols.IrFileSymbol, IrFile> = mutableMapOf()

    val additionalExportedDeclarations: MutableSet<IrDeclaration> = hashSetOf()

    val bodilessBuiltInsPackageFragment: IrPackageFragment = IrExternalPackageFragmentImpl(
        DescriptorlessExternalPackageFragmentSymbol(),
        FqName("kotlin")
    )

    // ==================== Internal Package ====================

    val internalPackageFqn: FqName = BrsStandardClassIds.BASE_BRS_PACKAGE

    // ==================== Annotation FqNames ====================

    private val brsNameFqn = FqName("kotlin.brs.BrsName")
    private val brsExternalFqn = FqName("kotlin.brs.BrsExternal")
    private val brsStaticFqn = FqName("kotlin.brs.BrsStatic")

    // ==================== Name Mangling ====================

    /**
     * Suffix for mangled function names to prevent conflicts with user-defined names.
     * Note: BrightScript does not allow $ in identifiers, so we use _ instead.
     */
    private val MANGLED_NAME_SUFFIX = "_k_"

    // ==================== Exception Handling ====================

    /**
     * The catch-all throwable type.
     * In BrightScript, this is the roAssociativeArray used for exception objects.
     */
    val catchAllThrowableType: IrType
        get() = irBuiltIns.anyNType // Will be mapped to Dynamic/AA in BrightScript

    // ==================== Inner Classes Support ====================

    override val innerClassesSupport: InnerClassesSupport by lazy {
        BrsInnerClassesSupport(irFactory)
    }

    // ==================== Shared Variables ====================

    override val sharedVariablesManager: SharedVariablesManager by lazy {
        BrsSharedVariablesManager(irBuiltIns)
    }

    // ==================== Partial Linkage ====================

    override val partialLinkageSupport = createPartialLinkageSupportForLowerings(
        configuration.partialLinkageConfig,
        irBuiltIns,
        configuration.messageCollector
    )

    // ==================== Mapping and Caching ====================

    val mapping: BrsMapping = BrsMapping()

    /**
     * Dependency collector for tracking which files call functions from other files.
     * Used to generate accurate deps.json for SceneGraph components.
     */
    val dependencyCollector: BrsDependencyCollector = BrsDependencyCollector()

    /**
     * Cache for generated class names.
     */
    val classNameCache = WeakHashMap<IrClass, String>()

    /**
     * Cache for generated function names.
     */
    val functionNameCache = WeakHashMap<IrFunction, String>()

    /**
     * Classes that need to be generated as SceneGraph components.
     */
    val componentClasses = mutableSetOf<IrClass>()

    /**
     * Files that contain exported declarations.
     */
    val exportedFiles = mutableSetOf<IrFile>()

    /**
     * Map from component classes to their field observers.
     * Key is field name, value is the observer function.
     */
    val componentFieldObservers = mutableMapOf<IrClassSymbol, Map<String, IrFunction>>()

    /**
     * Map from @BrsInline function symbols to their parsed inline info.
     * Populated by BrsCodeOutliningLowering, used during code generation.
     */
    val inlineFunctionInfo = mutableMapOf<IrFunctionSymbol, Any>()

    /**
     * Map from IrCall expressions to their resolved namespace-style function names.
     * Populated by BrsExternalLowering for @BrsNamespace calls.
     * Key: IrCall expression, Value: resolved name like "Utils_getMessage"
     */
    val namespaceCallNames = mutableMapOf<IrElement, String>()

    /**
     * Map from function symbols to their shared variables (mutable vars captured by closures).
     * Populated by BrsSharedVariableDetectionLowering BEFORE local class extraction,
     * used during transformation to properly box these variables.
     */
    val sharedVariablesByFunction = mutableMapOf<IrFunctionSymbol, Set<org.jetbrains.kotlin.ir.symbols.IrValueSymbol>>()

    /**
     * Set of class-field combinations where the field holds a shared variable box.
     * When accessing these fields, we need to use .value to get/set the actual value.
     * Key format: "ClassName.fieldName"
     * Populated by BrsSharedVariableDetectionLowering.
     */
    val sharedVariableFields = mutableSetOf<String>()

    // ==================== Target Configuration ====================

    /**
     * Minimum Roku OS version being targeted.
     */
    val minRokuOS: RokuOSVersion
        get() = targetConfig.minRokuOS

    /**
     * Whether try/catch/throw is available on the target.
     */
    val supportsExceptions: Boolean
        get() = targetConfig.supportsExceptions()

    // ==================== Runtime Helpers ====================

    /**
     * Whether runtime helper functions need to be generated.
     * This is set to true initially and becomes false after helpers are added to the first file.
     */
    var needsRuntimeHelpers: Boolean = true

    /**
     * Whether continue statement is available on the target.
     */
    val supportsContinue: Boolean
        get() = targetConfig.supportsContinue()

    /**
     * Whether debug mode is enabled.
     */
    val debugMode: Boolean
        get() = targetConfig.debugMode

    // ==================== Utility Methods ====================

    /**
     * Get the BrightScript name for a class.
     */
    fun getBrsName(irClass: IrClass): String {
        return classNameCache.getOrPut(irClass) {
            generateBrsClassName(irClass)
        }
    }

    /**
     * Get the BrightScript name for a function.
     */
    fun getBrsName(irFunction: IrFunction): String {
        return functionNameCache.getOrPut(irFunction) {
            generateBrsFunctionName(irFunction)
        }
    }

    private fun generateBrsClassName(irClass: IrClass): String {
        // Check for @BrsName annotation and extract its value
        val brsNameAnnotation = irClass.getAnnotation(brsNameFqn)
        if (brsNameAnnotation != null) {
            val nameArg = brsNameAnnotation.getValueArgument(0)
            if (nameArg is IrConst) {
                return nameArg.value.toString()
            }
        }

        // Fallback: use the simple name with proper mangling
        val rawName = irClass.name.asString()

        // Handle anonymous classes (e.g., "<no name provided>" from object expressions)
        val baseName = if (rawName.startsWith("<") && rawName.endsWith(">")) {
            // Generate unique name using hash code
            "Anon_${kotlin.math.abs(irClass.hashCode()).toString(16)}"
        } else {
            rawName
        }

        val parent = irClass.parent

        return when (parent) {
            is IrClass -> "${getBrsName(parent)}_${baseName}"
            is IrPackageFragment -> baseName
            else -> baseName
        }
    }

    /**
     * Public wrapper to get a mangled string representation of a type.
     * Used by transformers for generating function names.
     */
    fun typeToMangledString(type: IrType): String = type.toMangledString()

    /**
     * Convert an IrType to a short string suitable for name mangling.
     * Uses JVM-style type descriptors for primitives with readable names for classes.
     */
    private fun IrType.toMangledString(): String = when {
        isUnit() -> "V"
        isBoolean() -> "Z"
        isByte() -> "B"
        isShort() -> "S"
        isInt() -> "I"
        isLong() -> "J"
        isFloat() -> "F"
        isDouble() -> "D"
        isChar() -> "C"
        isString() -> "Str"
        isNullable() -> makeNotNull().toMangledString() + "N"
        isArray() -> "Arr"
        else -> {
            val classifier = classifierOrNull?.owner
            when (classifier) {
                is IrClass -> {
                    val name = classifier.name.asString()
                    // Handle anonymous class names like "<no name provided>"
                    val baseName = if (name.startsWith("<") && name.endsWith(">")) {
                        "Anon"
                    } else {
                        name
                    }

                    // Type erasure: Only erase TYPE PARAMETERS (like T, K, V), keep concrete types
                    // This allows Sequence<Int>.sum() and Sequence<Short>.sum() to have different names,
                    // while Sequence<T> becomes just "Sequence" for interface method matching.
                    val simpleType = this as? org.jetbrains.kotlin.ir.types.IrSimpleType
                    val typeArgs = simpleType?.arguments?.mapNotNull { arg ->
                        when (arg) {
                            is org.jetbrains.kotlin.ir.types.IrTypeProjection -> {
                                // Check if this type argument is a type parameter or a concrete type
                                val argClassifier = arg.type.classifierOrNull?.owner
                                if (argClassifier is org.jetbrains.kotlin.ir.declarations.IrTypeParameter) {
                                    // Erase type parameters - they vary at runtime
                                    null
                                } else {
                                    // Keep concrete types - they're stable
                                    arg.type.toMangledString()
                                }
                            }
                            is org.jetbrains.kotlin.ir.types.IrStarProjection -> null // Erase star projections
                        }
                    } ?: emptyList()

                    if (typeArgs.isNotEmpty()) {
                        "$baseName${typeArgs.joinToString("")}"
                    } else {
                        baseName
                    }
                }
                else -> "Any"
            }
        }
    }

    /**
     * Find the original interface method that this function overrides.
     * Returns null if the function doesn't override an interface method with generic type parameters.
     *
     * This is used to ensure that implementing classes use the same method name as the interface,
     * with type parameters erased, so that callers using the interface type can find the method.
     */
    private fun findOverriddenInterfaceMethod(irFunction: IrSimpleFunction): IrSimpleFunction? {
        if (irFunction.overriddenSymbols.isEmpty()) return null

        // Walk up the override chain to find an interface method
        for (overriddenSymbol in irFunction.overriddenSymbols) {
            val overridden = overriddenSymbol.owner
            val parent = overridden.parent

            if (parent is IrClass && parent.isInterface) {
                // Found an interface method - check if it has type parameters in its signature
                // that would cause different mangling
                val hasTypeParameterInSignature = overridden.valueParameters.any { param ->
                    hasTypeParameter(param.type)
                } || (overridden.extensionReceiverParameter?.let { hasTypeParameter(it.type) } == true)

                if (hasTypeParameterInSignature) {
                    return overridden
                }
            }

            // Recursively check overridden methods
            val result = findOverriddenInterfaceMethod(overridden)
            if (result != null) return result
        }

        return null
    }

    /**
     * Check if a type contains any type parameters (that would be erased differently
     * than concrete types in the implementing class).
     */
    private fun hasTypeParameter(type: IrType): Boolean {
        val simpleType = type as? org.jetbrains.kotlin.ir.types.IrSimpleType ?: return false

        // Check if the type itself is a type parameter
        if (simpleType.classifierOrNull?.owner is org.jetbrains.kotlin.ir.declarations.IrTypeParameter) {
            return true
        }

        // Check type arguments
        return simpleType.arguments.any { arg ->
            when (arg) {
                is org.jetbrains.kotlin.ir.types.IrTypeProjection -> hasTypeParameter(arg.type)
                is org.jetbrains.kotlin.ir.types.IrStarProjection -> true // Star projections are type parameters
            }
        }
    }

    /**
     * Calculate the mangled function signature.
     * Functions with parameters, extension receivers, or non-Unit return types get type info appended
     * to prevent overload conflicts.
     *
     * IMPORTANT: When a function overrides an interface method with generic type parameters,
     * we use the interface method's parameter types (with type parameters erased) rather than
     * the implementation's specialized types. This ensures that callers using the interface
     * type can find the method.
     *
     * Example: class MyClass : Continuation<Int> { override fun resumeWith(result: Result<Int>) }
     * The method name should be resumeWith_RESULT_k_ (erased), not resumeWith_RESULTI_k_ (specialized),
     * because callers using Continuation<T>.resumeWith() expect the erased signature.
     *
     * @param irFunction The function to calculate signature for
     * @param baseName The base name (e.g., "ClassName_functionName" or "ClassName_create")
     * @return The mangled name, or baseName if no mangling is needed
     */
    private fun calculateBrsFunctionSignature(irFunction: IrFunction, baseName: String): String {
        val signatureParts = mutableListOf<String>()

        // Check if this function overrides an interface method with generic type parameters.
        // If so, use the interface method's parameter types to ensure callers can find it.
        val interfaceMethod = if (irFunction is IrSimpleFunction) {
            findOverriddenInterfaceMethod(irFunction)
        } else null

        // Use interface method's parameters if available, otherwise use this function's
        val parametersToMangle = interfaceMethod?.valueParameters ?: irFunction.valueParameters
        val extensionReceiver = if (interfaceMethod != null) {
            interfaceMethod.extensionReceiverParameter
        } else {
            (irFunction as? IrSimpleFunction)?.extensionReceiverParameter
        }

        // Include extension receiver type if present (for extension functions)
        extensionReceiver?.let { receiver ->
            signatureParts.add("r" + receiver.type.toMangledString())
        }

        // Include value parameter types
        parametersToMangle.forEach { param ->
            signatureParts.add(param.type.toMangledString())
        }

        // Return types are NOT included in method name mangling (like Java does).
        // This allows polymorphic dispatch to work correctly when the static type
        // differs from the runtime type (e.g., Collection vs ArrayList).
        // Kotlin/Java don't allow overloading by return type anyway.

        // Special case: main() must not have the _k_ suffix - Roku requires exactly "main"
        if (baseName == "main" && signatureParts.isEmpty()) {
            return baseName
        }

        // Always add _k_ suffix for consistency, even for parameterless functions
        if (signatureParts.isEmpty()) {
            return "${baseName}${MANGLED_NAME_SUFFIX}"
        }

        // Build signature string from parameters only
        val fullSignature = signatureParts.joinToString("_")

        // Check if the resulting name would be too long (keep under 100 chars for readability)
        val fullName = "${baseName}_${fullSignature}${MANGLED_NAME_SUFFIX}"
        return if (fullName.length > 100) {
            // Use hash for very long signatures
            val hash = abs(fullSignature.hashCode()).toString(Character.MAX_RADIX)
            "${baseName}_${hash}${MANGLED_NAME_SUFFIX}"
        } else {
            fullName
        }
    }

    private fun generateBrsFunctionName(irFunction: IrFunction): String {
        // 1. @BrsName annotation takes precedence - no mangling
        val brsNameAnnotation = irFunction.getAnnotation(brsNameFqn)
        if (brsNameAnnotation != null) {
            val nameArg = brsNameAnnotation.getValueArgument(0)
            if (nameArg is IrConst) {
                return nameArg.value.toString()
            }
        }

        // 2. @BrsStatic - use raw name without mangling (but with parent prefix for objects)
        val brsStaticAnnotation = irFunction.getAnnotation(brsStaticFqn)
        if (brsStaticAnnotation != null) {
            val rawName = irFunction.name.asString()
            // For object/companion object members, include parent name prefix
            return when (val parent = irFunction.parent) {
                is IrClass -> {
                    if (parent.isCompanion) {
                        // companion object of Foo -> Foo_functionName
                        val outerClass = parent.parent as? IrClass
                        if (outerClass != null) {
                            "${getBrsName(outerClass)}_$rawName"
                        } else {
                            rawName
                        }
                    } else if (parent.kind == ClassKind.OBJECT) {
                        // singleton object Foo -> Foo_functionName
                        "${getBrsName(parent)}_$rawName"
                    } else {
                        // Regular class member - shouldn't happen (validation should catch this)
                        rawName
                    }
                }
                is IrPackageFragment -> rawName  // top-level
                else -> rawName
            }
        }

        // 3. Handle constructors - generate ClassName_create with mangling
        if (irFunction is IrConstructor) {
            val irClass = irFunction.parent as IrClass
            val className = getBrsName(irClass)
            return calculateBrsFunctionSignature(irFunction, "${className}_create")
        }

        // 4. Regular functions - calculate base name then apply mangling
        val rawName = irFunction.name.asString()
        // Sanitize property accessor names: <get-foo> -> __get_foo, <set-foo> -> __set_foo
        // We use "__get_" and "__set_" prefixes (double underscore) to clearly distinguish
        // compiler-generated property accessors from user-defined functions, following
        // common conventions for internal/generated names.
        val sanitizedName = when {
            rawName.startsWith("<get-") && rawName.endsWith(">") -> {
                "__get_" + rawName.removePrefix("<get-").removeSuffix(">")
            }
            rawName.startsWith("<set-") && rawName.endsWith(">") -> {
                "__set_" + rawName.removePrefix("<set-").removeSuffix(">")
            }
            else -> rawName
        }

        val baseName = when (val parent = irFunction.parent) {
            is IrClass -> "${getBrsName(parent)}_${sanitizedName}"
            is IrPackageFragment -> sanitizedName
            else -> sanitizedName
        }

        return calculateBrsFunctionSignature(irFunction, baseName)
    }

    /**
     * Check if a class should be generated as a SceneGraph component.
     */
    fun isComponent(irClass: IrClass): Boolean {
        return irClass in componentClasses ||
                irClass.annotations.any { annotation ->
                    annotation.type.classifierOrNull?.let { classifier ->
                        (classifier.owner as? IrClass)?.name?.asString() == "BrsComponent"
                    } ?: false
                }
    }

    /**
     * Register a class as a SceneGraph component.
     */
    fun registerComponent(irClass: IrClass) {
        componentClasses.add(irClass)
    }

    // ==================== Enum Constant Optimization ====================

    /**
     * Get the ordinal value for an enum entry if known.
     * Returns null if the entry has not been processed yet.
     */
    fun getEnumOrdinal(entry: IrEnumEntry): Int? = mapping.enumEntryOrdinals[entry]

    /**
     * Get the name for an enum entry if known.
     * Returns null if the entry has not been processed yet.
     */
    fun getEnumName(entry: IrEnumEntry): String? = mapping.enumEntryNames[entry]

    /**
     * Get constant property values for an enum entry if known.
     * Returns null if the entry has no constant properties or hasn't been processed.
     */
    fun getEnumConstantProperties(entry: IrEnumEntry): Map<String, Any?>? =
        mapping.enumEntryConstantProperties[entry]

    // ==================== @BrsConstant Object Optimization ====================

    /**
     * Check if a class is marked with @BrsConstant.
     * These objects are not emitted to BrightScript - their properties are inlined at usage sites.
     */
    fun isConstantObject(irClass: IrClass): Boolean = irClass in mapping.constantObjectClasses

    /**
     * Get the evaluated constant value for a property in a @BrsConstant object.
     * Returns null if not a constant object or property not found.
     */
    fun getConstantObjectProperty(irClass: IrClass, propertyName: String): IrConst? =
        mapping.constantObjectProperties[irClass]?.get(propertyName)

    // ==================== Error Reporting ====================

    /**
     * Report an error on an IR element.
     */
    fun reportError(element: IrElement, message: String) {
        val file = (element as? IrDeclaration)?.let { it.parent as? IrFile }
        val location = if (file != null) element.getCompilerMessageLocation(file) else null
        messageCollector.report(
            org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR,
            message,
            location
        )
    }
}

/**
 * Mapping data for BrightScript code generation.
 */
class BrsMapping {
    /**
     * Map from IR classes to their generated field data.
     */
    val classToFieldData = WeakHashMap<IrClass, MutableMap<IrField, String>>()

    /**
     * Map from IR functions to their parameter names.
     */
    val functionToParamNames = WeakHashMap<IrFunction, List<String>>()

    /**
     * Map from outer classes to their inner class constructors.
     */
    val outerThisFieldSymbols = WeakHashMap<IrClass, org.jetbrains.kotlin.ir.symbols.IrFieldSymbol>()

    /**
     * Map from inner classes to their inner class constructors.
     */
    val innerClassConstructors = WeakHashMap<IrClass, IrConstructor>()

    /**
     * Map from original properties to their backing fields.
     */
    val backingFields = WeakHashMap<IrProperty, IrField>()

    // ==================== Enum Constant Optimization ====================

    /**
     * Map from enum entries to their ordinal values.
     * Populated during enum class transformation for constant inlining.
     */
    val enumEntryOrdinals = WeakHashMap<IrEnumEntry, Int>()

    /**
     * Map from enum entries to their name strings.
     * Populated during enum class transformation for constant inlining.
     */
    val enumEntryNames = WeakHashMap<IrEnumEntry, String>()

    /**
     * Map from enum entries to their constant property values.
     * Only populated for val properties with IrConst initializers.
     * Key is property name, value is the constant value.
     */
    val enumEntryConstantProperties = WeakHashMap<IrEnumEntry, Map<String, Any?>>()

    // ==================== @BrsConstant Object Tracking ====================

    /**
     * Map from @BrsConstant object classes to their evaluated constant property values.
     * Populated during BrsConstantEvaluationLowering, used during code generation for inlining.
     * Key: IrClass of the @BrsConstant object
     * Value: Map of property name to its evaluated constant value (as IrConst)
     */
    val constantObjectProperties = WeakHashMap<IrClass, Map<String, IrConst>>()

    /**
     * Set of @BrsConstant object classes.
     * These objects are not emitted to BrightScript - their properties are inlined at usage sites.
     */
    val constantObjectClasses = mutableSetOf<IrClass>()
}

/**
 * Collects dependency information during IR-to-BrightScript transformation.
 *
 * This tracks which .brs files are called from each source file, enabling
 * accurate dependency injection for SceneGraph components.
 */
class BrsDependencyCollector {
    /**
     * Map: source file path → set of dependency .brs file names.
     * Records which output files are called from each source file.
     */
    private val fileDependencies = mutableMapOf<String, MutableSet<String>>()

    /**
     * Map: source file path → set of runtime functions used.
     * Records __kotlin_* functions called from each source file.
     */
    private val fileRuntimeFunctions = mutableMapOf<String, MutableSet<String>>()

    /**
     * Record a dependency from one file to another.
     *
     * @param fromFile The source file making the call (full path)
     * @param toBrsFile The dependency .brs file name (e.g., "ArrayList.brs")
     */
    fun recordDependency(fromFile: String, toBrsFile: String) {
        // Don't record self-dependencies
        val fromFileName = java.io.File(fromFile).nameWithoutExtension + "Kt.brs"
        if (fromFileName != toBrsFile) {
            fileDependencies.getOrPut(fromFile) { mutableSetOf() }.add(toBrsFile)
        }
    }

    /**
     * Record usage of a runtime helper function.
     *
     * @param fromFile The source file using the runtime function (full path)
     * @param functionName The runtime function name (e.g., "__kotlin_nextObjectId")
     */
    fun recordRuntimeFunction(fromFile: String, functionName: String) {
        fileRuntimeFunctions.getOrPut(fromFile) { mutableSetOf() }.add(functionName)
    }

    /**
     * Get all dependencies for a file.
     *
     * @param filePath The source file path
     * @return Set of .brs file names this file depends on
     */
    fun getDependencies(filePath: String): Set<String> =
        fileDependencies[filePath] ?: emptySet()

    /**
     * Get all runtime functions used by a file.
     *
     * @param filePath The source file path
     * @return Set of __kotlin_* function names used
     */
    fun getRuntimeFunctions(filePath: String): Set<String> =
        fileRuntimeFunctions[filePath] ?: emptySet()

    /**
     * Get the complete file dependency graph for all files in this compilation.
     * Keys are .brs filenames (not full paths), values are sets of dependency .brs filenames.
     * Used to resolve transitive dependencies for user code files.
     */
    fun getAllFileDependencies(): Map<String, Set<String>> {
        return fileDependencies.entries.associate { (fullPath, deps) ->
            java.io.File(fullPath).nameWithoutExtension + "Kt.brs" to deps.toSet()
        }
    }

    /**
     * Clear all collected dependency data.
     */
    fun clear() {
        fileDependencies.clear()
        fileRuntimeFunctions.clear()
    }
}
