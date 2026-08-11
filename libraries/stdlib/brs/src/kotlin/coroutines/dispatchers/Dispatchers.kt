/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.coroutines.CoroutineContext

/**
 * Groups various coroutine dispatchers available for use.
 *
 * In BrightScript/Roku, coroutines run on either the render thread (SceneGraph)
 * or Task node threads. This object provides access to dispatchers for both.
 *
 * **In SceneGraph components** dispatched work needs no wiring: the
 * self-scheduling pump ([kotlin.coroutines.pump.PumpScheduler], attached
 * automatically at component init) drains the queue whenever work exists —
 * use `launch {}` ([kotlin.brs.launch]) and never call the pump entry points
 * from component code.
 *
 * **Main-thread drivers** (a `main()` port loop, test harnesses) own their run
 * loop and must call [processCoroutineQueue] and [processCoroutineDelays] on
 * each iteration — `runBlocking` and the kotlin.test device driver's
 * `runPumping` already do:
 *
 * ```kotlin
 * // Main thread event loop
 * while (true) {
 *     val msg = port.waitMessage(10)
 *     processCoroutineQueue()  // Process dispatched coroutine work
 *     processCoroutineDelays() // Check and fire delay callbacks
 *     // ... handle messages
 * }
 * ```
 */
public object Dispatchers {

    /**
     * The default coroutine dispatcher that is used by all standard builders
     * like [launch] and [async] if no dispatcher or other [ContinuationInterceptor]
     * is specified in their context.
     *
     * In BrightScript, this dispatcher enqueues work to be processed by the
     * current thread's run loop. The work is executed when [processCoroutineQueue]
     * is called.
     *
     * This enables proper cooperative scheduling - coroutines can yield to each
     * other and delays work without blocking the thread.
     */
    public val Default: CoroutineDispatcher = DefaultDispatcher

    /**
     * A coroutine dispatcher that confines coroutine execution to the main/UI thread.
     *
     * In BrightScript, this is the SceneGraph render thread. This is the
     * dispatcher [kotlin.brs.componentScope] uses — in components the
     * self-scheduling pump services it automatically.
     *
     * **Note:** Currently, [Main] uses the same implementation as [Default].
     * Both dispatch to the current thread's queue (which, for component code,
     * IS the render thread's queue). True cross-thread confinement will be
     * added in a future milestone.
     */
    public val Main: CoroutineDispatcher = DefaultDispatcher

    /**
     * A coroutine dispatcher that is not confined to any specific thread.
     *
     * It executes the initial continuation of a coroutine in the current call-frame
     * and lets the coroutine resume in whatever thread that is used by the
     * corresponding suspending function, without mandating any specific threading policy.
     *
     * **Note:** Use with caution. [Unconfined] dispatcher should not normally be used
     * in code. It does not go through the queue, so delays and yields may not work
     * as expected.
     */
    public val Unconfined: CoroutineDispatcher = UnconfinedDispatcher

    /**
     * UNSUPPORTED — referencing this in user code is a compile ERROR
     * (`BRS_IO_DISPATCHER_UNSUPPORTED`).
     *
     * The IO/TaskPool pipeline is quarantined: with [TaskPool] uninitialized
     * (always, in supported configurations), IO dispatch silently falls back
     * to the current thread's queue — i.e. it behaves exactly like [Main],
     * which is precisely the trap the diagnostic guards against. The
     * sanctioned mechanism for background work is a typed task:
     * `runTask<T> { ... }` (see kotlin.coroutines.task.TaskRunner).
     *
     * The pipeline is slated to be re-layered as sugar that synthesizes a
     * typed task per block (M3 backlog); deliberate experimentation requires
     * `@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED")`.
     */
    public val IO: CoroutineDispatcher = IODispatcher
}
