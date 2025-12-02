/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Returns a string representation of the contents of this array as if it was a [List].
 */
public fun <T> Array<out T>?.contentToString(): String {
    if (this == null) return "null"
    val sb = StringBuilder()
    sb.append("[")
    for (i in 0 until size) {
        if (i > 0) sb.append(", ")
        sb.append(this[i])
    }
    sb.append("]")
    return sb.toString()
}

/**
 * Returns true if the two specified arrays are structurally equal to one another.
 */
public infix fun <T> Array<out T>?.contentEquals(other: Array<out T>?): Boolean {
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
public fun ByteArray?.contentToString(): String {
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
public infix fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
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
public fun ShortArray?.contentToString(): String {
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
public infix fun ShortArray?.contentEquals(other: ShortArray?): Boolean {
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
public fun IntArray?.contentToString(): String {
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
public infix fun IntArray?.contentEquals(other: IntArray?): Boolean {
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
public fun LongArray?.contentToString(): String {
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
public infix fun LongArray?.contentEquals(other: LongArray?): Boolean {
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
public fun FloatArray?.contentToString(): String {
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
public infix fun FloatArray?.contentEquals(other: FloatArray?): Boolean {
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
public fun DoubleArray?.contentToString(): String {
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
public infix fun DoubleArray?.contentEquals(other: DoubleArray?): Boolean {
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
public fun BooleanArray?.contentToString(): String {
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
public infix fun BooleanArray?.contentEquals(other: BooleanArray?): Boolean {
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
public fun CharArray?.contentToString(): String {
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
public infix fun CharArray?.contentEquals(other: CharArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.size != other.size) return false
    for (i in 0 until size) {
        if (this[i] != other[i]) return false
    }
    return true
}
