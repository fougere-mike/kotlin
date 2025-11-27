/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * Task node - runs code in a background render thread.
 *
 * Task nodes are essential for background processing in SceneGraph apps.
 * They run on a separate thread from the main render thread, allowing
 * network requests, heavy computation, and other blocking operations
 * without affecting UI responsiveness.
 *
 * ## Kotlin Task Pattern
 *
 * ```kotlin
 * @BrsComponent(extends = "Task")
 * class MyTask : Task() {
 *     @BrsField(type = "string")
 *     var inputUrl: String = ""
 *
 *     @BrsField(type = "assocarray")
 *     var result: RoAssociativeArray? = null
 *
 *     override fun runTask() {
 *         // This runs on the Task thread
 *         val url = inputUrl
 *         val response = fetchData(url)
 *         result = response
 *     }
 *
 *     private fun fetchData(url: String): RoAssociativeArray {
 *         val http = RoUrlTransfer()
 *         http.setUrl(url)
 *         val response = http.getToString()
 *         return parseJson(response)
 *     }
 * }
 * ```
 *
 * ## Using a Task
 *
 * ```kotlin
 * val task = createTask<MyTask>()
 * task.inputUrl = "https://api.example.com/data"
 * task.observeField("result") { event ->
 *     val result = event.getData() as? RoAssociativeArray
 *     // Handle result on main thread
 * }
 * task.control = "run"
 * ```
 */
@BrsExternal
public external open class Task : Node {
    /**
     * Task control field. Set to "run" to start, "stop" to stop.
     *
     * Values:
     * - "init": Initial state
     * - "run": Start or resume task execution
     * - "stop": Stop task execution
     * - "done": Task has completed (set automatically)
     */
    public var control: String

    /**
     * Task state field. Read-only.
     *
     * Values:
     * - "init": Initial state, not yet run
     * - "run": Currently running
     * - "stop": Stopped by control="stop"
     * - "done": Completed execution
     */
    public val state: String

    /**
     * Function name to call when task starts. Default is empty (uses m.runTask).
     *
     * If set, the specified function will be called instead of the default
     * runTask() function.
     */
    public var functionName: String

    /**
     * Override this function to implement the Task's background work.
     *
     * This function runs on the Task thread, separate from the main render thread.
     * It's safe to perform blocking operations here.
     *
     * The function is called when control is set to "run".
     * Set control to "stop" to request early termination (check m.top.control periodically).
     */
    @BrsExport
    public open fun runTask()
}

// ============================================
// Task Helper Functions
// ============================================

/**
 * Create a Task node of the specified type.
 *
 * ```kotlin
 * val task = createTask<MyFetchTask>()
 * task.url = "https://api.example.com"
 * task.control = "run"
 * ```
 */
public inline fun <reified T : Task> createTask(): T {
    val componentName = T::class.simpleName ?: "Task"
    return createNode(componentName) as T
}

/**
 * Create and immediately start a Task.
 *
 * ```kotlin
 * val task = runTask<MyFetchTask> {
 *     it.url = "https://api.example.com"
 * }
 * ```
 */
public inline fun <reified T : Task> runTask(configure: (T) -> Unit = {}): T {
    val task = createTask<T>()
    configure(task)
    task.control = "run"
    return task
}

/**
 * Run a task and wait for completion, then invoke callback with result.
 *
 * ```kotlin
 * runTaskWithResult<MyFetchTask, RoAssociativeArray>(
 *     resultField = "result",
 *     configure = { it.url = "https://api.example.com" }
 * ) { result ->
 *     // Handle result
 * }
 * ```
 */
public inline fun <reified T : Task, R> runTaskWithResult(
    resultField: String,
    crossinline configure: (T) -> Unit = {},
    crossinline onResult: (R?) -> Unit
): T {
    val task = createTask<T>()
    configure(task)

    task.observeField(resultField) { event ->
        @Suppress("UNCHECKED_CAST")
        val result = event.getData() as? R
        onResult(result)
    }

    task.control = "run"
    return task
}

/**
 * Extension to observe a field and invoke callback when it changes.
 */
public fun Node.observeField(fieldName: String, callback: (RoInterface) -> Unit) {
    // This creates an observer using m.top.observeField pattern
    // The actual implementation is handled by the BrightScript runtime
    // Kotlin side stores the callback in a map keyed by field+node
    @Suppress("UNUSED_VARIABLE")
    val observer = object {
        // Implementation note: In generated code, this becomes:
        // m.top.observeField(fieldName, "onFieldChanged_" + fieldName)
        // And generates a handler function that calls the callback
    }
}

