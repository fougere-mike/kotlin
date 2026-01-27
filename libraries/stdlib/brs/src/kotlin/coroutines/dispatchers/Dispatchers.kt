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
 * **Important:** For dispatchers to work correctly, your run loop must call
 * [processCoroutineQueue] and [processCoroutineDelays] on each iteration:
 *
 * ```kotlin
 * // Main thread event loop
 * while (true) {
 *     val msg = port.getMessage()
 *     processCoroutineQueue()  // Process dispatched coroutine work
 *     processCoroutineDelays() // Check and fire delay callbacks
 *     // ... handle messages
 * }
 *
 * // Render thread - use Timer node with observeField("fire", "onTick")
 * fun onTick() {
 *     processCoroutineQueue()
 *     processCoroutineDelays()
 * }
 *
 * // Task thread
 * while (running) {
 *     val msg = port.waitMessage(10)  // Short timeout for responsive delays
 *     processCoroutineQueue()
 *     processCoroutineDelays()
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
     * In BrightScript, this is the SceneGraph render thread. Use this dispatcher
     * when you need to update UI elements from a coroutine.
     *
     * **Note:** Currently, [Main] uses the same implementation as [Default].
     * Both dispatch to the current thread's queue. True render-thread confinement
     * will be added in a future milestone.
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
     * A coroutine dispatcher that is designed for offloading blocking IO tasks
     * to a shared pool of threads.
     *
     * In BrightScript, this would ideally dispatch to Task nodes for background work.
     * Currently, [IO] uses the same implementation as [Default] and dispatches to
     * the current thread's queue.
     *
     * True Task node dispatch will be added in a future milestone (TaskPool).
     */
    public val IO: CoroutineDispatcher = DefaultDispatcher
}
