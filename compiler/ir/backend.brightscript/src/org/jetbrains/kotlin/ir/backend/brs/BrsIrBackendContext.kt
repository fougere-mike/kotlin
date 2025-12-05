/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLowerings
import org.jetbrains.kotlin.backend.common.lower.InnerClassesSupport
import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.RokuOSVersion
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInnerClassesSupport
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsSharedVariablesManager
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.util.getAnnotation
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
    val isStdlibCompilation: Boolean = false
) : CommonBackendContext {

    // ==================== Type System ====================

    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override val irFactory: IrFactory = symbolTable.irFactory

    // ==================== Verbose Mode ====================

    override var inVerbosePhase: Boolean = false

    // ==================== Intrinsics and Symbols ====================

    val intrinsics: BrsIntrinsics = BrsIntrinsics(irBuiltIns)

    val symbols: BrsSymbols = BrsSymbols(irBuiltIns, intrinsics, isStdlibCompilation)

    override val ir = object : Ir() {
        override val symbols = this@BrsIrBackendContext.symbols
        override fun shouldGenerateHandlerParameterForDefaultBodyFun() = true
    }

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

    // ==================== Name Mangling ====================

    /**
     * Suffix for mangled function names to prevent conflicts with user-defined names.
     */
    private val MANGLED_NAME_SUFFIX = "_k$"

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

    override val mapping: BrsMapping = BrsMapping()

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

                    // Include type arguments to distinguish e.g. Sequence<Sequence<T>> from Sequence<Iterable<T>>
                    val simpleType = this as? org.jetbrains.kotlin.ir.types.IrSimpleType
                    val typeArgs = simpleType?.arguments?.map { arg ->
                        when (arg) {
                            is org.jetbrains.kotlin.ir.types.IrTypeProjection -> arg.type.toMangledString()
                            is org.jetbrains.kotlin.ir.types.IrStarProjection -> "Star"
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
     * Calculate the mangled function signature.
     * Functions with parameters, extension receivers, or non-Unit return types get type info appended
     * to prevent overload conflicts.
     *
     * @param irFunction The function to calculate signature for
     * @param baseName The base name (e.g., "ClassName_functionName" or "ClassName_create")
     * @return The mangled name, or baseName if no mangling is needed
     */
    private fun calculateBrsFunctionSignature(irFunction: IrFunction, baseName: String): String {
        val signatureParts = mutableListOf<String>()

        // Include extension receiver type if present (for extension functions)
        if (irFunction is IrSimpleFunction) {
            irFunction.extensionReceiverParameter?.let { receiver ->
                signatureParts.add("r" + receiver.type.toMangledString())
            }
        }

        // Include value parameter types
        irFunction.valueParameters.forEach { param ->
            signatureParts.add(param.type.toMangledString())
        }

        // Include return type for non-Unit functions to distinguish overloads that differ only by return type
        // (e.g., Iterable<Int>.sum(): Int vs Iterable<Long>.sum(): Long after type erasure)
        val returnType = irFunction.returnType
        val returnTypeStr = if (!returnType.isUnit() && !returnType.isNothing()) {
            returnType.toMangledString()
        } else {
            null
        }

        // No parameters, extension receiver, or return type = no mangling needed
        if (signatureParts.isEmpty() && returnTypeStr == null) {
            return baseName
        }

        // Build signature string
        val paramTypes = signatureParts.joinToString("_")
        val fullSignature = if (returnTypeStr != null) {
            if (paramTypes.isEmpty()) returnTypeStr else "${paramTypes}_$returnTypeStr"
        } else {
            paramTypes
        }

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

        // 2. Handle constructors - generate ClassName_create with mangling
        if (irFunction is IrConstructor) {
            val irClass = irFunction.parent as IrClass
            val className = getBrsName(irClass)
            return calculateBrsFunctionSignature(irFunction, "${className}_create")
        }

        // 3. Regular functions - calculate base name then apply mangling
        val rawName = irFunction.name.asString()
        // Sanitize property accessor names: <get-foo> -> get_foo, <set-foo> -> set_foo
        val sanitizedName = when {
            rawName.startsWith("<get-") && rawName.endsWith(">") -> {
                "get_" + rawName.removePrefix("<get-").removeSuffix(">")
            }
            rawName.startsWith("<set-") && rawName.endsWith(">") -> {
                "set_" + rawName.removePrefix("<set-").removeSuffix(">")
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
}

/**
 * Mapping data for BrightScript code generation.
 */
class BrsMapping : org.jetbrains.kotlin.backend.common.Mapping() {
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
}
