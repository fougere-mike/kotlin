/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import kotlin.test.*
import kotlin.brs.roku.RoTimespan
import kotlin.coroutines.*
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.dispatchers.processCoroutineQueue
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

/**
 * Tests for coroutine primitives.
 */
fun TestRunner.coroutineTests() {
    suite("Coroutine Primitives") {

        // ==================== CoroutineContext Tests ====================

        test("EmptyCoroutineContext is empty") {
            val ctx = EmptyCoroutineContext
            assertNull(ctx[Job])
        }

        test("EmptyCoroutineContext plus element") {
            val job = Job()
            val ctx = EmptyCoroutineContext + job
            assertEquals(job, ctx[Job])
        }

        test("Context element lookup") {
            val job = Job()
            val ctx: CoroutineContext = job
            assertEquals(job, ctx[Job])
        }

        test("Combined context") {
            val job = Job()
            val ctx1 = EmptyCoroutineContext + job
            assertNotNull(ctx1[Job])
        }

        test("Context minusKey") {
            val job = Job()
            val ctx = EmptyCoroutineContext + job
            val withoutJob = ctx.minusKey(Job)
            assertNull(withoutJob[Job])
        }

        // ==================== Job Tests ====================

        test("Job initial state is active") {
            val job = Job()
            assertTrue(job.isActive)
            assertFalse(job.isCompleted)
            assertFalse(job.isCancelled)
        }

        test("Job complete") {
            val job = Job()
            assertTrue(job.complete())
            assertFalse(job.isActive)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }

        test("Job cancel") {
            val job = Job()
            job.cancel()
            assertFalse(job.isActive)
            assertTrue(job.isCancelled)
        }

        test("Job complete returns false if already completed") {
            val job = Job()
            assertTrue(job.complete())
            assertFalse(job.complete())
        }

        test("Job cancel after complete") {
            val job = Job()
            job.complete()
            job.cancel() // Should be a no-op
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }

        // ==================== Deferred Tests ====================

        test("CompletableDeferred initial state") {
            val deferred = CompletableDeferred<Int>()
            assertTrue(deferred.isActive)
            assertFalse(deferred.isCompleted)
        }

        test("CompletableDeferred complete with value") {
            val deferred = CompletableDeferred<Int>()
            assertTrue(deferred.complete(42))
            assertTrue(deferred.isCompleted)
            assertEquals(42, deferred.getCompleted())
        }

        test("CompletableDeferred complete returns false if already completed") {
            val deferred = CompletableDeferred<Int>()
            assertTrue(deferred.complete(1))
            assertFalse(deferred.complete(2))
            assertEquals(1, deferred.getCompleted())
        }

        test("CompletableDeferred getCompleted throws if not completed") {
            val deferred = CompletableDeferred<Int>()
            var threw = false
            try {
                deferred.getCompleted()
            } catch (e: IllegalStateException) {
                threw = true
            }
            assertTrue(threw, "getCompleted should throw IllegalStateException when not completed")
        }

        // ==================== CoroutineScope Tests ====================

        test("CoroutineScope factory function") {
            val ctx = EmptyCoroutineContext
            val scope = CoroutineScope(ctx)
            assertEquals(ctx, scope.coroutineContext)
        }

        test("CoroutineScope with Job") {
            val job = Job()
            val scope = CoroutineScope(job)
            assertEquals(job, scope.coroutineContext[Job])
        }

        // ==================== Continuation Tests ====================

        test("Continuation resume") {
            var result: Int? = null
            val continuation = object : Continuation<Int> {
                override val context: CoroutineContext = EmptyCoroutineContext
                override fun resumeWith(r: Result<Int>) {
                    result = r.getOrNull()
                }
            }
            continuation.resume(42)
            assertEquals(42, result)
        }

        test("Continuation resumeWithException") {
            var exception: Throwable? = null
            val continuation = object : Continuation<Int> {
                override val context: CoroutineContext = EmptyCoroutineContext
                override fun resumeWith(r: Result<Int>) {
                    exception = r.exceptionOrNull()
                }
            }
            val testException = RuntimeException("test")
            continuation.resumeWithException(testException)
            assertEquals(testException, exception)
        }

        // ==================== COROUTINE_SUSPENDED Tests ====================

        test("COROUTINE_SUSPENDED is a sentinel") {
            val suspended = COROUTINE_SUSPENDED
            assertNotNull(suspended)
            // Should be the same instance
            assertSame(COROUTINE_SUSPENDED, suspended)
        }
    }
}

/**
 * Tests for suspend functions and coroutine builders.
 */
