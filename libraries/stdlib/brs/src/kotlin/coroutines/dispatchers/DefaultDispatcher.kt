/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.concurrent.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * The default coroutine dispatcher for BrightScript.
 *
 * This dispatcher enqueues work to the [CoroutineQueue], which is processed by
 * the current thread's run loop when [processCoroutineQueue] is called.
 *
 * Unlike [UnconfinedDispatcher] which runs work immediately, this dispatcher
 * defers execution until the run loop processes the queue. This allows:
 * - Multiple coroutines to be launched before any start running
 * - Proper yielding between coroutines via `yield()`
 * - Non-blocking `delay()` that returns control to the run loop
 *
 * The user must integrate with their run loop:
 * ```kotlin
 * // Main thread event loop
 * while (true) {
 *     val msg = port.getMessage()
 *     processCoroutineQueue()  // Process dispatched coroutine work
 *     processCoroutineDelays() // Check and fire delay callbacks
 *     // ... handle messages
 * }
 * ```
 */
internal object DefaultDispatcher : CoroutineDispatcher() {

    /**
     * Returns `true` because this dispatcher needs to dispatch work to the queue
     * rather than running it immediately.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    /**
     * Enqueues the work to be processed by the run loop.
     *
     * @param context The coroutine context (unused by this dispatcher).
     * @param block The work to execute.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        CoroutineQueue.enqueue(block)
    }

    override fun toString(): String = "Dispatchers.Default"
}
