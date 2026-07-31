/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.task

import kotlin.brs.brsName
import kotlin.brs.Dynamic
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages a pool of [CoroutineTask] nodes for executing background work.
 *
 * TaskPool provides the bridge between coroutines on the render thread and
 * background Task thread execution. It maintains a pool of Task nodes and
 * tracks pending continuations that are waiting for work to complete.
 *
 * ## Architecture
 *
 * ```
 * Render Thread                          Task Thread
 * ─────────────                          ───────────
 * withContext(IO) {
 *   block
 * }
 *     │
 *     ▼
 * TaskPool.submitWithContinuation()
 *     │
 *     ├── stores continuation
 *     │   in pendingContinuations
 *     │
 *     └── sets workData field ──────────► Task.executeWork()
 *         sets control = "run"               │
 *                                            ▼
 *                                        result = block()
 *                                            │
 *                                            ▼
 * onTaskComplete() ◄────────────────────  isComplete = true
 *     │
 *     ├── looks up continuation
 *     │   from pendingContinuations
 *     │
 *     └── continuation.resume(result)
 * ```
 *
 * ## Initialization
 *
 * TaskPool must be initialized from a SceneGraph component before use:
 *
 * ```kotlin
 * class MainScene : SceneComponent() {
 *     init {
 *         TaskPool.initialize(top, poolSize = 4)
 *     }
 * }
 * ```
 *
 * ## Thread Safety
 *
 * - Continuations stay on the render thread and are never sent to Task threads
 * - Only serializable work blocks cross the thread boundary
 * - Field observers automatically fire on the render thread
 *
 * @see withContext
 * @see CoroutineTask
 */
