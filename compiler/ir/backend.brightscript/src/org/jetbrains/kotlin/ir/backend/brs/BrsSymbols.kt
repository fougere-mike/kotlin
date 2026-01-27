/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.kotlinPackageFqn
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME

/**
 * BrightScript-specific symbols for the Kotlin IR backend.
 *
 * This class provides access to standard library symbols and intrinsics
 * needed during IR lowering and code generation.
 *
 * Note: Many symbols are lazily evaluated and handle missing symbols gracefully,
 * since the bootstrap stdlib may not have all required functions.
 *
 * @param isStdlibCompilation When true, missing stdlib symbols will return null
 *        instead of throwing errors. This is used during stdlib compilation itself,
 *        where the symbols being looked up are being defined, not linked.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class, InternalSymbolFinderAPI::class)
class BrsSymbols(
    irBuiltIns: IrBuiltIns,
    private val intrinsics: BrsIntrinsics,
    private val isStdlibCompilation: Boolean = false
) : Symbols(irBuiltIns) {

    /**
     * Coroutine-specific symbols for suspend function lowering.
     */
    val coroutineSymbols: BrsCoroutineSymbols by lazy {
        BrsCoroutineSymbols(symbolFinder, isStdlibCompilation)
    }

    // Helper to find optional functions that may not exist in bootstrap stdlib
    private fun findOptionalFunction(packageName: FqName, name: String): IrSimpleFunctionSymbol? =
        symbolFinder.topLevelFunctions(packageName, name).firstOrNull()

    // Helper to find optional classes that may not exist in bootstrap stdlib
    private fun findOptionalClass(packageName: FqName, name: String): IrClassSymbol? =
        symbolFinder.findClass(Name.identifier(name), packageName)

    // ==================== Exception Handling ====================
    // These are lazily evaluated to avoid failures during context initialization.
    // In stdlib compilation mode, missing symbols return null and the backend generates inline code.
    // For user code compilation, the stdlib must be linked.

    // Nullable versions for use in lowering phases that need to handle stdlib compilation mode
    val throwNullPointerExceptionOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "THROW_NPE")
    }

    val throwTypeCastExceptionOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "THROW_CCE")
    }

    val throwUninitializedPropertyAccessExceptionOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "throwUninitializedPropertyAccessException")
    }

    val throwKotlinNothingValueExceptionOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "throwKotlinNothingValueException")
    }

    val throwISEOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "THROW_ISE")
    }

    val throwIAEOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "THROW_IAE")
    }

    // Required overrides - will error if used when symbols are not available
    // Lowering phases should check the OrNull versions first in stdlib compilation mode
    override val throwNullPointerException: IrSimpleFunctionSymbol by lazy {
        throwNullPointerExceptionOrNull
            ?: if (isStdlibCompilation) error("THROW_NPE accessed during stdlib compilation - use throwNullPointerExceptionOrNull")
            else error("THROW_NPE not found - ensure stdlib is linked")
    }

    override val throwTypeCastException: IrSimpleFunctionSymbol by lazy {
        throwTypeCastExceptionOrNull
            ?: if (isStdlibCompilation) error("THROW_CCE accessed during stdlib compilation - use throwTypeCastExceptionOrNull")
            else error("THROW_CCE not found - ensure stdlib is linked")
    }

    override val throwUninitializedPropertyAccessException: IrSimpleFunctionSymbol by lazy {
        throwUninitializedPropertyAccessExceptionOrNull
            ?: if (isStdlibCompilation) error("throwUninitializedPropertyAccessException accessed during stdlib compilation - use throwUninitializedPropertyAccessExceptionOrNull")
            else error("throwUninitializedPropertyAccessException not found - ensure stdlib is linked")
    }

    override val throwKotlinNothingValueException: IrSimpleFunctionSymbol by lazy {
        throwKotlinNothingValueExceptionOrNull
            ?: if (isStdlibCompilation) error("throwKotlinNothingValueException accessed during stdlib compilation - use throwKotlinNothingValueExceptionOrNull")
            else error("throwKotlinNothingValueException not found - ensure stdlib is linked")
    }

    override val throwISE: IrSimpleFunctionSymbol by lazy {
        throwISEOrNull
            ?: if (isStdlibCompilation) error("THROW_ISE accessed during stdlib compilation - use throwISEOrNull")
            else error("THROW_ISE not found - ensure stdlib is linked")
    }

    override val throwIAE: IrSimpleFunctionSymbol by lazy {
        throwIAEOrNull
            ?: if (isStdlibCompilation) error("THROW_IAE accessed during stdlib compilation - use throwIAEOrNull")
            else error("THROW_IAE not found - ensure stdlib is linked")
    }

    val throwUnsupportedOperationExceptionOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinPackageFqn, "THROW_UOE")
    }

    override val throwUnsupportedOperationException: IrSimpleFunctionSymbol by lazy {
        throwUnsupportedOperationExceptionOrNull
            ?: if (isStdlibCompilation) error("THROW_UOE accessed during stdlib compilation - use throwUnsupportedOperationExceptionOrNull")
            else error("THROW_UOE not found - ensure stdlib is linked")
    }

    // ==================== Default Constructor Marker ====================

    val defaultConstructorMarkerOrNull: IrClassSymbol? by lazy {
        findOptionalClass(BrsStandardClassIds.BASE_BRS_PACKAGE, "DefaultConstructorMarker")
    }

    override val defaultConstructorMarker: IrClassSymbol by lazy {
        defaultConstructorMarkerOrNull
            ?: if (isStdlibCompilation) error("DefaultConstructorMarker accessed during stdlib compilation - use defaultConstructorMarkerOrNull")
            else error("DefaultConstructorMarker not found - ensure stdlib is linked")
    }

    // ==================== String Builder ====================

    val stringBuilderOrNull: IrClassSymbol? by lazy {
        findOptionalClass(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, "StringBuilder")
    }

    override val stringBuilder: IrClassSymbol by lazy {
        stringBuilderOrNull
            ?: if (isStdlibCompilation) error("StringBuilder accessed during stdlib compilation - use stringBuilderOrNull")
            else error("StringBuilder not found - ensure stdlib is linked")
    }

    // ==================== Coroutines ====================

    /**
     * The CoroutineImpl class - base class for generated coroutine state machines.
     */
    override val coroutineImpl: IrClassSymbol by lazy {
        coroutineSymbols.coroutineImpl
            ?: if (isStdlibCompilation) error("CoroutineImpl accessed during stdlib compilation - use coroutineSymbols.coroutineImpl")
            else error("CoroutineImpl not found - ensure stdlib is linked")
    }

    /**
     * Getter for COROUTINE_SUSPENDED intrinsic.
     */
    override val coroutineSuspendedGetter: IrSimpleFunctionSymbol by lazy {
        coroutineSymbols.coroutineSuspendedGetter
            ?: if (isStdlibCompilation) error("coroutineSuspendedGetter accessed during stdlib compilation")
            else error("COROUTINE_SUSPENDED getter not found - ensure stdlib is linked")
    }

    /**
     * Function to get the current continuation in a suspend function.
     * In BRS, this is a compiler intrinsic - calls are transformed during lowering.
     *
     * During stdlib compilation, this IS available because getContinuation() is defined
     * in the stdlib being compiled. The symbol finder can resolve it.
     */
    override val getContinuation: IrSimpleFunctionSymbol by lazy {
        // Look in kotlin.coroutines.intrinsics package where it's defined
        findOptionalFunction(FqName("kotlin.coroutines.intrinsics"), "getContinuation")
            ?: error("getContinuation not found - ensure stdlib is linked or you're compiling the stdlib")
    }

    /**
     * Nullable version for checking during transformations.
     */
    val getContinuationOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(FqName("kotlin.coroutines.intrinsics"), "getContinuation")
    }

    /**
     * The Continuation interface class.
     *
     * During stdlib compilation, this IS available because Continuation is defined
     * in the stdlib being compiled. The symbol finder can resolve it.
     */
    override val continuationClass: IrClassSymbol by lazy {
        coroutineSymbols.continuationClass
            ?: error("Continuation class not found - ensure stdlib is linked or you're compiling the stdlib")
    }

    /**
     * Getter for the coroutineContext suspend property.
     */
    override val coroutineContextGetter: IrSimpleFunctionSymbol by lazy {
        coroutineSymbols.coroutineContextGetter
            ?: if (isStdlibCompilation) error("coroutineContextGetter accessed during stdlib compilation")
            else error("coroutineContext getter not found - ensure stdlib is linked")
    }

    /**
     * The suspendCoroutineUninterceptedOrReturn intrinsic function.
     */
    override val suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol by lazy {
        // Look in kotlin.coroutines.intrinsics package
        symbolFinder.topLevelFunctions(FqName("kotlin.coroutines.intrinsics"), "suspendCoroutineUninterceptedOrReturn")
            .firstOrNull()
            ?: if (isStdlibCompilation) error("suspendCoroutineUninterceptedOrReturn accessed during stdlib compilation")
            else error("suspendCoroutineUninterceptedOrReturn not found - ensure stdlib is linked")
    }

    /**
     * Function to get the coroutine context from a continuation.
     */
    override val coroutineGetContext: IrSimpleFunctionSymbol by lazy {
        coroutineSymbols.coroutineGetContext
            ?: if (isStdlibCompilation) error("coroutineGetContext accessed during stdlib compilation")
            else error("Continuation.context getter not found - ensure stdlib is linked")
    }

    /**
     * The returnIfSuspended helper function.
     */
    override val returnIfSuspended: IrSimpleFunctionSymbol by lazy {
        findOptionalFunction(FqName("kotlin.coroutines.intrinsics"), "returnIfSuspended")
            ?: if (isStdlibCompilation) error("returnIfSuspended accessed during stdlib compilation")
            else error("returnIfSuspended not found - ensure stdlib is linked")
    }

    // ==================== Function Adapter ====================

    val functionAdapterOrNull: IrClassSymbol? by lazy {
        findOptionalClass(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, "FunctionAdapter")
    }

    override val functionAdapter: IrClassSymbol by lazy {
        functionAdapterOrNull
            ?: if (isStdlibCompilation) error("FunctionAdapter accessed during stdlib compilation - use functionAdapterOrNull")
            else error("FunctionAdapter not found - ensure stdlib is linked")
    }

    // ==================== Array Content Equals ====================

    private val _arraysContentEquals by lazy {
        symbolFinder.topLevelFunctions(COLLECTIONS_PACKAGE_FQ_NAME, "contentEquals").filter {
            it.descriptor.extensionReceiverParameter?.type?.isMarkedNullable == true
        }
    }

    override val arraysContentEquals: Map<IrType, IrSimpleFunctionSymbol> by lazy {
        _arraysContentEquals.associateBy { it.owner.parameters[0].type.makeNotNull() }
    }

    // ==================== Progression Utilities ====================

    private val getProgressionLastElementSymbols by lazy {
        symbolFinder.findFunctions(Name.identifier("getProgressionLastElement"), "kotlin", "internal")
    }

    override val getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        getProgressionLastElementSymbols.associateBy { it.owner.returnType.classifierOrFail }
    }

    // ==================== Unsigned Integers ====================

    private val toUIntSymbols by lazy {
        symbolFinder.findFunctions(Name.identifier("toUInt"), "kotlin")
    }

    override val toUIntByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        toUIntSymbols.associateBy { it.owner.parameters[0].type.classifierOrFail }
    }

    private val toULongSymbols by lazy {
        symbolFinder.findFunctions(Name.identifier("toULong"), "kotlin")
    }

    override val toULongByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy {
        toULongSymbols.associateBy { it.owner.parameters[0].type.classifierOrFail }
    }

    // ==================== Side Effect Analysis ====================

    override fun isSideEffectFree(call: IrCall): Boolean {
        return call.symbol == intrinsics.arrayLiteral ||
                call.symbol == intrinsics.aaLiteral ||
                call.symbol in intrinsics.primitiveToBoxConstructor.values
    }

    // ==================== BrightScript-specific Symbols ====================

    /**
     * The Invalid type symbol (BrightScript's null).
     */
    val invalidSymbol: IrClassSymbol? by lazy {
        symbolFinder.findClass(
            Name.identifier("Invalid"),
            BrsStandardClassIds.BASE_BRS_PACKAGE
        )
    }

    /**
     * Function to check if a value is Invalid.
     */
    val isInvalidFunction: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            Name.identifier("isInvalid"),
            BrsStandardClassIds.BASE_BRS_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Function to get a field value from an associative array.
     */
    val aaGetField: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            Name.identifier("aaGet"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Function to set a field value in an associative array.
     */
    val aaSetField: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            Name.identifier("aaSet"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Function to add an item to an array.
     */
    val arrayPush: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            Name.identifier("arrayPush"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }

    /**
     * Function to get array length.
     */
    val arrayCount: IrSimpleFunctionSymbol? by lazy {
        symbolFinder.findFunctions(
            Name.identifier("arrayCount"),
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE.asString()
        ).firstOrNull()
    }
}
