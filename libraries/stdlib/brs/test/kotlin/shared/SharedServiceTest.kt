/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.shared

import kotlin.brs.SharedService
import kotlin.brs.canShare
import kotlin.brs.shareOn
import kotlin.brs.sharedFrom
import kotlin.brs.sharedFromOrNull
import kotlin.brs.sharedServiceKeyOf
import kotlin.brs.roku.RoSGNode
import kotlin.test.*

/**
 * Tests for the SharedService publish/acquire surface (shareOn/sharedFrom).
 *
 * This suite runs on the MAIN thread (runBlocking regime), where there is no
 * ambient render-thread component — so it pins exactly the main-thread-legal
 * subset: the guided context ISEs, never-shared liveness, default-key
 * derivation, and canShare()'s off-render answer. The sharing round trips
 * (publish, acquire, republish, liveness against a live stash) are
 * render-thread-only by design and get their device coverage in E2E Suite 9.
 */

/** Top-level so its runtime class name is the bare simple name. */
private class SharedKeyProbe : SharedService()

fun TestRunner.sharedServiceTests() {
    suite("SharedService (shareOn/sharedFrom)") {
        test("shareOn outside component context throws the guided ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                shareOn(node, SharedKeyProbe())
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "shareOn off-component should throw IllegalStateException")
            assertEquals(
                "shareOn must be called from a render-thread component context (like runTask)",
                "${thrown?.message}"
            )
        }

        test("sharedFrom outside component context throws the guided ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                sharedFrom<SharedKeyProbe>(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "sharedFrom off-component should throw IllegalStateException")
            // Exact-message assert doubles as a lowering pin: an un-rewritten
            // call site would surface the brsSharedServiceName stub error here.
            assertEquals(
                "sharedFrom must be called from a render-thread component context (like runTask)",
                "${thrown?.message}"
            )
        }

        test("sharedFromOrNull outside component context throws too") {
            // orNull covers ABSENCE (nothing shared), not caller bugs: a wrong
            // calling context is guided, never swallowed into null.
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                sharedFromOrNull<SharedKeyProbe>(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "sharedFromOrNull off-component should throw IllegalStateException")
            assertTrue(
                "${thrown?.message}".contains("render-thread component context"),
                "message should name the context rule, got: ${thrown?.message}"
            )
        }

        test("isLive() is false on a never-shared instance") {
            assertFalse(SharedKeyProbe().isLive(), "an instance never passed to shareOn cannot be live")
        }

        test("default key derives the runtime class simple name") {
            assertEquals("SharedKeyProbe", sharedServiceKeyOf(SharedKeyProbe()))
        }

        test("canShare() is false off the render thread") {
            assertFalse(canShare(), "no ambient component on the main thread, so sharing is unavailable")
        }
    }
}
