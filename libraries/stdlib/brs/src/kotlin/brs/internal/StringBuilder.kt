/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.internal

/**
 * Internal StringBuilder implementation for BrightScript.
 * Used by string concatenation lowering.
 *
 * In generated BrightScript, this maps to roString operations.
 */
internal class StringBuilder {
    private var value: String = ""

    fun append(obj: Any?): StringBuilder {
        value += obj.toString()
        return this
    }

    fun append(str: String?): StringBuilder {
        value += str ?: "null"
        return this
    }

    fun append(c: Char): StringBuilder {
        value += c
        return this
    }

    override fun toString(): String = value
}
