/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.Job
import kotlin.coroutines.builders.launch

private class ActionCollector<T>(private val action: suspend (T) -> Unit) : FlowCollector<T> {
    override suspend fun emit(value: T) {
        action(value)
    }
}

/** Collects the flow, invoking [action] for every emitted value. */
public suspend fun <T> Flow<T>.collect(action: suspend (T) -> Unit) {
    collect(ActionCollector(action))
}

/**
 * Collector for the first() family: records the first (matching) value, then
 * aborts upstream with [AbortFlowException]. Mutable state lives in fields, not
 * captured locals.
 */
private class FirstCollector<T>(private val predicate: (suspend (T) -> Boolean)?) : FlowCollector<T> {
    var found: Boolean = false
    var result: Any? = null

    override suspend fun emit(value: T) {
        val p = predicate
        if (p != null && !p(value)) return
        result = value
        found = true
        throw AbortFlowException(this)
    }
}

/**
 * Shared machinery of the first() family: collects until [FirstCollector] aborts,
 * swallowing exactly its own abort (owner identity). An [AbortFlowException]
 * owned by someone else — another terminal, an operator like [take] — is in
 * flight to ITS catch site and keeps unwinding.
 */
private suspend fun <T> Flow<T>.collectFirst(predicate: (suspend (T) -> Boolean)?): FirstCollector<T> {
    val collector = FirstCollector<T>(predicate)
    try {
        collect(collector)
    } catch (e: AbortFlowException) {
        if (e.owner !== collector) throw e
    }
    return collector
}

/**
 * The first value the flow emits; collection is aborted as soon as it arrives —
 * the producer body never runs past that emit. Throws [NoSuchElementException]
 * if the flow completes without emitting.
 */
public suspend fun <T> Flow<T>.first(): T {
    val collector = collectFirst(null)
    if (!collector.found) throw NoSuchElementException("Expected at least one element")
    @Suppress("UNCHECKED_CAST")
    return collector.result as T
}

/**
 * The first value matching [predicate]; collection is aborted at the match.
 * Throws [NoSuchElementException] if the flow completes without a match.
 */
public suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T {
    val collector = collectFirst(predicate)
    if (!collector.found) throw NoSuchElementException("Expected at least one element matching the predicate")
    @Suppress("UNCHECKED_CAST")
    return collector.result as T
}

/** The first value the flow emits, or null if it completes without emitting. */
public suspend fun <T> Flow<T>.firstOrNull(): T? {
    val collector = collectFirst(null)
    @Suppress("UNCHECKED_CAST")
    return if (collector.found) collector.result as T else null
}

private class ToListCollector<T>(val destination: ArrayList<T>) : FlowCollector<T> {
    override suspend fun emit(value: T) {
        destination.add(value)
    }
}

/** Collects the flow to completion and returns everything it emitted, in order. */
public suspend fun <T> Flow<T>.toList(): List<T> {
    val destination = ArrayList<T>()
    collect(ToListCollector(destination))
    return destination
}

/** [launchIn] discards values; side effects belong in the flow (onEach, later task). */
private object NopCollector : FlowCollector<Any?> {
    override suspend fun emit(value: Any?) {
    }
}

/**
 * Launches collection of the flow as a new coroutine in [scope] and returns its
 * [Job]. Emitted values are discarded — chain operators (e.g. a future `onEach`)
 * for per-value work. The kotlinx `events.onEach { ... }.launchIn(scope)` idiom.
 */
public fun <T> Flow<T>.launchIn(scope: CoroutineScope): Job = scope.launch {
    collect(NopCollector)
}
