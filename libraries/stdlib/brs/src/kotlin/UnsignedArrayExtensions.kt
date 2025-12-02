/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Returns a string representation of the contents of this array.
 */
@ExperimentalUnsignedTypes
public fun UByteArray?.contentToString(): String {
    if (this == null) return "null"
    val list = ArrayList<String>()
    for (item in this) {
        list.add(item.toString())
    }
    return "[" + list.joinToString(", ") + "]"
}

/**
 * Returns true if the two specified arrays are structurally equal to one another.
 */
@ExperimentalUnsignedTypes
public infix fun UByteArray?.contentEquals(other: UByteArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.size != other.size) return false
    for (i in 0 until size) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns a string representation of the contents of this array.
 */
@ExperimentalUnsignedTypes
public fun UShortArray?.contentToString(): String {
    if (this == null) return "null"
    val list = ArrayList<String>()
    for (item in this) {
        list.add(item.toString())
    }
    return "[" + list.joinToString(", ") + "]"
}

/**
 * Returns true if the two specified arrays are structurally equal to one another.
 */
@ExperimentalUnsignedTypes
public infix fun UShortArray?.contentEquals(other: UShortArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.size != other.size) return false
    for (i in 0 until size) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns a string representation of the contents of this array.
 */
@ExperimentalUnsignedTypes
public fun UIntArray?.contentToString(): String {
    if (this == null) return "null"
    val list = ArrayList<String>()
    for (item in this) {
        list.add(item.toString())
    }
    return "[" + list.joinToString(", ") + "]"
}

/**
 * Returns true if the two specified arrays are structurally equal to one another.
 */
@ExperimentalUnsignedTypes
public infix fun UIntArray?.contentEquals(other: UIntArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.size != other.size) return false
    for (i in 0 until size) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns a string representation of the contents of this array.
 */
@ExperimentalUnsignedTypes
public fun ULongArray?.contentToString(): String {
    if (this == null) return "null"
    val list = ArrayList<String>()
    for (item in this) {
        list.add(item.toString())
    }
    return "[" + list.joinToString(", ") + "]"
}

/**
 * Returns true if the two specified arrays are structurally equal to one another.
 */
@ExperimentalUnsignedTypes
public infix fun ULongArray?.contentEquals(other: ULongArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.size != other.size) return false
    for (i in 0 until size) {
        if (this[i] != other[i]) return false
    }
    return true
}
