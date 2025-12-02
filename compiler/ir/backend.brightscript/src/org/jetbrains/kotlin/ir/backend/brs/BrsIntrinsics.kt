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
                symbol == brsInline ||
                isStdlibIntrinsic(symbol)
    }

    /**
     * Check if a function is a stdlib intrinsic (external function with brsIntrinsic* prefix).
     * These are defined in the stdlib as `internal external fun brsIntrinsic*()` and
     * are replaced by native BrightScript code during code generation.
     */
    fun isStdlibIntrinsic(symbol: IrSimpleFunctionSymbol): Boolean {
        val function = symbol.owner
        return function.isExternal && function.name.asString().startsWith("brsIntrinsic")
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
        "brsIntrinsicPow" to StdlibIntrinsic.Pow,                 // x ^ y
        "brsIntrinsicSinh" to StdlibIntrinsic.Sinh,               // (Exp(x) - Exp(-x)) / 2
        "brsIntrinsicCosh" to StdlibIntrinsic.Cosh,               // (Exp(x) + Exp(-x)) / 2
        "brsIntrinsicTanh" to StdlibIntrinsic.Tanh,               // sinh/cosh

        // String intrinsics
        "brsIntrinsicUCase" to StdlibIntrinsic.SimpleCall("UCase"),
        "brsIntrinsicLCase" to StdlibIntrinsic.SimpleCall("LCase"),
        "brsIntrinsicInstr" to StdlibIntrinsic.SimpleCall("Instr"),
        "brsIntrinsicLen" to StdlibIntrinsic.SimpleCall("Len"),
        "brsIntrinsicLeft" to StdlibIntrinsic.SimpleCall("Left"),
        "brsIntrinsicRight" to StdlibIntrinsic.SimpleCall("Right"),
        "brsIntrinsicMid" to StdlibIntrinsic.SimpleCall("Mid"),
        "brsIntrinsicAsc" to StdlibIntrinsic.SimpleCall("Asc"),
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
        "brsIntrinsicRandomSeed" to StdlibIntrinsic.RandomSeed,

        // JSON intrinsics
        "brsIntrinsicParseJson" to StdlibIntrinsic.SimpleCall("ParseJson"),
        "brsIntrinsicFormatJson" to StdlibIntrinsic.SimpleCall("FormatJson"),

        // Type intrinsics
        "brsIntrinsicType" to StdlibIntrinsic.SimpleCall("Type"),
        "brsIntrinsicGetGlobalAA" to StdlibIntrinsic.SimpleCall("GetGlobalAA"),

        // Node intrinsics
        "brsIntrinsicCreateObject" to StdlibIntrinsic.CreateObject
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
