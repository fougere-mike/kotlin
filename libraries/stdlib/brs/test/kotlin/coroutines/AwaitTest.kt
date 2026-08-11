package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.awaitTests() {
    suite("join/await suspension") {

        test("join suspends until launched job completes") {
            var order = ""
            runBlocking {
                val job = launch {
                    delay(30)
                    order = order + "A"
                }
                order = order + "B"
                job.join()
                order = order + "C"
            }
            assertEquals("BAC", order)
        }

        test("join on already-completed job returns immediately") {
            runBlocking {
                val job = launch { }
                job.join()
                job.join()
                assertTrue(job.isCompleted)
            }
        }

        test("join on failed job returns normally") {
            var joined = false
            runBlocking {
                val job = launch(EmptyCoroutineContext) {
                    throw IllegalStateException("boom")
                }
                job.join()
                joined = true
            }
            assertTrue(joined)
        }

        test("join on cancelled job returns normally") {
            runBlocking {
                val job = Job()
                job.cancel()
                job.join()
                assertTrue(job.isCancelled)
            }
        }

        test("await returns async result") {
            runBlocking {
                val d = async {
                    delay(20)
                    21 * 2
                }
                assertEquals(42, d.await())
            }
        }

        test("await on completed deferred is immediate") {
            runBlocking {
                val d = CompletableDeferred<String>()
                d.complete("v")
                assertEquals("v", d.await())
            }
        }

        test("await rethrows async failure") {
            runBlocking {
                val d = async<Int> {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("boom", thrown?.message)
            }
        }

        test("await on cancelled deferred throws CancellationException") {
            runBlocking {
                val d = CompletableDeferred<String>()
                d.cancel()
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is CancellationException)
            }
        }

        test("two coroutines can await the same deferred") {
            runBlocking {
                val d = CompletableDeferred<Int>()
                var sum = 0
                val j1 = launch { sum = sum + d.await() }
                val j2 = launch { sum = sum + d.await() }
                delay(10)
                d.complete(5)
                j1.join()
                j2.join()
                assertEquals(10, sum)
            }
        }

        test("caller cancellation wakes a parked join") {
            runBlocking {
                val gate = Job()               // never completed
                var observed: Throwable? = null
                val waiter = launch {
                    try {
                        gate.join()
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(10)                       // let the waiter park
                waiter.cancel()
                delay(10)                       // let the wakeup dispatch
                assertTrue(observed is CancellationException)
                assertTrue(waiter.isCompleted)
            }
        }

        test("ensureActive and isActive") {
            runBlocking {
                val job = Job()
                job.ensureActive()             // active: no throw
                job.cancel()
                var thrown: Throwable? = null
                try {
                    job.ensureActive()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is CancellationException)
            }
        }
    }
}

fun TestRunner.delayCancellationTests() {
    suite("delay/yield cancellation") {

        test("cancel wakes a coroutine parked in delay") {
            runBlocking {
                var observed: Throwable? = null
                var after = false
                val sleeper = launch {
                    try {
                        delay(10000)
                        after = true
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(20)                    // let it park
                sleeper.cancel()
                delay(20)                    // let the wakeup dispatch
                assertTrue(observed is CancellationException)
                assertFalse(after)
                assertTrue(sleeper.isCompleted)
            }
        }

        test("delay entry check throws when already cancelled") {
            runBlocking {
                var thrown: Throwable? = null
                val job = launch {
                    val self = coroutineContext[Job]
                    self?.cancel()
                    try {
                        delay(10)
                    } catch (e: Throwable) {
                        thrown = e
                    }
                }
                job.join()
                assertTrue(thrown is CancellationException)
            }
        }

        test("stale DelayTracker callback after cancel is a no-op") {
            runBlocking {
                val sleeper = launch {
                    delay(50)
                }
                delay(10)
                sleeper.cancel()
                // Ride past the original deadline: the tracker still fires the
                // registered callback; the once-guard must swallow it.
                delay(100)
                assertTrue(sleeper.isCompleted)
            }
        }

        test("yield entry check throws when cancelled") {
            runBlocking {
                var thrown: Throwable? = null
                val job = launch {
                    coroutineContext[Job]?.cancel()
                    try {
                        yield()
                    } catch (e: Throwable) {
                        thrown = e
                    }
                }
                job.join()
                assertTrue(thrown is CancellationException)
            }
        }
    }
}
