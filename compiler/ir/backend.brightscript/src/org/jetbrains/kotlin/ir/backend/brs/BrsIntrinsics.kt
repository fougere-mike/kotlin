/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds

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
                symbol == brsInline
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
}
