/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.task

import kotlin.brs.*
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode

/**
 * SceneGraph Task component for executing coroutine work on a background thread.
 *
 * This component is used internally by [TaskPool] to execute work blocks on Task threads.
 * Results are communicated back to the render thread via field observers.
 *
 * ## Worker Registry Pattern
 *
 * Function references (lambdas) do NOT survive the Task thread boundary in BrightScript
 * because Task threads receive a clone of `m`. To work around this, we use a **worker
 * registry pattern**:
 *
 * 1. The compiler extracts `withContext(IO)` blocks to top-level worker functions
 * 2. Worker functions are registered by name on `m.global.__kotlinIOWorkers` at module init
 * 3. [TaskPool] passes the worker name (String) + captures (RoAssociativeArray) to this Task
 * 4. This Task looks up the worker function by name and invokes it with the captures
 *
 * ## Thread Model
 *
 * - `executeWork()` runs on the Task thread
 * - Field observers on `isComplete` fire on the render thread
 * - Only serializable data (strings, arrays, assoc arrays) crosses the boundary
 *
 * ## Fields
 *
 * - `workId` (Integer): Unique ID for request/response matching
 * - `workData` (AssocArray): Contains either:
 *   - `workerName` (String) + `captures` (AssocArray): Worker registry pattern
 *   - `block` (Function): Legacy direct lambda (unreliable)
 * - `completionValue` (Dynamic): Result value from successful execution
 * - `completionError` (String): Error message if execution failed
 * - `isComplete` (Boolean): Set to true when work completes (success or failure)
 *
 * @see TaskPool
 */
@BrsSceneGraphComponent(extends = "Task")
internal class CoroutineTask : ComponentBase() {

    init {
        // Set the function to call when the Task's control field changes to "run"
        top.setField("functionName", "executeWork")
    }

    /**
     * Executes the work specified in workData.
     *
     * This function is called on the Task thread when the Task's control field
     * is set to "run". It retrieves the work specification from workData and
     * executes it using either:
     *
     * 1. **Worker Registry Pattern** (reliable): Looks up `workerName` in the global
     *    worker registry (`m.global.__kotlinIOWorkers`) and invokes it with `captures`.
     *
     * 2. **Direct Lambda** (unreliable): Attempts to invoke `block` directly. This often
     *    fails because function references don't survive the Task thread boundary.
     *
     * The result is stored in `completionValue` (or error in `completionError`).
     * The `isComplete` field is set to true when done, triggering the render-thread
     * observer to resume the waiting continuation.
     */
    @BrsExport
    fun executeWork() {
        val workData = top.getField("workData") as? RoAssociativeArray
        if (workData == null) {
            top.setField("completionError", "No work data provided")
            top.setField("isComplete", true)
            return
        }

        // Check for worker registry pattern (preferred)
        val workerName = workData.lookup("workerName") as? String
        if (workerName != null) {
            executeWorkerByName(workerName, workData.lookup("captures"))
            return
        }

        // Fall back to direct lambda invocation (legacy, unreliable)
        val work = workData.lookup("block")
        if (work == null) {
            top.setField("completionError", "No workerName or block in workData")
            top.setField("isComplete", true)
            return
        }

        try {
            // Cast to function and invoke
            @Suppress("UNCHECKED_CAST")
            val workBlock = work as () -> Any?
            val result = workBlock.invoke()
            wrapAndSetResult(result)
        } catch (e: Throwable) {
            top.setField("completionError", e.message ?: "Unknown error during work execution")
            top.setField("isComplete", true)
        }
    }

    /**
     * Executes a worker function from the global registry.
     *
     * Workers are registered on `m.global.__kotlinIOWorkers` by compiler-generated
     * initialization code. Each worker is a top-level function that takes a captures
     * object and returns a result.
     *
     * @param workerName The name of the registered worker function
     * @param captures The captured variables to pass to the worker
     */
    private fun executeWorkerByName(workerName: String, captures: Dynamic?) {
        try {
            // Look up the worker registry on m.global
            val workers = global.getField("__kotlinIOWorkers") as? RoAssociativeArray
            if (workers == null) {
                top.setField("completionError", "Worker registry not initialized (m.global.__kotlinIOWorkers is null)")
                top.setField("isComplete", true)
                return
            }

            // Look up the worker function by name
            val worker = workers.lookup(workerName)
            if (worker == null) {
                top.setField("completionError", "Worker not found: $workerName")
                top.setField("isComplete", true)
                return
            }

            // Invoke the worker with captures
            @Suppress("UNCHECKED_CAST")
            val workerFn = worker as (Dynamic?) -> Any?
            val result = workerFn(captures)
            wrapAndSetResult(result)
        } catch (e: Throwable) {
            top.setField("completionError", "Worker execution failed: ${e.message}")
            top.setField("isComplete", true)
        }
    }

    /**
     * Wraps the result in an associative array and sets completion fields.
     */
    private fun wrapAndSetResult(result: Any?) {
        // Wrap the result in an assocarray for transport
        val resultWrapper = RoAssociativeArray.create()
        resultWrapper.addReplace("value", result)
        top.setField("completionValue", resultWrapper)
        top.setField("isComplete", true)
    }
}