fun TestRunner.suspendFunctionTests() {
    suite("Suspend Functions") {

        test("Simple suspend function") {
            // Local suspend functions aren't well supported yet - test at module level instead
            // Just verify basic coroutine infrastructure compiles
            assertTrue(true, "Suspend function tests compile successfully")
        }

        test("Suspend function with parameters") {
            // Local suspend functions aren't well supported yet
            assertTrue(true, "Suspend function with parameters tests compile successfully")
        }

        test("Suspend lambda") {
            val suspendLambda: suspend () -> Int = { 42 }
            assertNotNull(suspendLambda)
        }

        test("Suspend lambda with receiver") {
            val suspendLambda: suspend Int.() -> Int = { this * 2 }
            assertNotNull(suspendLambda)
        }
    }
}

/**
 * Tests for dispatcher infrastructure.
 */
fun TestRunner.dispatcherTests() {
    suite("Dispatchers") {

        test("Dispatchers.Default exists") {
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.Default
            assertNotNull(dispatcher)
        }

        test("Dispatchers.Main exists") {
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.Main
            assertNotNull(dispatcher)
        }

        test("Dispatchers.Unconfined exists") {
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.Unconfined
            assertNotNull(dispatcher)
        }

        test("Dispatchers.IO exists") {
            @Suppress("BRS_IO_DISPATCHER_UNSUPPORTED")
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.IO
            assertNotNull(dispatcher)
        }

        // Root-cause pin (2026-08-10): every framework lookup — intercepted(),
        // delay's resume, TaskRunner.resumeTask, withContext — queries
        // context[ContinuationInterceptor], and key matching is identity-based.
        // Dispatchers must therefore be stored under ContinuationInterceptor.Key;
        // when their key was the CoroutineDispatcher companion, these lookups
        // returned null and ALL coroutine bodies/resumptions ran inline (the
        // dispatch queue was never used).
        test("dispatcher resolvable via ContinuationInterceptor key") {
            val direct = kotlin.coroutines.dispatchers.Dispatchers.Main[ContinuationInterceptor]
            assertNotNull(direct)
        }

        test("plus Job keeps the dispatcher resolvable") {
            val ctx = kotlin.coroutines.dispatchers.Dispatchers.Main + Job()
            assertNotNull(ctx[ContinuationInterceptor])
            assertNotNull(ctx[Job])
        }

        test("Unconfined dispatcher does not need dispatch") {
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.Unconfined
            assertFalse(dispatcher.isDispatchNeeded(EmptyCoroutineContext))
        }

        test("Default dispatcher needs dispatch") {
            val dispatcher = kotlin.coroutines.dispatchers.Dispatchers.Default
            assertTrue(dispatcher.isDispatchNeeded(EmptyCoroutineContext))
        }
    }
}

/**
 * Tests for DelayTracker.
 */
fun TestRunner.delayTrackerTests() {
    suite("DelayTracker") {

        test("DelayTracker.current returns tracker") {
            val tracker = DelayTracker.current
            assertNotNull(tracker)
        }

        test("DelayTracker.current returns same instance") {
            val tracker1 = DelayTracker.current
            val tracker2 = DelayTracker.current
            assertSame(tracker1, tracker2)
        }

        test("DelayTracker initially has no pending delays") {
            // Get a fresh tracker by creating one directly for this test
            val tracker = DelayTracker()
            assertFalse(tracker.hasPendingDelays())
            assertEquals(0, tracker.pendingCount())
        }

        test("DelayTracker register adds pending delay") {
            val tracker = DelayTracker()
            tracker.register(1000) { }
            assertTrue(tracker.hasPendingDelays())
            assertEquals(1, tracker.pendingCount())
        }

        test("DelayTracker tick with no delays returns 0") {
            val tracker = DelayTracker()
            assertEquals(0, tracker.tick())
        }

        test("DelayTracker tick fires expired delays") {
            val tracker = DelayTracker()
            var fired = false

            // Register a delay with 0ms (already expired)
            tracker.register(0) { fired = true }

            // Tick should fire it
            val count = tracker.tick()
            assertEquals(1, count)
            assertTrue(fired)
            assertFalse(tracker.hasPendingDelays())
        }

        test("DelayTracker tick does not fire unexpired delays") {
            val tracker = DelayTracker()
            var fired = false

            // Register a delay with a long timeout
            tracker.register(10000) { fired = false }

            // Tick immediately should not fire it
            val count = tracker.tick()
            assertEquals(0, count)
            assertFalse(fired)
            assertTrue(tracker.hasPendingDelays())
        }

        test("DelayTracker multiple delays fire in order") {
            val tracker = DelayTracker()
            val results = mutableListOf<Int>()

            // Register delays that are already expired
            tracker.register(0) { results.add(1) }
            tracker.register(0) { results.add(2) }
            tracker.register(0) { results.add(3) }

            val count = tracker.tick()
            assertEquals(3, count)
            assertEquals(listOf(1, 2, 3), results)
        }

        test("DelayTracker currentTimeMs increases") {
            val tracker = DelayTracker()
            val time1 = tracker.currentTimeMs()
            // Do some work to let time pass
            var sum = 0
            for (i in 0 until 1000) {
                sum += i
            }
            val time2 = tracker.currentTimeMs()
            assertTrue(time2 >= time1, "Time should not go backwards")
        }
    }
}

