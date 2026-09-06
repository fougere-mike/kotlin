/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.pump

import kotlin.brs.BrsInline
import kotlin.brs.brsName
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.dispatchers.hasCoroutineWork
import kotlin.coroutines.dispatchers.processCoroutineQueue
import kotlin.coroutines.processCoroutineDelays

/**
 * Self-scheduling coroutine pump for SceneGraph components.
 *
 * Historically, every component using coroutines had to hand-wire a repeating
 * Timer that called [processCoroutineQueue]/[processCoroutineDelays] on each
 * tick, plus a per-component heuristic for when to stop it. The scheduler
 * replaces all of that: the queue and the delay tracker notify it the moment
 * work arrives ([onEnqueue]/[onDelayRegistered]), and it schedules exactly one
 * wakeup per burst of work. Nothing repeats; an idle component costs nothing.
 *
 * Wakeup backends (detected once per app session, cached on the global node):
 * - `"rtq"` — roRenderThreadQueue (Roku OS 15.0+): `PostMessage` to a
 *   per-instance channel; the registered handler drains the queue on the
 *   render thread with sub-frame latency. Device-verified by
 *   `spikes/render-thread-queue-spike` (OS 15.2.4): the handler runs in the
 *   REGISTERING component's own script context, creation feature-detects via
 *   CreateObject-returns-invalid, and posts to unregistered channels are
 *   dropped — hence registration always precedes the first post.
 * - `"timer"` — one-shot Timer child of the host component's `top`
 *   (duration ~1ms, `repeat=false`), re-armed per wakeup. Also used on BOTH
 *   backends to wake at the next `delay()` deadline (roRenderThreadQueue has
 *   no delayed post).
 *
 * Identity: this is a per-GetGlobalAA-scope singleton — the same identity
 * domain as [kotlin.coroutines.dispatchers.CoroutineQueue] and [DelayTracker].
 * On the render thread GetGlobalAA is per-COMPONENT-INSTANCE (spike-verified),
 * so each component gets its own scheduler draining its own queue. On the main
 * thread the scheduler is never attached and both hooks are no-ops —
 * `runBlocking`/`runPumping` own their loops exactly as before.
 *
 * Attachment happens automatically: the compiler injects
 * `__kotlinComponentAttach(m.top, m.global)` (kotlin.brs, ComponentLifecycle.kt)
 * as the first statement of every render component's generated `init()` — it
 * calls [attach] internally — and [kotlin.brs.componentScope]/
 * [kotlin.brs.launch] attach lazily as a fallback.
 */
internal object PumpScheduler {
    private var hostTop: RoSGNode? = null
    private var hostGlobal: RoSGNode? = null

    /** "" until resolved; then "rtq" or "timer". */
    private var backend: String = ""
    private var rtq: RoRenderThreadQueue? = null
    private var rtqChannel: String = ""
    private var rtqRegistered: Boolean = false

    /** Lazily-created ONE-SHOT Timer child of [hostTop] (never repeats). */
    private var timer: RoSGNode? = null

    /** An immediate wakeup (post or ~1ms timer) is already in flight. */
    private var wakeupPending: Boolean = false

    /**
     * Absolute deadline (DelayTracker clock, ms) the timer is currently armed
     * for, or -1 when it is not armed for a delay deadline.
     */
    private var timerArmedForMs: Int = -1

    /** True while [drain] runs — suppresses redundant self-notifications. */
    private var draining: Boolean = false

    /**
     * Attaches the scheduler to its host component. Idempotent; stores refs
     * only — backend resolution, channel registration, and Timer creation are
     * all deferred to the first actual wakeup, so coroutine-free components
     * pay nothing beyond this call.
     */
    fun attach(top: RoSGNode, global: RoSGNode) {
        if (hostTop != null) return
        hostTop = top
        hostGlobal = global
        // Catch-up: work enqueued before attach (possible via the lazy
        // componentScope() path) must not sit unscheduled.
        if (hasCoroutineWork()) ensureImmediateWakeup()
        if (DelayTracker.current.hasPendingDelays()) armForNextDeadline()
    }

    /** Hook: called by CoroutineQueue.enqueue for every dispatched work item. */
    fun onEnqueue() {
        if (hostTop == null || draining) return
        ensureImmediateWakeup()
    }

    /** Hook: called by DelayTracker.register for every new delay. */
    fun onDelayRegistered() {
        if (hostTop == null || draining) return
        // An imminent drain re-arms for the next deadline anyway.
        if (wakeupPending) return
        armForNextDeadline()
    }