public class TaskPool private constructor(
    private val poolSize: Int,
    private val parentNode: RoSGNode
) {
    /**
     * Companion object providing singleton access and initialization.
     */
    public companion object {
        /**
         * Default number of Task nodes in the pool.
         */
        public const val DEFAULT_POOL_SIZE: Int = 4

        private var _instance: TaskPool? = null

        /**
         * Gets the TaskPool instance.
         *
         * @throws IllegalStateException if TaskPool has not been initialized
         */
        public val instance: TaskPool
            get() = _instance ?: error("TaskPool not initialized. Call TaskPool.initialize() from your Scene's init block.")

        /**
         * Checks if TaskPool has been initialized.
         */
        public val isInitialized: Boolean
            get() = _instance != null

        /**
         * Initializes the TaskPool.
         *
         * Must be called from a SceneGraph component's init block before
         * using `withContext(Dispatchers.IO)`.
         *
         * @param parentNode The node to create Task children under (typically `top` or a dedicated container)
         * @param poolSize The number of Task nodes to create (default: 4)
         * @return The initialized TaskPool instance
         * @throws IllegalStateException if already initialized
         */
        public fun initialize(parentNode: RoSGNode, poolSize: Int = DEFAULT_POOL_SIZE): TaskPool {
            require(_instance == null) { "TaskPool already initialized" }
            _instance = TaskPool(poolSize, parentNode)

            // Sync any pending worker registrations to m.global
            // We need to get m.global from the parent node's scene
            val globalNode = parentNode.getField("global") as? RoSGNode
            if (globalNode != null) {
                IOWorkerRegistry.syncToGlobal(globalNode)
            }

            return _instance!!
        }

        /**
         * Resets the TaskPool (for testing purposes).
         *
         * This removes all Task nodes and clears the singleton instance.
         */
        internal fun reset() {
            _instance?.cleanup()
            _instance = null
        }
    }

    /**
     * Represents a Task node in the pool with its busy state.
     */
    private class PooledTask(
        val node: RoSGNode,
        var isBusy: Boolean = false
    )

    /**
     * Represents a queued work item waiting for an available Task.
     *
     * Work can be specified either as a direct lambda (for simple cases where
     * the lambda is created inline and won't cross thread boundaries) or as a
     * worker name + captures (for the worker registry pattern).
     */
    private class PendingWorkItem(
        val workId: Int,
        val block: (() -> Any?)? = null,
        val workerName: String? = null,
        val captures: Dynamic? = null
    )

    // Task nodes in the pool
    private val tasks = mutableListOf<PooledTask>()

    // Maps workId to the continuation waiting for that work to complete
    private val pendingContinuations = mutableMapOf<Int, Continuation<Any?>>()

    // Work items waiting for an available Task
    private val pendingWork = mutableListOf<PendingWorkItem>()

    // Counter for generating unique work IDs
    private var nextWorkId = 1

    init {
        // Create the Task nodes
        for (i in 0 until poolSize) {
            val task = parentNode.createChild("CoroutineTask")

            // Add required fields to the Task node
            task.addField("workId", "integer", true)
            task.addField("workData", "assocarray", false)
            task.addField("completionValue", "assocarray", true)  // Use assocarray to wrap any value
            task.addField("completionError", "string", true)
            task.addField("isComplete", "boolean", true)

            // Set up the field observer for completion
            task.observeFieldScoped("isComplete", brsName(::onTaskComplete))

            tasks.add(PooledTask(task))
        }
    }

    /**
     * Submits work to be executed on a Task thread with continuation tracking.
     *
     * This method is called by `withContext(Dispatchers.IO)` to execute a block
     * on a background thread and resume the coroutine with the result.
     *
     * **Warning:** This method passes a lambda to the Task thread via field data.
     * Function references do NOT survive the Task thread boundary in BrightScript.
     * Use [submitIOWork] with a registered worker name instead for reliable execution.
     *
     * @param block The work to execute (must not capture render-thread references)
     * @param continuation The continuation to resume with the result
     */
    internal fun <T> submitWithContinuation(
        block: () -> T,
        continuation: Continuation<T>
    ) {
        val workId = nextWorkId++

        // Store the continuation (cast to Any? for type erasure)
        @Suppress("UNCHECKED_CAST")
        pendingContinuations[workId] = continuation as Continuation<Any?>

        // Wrap the block to return Any?
        val wrappedBlock: () -> Any? = { block() }

        // Find an idle Task or queue the work
        val idleTask = tasks.find { !it.isBusy }
        if (idleTask != null) {
            assignWork(idleTask, workId, wrappedBlock)
        } else {
            // All Tasks are busy, queue the work
            pendingWork.add(PendingWorkItem(workId, block = wrappedBlock))
        }
    }

    /**
     * Submits IO work using the worker registry pattern.
     *
     * This method is the correct way to execute code on Task threads in BrightScript.
     * Instead of passing a lambda (which doesn't survive the thread boundary), we pass:
     * - A worker name (String) that identifies a registered top-level function
     * - A captures object containing serializable captured variables
     *
     * The CoroutineTask looks up the worker function by name from `m.global.__kotlinIOWorkers`
     * and invokes it with the captures.
     *
     * ## Worker Registration
     *
     * Workers are registered at module initialization time by the compiler-generated code:
     * ```brightscript
     * sub __registerIOWorkers()
     *     if m.global.__kotlinIOWorkers = invalid then m.global.__kotlinIOWorkers = {}
     *     m.global.__kotlinIOWorkers["fetchData_1"] = __ioWorker_fetchData_1
     * end sub
     * ```
     *
     * @param workerName The name of the registered worker function
     * @param captures An associative array containing captured variables (must be serializable)
     * @param continuation The continuation to resume with the result
     */
    @PublishedApi
    internal fun <T> submitIOWork(
        workerName: String,
        captures: Dynamic?,
        continuation: Continuation<T>
    ) {
        val workId = nextWorkId++

        // Store the continuation (cast to Any? for type erasure)
        @Suppress("UNCHECKED_CAST")
        pendingContinuations[workId] = continuation as Continuation<Any?>

        // Find an idle Task or queue the work
        val idleTask = tasks.find { !it.isBusy }
        if (idleTask != null) {
            assignWorkByName(idleTask, workId, workerName, captures)
        } else {
            // All Tasks are busy, queue the work
            pendingWork.add(PendingWorkItem(workId, workerName = workerName, captures = captures))
        }
    }

    /**
     * Assigns work to a Task node and starts execution (lambda-based, legacy).
     */
    private fun assignWork(task: PooledTask, workId: Int, block: () -> Any?) {
        task.isBusy = true
        val taskNode = task.node

        // Reset completion state
        taskNode.setField("isComplete", false)
        taskNode.setField("completionError", "")
        taskNode.setField("completionValue", null)

        // Set the work ID for matching on completion
        taskNode.setField("workId", workId)

        // Package the work block in an associative array
        val workData = RoAssociativeArray.create()
        workData.addReplace("block", block)
        taskNode.setField("workData", workData)

        // Start the Task
        taskNode.setField("control", "run")
    }

    /**
     * Assigns work to a Task node using worker registry pattern.
     *
     * This is the reliable method for Task thread execution because it passes
     * only serializable data (strings and associative arrays) across the thread boundary.
     */
    private fun assignWorkByName(task: PooledTask, workId: Int, workerName: String, captures: Dynamic?) {
        task.isBusy = true
        val taskNode = task.node

        // Reset completion state
        taskNode.setField("isComplete", false)
        taskNode.setField("completionError", "")
        taskNode.setField("completionValue", null)

        // Set the work ID for matching on completion
        taskNode.setField("workId", workId)

        // Package the worker name and captures - no lambda crossing the boundary
        val workData = RoAssociativeArray.create()
        workData.addReplace("workerName", workerName)
        if (captures != null) {
            workData.addReplace("captures", captures)
        }
        taskNode.setField("workData", workData)

        // Start the Task
        taskNode.setField("control", "run")
    }

    /**
     * Field observer callback when a Task completes.
     *
     * This is called on the render thread when a Task's isComplete field
     * changes to true. It retrieves the result or error, looks up the
     * pending continuation, and resumes it.
     */
    private fun onTaskComplete(event: RoSGNodeEvent) {
        val isComplete = event.getData() as? Boolean ?: false
        if (!isComplete) return

        // getNode() returns the node's id string on device; getRoSGNode() is the node accessor
        val taskNode = event.getRoSGNode()

        // Find the PooledTask wrapper
        val task = tasks.find { it.node.isSameNode(taskNode) } ?: return

        // Get the work ID to look up the continuation
        val workId = taskNode.getField("workId") as? Int ?: return
        val continuation = pendingContinuations.remove(workId) ?: return

        // Check for error
        val error = taskNode.getField("completionError") as? String
        if (!error.isNullOrEmpty()) {
            continuation.resumeWithException(RuntimeException(error))
        } else {
            // Get the result - it may be wrapped in an assocarray or be null
            val result = taskNode.getField("completionValue")
            // If result is an assocarray wrapper, extract the value
            val value = if (result is RoAssociativeArray) {
                result.lookup("value")
            } else {
                result
            }
            continuation.resume(value)
        }

        // Mark task as available
        task.isBusy = false

        // Stop the Task
        taskNode.setField("control", "stop")

        // Check for queued work
        if (pendingWork.isNotEmpty()) {
            val next = pendingWork.removeAt(0)
            // Dispatch based on whether it's lambda-based or worker-name-based
            if (next.workerName != null) {
                assignWorkByName(task, next.workId, next.workerName, next.captures)
            } else if (next.block != null) {
                assignWork(task, next.workId, next.block)
            }
        }
    }

    /**
     * Cleans up the TaskPool by removing all Task nodes.
     */
    private fun cleanup() {
        for (task in tasks) {
            task.node.setField("control", "stop")
            parentNode.removeChild(task.node)
        }
        tasks.clear()
        pendingContinuations.clear()
        pendingWork.clear()
    }

    /**
     * Gets the number of Tasks currently busy.
     */
    public val busyCount: Int
        get() = tasks.count { it.isBusy }

    /**
     * Gets the number of work items waiting in the queue.
     */
    public val queuedCount: Int
        get() = pendingWork.size

    /**
     * Gets the total pool size.
     */
    public val size: Int
        get() = poolSize
}