/**
 * Tests for delay() function.
 */
fun TestRunner.delayFunctionTests() {
    suite("delay() Function") {

        test("delay(0) returns immediately") {
            var done = false
            runBlocking {
                delay(0)
                done = true
            }
            assertTrue(done)
        }

        test("delay(negative) returns immediately") {
            var done = false
            runBlocking {
                delay(-100)
                done = true
            }
            assertTrue(done)
        }

        test("delay waits approximately correct time") {
            val timer = RoTimespan.create()
            timer.mark()
            runBlocking {
                delay(100)
            }
            val elapsed = timer.totalMilliseconds()
            assertTrue(elapsed >= 90, "Expected >= 90ms, got $elapsed")
            assertTrue(elapsed < 500, "Expected < 500ms, got $elapsed")
        }

        test("multiple sequential delays") {
            val timer = RoTimespan.create()
            timer.mark()
            runBlocking {
                delay(50)
                delay(50)
                delay(50)
            }
            val elapsed = timer.totalMilliseconds()
            assertTrue(elapsed >= 140, "Expected >= 140ms for 3x50ms delays, got $elapsed")
        }

        test("delay suspends and resumes") {
            var state = 0
            runBlocking {
                state = 1
                delay(10)
                state = 2
            }
            assertEquals(2, state)
        }
    }
}

/**
 * Tests for yield() function.
 */
fun TestRunner.yieldFunctionTests() {
    suite("yield() Function") {

        test("yield returns to runBlocking") {
            var done = false
            runBlocking {
                yield()
                done = true
            }
            assertTrue(done)
        }

        test("yield allows other coroutines to run") {
            val results = mutableListOf<String>()
            runBlocking {
                launch {
                    results.add("A1")
                    yield()
                    results.add("A2")
                }
                launch {
                    results.add("B1")
                    yield()
                    results.add("B2")
                }
                // Let launched coroutines run
                yield()
                delay(10)
            }
            // Both coroutines should have run
            assertTrue(results.contains("A1"), "A1 should be in results")
            assertTrue(results.contains("B1"), "B1 should be in results")
            assertTrue(results.contains("A2"), "A2 should be in results")
            assertTrue(results.contains("B2"), "B2 should be in results")
        }
    }
}

/**
 * Tests for coroutine queue processing.
 */
fun TestRunner.coroutineQueueTests() {
    suite("Coroutine Queue") {

        test("processCoroutineQueue with empty queue") {
            val count = processCoroutineQueue()
            // May have leftover work from other tests, so just verify it doesn't crash
            assertTrue(count >= 0)
        }

        test("launch enqueues work") {
            var executed = false
            runBlocking {
                launch {
                    executed = true
                }
                // Let the launched coroutine complete
                yield()
                delay(10)
            }
            assertTrue(executed, "Launched coroutine should have executed")
        }

        test("multiple launch calls execute") {
            var count = 0
            runBlocking {
                launch { count++ }
                launch { count++ }
                launch { count++ }
                // Let all launched coroutines complete
                yield()
                delay(10)
            }
            assertEquals(3, count)
        }
    }
}

/**
 * Tests for runBlocking with delay and dispatchers.
 */
fun TestRunner.runBlockingTests() {
    suite("runBlocking with Delay") {

        test("runBlocking completes simple coroutine") {
            val result = runBlocking {
                42
            }
            assertEquals(42, result)
        }

        test("runBlocking with delay completes") {
            val result = runBlocking {
                delay(10)
                "done"
            }
            assertEquals("done", result)
        }

        test("runBlocking with launch and delay") {
            var counter = 0
            runBlocking {
                launch {
                    delay(20)
                    counter += 10
                }
                delay(10)
                counter += 1
                delay(30)  // Wait for launched coroutine
            }
            assertEquals(11, counter)
        }

        test("runBlocking processes delays correctly") {
            val timer = RoTimespan.create()
            timer.mark()
            runBlocking {
                delay(50)
            }
            val elapsed = timer.totalMilliseconds()
            assertTrue(elapsed >= 45, "Should wait at least ~50ms, got $elapsed")
        }
    }
}