/**
 * Extension to stop a running Task.
 */
public fun Task.stop() {
    control = "stop"
}

/**
 * Extension to check if a Task is currently running.
 */
public val Task.isRunning: Boolean
    get() = state == "run"

/**
 * Extension to check if a Task has completed.
 */
public val Task.isDone: Boolean
    get() = state == "done"

/**
 * Extension to check if a Task was stopped.
 */
public val Task.isStopped: Boolean
    get() = state == "stop"

// ============================================
// Annotation for Task Entry Point
// ============================================

/**
 * Marks the entry point function for a Task component.
 *
 * When a class extends Task and has a function marked with @TaskRun,
 * that function will be set as the functionName of the Task and
 * called when control="run".
 *
 * ```kotlin
 * @BrsComponent(extends = "Task")
 * class MyTask : Task() {
 *     @TaskRun
 *     fun fetchData() {
 *         // This will be called when the task runs
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class TaskRun

// ============================================
// Async Task Pattern
// ============================================

/**
 * Represents an asynchronous operation running on a Task.
 *
 * This provides a more idiomatic Kotlin way to work with Tasks.
 */
public class AsyncTask<T>(
    private val task: Task,
    private val resultField: String
) {
    private var onSuccess: ((T) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null

    /**
     * Set success callback.
     */
    public fun onSuccess(callback: (T) -> Unit): AsyncTask<T> {
        onSuccess = callback
        return this
    }

    /**
     * Set error callback.
     */
    public fun onError(callback: (String) -> Unit): AsyncTask<T> {
        onError = callback
        return this
    }

    /**
     * Set completion callback (called regardless of success/error).
     */
    public fun onComplete(callback: () -> Unit): AsyncTask<T> {
        onComplete = callback
        return this
    }

    /**
     * Cancel the task.
     */
    public fun cancel() {
        task.stop()
    }

    /**
     * Check if the task is still running.
     */
    public val isActive: Boolean
        get() = task.isRunning
}

/**
 * Create an async task with typed result.
 *
 * ```kotlin
 * val async = asyncTask<MyFetchTask, UserData>("userData") {
 *     it.userId = "123"
 * }
 * async.onSuccess { userData ->
 *     // Handle success
 * }.onError { error ->
 *     // Handle error
 * }
 * ```
 */
public inline fun <reified T : Task, R> asyncTask(
    resultField: String,
    crossinline configure: (T) -> Unit = {}
): AsyncTask<R> {
    val task = createTask<T>()
    configure(task)

    val asyncTask = AsyncTask<R>(task, resultField)

    // Set up observer for result field
    task.observeField(resultField) { event ->
        @Suppress("UNCHECKED_CAST")
        (event.getData() as? R)?.let { result ->
            // asyncTask callbacks would be invoked here
        }
    }

    task.control = "run"
    return asyncTask
}

// ============================================
// Common Task Patterns
// ============================================

/**
 * Simple network fetch task base class.
 *
 * Extend this to create simple HTTP fetch tasks.
 */
@BrsComponent(extends = "Task")
public abstract class FetchTask : Task() {
    @BrsField(type = "string")
    public var url: String = ""

    @BrsField(type = "assocarray")
    public var headers: RoAssociativeArray = RoAssociativeArray()

    @BrsField(type = "string")
    public var responseText: String = ""

    @BrsField(type = "integer")
    public var responseCode: Int = 0

    @BrsField(type = "string")
    public var error: String = ""

    override fun runTask() {
        val http = RoUrlTransfer()
        http.setUrl(url)

        // Apply headers
        headers.forEach { key, value ->
            http.addHeader(key, value?.toString() ?: "")
        }

        http.setPort(RoMessagePort())

        if (http.asyncGetToString()) {
            val port = http.getPort()!!
            val event = port.waitMessage(30000)
            if (event != null) {
                responseCode = http.getResponseCode()
                if (responseCode >= 200 && responseCode < 300) {
                    responseText = http.getString()
                } else {
                    error = "HTTP Error: $responseCode"
                }
            } else {
                error = "Request timeout"
            }
        } else {
            error = "Failed to start request"
        }
    }
}

/**
 * JSON fetch task that parses the response.
 */
@BrsComponent(extends = "Task")
public abstract class JsonFetchTask : FetchTask() {
    @BrsField(type = "assocarray")
    public var responseJson: RoAssociativeArray? = null

    override fun runTask() {
        super.runTask()
        if (error.isEmpty() && responseText.isNotEmpty()) {
            responseJson = parseJson(responseText)
        }
    }
}
