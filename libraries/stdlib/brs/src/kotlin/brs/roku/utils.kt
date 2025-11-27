/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * BrightScript roSystemLog - system logging.
 */
@BrsExternal
public external class RoSystemLog : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun enableType(logType: String): Boolean
}

/**
 * BrightScript roChannelStore - in-app purchases.
 */
@BrsExternal
public external class RoChannelStore : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun getCatalog(): RoArray
    public fun getStoreCatalog(): RoArray
    public fun getUserData(): RoAssociativeArray
    public fun getPurchases(): RoArray
    public fun getAllPurchases(): RoArray
    public fun setOrder(order: RoArray)
    public fun clearOrder(): Boolean
    public fun doOrder(): RoAssociativeArray
    public fun confirmPartnerOrder(order: RoAssociativeArray): Boolean
    public fun fakeServer(enable: Boolean)
    public fun deltaOrder(delta: RoArray): RoAssociativeArray
    public fun getUserRegionData(): RoAssociativeArray
    public fun getPartialUserData(properties: String): RoAssociativeArray
    public fun requestPartnerOrder(order: RoArray, sku: String)
    public fun storeChannelCredData(data: String): Boolean
    public fun getChannelCred(): String
}

/**
 * BrightScript roTextToSpeech - accessibility text-to-speech.
 */
@BrsExternal
public external class RoTextToSpeech : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun say(text: String): Int
    public fun flush(): Boolean
    public fun silence(duration: Int): Int
    public fun canSpeak(): Boolean
    public fun getVoices(): RoArray
    public fun getVoice(): String
    public fun setVoice(voice: String): Boolean
    public fun getRate(): Int
    public fun setRate(rate: Int): Boolean
    public fun getVolume(): Int
    public fun setVolume(volume: Int): Boolean
}

// ============================================
// Extension functions for Kotlin-idiomatic API
// ============================================

/**
 * Creates a new RoArray from the given elements.
 */
public fun roArrayOf(vararg elements: Any?): RoArray {
    val array = RoArray()
    for (element in elements) {
        array.push(element)
    }
    return array
}

/**
 * Creates a new RoAssociativeArray from the given pairs.
 */
public fun roAssociativeArrayOf(vararg pairs: Pair<String, Any?>): RoAssociativeArray {
    val aa = RoAssociativeArray()
    for ((key, value) in pairs) {
        aa.addReplace(key, value)
    }
    return aa
}

/**
 * Converts a Kotlin List to RoArray.
 */
public fun List<Any?>.toRoArray(): RoArray {
    val array = RoArray(this.size, true)
    for (element in this) {
        array.push(element)
    }
    return array
}

/**
 * Converts a Kotlin Map to RoAssociativeArray.
 */
public fun Map<String, Any?>.toRoAssociativeArray(): RoAssociativeArray {
    val aa = RoAssociativeArray()
    for ((key, value) in this) {
        aa.addReplace(key, value)
    }
    return aa
}

/**
 * Iterates over all elements in a RoArray.
 */
public inline fun RoArray.forEach(action: (Any?) -> Unit) {
    val count = this.count()
    for (i in 0 until count) {
        action(this[i])
    }
}

/**
 * Iterates over all key-value pairs in a RoAssociativeArray.
 */
public inline fun RoAssociativeArray.forEach(action: (String, Any?) -> Unit) {
    val keys = this.keys()
    keys.forEach { key ->
        @Suppress("UNCHECKED_CAST")
        action(key as String, this.lookup(key))
    }
}

/**
 * Maps elements of RoArray to a new RoArray.
 */
public inline fun RoArray.map(transform: (Any?) -> Any?): RoArray {
    val result = RoArray()
    this.forEach { element ->
        result.push(transform(element))
    }
    return result
}

/**
 * Filters elements of RoArray.
 */
public inline fun RoArray.filter(predicate: (Any?) -> Boolean): RoArray {
    val result = RoArray()
    this.forEach { element ->
        if (predicate(element)) {
            result.push(element)
        }
    }
    return result
}

/**
 * Checks if RoArray is empty.
 */
public fun RoArray.isEmpty(): Boolean = this.count() == 0

/**
 * Checks if RoArray is not empty.
 */
public fun RoArray.isNotEmpty(): Boolean = this.count() > 0

/**
 * Checks if RoAssociativeArray is empty.
 */
public fun RoAssociativeArray.isEmpty(): Boolean = this.count() == 0

/**
 * Checks if RoAssociativeArray is not empty.
 */
public fun RoAssociativeArray.isNotEmpty(): Boolean = this.count() > 0

/**
 * Gets a value from RoAssociativeArray with a default.
 */
public fun RoAssociativeArray.getOrDefault(key: String, default: Any?): Any? {
    return if (this.doesExist(key)) this.lookup(key) else default
}

/**
 * Gets a value from RoAssociativeArray or computes it.
 */
public inline fun RoAssociativeArray.getOrPut(key: String, defaultValue: () -> Any?): Any? {
    return if (this.doesExist(key)) {
        this.lookup(key)
    } else {
        val value = defaultValue()
        this.addReplace(key, value)
        value
    }
}
