/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test.device

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic
import kotlin.brs.roku.RoMessagePort
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.roku.RoTimespan
import kotlin.brs.typeOf
import kotlin.coroutines.CompletableDeferred
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.Job
import kotlin.coroutines.builders.startCoroutine
import kotlin.coroutines.clearCoroutineDelays
import kotlin.coroutines.dispatchers.clearCoroutineQueue
import kotlin.coroutines.dispatchers.processCoroutineQueue
import kotlin.coroutines.processCoroutineDelays
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Main-thread driver for on-device SceneGraph tests.
 *
 * A test body runs as a coroutine inside [runPumping], which pumps the shared
 * [TestPort] message port. Awaiting a SceneGraph field change ([awaitField],
 * [awaitFieldEquals], [roundTrip]) attaches a port-form observer and suspends;
 * when the matching roSGNodeEvent is dequeued the await resumes with the
 * event's data. Field changes made on the render thread or a task thread are
 * delivered to the main thread's port (proven on device by
 * spikes/port-observe-spike, 8/8, and spikes/task-node-spike, 10/10).
 *
 * Semantics callers must be aware of (spike findings):
 * - Every @SG field fires one initial default-value onChange at component init,
 *   and the driver's own setField calls on observed fields are delivered back
 *   to the port. Awaits therefore tolerate stray events: a predicate mismatch
 *   keeps the await armed instead of failing it.
 * - Cross-field event ordering is not write order; correlate by (field, value),
 *   never by arrival order across different fields.
 */

/**
 * [RoSGNodeEvent.getRoSGNode] with an honest return type: on device it
 * returns invalid when the event's node has been destroyed between the event
 * being queued and being dequeued (the typed interface declares it non-null).
 */
@BrsInline("return event.getRoSGNode()")
private external fun eventNodeOrNull(event: RoSGNodeEvent): RoSGNode?

/**
 * The message port shared by the test driver and all field observers.
 *
 * Created lazily on first use; a test main that already owns a port (e.g. the
 * one attached to its roSGScreen) can install it with [use] before the first
 * await.
 */
public object TestPort {
    private var current: RoMessagePort? = null

    /** The shared message port, created on first access. */
    public val port: RoMessagePort
        get() {
            val existing = current
            if (existing != null) {
                return existing
            }
            val created = RoMessagePort.create()
            current = created
            return created
        }

    /** Installs an externally created port as the shared port. */
    public fun use(port: RoMessagePort) {
        current = port
    }
}

/**
 * One suspended await: which node/field it watches, when it gives up, and how
 * to decide whether an event value satisfies it.
 */
internal class PendingAwait(
    val node: RoSGNode,
    val fieldName: String,
    val predicate: (Dynamic?) -> Boolean,
    val continuation: Continuation<Dynamic?>,
    val timeoutMs: Int,
) {
    val fieldLower: String = fieldName.lowercase()
    private val clock = RoTimespan.create()

    init {
        clock.mark()
    }

    fun isExpired(): Boolean = clock.totalMilliseconds() > timeoutMs

    fun describe(): String =
        "${node.subtype()}.$fieldName (waited ${clock.totalMilliseconds()}ms of ${timeoutMs}ms)"
}

/**
 * Registry of pending field awaits, matched against incoming roSGNodeEvents
 * by [runPumping]'s pump loop.
 */
public object FieldEventBus {
    private val pending = mutableListOf<PendingAwait>()

    internal fun register(entry: PendingAwait) {
        pending.add(entry)
    }