    /** Current backend for test assertions: "none" until first wakeup. */
    fun backendName(): String = if (backend == "") "none" else backend

    /**
     * The attached host component's `top`, or null when unattached (main
     * thread, or a component that never ran coroutine machinery). This is the
     * ambient-component oracle for stdlib code with no component receiver
     * (ScopeHandle.run): attach() received the literal `m.top` at component
     * init or via componentScope()/launch — the only ways a coroutine exists
     * in a component.
     */
    fun hostTopOrNull(): RoSGNode? = hostTop

    /**
     * The attached host component's `global`, or null when unattached — the
     * ambient-global oracle for stdlib code with no component receiver
     * (scope carrier detection), mirroring [hostTopOrNull].
     */
    fun hostGlobalOrNull(): RoSGNode? = hostGlobal

    /**
     * Test hook: pin THIS scheduler to the Timer backend (used together with
     * the session-wide cache write in [kotlinPumpForceTimerBackend]).
     */
    fun forceTimerLocally() {
        backend = "timer"
    }

    private fun resolveBackend() {
        if (backend != "") return
        val g = hostGlobal
        var resolved = ""
        if (g != null) {
            val cached = g.getField(BACKEND_CACHE_FIELD)
            if (cached != null) resolved = "$cached"
        }
        if (resolved == "") {
            // First resolution anywhere in this app session: feature-detect
            // and publish the result for every other scheduler instance.
            val q = createRenderThreadQueueOrInvalid()
            resolved = if (q == null) "timer" else "rtq"
            if (q != null) rtq = q
            if (g != null) {
                g.addField(BACKEND_CACHE_FIELD, "string", false)
                g.setField(BACKEND_CACHE_FIELD, resolved)
            }
        }
        backend = resolved
    }

    /**
     * Registers this scheduler's message handler on its own per-instance
     * channel. Posts to unregistered channels are DROPPED by the platform
     * (spike-verified), so this always runs before the first post.
     */
    private fun ensureRtqReady(): Boolean {
        if (rtqRegistered) return true
        var q = rtq
        if (q == null) {
            q = createRenderThreadQueueOrInvalid()
            rtq = q
        }
        if (q == null) return false
        if (rtqChannel == "") {
            rtqChannel = "kotlin.pump." + randomUuid()
        }
        q.addMessageHandler(rtqChannel, brsName(::onKotlinPumpMessage))
        rtqRegistered = true
        return true
    }

    private fun ensureImmediateWakeup() {
        if (wakeupPending) return
        resolveBackend()
        if (backend == "rtq" && ensureRtqReady()) {
            wakeupPending = true
            val q = rtq
            if (q != null) q.postMessage(rtqChannel, "pump")
        } else {
            // Fallback (and pre-OS-15 path): a ~1ms one-shot timer fires on
            // the next render pass.
            backend = "timer"
            wakeupPending = true
            timerArmedForMs = -1
            armTimerSeconds(0.001)
        }
    }

    /**
     * Arms (or cancels) the one-shot timer for the next delay deadline. Keeps
     * an earlier-armed deadline in place; only re-arms when the new deadline
     * is sooner.
     */
    private fun armForNextDeadline() {
        val tracker = DelayTracker.current
        val ms = tracker.msUntilNextDeadline()
        if (ms < 0) {
            // No pending delays: make sure a stale delay arming can't fire.
            if (timerArmedForMs >= 0) {
                val t = timer
                if (t != null) t.setField("control", "stop")
                timerArmedForMs = -1
            }
            return
        }
        val newDeadline = tracker.currentTimeMs() + ms
        if (timerArmedForMs in 0..newDeadline) return
        timerArmedForMs = newDeadline
        val seconds = if (ms < 1) 0.001 else ms / 1000.0
        armTimerSeconds(seconds)
    }

    private fun ensureTimer(): RoSGNode? {
        var t = timer
        if (t == null) {
            val host = hostTop ?: return null
            t = host.createChild("Timer")
            t.setField("id", "__kotlinPumpTimer")
            t.setField("repeat", false)
            t.observeFieldScoped("fire", brsName(::onKotlinPumpTimerFire))
            timer = t
        }
        return t
    }

    private fun armTimerSeconds(seconds: Double) {
        val t = ensureTimer() ?: return
        // stop -> duration -> start replaces any pending fire cleanly.
        t.setField("control", "stop")
        t.setField("duration", seconds)
        t.setField("control", "start")
    }

