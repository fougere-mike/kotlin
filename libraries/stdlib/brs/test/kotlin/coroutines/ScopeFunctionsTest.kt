package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.builderHierarchyTests() {
    suite("Builder hierarchy") {

        test("launch attaches to scope job: cancel scope cancels launch") {
            runBlocking {
                val scopeJob = Job()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var woken: Throwable? = null
                scope.launch {
                    try {
                        delay(10000)
                    } catch (e: Throwable) {
                        woken = e
                    }
                }
                delay(20)
                scopeJob.cancel()
                delay(20)
                assertTrue(woken is CancellationException)
            }
        }

        test("launch on cancelled scope never runs the block") {
            runBlocking {
                val scopeJob = Job()
                scopeJob.cancel()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var ran = false
                val job = scope.launch { ran = true }
                assertTrue(job.isCancelled)
                assertTrue(job.isCompleted)
                delay(20)
                assertFalse(ran)
            }
        }

        test("child failure cancels sibling in same non-supervisor scope") {
            runBlocking {
                val scopeJob = Job()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var siblingWoken = false
                scope.launch {
                    try {
                        delay(10000)
                    } catch (e: CancellationException) {
                        siblingWoken = true
                    }
                }
                scope.launch {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                delay(50)
                assertTrue(scopeJob.isCancelled)
                assertTrue(siblingWoken)
            }
        }

        test("runBlocking root is supervisor: failed launch does not poison siblings") {
            var after = false
            runBlocking {
                launch {
                    delay(10)
                    throw IllegalStateException("reported-not-propagated")
                }
                delay(50)
                // A cancelled root would make this delay throw CancellationException.
                after = true
            }
            assertTrue(after)
        }

        test("async attaches and failure surfaces only at await") {
            runBlocking {
                val d = async<Int> {
                    delay(10)
                    throw IllegalStateException("async-boom")
                }
                delay(50)
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("async-boom", thrown?.message)
            }
        }

        test("launch job completes only after its own launched children") {
            runBlocking {
                var childDone = false
                val outer = launch {
                    launch {
                        delay(40)
                        childDone = true
                    }
                    // outer body ends now; job must hold in Completing
                }
                outer.join()
                assertTrue(childDone)
            }
        }
    }
}
