package test.coroutines.flow

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.flow.*

fun TestRunner.flowOperatorsTests() {
    suite("FlowOperators") {

        test("mapTransformsValues") {
            runBlocking {
                assertEquals(listOf(10, 20, 30), flowOf(1, 2, 3).map { value -> value * 10 }.toList())
            }
        }

        test("filterKeepsMatching") {
            runBlocking {
                assertEquals(listOf(2, 4), flowOf(1, 2, 3, 4).filter { value -> value % 2 == 0 }.toList())
            }
        }

        test("filterNotNullDropsNulls") {
            runBlocking {
                assertEquals(listOf(1, 2), flowOf(1, null, 2, null).filterNotNull().toList())
            }
        }

        test("transformEmitsPerValue") {
            runBlocking {
                val out = flowOf(1, 2).transform { value ->
                    emit(value)
                    emit(value * 10)
                }.toList()
                assertEquals(listOf(1, 10, 2, 20), out)
            }
        }

        // onEach's action runs before the value reaches the downstream collector,
        // once per value, interleaved in emission order.
        test("onEachRunsBeforeDownstreamPerValue") {
            runBlocking {
                val log = ArrayList<String>()
                flowOf(1, 2).onEach { value -> log.add("each:" + value) }.collect { value ->
                    log.add("down:" + value)
                }
                assertEquals(listOf("each:1", "down:1", "each:2", "down:2"), log)
            }
        }

        // onStart's action runs BEFORE the upstream body executes; values it emits
        // arrive downstream ahead of upstream values.
        test("onStartEmitsBeforeUpstream") {
            runBlocking {
                val log = ArrayList<Int>()
                val out = flow {
                    log.add(2)
                    emit(20)
                }.onStart {
                    log.add(1)
                    emit(10)
                }.toList()
                assertEquals(listOf(10, 20), out)
                assertEquals(listOf(1, 2), log)
            }
        }

        // onCompletion doFinally matrix 1/3: normal completion fires the action
        // exactly once with a null cause, and the action may emit trailing values.
        test("onCompletionNormalCauseNull") {
            runBlocking {
                var fired = 0
                var cause: Throwable? = null
                val out = flowOf(1, 2).onCompletion { c ->
                    fired++
                    cause = c
                    emit(99)
                }.toList()
                assertEquals(listOf(1, 2, 99), out)
                assertEquals(1, fired)
                assertNull(cause, "onCompletion cause must be null on normal completion")
            }
        }

        // onCompletion doFinally matrix 2/3: an upstream failure is handed to the
        // action AND still propagates out of collect (onCompletion never swallows).
        test("onCompletionSeesUpstreamFailure") {
            runBlocking {
                var cause: Throwable? = null
                var propagated: Throwable? = null
                try {
                    flow {
                        emit(1)
                        throw IllegalArgumentException("boom")
                    }.onCompletion { c -> cause = c }.collect { }
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertTrue(propagated != null, "the upstream failure must propagate out of collect")
                assertTrue(cause is IllegalArgumentException, "onCompletion must see the upstream failure")
                assertEquals("boom", (cause as IllegalArgumentException).message)
            }
        }

        // onCompletion doFinally matrix 3/3: cancelling the collector delivers the
        // CancellationException as the cause (and no value after the cancel point).
        test("onCompletionSeesCancellation") {
            runBlocking {
                var cause: Throwable? = null
                var received = 0
                val job = launch {
                    val self = coroutineContext[Job]!!
                    flow {
                        emit(1)
                        emit(2)
                    }.onCompletion { c -> cause = c }.collect { value ->
                        received++
                        if (value == 1) self.cancel()
                    }
                }
                job.join()
                assertEquals(1, received)
                assertTrue(cause is CancellationException, "onCompletion must see the CancellationException on cancellation")
            }
        }

        // catch sees the upstream exception object and may emit fallback values.
        test("catchEmitsFallbackOnUpstreamFailure") {
            runBlocking {
                var seen: Throwable? = null
                val out = flow {
                    emit(1)
                    throw IllegalArgumentException("boom")
                }.catch { e ->
                    seen = e
                    emit(-1)
                }.toList()
                assertEquals(listOf(1, -1), out)
                assertTrue(seen is IllegalArgumentException, "catch must see the upstream exception")
                assertEquals("boom", (seen as IllegalArgumentException).message)
            }
        }

        // Exception transparency: a DOWNSTREAM (collector-block) exception passes
        // through catch uncaught — catch handles upstream failures only.
        test("catchDoesNotSeeDownstreamException") {
            runBlocking {
                var catchSaw = false
                var propagated = false
                try {
                    flowOf(1).catch { catchSaw = true }.collect { value ->
                        throw IllegalArgumentException("downstream " + value)
                    }
                } catch (e: IllegalArgumentException) {
                    propagated = true
                }
                assertTrue(propagated, "downstream exception must propagate out of collect")
                assertFalse(catchSaw, "catch must not see downstream exceptions")
            }
        }

        // catch must rethrow CancellationException unconditionally (CE extends
        // IllegalStateException on this platform — a bare catch would swallow it):
        // the coroutine actually cancels, catch's action never runs, and an
        // onCompletion downstream of catch sees the CE.
        test("catchRethrowsCancellation") {
            runBlocking {
                var catchRan = false
                var cause: Throwable? = null
                var received = 0
                val job = launch {
                    val self = coroutineContext[Job]!!
                    flow {
                        emit(1)
                        emit(2)
                        emit(3)
                    }.catch { catchRan = true }
                        .onCompletion { c -> cause = c }
                        .collect { value ->
                            received++
                            if (value == 1) self.cancel()
                        }
                }
                job.join()
                assertTrue(job.isCancelled, "the collecting coroutine must actually cancel")
                assertFalse(catchRan, "catch must NOT swallow the CancellationException")
                assertTrue(cause is CancellationException, "onCompletion downstream of catch must see the CE")
                assertEquals(1, received)
            }
        }

        // take(n) delivers exactly n values then aborts the upstream producer:
        // the side-effect counter proves the producer never ran past element n,
        // and the abort machinery never leaks to the caller.
        test("takeLimitsAndAbortsUpstream") {
            runBlocking {
                var produced = 0
                val out = flow {
                    var i = 1
                    while (i <= 5) {
                        produced++
                        emit(i)
                        i++
                    }
                }.take(2).toList()
                assertEquals(listOf(1, 2), out)
                assertEquals(2, produced)
            }
        }

        test("takeMoreThanSizePassesAll") {
            runBlocking {
                assertEquals(listOf(1, 2), flowOf(1, 2).take(5).toList())
            }
        }

        // Two abort users must not cross-trip: first()'s abort thrown through an
        // inner take's machinery must unwind PAST the take (ownership check), so
        // the outer flow body stops instead of continuing to emit(42).
        test("takeAbortDoesNotCrossTripFirst") {
            runBlocking {
                val f = flow {
                    flowOf(1, 2, 3).take(2).collect { value -> emit(value) }
                    emit(42)
                }
                assertEquals(listOf(1, 2, 42), f.toList())
                assertEquals(1, f.first())
            }
        }

        // Upstream failures must PROPAGATE through take's abort machinery, not be
        // captured by it: the state machine treats typed catch clauses as
        // catch-alls (BrsStateMachineBuilder visitTry), so take/collectFirst catch
        // Throwable and discriminate manually — a foreign exception rethrows.
        test("upstreamFailurePropagatesThroughTake") {
            runBlocking {
                var received = 0
                var propagated: Throwable? = null
                try {
                    flow {
                        emit(1)
                        throw IllegalArgumentException("boom")
                    }.take(5).collect { received++ }
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertEquals(1, received)
                assertTrue(propagated is IllegalArgumentException, "upstream failure must propagate through take as ITSELF, got: " + (propagated?.message ?: "null"))
                assertEquals("boom", (propagated as IllegalArgumentException).message)
            }
        }

        test("upstreamFailurePropagatesThroughFirst") {
            runBlocking {
                var propagated: Throwable? = null
                try {
                    flow<Int> {
                        throw IllegalArgumentException("boom")
                    }.first()
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertTrue(propagated is IllegalArgumentException, "upstream failure must propagate through first() as ITSELF, got: " + (propagated?.message ?: "null"))
                assertEquals("boom", (propagated as IllegalArgumentException).message)
            }
        }

        test("dropSkipsCount") {
            runBlocking {
                assertEquals(listOf(3, 4), flowOf(1, 2, 3, 4).drop(2).toList())
                assertEquals(listOf(1, 2), flowOf(1, 2).drop(0).toList())
                assertTrue(flowOf(1, 2).drop(5).toList().isEmpty())
            }
        }

        // distinctUntilChanged compares with == (value equality, not identity),
        // suppresses consecutive repeats only, and handles null values.
        test("distinctUntilChangedSuppressesRepeats") {
            runBlocking {
                assertEquals(listOf(1, 2, 1), flowOf(1, 1, 2, 2, 1).distinctUntilChanged().toList())
                var suffix = "b"
                assertEquals(listOf("ab"), flowOf("ab", "a" + suffix).distinctUntilChanged().toList())
                assertEquals(listOf(null, 1), flowOf<Int?>(null, null, 1).distinctUntilChanged().toList())
            }
        }

        test("operatorChainEndToEnd") {
            runBlocking {
                val out = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).asFlow()
                    .filter { value -> value % 2 == 0 }
                    .map { value -> value * 10 }
                    .take(3)
                    .toList()
                assertEquals(listOf(20, 40, 60), out)
            }
        }

        // Count validation fires at operator-construction time (kotlinx parity):
        // take requires a positive count, drop a non-negative one.
        test("takeAndDropCountValidation") {
            runBlocking {
                var takeThrew = false
                try {
                    flowOf(1).take(0)
                } catch (e: IllegalArgumentException) {
                    takeThrew = true
                }
                assertTrue(takeThrew, "take(0) must throw IllegalArgumentException")
                var dropThrew = false
                try {
                    flowOf(1).drop(-1)
                } catch (e: IllegalArgumentException) {
                    dropThrew = true
                }
                assertTrue(dropThrew, "drop(-1) must throw IllegalArgumentException")
            }
        }
    }
}
