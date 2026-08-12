package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.awaitAllTests() {
    suite("awaitAll/joinAll") {

        test("awaitAll returns results in input order") {
            runBlocking {
                val slow = async {
                    delay(60)
                    "slow"
                }
                val fast = async {
                    delay(10)
                    "fast"
                }
                val results = awaitAll(slow, fast)
                assertEquals(2, results.size)
                assertEquals("slow", results[0])
                assertEquals("fast", results[1])
            }
        }

        test("awaitAll runs deferreds concurrently, not sequentially") {
            runBlocking {
                val clock = kotlin.brs.roku.RoTimespan.create()
                clock.mark()
                val a = async { delay(80) }
                val b = async { delay(80) }
                awaitAll(a, b)
                val elapsed = clock.totalMilliseconds()
                assertTrue(elapsed < 150, "expected concurrent (<150ms), got $elapsed")
            }
        }

        test("awaitAll of Collection extension") {
            runBlocking {
                val list = ArrayList<Deferred<Int>>()
                list.add(async { 1 })
                list.add(async { 2 })
                list.add(async { 3 })
                val results = list.awaitAll()
                assertEquals(3, results.size)
                assertEquals(6, results[0] + results[1] + results[2])
            }
        }

        test("awaitAll on empty vararg returns empty list") {
            runBlocking {
                val results = awaitAll<Int>()
                assertEquals(0, results.size)
            }
        }

        test("awaitAll with already-completed deferreds is immediate") {
            runBlocking {
                val a = CompletableDeferred<Int>()
                val b = CompletableDeferred<Int>()
                a.complete(1)
                b.complete(2)
                val results = awaitAll(a, b)
                assertEquals(1, results[0])
                assertEquals(2, results[1])
            }
        }

        test("awaitAll rethrows first failure without waiting for the rest") {
            runBlocking {
                val slow = async {
                    delay(5000)
                    "never"
                }
                val failing = async<String> {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                var thrown: Throwable? = null
                val clock = kotlin.brs.roku.RoTimespan.create()
                clock.mark()
                try {
                    awaitAll(slow, failing)
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("boom", thrown?.message)
                assertTrue(clock.totalMilliseconds() < 1000)
                // kotlinx parity: siblings are NOT cancelled by awaitAll
                assertFalse(slow.isCancelled)
                slow.cancel() // clean up so runBlocking can exit
            }
        }

        test("awaitAll delivers the original failure under a non-supervisor parent") {
            // The deferreds share a NON-supervisor parent (the launch job)
            // with the awaiting caller: the failing child's upcall cancels
            // that parent. Own-handlers-first tryFinish ordering guarantees
            // the park settles with the ORIGINAL exception, not the parent's
            // "Job was cancelled" CE — while the parent still gets cancelled.
            runBlocking {
                var caught: Throwable? = null
                val worker = launch {
                    val slow = async {
                        delay(5000)
                        "slow"
                    }
                    val bad = async<String> {
                        delay(10)
                        throw IllegalStateException("boom")
                    }
                    try {
                        awaitAll(slow, bad)
                    } catch (e: Throwable) {
                        caught = e
                    }
                    slow.cancel()
                }
                worker.join()
                assertEquals("boom", caught?.message)
                assertTrue(worker.isCancelled) // structural propagation intact
            }
        }

        test("awaitAll with an already-failed deferred throws on entry") {
            runBlocking {
                val failed = CompletableDeferred<Int>()
                failed.completeExceptionally(IllegalStateException("early"))
                val fine = CompletableDeferred<Int>()
                fine.complete(1)
                var thrown: Throwable? = null
                try {
                    awaitAll(fine, failed)
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("early", thrown?.message)
            }
        }

        test("awaitAll duplicate deferred in input") {
            runBlocking {
                val d = async {
                    delay(10)
                    7
                }
                val results = awaitAll(d, d)
                assertEquals(7, results[0])
                assertEquals(7, results[1])
            }
        }

        test("awaitAll wakes on caller cancellation") {
            runBlocking {
                val never = CompletableDeferred<Int>()
                var observed: Throwable? = null
                val waiter = launch {
                    try {
                        awaitAll(never)
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(20)
                waiter.cancel()
                delay(20)
                assertTrue(observed is CancellationException)
            }
        }

        test("joinAll waits for all jobs including failed ones") {
            runBlocking {
                var count = 0
                val ok = launch {
                    delay(10)
                    count = count + 1
                }
                val bad = launch(EmptyCoroutineContext) {
                    delay(20)
                    throw IllegalStateException("boom")
                }
                joinAll(ok, bad)
                assertEquals(1, count)
                assertTrue(ok.isCompleted)
                assertTrue(bad.isCompleted)
            }
        }

        test("joinAll on empty and completed inputs is immediate") {
            runBlocking {
                joinAll()
                val done = launch { }
                done.join()
                joinAll(done, done)
                assertTrue(done.isCompleted)
            }
        }
    }
}
