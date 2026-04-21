/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.concurrent.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.task.TaskPool

/**
 * A coroutine dispatcher that dispatches work to background Task threads.
 *
 * In BrightScript/Roku, this dispatcher uses [TaskPool] to execute work on
 * SceneGraph Task nodes, which run on separate threads from the render thread.
 *
 * ## Requirements
 *
 * [TaskPool] must be initialized before using this dispatcher:
 *
 * ```kotlin
 * class MainScene : SceneComponent() {
 *     init {
 *         TaskPool.initialize(top, poolSize = 4)
 *     }
 * }
 * ```
 *
 * ## Fallback Behavior
 *
 * If TaskPool is not initialized, this dispatcher falls back to [CoroutineQueue],
 * which executes work on the current thread. This allows code to work in both
 * render-thread-only and Task-enabled scenarios.
 *
 * ## Usage
 *
 * Prefer using [withContext] for IO-bound work:
 *
 * ```kotlin
 * val data = withContext(Dispatchers.IO) {
 *     // This runs on a Task thread
 *     val http = RoUrlTransfer.create()
 *     http.setUrl("https://api.example.com/data")
 *     http.getToString()
 * }
 * // Back on render thread
 * label.setField("text", data)
 * ```
 *
 * @see Dispatchers.IO
 * @see TaskPool
 * @see withContext
 */
internal object IODispatcher : CoroutineDispatcher() {

    /**
     * IO operations should always be dispatched to a background thread.
     *
     * @return Always `true` to ensure work goes through the Task pool
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    /**
     * Dispatches a runnable to a Task thread.
     *
     * If TaskPool is not initialized, falls back to the render-thread queue.
     *
     * @param context The coroutine context
     * @param block The work to dispatch
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!TaskPool.isInitialized) {
            // Fallback: run on the render-thread queue
            // This allows basic coroutine functionality even without Task support
            CoroutineQueue.enqueue(block)
            return
        }

        // Submit to TaskPool for background execution
        // Note: This is a simple dispatch without continuation tracking.
        // For full context switching with results, use withContext(Dispatchers.IO).
        TaskPool.instance.submitWithContinuation(
            block = { block.run() },
            continuation = object : kotlin.coroutines.Continuation<Any?> {
                override val context: CoroutineContext = context
                override fun resumeWith(result: Result<Any?>) {
                    // Result is ignored for simple dispatch
                    // Errors should be handled by the block itself
                    result.exceptionOrNull()?.let { e ->
                        // Log or handle the error
                        // In a full implementation, this could propagate to an exception handler
                        println("IODispatcher: uncaught exception in dispatched block: ${e.message}")
                    }
                }
            }
        )
    }
}
