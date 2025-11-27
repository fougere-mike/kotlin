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
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME

/**
 * BrightScript-specific symbols for the Kotlin IR backend.
 *
 * This class provides access to standard library symbols and intrinsics
 * needed during IR lowering and code generation.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class, InternalSymbolFinderAPI::class)
class BrsSymbols(
    irBuiltIns: IrBuiltIns,
    private val intrinsics: BrsIntrinsics
) : Symbols(irBuiltIns) {

    // ==================== Exception Handling ====================

    override val throwNullPointerException: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "THROW_NPE")

    override val throwTypeCastException: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "THROW_CCE")

    override val throwUninitializedPropertyAccessException: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "throwUninitializedPropertyAccessException")

    override val throwKotlinNothingValueException: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "throwKotlinNothingValueException")

    override val throwISE: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "THROW_ISE")

    override val throwIAE: IrSimpleFunctionSymbol =
        symbolFinder.topLevelFunction(kotlinPackageFqn, "THROW_IAE")

    // ==================== Default Constructor Marker ====================

    override val defaultConstructorMarker: IrClassSymbol =
        symbolFinder.topLevelClass(BrsStandardClassIds.BASE_BRS_PACKAGE, "DefaultConstructorMarker")

    // ==================== String Builder ====================

    override val stringBuilder: IrClassSymbol
        get() = symbolFinder.topLevelClass(
            BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE,
            "StringBuilder"
        )

    // ==================== Coroutines (Not supported in BrightScript) ====================

    override val coroutineImpl: IrClassSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val coroutineSuspendedGetter: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val getContinuation: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val continuationClass: IrClassSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val coroutineContextGetter: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val coroutineGetContext: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    override val returnIfSuspended: IrSimpleFunctionSymbol
        get() = error("Coroutines are not supported in BrightScript")

    // ==================== Function Adapter ====================

    override val functionAdapter: IrClassSymbol
        get() = symbolFinder.topLevelClass(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, "FunctionAdapter")

    // ==================== Array Content Equals ====================

    private val _arraysContentEquals = symbolFinder.topLevelFunctions(COLLECTIONS_PACKAGE_FQ_NAME, "contentEquals").filter {
        it.descriptor.extensionReceiverParameter?.type?.isMarkedNullable == true
    }

    override val arraysContentEquals: Map<IrType, IrSimpleFunctionSymbol>
        get() = _arraysContentEquals.associateBy { it.owner.parameters[0].type.makeNotNull() }

    // ==================== Progression Utilities ====================

    private val getProgressionLastElementSymbols =
        symbolFinder.findFunctions(Name.identifier("getProgressionLastElement"), "kotlin", "internal")

    override val getProgressionLastElementByReturnType: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
        getProgressionLastElementSymbols.associateBy { it.owner.returnType.classifierOrFail }
    }

    // ==================== Unsigned Integers ====================

    private val toUIntSymbols = symbolFinder.findFunctions(Name.identifier("toUInt"), "kotlin")

    override val toUIntByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
        toUIntSymbols.associateBy { it.owner.parameters[0].type.classifierOrFail }
    }

    private val toULongSymbols = symbolFinder.findFunctions(Name.identifier("toULong"), "kotlin")

    override val toULongByExtensionReceiver: Map<IrClassifierSymbol, IrSimpleFunctionSymbol> by lazy(LazyThreadSafetyMode.NONE) {
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
