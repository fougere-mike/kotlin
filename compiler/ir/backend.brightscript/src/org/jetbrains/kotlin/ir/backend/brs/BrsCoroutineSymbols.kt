/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.SymbolFinder
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getPropertySetter
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.atMostOne

/**
 * BrightScript-specific coroutine symbols.
 *
 * Provides access to the standard coroutine library classes and functions
 * needed for suspend function lowering and code generation.
 */
@OptIn(InternalSymbolFinderAPI::class)
class BrsCoroutineSymbols(
    private val symbolFinder: SymbolFinder,
    private val isStdlibCompilation: Boolean
) {
    // ==================== Helper Functions ====================

    private fun findOptionalClass(packageFqName: FqName, name: String): IrClassSymbol? =
        try {
            symbolFinder.topLevelClass(packageFqName, name)
        } catch (e: Exception) {
            if (isStdlibCompilation) null else throw e
        }

    private fun findOptionalFunction(packageFqName: FqName, name: String): IrSimpleFunctionSymbol? =
        symbolFinder.topLevelFunctions(packageFqName, name).firstOrNull()

    private fun findOptionalPropertyGetter(packageFqName: FqName, name: String): IrSimpleFunctionSymbol? =
        try {
            val properties = symbolFinder.findProperties(Name.identifier(name), packageFqName)
            val property = properties.firstOrNull() ?: return null
            symbolFinder.findGetter(property)
        } catch (e: Exception) {
            if (isStdlibCompilation) null else throw e
        }

    // ==================== Coroutine Implementation Class ====================

    /**
     * The CoroutineImpl class symbol - base class for generated coroutine state machines.
     * Located in kotlin.coroutines.CoroutineImpl
     */
    val coroutineImpl: IrClassSymbol? by lazy {
        findOptionalClass(COROUTINE_PACKAGE_FQNAME, COROUTINE_IMPL_NAME.asString())
    }

    /**
     * Getter for CoroutineImpl.state property (current state machine state)
     */
    val coroutineImplLabelPropertyGetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertyGetter("state")?.owner
    }

    /**
     * Setter for CoroutineImpl.state property
     */
    val coroutineImplLabelPropertySetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertySetter("state")?.owner
    }

    /**
     * Getter for CoroutineImpl.result property (last suspension result)
     */
    val coroutineImplResultSymbolGetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertyGetter("result")?.owner
    }

    /**
     * Setter for CoroutineImpl.result property
     */
    val coroutineImplResultSymbolSetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertySetter("result")?.owner
    }

    /**
     * Getter for CoroutineImpl.exception property (last suspension exception)
     */
    val coroutineImplExceptionPropertyGetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertyGetter("exception")?.owner
    }

    /**
     * Setter for CoroutineImpl.exception property
     */
    val coroutineImplExceptionPropertySetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertySetter("exception")?.owner
    }

    /**
     * Getter for CoroutineImpl.exceptionState property (exception handler state)
     */
    val coroutineImplExceptionStatePropertyGetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertyGetter("exceptionState")?.owner
    }

    /**
     * Setter for CoroutineImpl.exceptionState property
     */
    val coroutineImplExceptionStatePropertySetter: IrSimpleFunction? by lazy {
        coroutineImpl?.getPropertySetter("exceptionState")?.owner
    }

    // ==================== Continuation Class ====================

    /**
     * The Continuation interface symbol.
     * Located in kotlin.coroutines.Continuation
     */
    val continuationClass: IrClassSymbol? by lazy {
        findOptionalClass(COROUTINE_PACKAGE_FQNAME, CONTINUATION_NAME.asString())
    }

    /**
     * Getter for Continuation.context property
     */
    val coroutineGetContext: IrSimpleFunctionSymbol? by lazy {
        continuationClass?.owner?.declarations
            ?.filterIsInstance<IrSimpleFunction>()
            ?.atMostOne { it.name == CONTINUATION_CONTEXT_GETTER_NAME }?.symbol
            ?: continuationClass?.owner?.declarations
                ?.filterIsInstance<IrProperty>()
                ?.atMostOne { it.name == CONTINUATION_CONTEXT_PROPERTY_NAME }?.getter?.symbol
    }

    // ==================== COROUTINE_SUSPENDED ====================

    /**
     * Getter for COROUTINE_SUSPENDED intrinsic property.
     * Located in kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
     */
    val coroutineSuspendedGetter: IrSimpleFunctionSymbol? by lazy {
        findOptionalPropertyGetter(COROUTINE_INTRINSICS_PACKAGE_FQNAME, COROUTINE_SUSPENDED_NAME.asString())
    }

    // ==================== Coroutine Context ====================

    /**
     * Getter for coroutineContext suspend property.
     * Located in kotlin.coroutines.coroutineContext
     */
    val coroutineContextGetter: IrSimpleFunctionSymbol? by lazy {
        findOptionalPropertyGetter(COROUTINE_PACKAGE_FQNAME, COROUTINE_CONTEXT_NAME.asString())
    }

    // ==================== Availability Check ====================

    /**
     * Returns true if all required coroutine symbols are available.
     * This is used to determine whether coroutine lowering can be performed.
     *
     * During stdlib compilation or when no stdlib is linked (e.g., in tests),
     * coroutine symbols won't be available and lowering should be skipped.
     */
    val areCoroutineSymbolsAvailable: Boolean by lazy {
        try {
            // Check for the critical symbols needed for coroutine lowering
            coroutineImpl != null &&
                continuationClass != null &&
                coroutineSuspendedGetter != null
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private val INTRINSICS_PACKAGE_NAME = Name.identifier("intrinsics")
        private val COROUTINE_SUSPENDED_NAME = Name.identifier("COROUTINE_SUSPENDED")
        private val COROUTINE_CONTEXT_NAME = Name.identifier("coroutineContext")
        private val COROUTINE_IMPL_NAME = Name.identifier("CoroutineImpl")
        private val CONTINUATION_NAME = Name.identifier("Continuation")
        private val CONTINUATION_CONTEXT_GETTER_NAME = Name.special("<get-context>")
        private val CONTINUATION_CONTEXT_PROPERTY_NAME = Name.identifier("context")
        private val COROUTINE_PACKAGE_FQNAME = FqName.fromSegments(listOf("kotlin", "coroutines"))
        private val COROUTINE_INTRINSICS_PACKAGE_FQNAME = COROUTINE_PACKAGE_FQNAME.child(INTRINSICS_PACKAGE_NAME)
    }
}
