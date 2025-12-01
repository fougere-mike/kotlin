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
// Note: Helper functions like createTask<T>() require intrinsic support
// from the BrightScript backend (createNode function).
// These will be enabled once the intrinsics are implemented.

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
// Note: AsyncTask pattern and helper classes (FetchTask, JsonFetchTask)
// require intrinsic support from the BrightScript backend.
// These will be enabled once createNode, parseJson, etc. are implemented.
