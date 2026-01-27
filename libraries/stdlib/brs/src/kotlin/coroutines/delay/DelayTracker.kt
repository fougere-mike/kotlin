/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.delay

import kotlin.brs.roku.RoTimespan

/**
 * Thread-local delay tracker using roTimespan for precise timing.
 *
 * Each thread in BrightScript (main thread, render thread, Task threads) maintains
 * its own DelayTracker instance via the `m` scope, providing thread-local behavior.
 *
 * Usage:
 * 1. Get the current thread's tracker: `DelayTracker.current`
 * 2. Register delays: `tracker.register(delayMs) { callback }`
 * 3. Call `tick()` from your run loop to check and fire completed delays
 *
 * Example integration in a run loop:
 * ```kotlin
 * while (true) {
 *     val msg = port.getMessage()
 *     processCoroutineQueue()
 *     processCoroutineDelays()  // Calls DelayTracker.current.tick()
 *     // ... handle messages
 * }
 * ```
 */
public class DelayTracker {
    private val timespan: RoTimespan = RoTimespan.create()
    private val pendingDelays: MutableList<PendingDelay> = mutableListOf()

    init {
        timespan.mark()
    }

    /**
     * Internal representation of a pending delay.
     */
    private class PendingDelay(
        val deadlineMs: Int,
        val callback: () -> Unit
    )

    /**
     * Returns the current time in milliseconds since this tracker was created.
     */
    public fun currentTimeMs(): Int = timespan.totalMilliseconds()

    /**
     * Registers a delay callback. The callback will fire when [tick] is called
     * after the deadline has passed.
     *
     * @param delayMs The delay time in milliseconds.
     * @param callback The callback to invoke when the delay expires.
     */
    public fun register(delayMs: Long, callback: () -> Unit) {
        val deadline = currentTimeMs() + delayMs.toInt()
        pendingDelays.add(PendingDelay(deadline, callback))
    }

    /**
     * Checks all pending delays and fires any whose deadlines have passed.
     * Call this from your run loop on each iteration.
     *
     * This method processes delays in the order they were registered, but fires
     * all delays whose deadlines have passed in a single tick.
     *
     * @return The number of delays that fired.
     */
    public fun tick(): Int {
        if (pendingDelays.isEmpty()) return 0

        val now = currentTimeMs()
        var firedCount = 0

        // Use index-based removal to avoid iterator issues during callback execution
        var i = 0
        while (i < pendingDelays.size) {
            val delay = pendingDelays[i]
            if (now >= delay.deadlineMs) {
                pendingDelays.removeAt(i)
                delay.callback()
                firedCount++
                // Don't increment i since we removed an element
            } else {
                i++
            }
        }

        return firedCount
    }

    /**
     * Checks if there are any pending delays.
     *
     * @return `true` if there are pending delays waiting to fire.
     */
    public fun hasPendingDelays(): Boolean = pendingDelays.isNotEmpty()

    /**
     * Returns the number of pending delays.
     */
    public fun pendingCount(): Int = pendingDelays.size

    public companion object {
        // Thread-local storage via the BrightScript `m` scope.
        // Each thread's `m` is independent, so this gives thread-local behavior.
        // The backing field is stored in the module scope's `m`.
        private var _instance: DelayTracker? = null

        /**
         * Gets the current thread's [DelayTracker] instance.
         *
         * In BrightScript, each thread (main, render, Task) has its own `m` scope,
         * so this property automatically provides thread-local behavior.
         */
        public val current: DelayTracker
            get() {
                if (_instance == null) {
                    _instance = DelayTracker()
                }
                return _instance!!
            }
    }
}
