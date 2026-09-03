/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.builders.launch

/** Pipes bridged upstream values into the backing state (the emit protocol applies: dedup, doorbell ring). */
private class StateInCollector<T>(private val state: StateFlowImpl<T>) : FlowCollector<T> {
    override suspend fun emit(value: T) {
        stateFlowSetValue(state, value)
    }
}

/**
 * Bridges this flow into a [StateFlow] (flow-program spec §6, eager-only v1):
 * launches a collector coroutine in [scope] immediately, piping every upstream
 * value (typically a `flowOn(Dispatchers.Task)` repository flow) into a fresh
 * backing `MutableStateFlow(initialValue)`, and returns the read-only view.
 *
 * Scope discipline: Jobs are same-component-only, so [scope] must be the
 * OWNING component's (`componentScope()` passed into the VM — the
 * ScopeHandle-injection idiom); the bridge dies with the screen. After scope
 * death the StateFlow stays readable — the last value is FROZEN — and
 * collectors see no further emissions. An upstream failure cancels the bridge
 * the same way (the launch's unhandled-exception console line names it);
 * kotlinx's SharingStarted/subscriber-count modes are out of scope for v1.
 */
public fun <T> Flow<T>.stateIn(scope: CoroutineScope, initialValue: T): StateFlow<T> {
    val upstream = this
    val state = StateFlowImpl(initialValue)
    scope.launch {
        flowCollectDispatch(upstream, StateInCollector(state))
    }
    return ReadonlyStateFlow(state)
}
