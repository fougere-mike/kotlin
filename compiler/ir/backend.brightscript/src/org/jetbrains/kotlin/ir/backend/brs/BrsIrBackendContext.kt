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
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.FqName
import java.util.*

/**
 * Backend context for BrightScript code generation.
 *
 * This class provides access to all the infrastructure needed for lowering
 * Kotlin IR to BrightScript AST.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
class BrsIrBackendContext(
    val module: ModuleDescriptor,
    override val irBuiltIns: IrBuiltIns,
    val symbolTable: SymbolTable,
    override val configuration: CompilerConfiguration,
    val targetConfig: BrsTargetConfig = BrsTargetConfig.DEFAULT
) : CommonBackendContext {

    // ==================== Type System ====================

    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override val irFactory: IrFactory = symbolTable.irFactory

    // ==================== Verbose Mode ====================

    override var inVerbosePhase: Boolean = false

    // ==================== Intrinsics and Symbols ====================

    val intrinsics: BrsIntrinsics = BrsIntrinsics(irBuiltIns)

    val symbols: BrsSymbols = BrsSymbols(irBuiltIns, intrinsics)

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
        // Check for @BrsName annotation - simplified check by annotation class name
        val annotation = irClass.annotations.find { ann ->
            (ann.type.classifierOrNull?.owner as? IrClass)?.name?.asString() == "BrsName"
        }

        // For now, just use the simple name with proper mangling
        val baseName = irClass.name.asString()
        val parent = irClass.parent

        return when (parent) {
            is IrClass -> "${getBrsName(parent)}_${baseName}"
            is IrPackageFragment -> baseName
            else -> baseName
        }
    }

    private fun generateBrsFunctionName(irFunction: IrFunction): String {
        val baseName = irFunction.name.asString()
        val parent = irFunction.parent

        return when (parent) {
            is IrClass -> "${getBrsName(parent)}_${baseName}"
            is IrPackageFragment -> baseName
            else -> baseName
        }
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
