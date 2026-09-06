/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.lifecycle

import kotlin.brs.ComponentBase
import kotlin.brs.awaitReady
import kotlin.brs.kotlinLifecycleWatchdogFires
import kotlin.brs.kotlinLifecycleWatchdogMillis
import kotlin.brs.retire
import kotlin.brs.revive
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.builders.runBlocking
import kotlin.test.*

/**
 * Component lifecycle — the MAIN-THREAD-legal subset (runBlocking regime, no
 * ambient render component): guided context ISEs, the hasFunc-based retire
 * guard on a plain node, and the watchdog test hooks. Gate/driver/retire
 * round trips are render-thread-only by design: E2E Suite 11.
 */
fun TestRunner.componentLifecycleTests() {
    suite("ComponentLifecycle (awaitReady/retire/revive)") {
        test("awaitReady outside component context throws the guided ISE") {
            var thrown: Throwable? = null
            runBlocking {
                try {
                    lifecycleProbeAwait()
                } catch (e: Throwable) {
                    thrown = e
                }
            }
            assertTrue(thrown is IllegalStateException, "awaitReady off-component should throw IllegalStateException")
            assertEquals(
                "awaitReady must be called from a render-thread component context (like runTask)",
                "${thrown?.message}"
            )
        }

        test("retire on a plain node throws the guided not-a-component ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                retire(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "retire on a plain node should throw IllegalStateException")
            assertTrue(
                "${thrown?.message}".contains("is not a Kotlin render component"),
                "guided message expected, got: ${thrown?.message}"
            )
        }

        test("revive on a plain node throws the guided not-a-component ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                revive(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "revive on a plain node should throw IllegalStateException")
        }

        test("watchdog hooks: counter starts at zero and millis override is accepted") {
            assertEquals(0, kotlinLifecycleWatchdogFires())
            kotlinLifecycleWatchdogMillis(400)
            kotlinLifecycleWatchdogMillis(30_000)
        }
    }
}

// awaitReady is an extension on ComponentBase; off-thread there is no
// component, so the probe supplies a throwaway receiver — the guard fires on
// the ambient oracle before the receiver is ever touched.
private object LifecycleNoComponent : ComponentBase()

private suspend fun lifecycleProbeAwait() {
    LifecycleNoComponent.awaitReady()
}
