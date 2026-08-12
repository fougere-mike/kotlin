package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.builders.withTimeout
import kotlin.coroutines.builders.withTimeoutOrNull
import kotlin.coroutines.builders.TimeoutCancellationException
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

fun TestRunner.scopeFunctionTests() {
    suite("coroutineScope/withContext") {

        test("coroutineScope returns block value and preserves dispatcher") {
            runBlocking {
                val v = coroutineScope {
                    delay(10)
                    "value"
                }
                assertEquals("value", v)
            }
        }

        test("coroutineScope waits for launched children") {
            runBlocking {
                var childDone = false
                coroutineScope {
                    launch {
                        delay(40)
                        childDone = true
                    }
                }
                assertTrue(childDone)
            }
        }

        test("coroutineScope rethrows child failure after cancelling siblings") {
            runBlocking {
                var siblingWoken = false
                var thrown: Throwable? = null
                try {
                    coroutineScope {
                        launch {
                            try {
                                delay(10000)
                            } catch (e: CancellationException) {
                                siblingWoken = true
                                throw e
                            }
                        }
                        launch {
                            delay(10)
                            throw IllegalStateException("scope-boom")
                        }
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("scope-boom", thrown?.message)
                assertTrue(siblingWoken)
            }
        }

        test("coroutineScope failure does not cancel the caller's job") {
            runBlocking {
                try {
                    coroutineScope {
                        throw IllegalStateException("contained")
                    }
                } catch (e: Throwable) {
                    // expected
                }
                // Caller continues: a cancelled caller job would make this throw.
                delay(10)
                assertTrue(true)
            }
        }

        test("withContext returns value with merged context") {
            runBlocking {
                val v = withContext(kotlin.coroutines.dispatchers.Dispatchers.Main) {
                    delay(10)
                    21 * 2
                }
                assertEquals(42, v)
            }
        }

        test("withContext propagates block failure") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withContext(kotlin.coroutines.dispatchers.Dispatchers.Main) {
                        throw IllegalStateException("wc-boom")
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("wc-boom", thrown?.message)
            }
        }
    }
}

fun TestRunner.withTimeoutTests() {
    suite("withTimeout") {

        test("block wins: value returned, no exception") {
            runBlocking {
                val v = withTimeout(5000) {
                    delay(20)
                    "fast"
                }
                assertEquals("fast", v)
            }
        }

        test("timeout expires: TimeoutCancellationException thrown, child woken") {
            runBlocking {
                var childWoken = false
                var thrown: Throwable? = null
                try {
                    withTimeout(60) {
                        try {
                            delay(10000)
                        } catch (e: CancellationException) {
                            childWoken = true
                            throw e
                        }
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is TimeoutCancellationException)
                assertTrue(childWoken)
            }
        }

        test("timeout expiry waits for children to unwind before rethrowing") {
            runBlocking {
                var siblingUnwound = false
                try {
                    withTimeout(40) {
                        launch {
                            try {
                                delay(10000)
                            } catch (e: CancellationException) {
                                siblingUnwound = true
                                throw e
                            }
                        }
                        delay(10000)
                    }
                } catch (e: Throwable) {
                    // by the time withTimeout rethrows, no child is running
                }
                assertTrue(siblingUnwound)
            }
        }

        test("block failure beats timeout and propagates as-is") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeout(5000) {
                        delay(10)
                        throw IllegalStateException("block-boom")
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("block-boom", thrown?.message)
                assertFalse(thrown is TimeoutCancellationException)
            }
        }

        test("stale timeout callback after a win is a no-op") {
            runBlocking {
                val v = withTimeout(50) {
                    delay(10)
                    "won"
                }
                // Ride past the original deadline; the tracker fires the stale
                // callback into a terminal scope job — nothing may explode.
                delay(100)
                assertEquals("won", v)
            }
        }

        test("withTimeout(<=0) throws immediately") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeout(0) {
                        "never"
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is TimeoutCancellationException)
            }
        }

        test("withTimeoutOrNull returns null on its own expiry") {
            runBlocking {
                val a = withTimeoutOrNull(40) {
                    delay(10000)
                    "a"
                }
                assertNull(a)
                val b = withTimeoutOrNull(5000) {
                    delay(10)
                    "b"
                }
                assertEquals("b", b)
            }
        }

        test("nested: inner timeout TCE propagates through outer withTimeoutOrNull") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeoutOrNull(5000) {
                        withTimeout(30) {
                            delay(10000)
                        }
                        "never"
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                // The INNER timeout's TCE is not the outer's instance: it must
                // propagate out as an exception, not become the outer's null.
                assertTrue(thrown is TimeoutCancellationException)
            }
        }
    }
}
