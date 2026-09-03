/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

/**
 * A hot, state-holding flow mirroring `kotlinx.coroutines.flow.StateFlow`: it
 * always has a current [value], every collector receives the current value
 * immediately and then every distinct update, and equal (`==`) values are
 * conflated away (the dedup law).
 *
 * THE cross-component flow (flow-program spec §6): unlike cold [Flow] objects
 * (which must never cross a component boundary — their lambdas strip), a
 * StateFlow is designed to be held in a SharedService VM and collected from
 * any component. Its implementation is a plain data holder; ALL behavior lives
 * in static top-level functions (Doorbells.kt), and the compiler rewrites
 * `value` accessor calls and member `collect` calls to those statics
 * (BrsFlowAccessLowering) so no cross-component function slot ever dispatches.
 *
 * v1 laws: `value` READS work anywhere (a plain property read); construction
 * works anywhere; emits (`value = x`) and collection require a render-thread
 * component context (guided IllegalStateException otherwise).
 */
public interface StateFlow<out T> : Flow<T> {
    /** The current value. Reads work anywhere; always the live value. */
    public val value: T
}

/**
 * A [StateFlow] whose [value] can be set, mirroring
 * `kotlinx.coroutines.flow.MutableStateFlow`. Setting an equal (`==`) value is
 * a no-op (kotlinx dedup); setting a distinct value stores it and rings the
 * doorbell so every collector wakes and reads the live value.
 */
// Suppression is sound (the Flow/flow precedent, Flow.kt): the interface emits
// no top-level runtime identifier, and the case-identical factory function
// below is signature-mangled — they cannot collide.
@Suppress("BRS_NAME_CASE_CLASH")
public interface MutableStateFlow<T> : StateFlow<T> {
    /** The current value; setting it emits (equality-gated, render-thread-only). */
    public override var value: T
}

/** Creates a [MutableStateFlow] holding [value]. Construction works anywhere. */
// BRS_NAME_CASE_CLASH vs `interface MutableStateFlow`: sound — see above.
@Suppress("BRS_NAME_CASE_CLASH")
public fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = StateFlowImpl(value)

/**
 * A read-only [StateFlow] view of this flow (the kotlinx VM idiom: private
 * `MutableStateFlow`, public `asStateFlow()` view). The view shares the source's
 * value and doorbell — never a copy.
 */
public fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> {
    @Suppress("UNCHECKED_CAST")
    val impl = this as? StateFlowImpl<T>
    // A foreign MutableStateFlow implementation has no impl to wrap; the
    // receiver already serves as its own read-only-typed view.
    if (impl == null) return this
    return ReadonlyStateFlow(impl)
}

/**
 * The [MutableStateFlow] implementation: a PLAIN DATA holder (flow-program
 * spec §6 implementation note). The stored value, the doorbell binding
 * ([uuid], "" until the first emit or collect lazily binds a global-node
 * field), and the [version] counter the doorbell field carries are all simple
 * data keys — they survive and stay LIVE wherever the object is reachable
 * (typically inside a SharedService VM crossing via SetRef).
 *
 * The members below exist for SAME-COMPONENT interface dispatch only (a
 * receiver the access lowering could not see through still works locally);
 * they delegate straight to the Doorbells.kt statics, and no cross-component
 * call path ever dispatches through them — user call sites are rewritten to
 * the statics by BrsFlowAccessLowering.
 */
internal class StateFlowImpl<T>(initial: T) : MutableStateFlow<T> {
    /** The live value. Read/written by the Doorbells statics, never copied. */
    internal var stored: T = initial

    /** Doorbell identity: "" until bound, then the global-node field suffix. */
    internal var uuid: String = ""

    /** The doorbell version counter (the int the global-node field carries). */
    internal var version: Int = 0

    override var value: T
        get() = stateFlowGetValue(this)
        set(newValue) {
            stateFlowSetValue(this, newValue)
        }

    override suspend fun collect(collector: FlowCollector<T>) {
        stateFlowCollectData(this, collector)
    }
}

/**
 * The read-only view [asStateFlow] returns: plain data holding the source
 * reference. The Doorbells statics unwrap it ([stateFlowImplOf]), so reads and
 * collections against the view hit the SAME stored value and doorbell.
 */
internal class ReadonlyStateFlow<T>(internal val source: StateFlowImpl<T>) : StateFlow<T> {
    override val value: T
        get() = stateFlowGetValue(this)

    override suspend fun collect(collector: FlowCollector<T>) {
        stateFlowCollectData(source, collector)
    }
}
