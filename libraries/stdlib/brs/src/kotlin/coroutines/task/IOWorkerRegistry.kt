/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.task

import kotlin.brs.Dynamic
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Registry for IO worker functions that can be executed on Task threads.
 *
 * ## Why This Exists
 *
 * BrightScript Task threads receive a **clone** of `m` (the component scope).
 * This means function references (lambdas, closures) stored in field data
 * become invalid when accessed from a Task thread.
 *
 * The Worker Registry Pattern solves this by:
 * 1. Storing worker functions on `m.global.__kotlinIOWorkers` (accessible via thread rendezvous)
 * 2. Passing only the worker **name** (String) and **captures** (RoAssociativeArray) to Tasks
 * 3. Having the Task look up and invoke the worker by name
 *
 * ## Usage
 *
 * ### 1. Define a Worker Function
 *
 * Workers are top-level functions that take a captures object and return a result:
 *
 * ```kotlin
 * fun fetchDataWorker(captures: Dynamic?): String {
 *     val cap = captures as? RoAssociativeArray ?: return ""
 *     val url = cap.lookup("url") as? String ?: return ""
 *
 *     val http = RoUrlTransfer.create()
 *     http.setUrl(url)
 *     return http.getToString() ?: ""
 * }
 * ```
 *
 * ### 2. Register the Worker
 *
 * Call `register` during your Scene's initialization:
 *
 * ```kotlin
 * class MainScene : SceneComponent() {
 *     init {
 *         TaskPool.initialize(top)
 *         IOWorkerRegistry.register("fetchData", ::fetchDataWorker)
 *     }
 * }
 * ```
 *
 * ### 3. Execute the Worker
 *
 * Use `runIOWorker` from a coroutine:
 *
 * ```kotlin
 * val data = runIOWorker<String>("fetchData") {
 *     put("url", "https://api.example.com/data")
 * }
 * label.setField("text", data)
 * ```
 *
 * ## Thread Safety
 *
 * - Workers are stored on `m.global`, which is accessible from all threads via rendezvous
 * - The captures object is cloned to the Task thread (only serializable data survives)
 * - Results are passed back via field observers on the render thread
 *
 * @see TaskPool
 * @see runIOWorker
 */
public object IOWorkerRegistry {

    /**
     * Registers a worker function that can be executed on Task threads.
     *
     * The worker function receives a captures object (RoAssociativeArray) and returns a result.
     * It will be stored on `m.global.__kotlinIOWorkers` and looked up by name when invoked.
     *
     * @param name The unique name for this worker
     * @param worker The worker function: (captures: Dynamic?) -> Any?
     */
    public fun register(name: String, worker: (Dynamic?) -> Any?) {
        // We need access to m.global, which requires a SceneGraph context
        // This is typically called from a Scene's init block, so we can access it via the global node
        // For now, store in a local registry that gets synced to m.global when TaskPool initializes

        pendingRegistrations[name] = worker
    }

    /**
     * Registers a typed worker function for convenience.
     *
     * This is a type-safe wrapper around [register] for workers with specific input/output types.
     *
     * @param name The unique name for this worker
     * @param worker The worker function that takes captures and returns a typed result
     */
    public inline fun <reified T> registerTyped(name: String, crossinline worker: (RoAssociativeArray?) -> T) {
        register(name) { captures ->
            worker(captures as? RoAssociativeArray)
        }
    }

    /**
     * Syncs pending registrations to m.global.
     *
     * This is called internally by TaskPool during initialization, after we have
     * access to the global node.
     *
     * @param globalNode The m.global node to store workers on
     */
    internal fun syncToGlobal(globalNode: RoSGNode) {
        // Get or create the workers registry on m.global
        var workers = globalNode.getField("__kotlinIOWorkers") as? RoAssociativeArray
        if (workers == null) {
            workers = RoAssociativeArray.create()
            globalNode.addField("__kotlinIOWorkers", "assocarray", false)
            globalNode.setField("__kotlinIOWorkers", workers)
        }

        // Add all pending registrations
        val keys = pendingRegistrations.keys.toList()
        for (name in keys) {
            val worker = pendingRegistrations[name]
            if (worker != null) {
                workers.addReplace(name, worker)
            }
        }

        // Clear pending registrations (they're now on m.global)
        pendingRegistrations.clear()
    }

    // Pending registrations before m.global is available
    private val pendingRegistrations = mutableMapOf<String, (Dynamic?) -> Any?>()
}

/**
 * Executes a registered IO worker on a Task thread.
 *
 * This is the safe way to run IO operations because it uses the Worker Registry Pattern,
 * which only passes serializable data (strings, arrays, assoc arrays) across the thread boundary.
 *
 * ## Example
 *
 * ```kotlin
 * // First, register the worker (in Scene init):
 * IOWorkerRegistry.register("fetchUser") { captures ->
 *     val cap = captures as? RoAssociativeArray ?: return@register null
 *     val userId = cap.lookup("userId") as? String ?: return@register null
 *     // ... fetch user from API ...
 * }
 *
 * // Then, use it from a coroutine:
 * val user = runIOWorker<User>("fetchUser") {
 *     put("userId", "12345")
 * }
 * ```
 *
 * @param workerName The name of the registered worker
 * @param capturesBuilder A builder to set captured variables (optional)
 * @return The result from the worker
 * @throws IllegalStateException if TaskPool is not initialized
 * @throws RuntimeException if the worker execution fails
 */
public suspend inline fun <reified T> runIOWorker(
    workerName: String,
    capturesBuilder: RoAssociativeArray.() -> Unit = {}
): T {
    if (!TaskPool.isInitialized) {
        throw IllegalStateException(
            "TaskPool not initialized. Call TaskPool.initialize(top) in your Scene's init block."
        )
    }

    val captures = RoAssociativeArray.create()
    captures.capturesBuilder()

    return suspendCoroutine { continuation ->
        @Suppress("UNCHECKED_CAST")
        TaskPool.instance.submitIOWork(
            workerName,
            captures as Dynamic?,
            continuation as Continuation<T>
        )
    }
}

/**
 * Extension function to add key-value pairs to captures.
 */
public fun RoAssociativeArray.put(key: String, value: Dynamic?) {
    addReplace(key, value)
}