    /**
     * Drains the queue and fires due delays until quiescent, then re-arms only
     * if work remains. The pass cap keeps a coroutine that re-enqueues forever
     * (e.g. a `yield()` loop) from freezing the render thread: leftover work
     * re-posts and continues on the next wakeup, so long computations actually
     * yield frames.
     */
    fun drain() {
        wakeupPending = false
        timerArmedForMs = -1
        draining = true
        var passes = 0
        while (passes < 256) {
            val ran = processCoroutineQueue()
            val fired = processCoroutineDelays()
            passes++
            if (ran == 0 && fired == 0 && !hasCoroutineWork()) break
        }
        draining = false
        if (hasCoroutineWork()) ensureImmediateWakeup()
        armForNextDeadline()
    }
}

private const val BACKEND_CACHE_FIELD: String = "__kotlinPumpBackend"

/**
 * Native roRenderThreadQueue surface the scheduler needs (Roku OS 15.0+).
 * Creation goes through [createRenderThreadQueueOrInvalid] because feature
 * detection needs the invalid-on-older-OS result (a @BrsCreateObject factory
 * is typed non-null).
 */
internal external interface RoRenderThreadQueue {
    /** Registers [handler] (a BRS function NAME) for [messageId]; render thread only. */
    fun addMessageHandler(messageId: String, handler: String): Any?

    /** Posts to [messageId] from any thread; non-blocking; data is moved. */
    fun postMessage(messageId: String, data: Any?)
}

@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
internal external fun createRenderThreadQueueOrInvalid(): RoRenderThreadQueue?

@BrsInline("return CreateObject(\"roDeviceInfo\").GetRandomUUID()")
internal external fun randomUuid(): String

/**
 * roRenderThreadQueue message handler (signature per ifRenderThreadQueue:
 * `sub Handler(data, msgInfo)`). Runs on the render thread in the REGISTERING
 * component's own script context (spike-verified), so this drains that
 * component's queue.
 */
internal fun onKotlinPumpMessage(data: Any?, msgInfo: Any?) {
    PumpScheduler.drain()
}

/** One-shot pump-timer fire handler (immediate wakeups and delay deadlines). */
internal fun onKotlinPumpTimerFire(event: RoSGNodeEvent) {
    PumpScheduler.drain()
}

/**
 * TEST HOOK: force the Timer backend for this app session (writes the
 * session-wide backend cache on the global node AND pins the calling
 * context's scheduler). Components attached BEFORE the call keep whatever
 * backend they already resolved. Call from a component's `init` before any
 * coroutine work.
 */
public fun kotlinPumpForceTimerBackend(global: RoSGNode) {
    global.addField(BACKEND_CACHE_FIELD, "string", false)
    global.setField(BACKEND_CACHE_FIELD, "timer")
    PumpScheduler.forceTimerLocally()
}

/**
 * TEST HOOK: force the Timer backend for the CALLING context's scheduler only
 * — other components keep their resolved backend. Lets one E2E fixture
 * exercise the Timer wakeup path on an OS 15+ device without flipping the
 * rest of the app run off roRenderThreadQueue.
 */
public fun kotlinPumpForceTimerBackendLocal() {
    PumpScheduler.forceTimerLocally()
}

/**
 * TEST HOOK: the calling context's active pump backend — `"none"` until the
 * first wakeup resolves it, then `"rtq"` or `"timer"`.
 */
public fun kotlinPumpBackendName(): String = PumpScheduler.backendName()

/**
 * The ambient render-thread component's `top` node, or null when there is no
 * component context (main thread, task thread, or a component that never ran
 * coroutine machinery) — [PumpScheduler.hostTopOrNull] published for
 * dependency klibs (test-hook-style public; the kotlinPump* precedent).
 *
 * This is THE ambient-component oracle for library code with no component
 * receiver: the flow klib's `flowOn(Dispatchers.Task)`/`spawnTask` guided
 * render-context guards read it (the shareOn/ScopeHandle.run guard pattern,
 * which uses the internal form directly).
 */
public fun kotlinAmbientTopOrNull(): RoSGNode? = PumpScheduler.hostTopOrNull()

/**
 * The ambient render-thread component's `global` node, or null when there is
 * no component context — [PumpScheduler.hostGlobalOrNull] published for
 * dependency klibs, mirroring [kotlinAmbientTopOrNull]. The StateFlow
 * doorbell carrier (flow-program hot tier) reaches `m.global` through this.
 */
public fun kotlinAmbientGlobalOrNull(): RoSGNode? = PumpScheduler.hostGlobalOrNull()
