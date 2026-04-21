/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

/**
 * Represents BrightScript's `invalid` value (equivalent to null).
 */
public object Invalid {
    override fun toString(): String = "invalid"
}

/**
 * The dynamic type for BrightScript interop.
 *
 * This type represents values that can be any BrightScript type.
 * It provides unchecked access to properties and methods.
 */
public external interface Dynamic

/**
 * Embeds inline BrightScript code at compile time.
 *
 * The [code] argument must be a compile-time constant string that parses to
 * exactly one BrightScript expression or statement. Multiple statements and
 * non-literal arguments are compile errors.
 *
 * See `compiler/ir/backend.brightscript/docs/brs-intrinsic.md` for the full
 * contract (input constraints, output semantics, error surface, examples).
 */
public external fun brs(code: String): Dynamic

/**
 * Creates a BrightScript object using CreateObject.
 *
 * @param T The Kotlin type to cast the result to.
 * @param type The BrightScript object type name.
 * @param args Additional arguments to pass to the constructor.
 * @return The created object.
 */
public external fun <T> createObject(type: String, vararg args: Any?): T

/**
 * Gets the runtime type name of a value.
 *
 * @param value The value to check.
 * @return The BrightScript type name.
 */
public external fun typeOf(value: Any?): String

/**
 * Checks if a value is invalid (BrightScript's null).
 *
 * @param value The value to check.
 * @return True if the value is invalid.
 */
public fun isInvalid(value: Any?): Boolean = value == null || typeOf(value) == "Invalid"

/**
 * Prints a value to the BrightScript debug console.
 *
 * @param message The message to print.
 */
public external fun print(message: Any?)

/**
 * Prints multiple values to the BrightScript debug console.
 *
 * @param messages The messages to print.
 */
public external fun print(vararg messages: Any?)

/**
 * Placeholder for externally defined values.
 *
 * Use this as the body of external property getters.
 */
public external val definedExternally: Nothing

/**
 * Casts a value to Dynamic for unchecked access.
 */
public fun Any?.asDynamic(): Dynamic = this.unsafeCast<Dynamic>()

/**
 * Unsafe cast without runtime checks.
 */
@Suppress("UNCHECKED_CAST")
public fun <T> Any?.unsafeCast(): T = this as T