    /**
     * True while some pending await still watches this (node, field). Used to
     * keep the scoped observer alive until the LAST await on that node+field
     * is resolved - unobserving while a sibling await is still armed would
     * starve it.
     */
    private fun stillWatched(node: RoSGNode, fieldLower: String): Boolean {
        for (entry in pending) {
            if (entry.fieldLower == fieldLower) {
                if (entry.node.isSameNode(node)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Matches a field-change event against the pending awaits.
     *
     * A pending await matches when the event's node is the awaited node
     * ([RoSGNode.isSameNode] via [RoSGNodeEvent.getRoSGNode] - never getNode(),
     * which returns the id string), the field name matches case-insensitively
     * (BrightScript is case-insensitive), and the predicate accepts the event
     * data. Matches are unobserved and resumed with the data; non-matching
     * awaits stay armed so stray/initial events are tolerated.
     *
     * Events whose node has since been DESTROYED are dropped: getRoSGNode()
     * returns invalid once the node is gone (proven on device - a stale
     * output event outlived its probe after the next test removed it from
     * the scene), and no pending await can match a dead node anyway.
     */
    public fun dispatch(event: RoSGNodeEvent) {
        val eventField = event.getField().lowercase()
        val eventNode = eventNodeOrNull(event)
        if (eventNode == null) {
            return
        }
        val data = event.getData()

        val matches = mutableListOf<PendingAwait>()
        var i = 0
        while (i < pending.size) {
            val entry = pending[i]
            var matched = false
            if (entry.fieldLower == eventField) {
                if (eventNode.isSameNode(entry.node)) {
                    if (entry.predicate(data)) {
                        matched = true
                    }
                }
            }
            if (matched) {
                matches.add(entry)
                pending.removeAt(i)
            } else {
                i++
            }
        }

        // Resume after the registry walk: resuming re-enters test code, which
        // may register new awaits while we are still iterating.
        for (entry in matches) {
            if (!stillWatched(entry.node, entry.fieldLower)) {
                entry.node.unobserveFieldScoped(entry.fieldName)
            }
            entry.continuation.resume(data)
        }
    }

    /**
     * Fails every await whose own deadline has passed by resuming it with an
     * AssertionError naming the node subtype and field. Only that await's test
     * fails; the run continues.
     */
    public fun expireOverdue() {
        val expired = mutableListOf<PendingAwait>()
        var i = 0
        while (i < pending.size) {
            val entry = pending[i]
            if (entry.isExpired()) {
                expired.add(entry)
                pending.removeAt(i)
            } else {
                i++
            }
        }

        for (entry in expired) {
            if (!stillWatched(entry.node, entry.fieldLower)) {
                entry.node.unobserveFieldScoped(entry.fieldName)
            }
            entry.continuation.resumeWithException(
                AssertionError("awaitField timed out: ${entry.describe()}")
            )
        }
    }

    /**
     * Drops all pending awaits without resuming them. Used when the whole
     * [runPumping] deadline expires and the driver is abandoning the test body.
     */
    public fun cancelAll() {
        for (entry in pending) {
            entry.node.unobserveFieldScoped(entry.fieldName)
        }
        pending.clear()
    }

    /** Human-readable summary of what is still being awaited. */
    public fun pendingDescription(): String {
        if (pending.isEmpty()) {
            return "none"
        }
        var description = ""
        for (entry in pending) {
            if (description.isNotEmpty()) {
                description += "; "
            }
            description += entry.describe()
        }
        return description
    }
}

/**
 * Runs [block] as a coroutine while pumping [port] on the current thread.
 *
 * A [kotlin.coroutines.builders.runBlocking] variant whose event loop also
 * services the message port: each tick processes dispatched coroutine work and
 * expired delays, fails overdue awaits, then waits up to 10ms for a message
 * and routes roSGNodeEvents to [FieldEventBus].
 *
 * If [block] has not completed within [timeoutMs], all pending awaits are
 * cancelled and an AssertionError describing them is thrown (failing the
 * enclosing test, not the run).
 *
 * Start-of-run containment: the port is drained before [block] starts, so
 * events left queued by a previous run (or arriving between runs) are never
 * dispatched into this run's awaits.
 *
 * End-of-run containment: on EVERY exit (result, body exception, whole-test
 * timeout) the run's job is cancelled, all pending awaits are dropped, and the
 * coroutine work queue and pending delays are purged via [clearCoroutineQueue]
 * / [clearCoroutineDelays]. Stdlib cancellation is flags-only, so a timed-out
 * body cannot be truly cancelled - but with its queued resumptions, delay
 * callbacks, and field awaits all discarded (and tests being sequential, so
 * nothing else owns that state), nothing can ever resume it: it stays
 * suspended and inert instead of executing during a later test. Residual
 * limit: a resumption arriving from OUTSIDE the pump (e.g. the task-node
 * completion paths behind withContext(IO), currently quarantined) is not
 * covered by the purge.
 */
public fun <T> runPumping(
    port: RoMessagePort,
    timeoutMs: Int,
    block: suspend CoroutineScope.() -> T,
): T {
    // Start-of-run containment: drop messages queued before this run. A
    // previous run can leak unconsumed roSGNodeEvents - re-observing a field
    // mid-test duplicates delivery (scoped unobserve from main scope does not
    // detach the port observer), and the pump exits on body completion without
    // draining. Left in the port, such an event is dispatched into THIS run's
    // awaits: same field name matches, and if its node was destroyed in the
    // meantime the isSameNode probe crashes ('Dot' Operator on invalid -
    // proven on device by the ComponentObserver rapid-sets test). Draining at
    // start rather than at exit also catches events that arrive from the
    // render thread after the previous run's endRun.
    while (port.getMessage() != null) {
    }

    val job = Job()
    val scope = CoroutineScope(EmptyCoroutineContext + job)
    val deferred = CompletableDeferred<T>()

    val continuation = RunPumpingContinuation(scope, block, deferred)
    continuation.start()

    val clock = RoTimespan.create()
    clock.mark()

    while (!deferred.isCompleted && !deferred.isCancelled) {
        // Process any dispatched coroutine work
        processCoroutineQueue()

        // Process any expired delays
        processCoroutineDelays()

        // Fail awaits whose per-await deadline has passed (run continues)
        FieldEventBus.expireOverdue()

        val msg = port.waitMessage(10)
        if (msg != null) {
            if (typeOf(msg) == "roSGNodeEvent") {
                FieldEventBus.dispatch(msg as RoSGNodeEvent)
            }
        }

        if (clock.totalMilliseconds() > timeoutMs) {
            if (!deferred.isCompleted) {
                if (!deferred.isCancelled) {
                    // Describe the pending awaits before endRun discards them.
                    val stillPending = FieldEventBus.pendingDescription()
                    endRun(job)
                    throw AssertionError(
                        "runPumping timed out after ${timeoutMs}ms; pending awaits: $stillPending"
                    )
                }
            }
        }
    }

    // Normal and body-exception exits: same containment before the result (or
    // the body's exception) is surfaced, so leftovers from launch{}ed children
    // cannot leak into the next test's pump.
    endRun(job)

    // Return the result or throw the exception
    return deferred.getCompleted()
}

/**
 * Containment on run exit: cancel the run's job (flags-only), drop all pending
 * field awaits, and purge queued coroutine work and pending delays so nothing
 * belonging to this run can execute during a later run. Tests are sequential,
 * so everything queued at exit time belongs to the exiting run.
 */
private fun endRun(job: Job) {
    job.cancel()
    FieldEventBus.cancelAll()
    clearCoroutineQueue()
    clearCoroutineDelays()
}

/**
 * Internal completion continuation for runPumping (mirrors the
 * RunBlockingContinuation pattern in coroutines/builders/Builders.kt).
 */
private class RunPumpingContinuation<T>(
    private val scope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> T,
    private val deferred: CompletableDeferred<T>,
) : Continuation<T> {

    override val context: CoroutineContext get() = scope.coroutineContext

    fun start() {
        try {
            block.startCoroutine(scope, this)
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
        }
    }

    override fun resumeWith(result: Result<T>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            deferred.completeExceptionally(exception)
        } else {
            deferred.complete(result.getOrThrow())
        }
    }
}

/**
 * Suspends until [field] on [node] changes to a value accepted by [predicate],
 * then resumes with that value (the event's getData()).
 *
 * Attaches a scoped port-form observer on [TestPort]. Events that fail the
 * predicate leave the await armed - this is what makes initial default-value
 * onChange events and the driver's own echoed writes harmless.
 *
 * If nothing acceptable arrives within [timeoutMs], the await resumes with an
 * AssertionError naming the node subtype and field.
 *
 * Concurrent awaits on the same (node, field) - e.g. from launch{}ed children -
 * are supported: one event resumes EVERY await whose predicate accepts it, and
 * the scoped observer is removed only when the last pending await on that
 * node+field is resolved. Each awaitField call re-invokes observeFieldScoped,
 * so overlapping awaits may briefly duplicate observers; duplicated events are
 * harmless under the predicate re-arm semantics above WITHIN a run, and cannot
 * leak into a later run because [runPumping] drains the port before starting
 * its body (a leaked duplicate whose node was later destroyed crashed the
 * dispatch isSameNode probe - device finding, ComponentObserver rapid-sets).
 */
public suspend fun awaitField(
    node: RoSGNode,
    field: String,
    timeoutMs: Int = 5000,
    predicate: (Dynamic?) -> Boolean = { true },
): Dynamic? {
    return suspendCoroutine { continuation ->
        FieldEventBus.register(PendingAwait(node, field, predicate, continuation, timeoutMs))
        node.observeFieldScopedPort(field, TestPort.port)
    }
}

/**
 * Suspends until [field] on [node] changes to [expected] (values compared with
 * Kotlin equality). Other values leave the await armed.
 */
public suspend fun awaitFieldEquals(
    node: RoSGNode,
    field: String,
    expected: Any?,
    timeoutMs: Int = 5000,
): Dynamic? {
    return awaitField(node, field, timeoutMs) { value -> value == expected }
}

/**
 * Sets [setField] to [value] on [node] and suspends until [awaitFieldName]
 * changes (any value). The await is armed before the write, so a response
 * that fires immediately cannot be missed.
 */
public suspend fun roundTrip(
    node: RoSGNode,
    setField: String,
    value: Any?,
    awaitFieldName: String,
    timeoutMs: Int = 5000,
): Dynamic? {
    return suspendCoroutine { continuation ->
        FieldEventBus.register(PendingAwait(node, awaitFieldName, { true }, continuation, timeoutMs))
        node.observeFieldScopedPort(awaitFieldName, TestPort.port)
        node.setField(setField, value)
    }
}
