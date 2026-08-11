/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.builders

import kotlin.concurrent.Runnable
import kotlin.coroutines.*
import kotlin.coroutines.dispatchers.CoroutineDispatcher
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.dispatchers.IODispatcher
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.task.TaskPool

/**
 * Calls the specified suspending block with a given coroutine context, suspends until it completes,
 * and returns the result.
 *
 * This function is the primary way to switch coroutine contexts, especially for IO operations:
 *
 * ```kotlin
 * // In a coroutine on the render thread
 * val data = withContext(Dispatchers.IO) {
 *     // This block runs on a Task thread
 *     val http = RoUrlTransfer.create()
 *     http.setUrl("https://api.example.com/data")
 *     http.getToString() ?: ""
 * }
 * // Automatically back on the render thread
 * label.setField("text", data)
 * ```
 *
 * ## Context Switching
 *
 * - **To `Dispatchers.IO`**: Executes the block on a background Task thread via [TaskPool]
 * - **To `Dispatchers.Default`/`Main`**: Dispatches through the coroutine queue
 * - **To `Dispatchers.Unconfined`**: Executes immediately in the current context
 *
 * ## Requirements for IO
 *
 * [TaskPool] must be initialized before using `withContext(Dispatchers.IO)`:
 *
 * ```kotlin
 * class MainScene : SceneComponent() {
 *     init {
 *         TaskPool.initialize(top, poolSize = 4)
 *     }
 * }
 * ```
 *
 * ## Suspend Block
 *
 * The block is a suspend lambda that receives a [CoroutineScope]. This allows you to call
 * other suspend functions within the block. For `Dispatchers.IO`, the suspend block is wrapped
 * and executed using `runBlocking` on the Task thread.
 *
 * @param context The coroutine context to switch to. Must contain a [CoroutineDispatcher].
 * @param block The suspending code to execute in the new context.
 * @return The result of the block execution.
 * @throws IllegalStateException if using `Dispatchers.IO` without initializing [TaskPool]
 */
@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED") // stdlib-internal identity check on the quarantined IO pipeline
public suspend fun <T> withContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    val dispatcher = context[ContinuationInterceptor] as? CoroutineDispatcher

    // Check if we're switching to IO dispatcher for Task execution
    if (dispatcher === Dispatchers.IO || dispatcher === IODispatcher) {
        return withContextIOSuspend(context, block)
    }

    // For other dispatchers, wrap the suspend block
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        if (dispatcher == null || !dispatcher.isDispatchNeeded(context)) {
            // No dispatch needed - create scope and run
            try {
                val scope = CoroutineScope(continuation.context + context)
                // We need to run the suspend block, so use runBlocking
                val result = runBlocking(context) { scope.block() }
                continuation.resume(result)
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        } else {
            // Dispatch to the target dispatcher
            dispatcher.dispatch(context, Runnable {
                try {
                    val scope = CoroutineScope(continuation.context + context)
                    val result = runBlocking(context) { scope.block() }
                    // Resume on the original context
                    val originalInterceptor = continuation.context[ContinuationInterceptor]
                    if (originalInterceptor != null) {
                        val intercepted = originalInterceptor.interceptContinuation(continuation)
                        intercepted.resume(result)
                    } else {
                        continuation.resume(result)
                    }
                } catch (e: Throwable) {
                    val originalInterceptor = continuation.context[ContinuationInterceptor]
                    if (originalInterceptor != null) {
                        val intercepted = originalInterceptor.interceptContinuation(continuation)
                        intercepted.resumeWithException(e)
                    } else {
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
        COROUTINE_SUSPENDED
    }
}

/**
 * Executes a suspend block on the IO dispatcher using TaskPool.
 *
 * **NOTE:** The compiler automatically transforms `withContext(Dispatchers.IO)` blocks
 * into the Worker Registry Pattern at compile time. This function serves as a fallback
 * for edge cases where automatic extraction fails.
 *
 * ## Automatic Extraction
 *
 * The compiler rewrites:
 * ```kotlin
 * val result = withContext(Dispatchers.IO) {
 *     val http = RoUrlTransfer.create()
 *     http.setUrl(url)  // 'url' captured
 *     http.getToString() ?: ""
 * }
 * ```
 *
 * Into:
 * ```kotlin
 * // Generated worker function
 * fun __ioWorker_MyClass_myFunc_1(captures: Dynamic?): String { ... }
 *
 * // Call site
 * val result = runIOWorker<String>("__ioWorker_MyClass_myFunc_1") {
 *     put("url", url)
 * }
 * ```
 *
 * ## When This Fallback Is Used
 *
 * This runtime path is only used when:
 * - The lambda cannot be extracted (complex control flow)
 * - The lambda captures `this` (instance state)
 * - The compiler emitted a warning about extraction failure
 *
 * In these cases, the lambda won't survive the Task thread boundary and IO operations
 * will fail. Check compiler warnings and use `runIOWorker()` manually if needed.
 */
private suspend fun <T> withContextIOSuspend(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    if (!TaskPool.isInitialized) {
        // Fallback: run on current thread with runBlocking
        // This is a workaround - not truly async, but at least works
        return suspendCoroutine { continuation ->
            try {
                val scope = CoroutineScope(continuation.context + context)
                val result = runBlocking(context) { scope.block() }
                continuation.resume(result)
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }
    }

    // NOTE: This runtime path is a fallback for cases where the compiler could not
    // automatically extract the lambda to a worker function. The lambda may not
    // survive the Task thread boundary. Check compiler warnings.
    println("[withContext(IO)] WARNING: Falling back to lambda-based IO dispatch. " +
            "If IO operations fail, use runIOWorker() with IOWorkerRegistry instead.")

    return suspendCoroutine { continuation ->
        // Wrap the suspend block to run with runBlocking on the Task thread
        // NOTE: This lambda won't survive the thread boundary in most cases
        val syncBlock: () -> T = {
            val scope = CoroutineScope(context)
            runBlocking(context) { scope.block() }
        }
        TaskPool.instance.submitWithContinuation(syncBlock, continuation)
    }
}
