/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.concurrent.Runnable

/**
 * Internal queue for coroutine work items.
 *
 * This queue holds [Runnable] work items that need to be processed by the run loop.
 * When a dispatcher dispatches work, it enqueues the work here. The run loop then
 * calls [processCoroutineQueue] to execute all pending work.
 *
 * In BrightScript, each thread has its own `m` scope, so this queue is effectively
 * thread-local - each thread has its own queue of work to process.
 */
internal object CoroutineQueue {
    // Use a simple list as the queue. Items are added to the end and processed
    // from the beginning (FIFO order).
    private val queue: MutableList<Runnable> = mutableListOf()

    /**
     * Enqueues a work item to be processed later.
     *
     * @param block The work to execute.
     */
    fun enqueue(block: Runnable) {
        queue.add(block)
    }

    /**
     * Processes all pending work items in the queue.
     *
     * Work items are processed in FIFO order. If a work item enqueues more work
     * during execution, that new work will also be processed in this call.
     *
     * @return The number of work items that were processed.
     */
    fun processAll(): Int {
        var count = 0
        // Process until queue is empty, including any work added during processing
        while (queue.isNotEmpty()) {
            val block = queue.removeAt(0)
            block.run()
            count++
        }
        return count
    }

    /**
     * Checks if the queue is empty.
     *
     * @return `true` if there are no pending work items.
     */
    fun isEmpty(): Boolean = queue.isEmpty()

    /**
     * Returns the number of pending work items.
     */
    fun size(): Int = queue.size
}

/**
 * Processes all pending coroutine work on the current thread.
 *
 * Call this function from your run loop on each iteration to process dispatched
 * coroutine work.
 *
 * Example usage in a main thread event loop:
 * ```kotlin
 * while (true) {
 *     val msg = port.getMessage()
 *     processCoroutineQueue()  // Process dispatched work
 *     processCoroutineDelays() // Check and fire delays
 *     // ... handle messages
 * }
 * ```
 *
 * @return The number of work items that were processed.
 */
public fun processCoroutineQueue(): Int = CoroutineQueue.processAll()

/**
 * Checks if there is pending coroutine work to process.
 *
 * @return `true` if there are pending work items in the queue.
 */
public fun hasCoroutineWork(): Boolean = !CoroutineQueue.isEmpty()
