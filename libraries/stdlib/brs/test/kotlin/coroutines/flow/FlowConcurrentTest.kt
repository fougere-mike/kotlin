package test.coroutines.flow

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.flow.*

// The concurrent-operator family (flatMap*, *Latest, combine, conflate) over the
// internal FlowChannel primitive. The channel is INTERNAL and deliberately has no
// direct tests here (the test module cannot see flow-klib internals); its FIFO
// drain, conflated mode, and close-with-cause semantics are pinned exclusively
// through these public operator surfaces.
//
// Determinism note: these tests run in the runBlocking (no-interceptor) regime,
// where launch runs a child inline to its first real suspension and resumes are
// inline. Event orderings asserted exactly below are derived from that regime's
// single-context scheduling; timing-sensitive orderings use delay values with
// 10x+ margins (1ms vs 10ms/30ms) so device timer coarseness cannot reorder them.
fun TestRunner.flowConcurrentTests() {
    suite("FlowConcurrent") {

        // flatMapConcat: each inner flow is collected to COMPLETION before the
        // next upstream value's inner starts, even when the inner suspends.
        test("flatMapConcatOrdering") {
            runBlocking {
                val out = flowOf(1, 2).flatMapConcat { v ->
                    flow {
                        emit("a" + v)
                        delay(1)
                        emit("b" + v)
                    }
                }.toList()
                assertEquals(listOf("a1", "b1", "a2", "b2"), out)
            }
        }

        test("flatMapConcatNonSuspendingInners") {
            runBlocking {
                val out = flowOf(1, 2, 3).flatMapConcat { v ->
                    flowOf(v * 10, v * 10 + 1)
                }.toList()
                assertEquals(listOf(10, 11, 20, 21, 30, 31), out)
            }
        }

        // flatMapMerge (default concurrency — also pins the cross-klib default
        // argument): suspending inners interleave — the second inner starts
        // before the first finishes, which concat can never produce.
        test("flatMapMergeInterleavesSuspendingInners") {
            runBlocking {
                val out = flowOf(1, 2).flatMapMerge { v ->
                    flow {
                        emit("start" + v)
                        delay(1)
                        emit("end" + v)
                    }
                }.toList()
                assertEquals(listOf("end1", "end2", "start1", "start2"), out.sorted())
                assertTrue(
                    out.indexOf("start2") < out.indexOf("end1"),
                    "merge must interleave: start2 must precede end1, got " + out.joinToString(",")
                )
            }
        }

        // concurrency = 1 degrades flatMapMerge to exact flatMapConcat ordering.
        test("flatMapMergeConcurrencyOneIsConcat") {
            runBlocking {
                val out = flowOf(1, 2).flatMapMerge(1) { v ->
                    flow {
                        emit("start" + v)
                        delay(1)
                        emit("end" + v)
                    }
                }.toList()
                assertEquals(listOf("start1", "end1", "start2", "end2"), out)
            }
        }

        // THE single-context interleaving law (spec §4, KDoc'd on flatMapMerge):
        // children interleave only at suspension points, so an inner that never
        // suspends runs to completion synchronously when started — flatMapMerge
        // over non-suspending inners degenerates to exact concat order. This is
        // documented truth, not a bug.
        test("flatMapMergeNonSuspendingInnersDegenerateToConcat") {
            runBlocking {
                val out = flowOf(1, 2, 3).flatMapMerge { v ->
                    flowOf(v, v * 10)
                }.toList()
                assertEquals(listOf(1, 10, 2, 20, 3, 30), out)
            }
        }

        // The concurrency gate holds upstream collection while the limit is
        // reached: inner 3 starts only after inner 1 finishes and frees a slot.
        test("flatMapMergeConcurrencyGateHoldsUpstream") {
            runBlocking {
                val events = ArrayList<String>()
                flowOf(1, 2, 3).flatMapMerge(2) { v ->
                    flow {
                        emit("s" + v)
                        delay(10L * v)
                        emit("e" + v)
                    }
                }.collect { value -> events.add(value) }
                assertEquals(listOf("s1", "s2", "e1", "s3", "e2", "e3"), events)
            }
        }

        test("flatMapMergeRejectsNonPositiveConcurrency") {
            runBlocking {
                var threw = false
                try {
                    flowOf(1).flatMapMerge(0) { v -> flowOf(v) }
                } catch (e: IllegalArgumentException) {
                    threw = true
                }
                assertTrue(threw, "flatMapMerge(0) must throw IllegalArgumentException at construction")
            }
        }

        // Upstream failure propagates out of collect as ITSELF; values emitted
        // before the failure were delivered (pins close-with-cause through the
        // merge surface).
        test("flatMapMergeUpstreamFailurePropagates") {
            runBlocking {
                var received = 0
                var propagated: Throwable? = null
                try {
                    flow {
                        emit(1)
                        delay(1)
                        throw IllegalArgumentException("boom")
                    }.flatMapMerge { v ->
                        flow {
                            emit(v)
                            delay(30)
                            emit(v * 10)
                        }
                    }.collect { received++ }
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertEquals(1, received)
                assertTrue(propagated is IllegalArgumentException, "upstream failure must propagate as itself")
                assertEquals("boom", (propagated as IllegalArgumentException).message)
            }
        }

        // A failing inner cancels its sibling inners (the sibling's finally
        // runs, its late emission never lands) and the failure propagates.
        test("flatMapMergeInnerFailureCancelsSiblings") {
            runBlocking {
                val log = ArrayList<String>()
                var propagated: Throwable? = null
                try {
                    flowOf(1, 2).flatMapMerge { v ->
                        if (v == 1) {
                            flow {
                                try {
                                    emit("a1")
                                    delay(30)
                                    emit("late1")
                                } finally {
                                    log.add("finally1")
                                }
                            }
                        } else {
                            flow {
                                delay(1)
                                throw IllegalArgumentException("inner-boom")
                            }
                        }
                    }.collect { value -> log.add(value) }
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertTrue(propagated is IllegalArgumentException, "inner failure must propagate out of collect")
                assertEquals("inner-boom", (propagated as IllegalArgumentException).message)
                assertEquals("a1", log[0])
                assertTrue(log.contains("finally1"), "cancelled sibling's finally must run, log: " + log.joinToString(","))
                assertFalse(log.contains("late1"), "cancelled sibling must not emit after teardown")
            }
        }

        // Downstream abort (take) tears the merge machinery down: buffered
        // values are dropped, in-flight inners are cancelled (finally runs),
        // and the abort never leaks.
        test("takeAbortsThroughFlatMapMerge") {
            runBlocking {
                var innerFinallyRan = false
                val out = flowOf(1, 2, 3).flatMapMerge { v ->
                    flow {
                        try {
                            emit(v)
                            delay(30)
                            emit(v * 10)
                        } finally {
                            if (v == 1) innerFinallyRan = true
                        }
                    }
                }.take(1).toList()
                assertEquals(listOf(1), out)
                assertTrue(innerFinallyRan, "in-flight inner's finally must run when take aborts the merge")
            }
        }

        // flatMapLatest — the Rx switchMap contract: a new upstream value
        // cancels the in-flight inner (its finally runs BEFORE the new inner's
        // first emission — cancel + join), its late emissions are dropped, and
        // the last inner runs to completion.
        test("flatMapLatestCancelsInFlightInner") {
            runBlocking {
                val events = ArrayList<String>()
                val out = flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }.flatMapLatest { v ->
                    flow {
                        try {
                            emit("i" + v)
                            delay(30)
                            emit("late" + v)
                        } finally {
                            events.add("finally" + v)
                        }
                    }
                }.onEach { value -> events.add(value) }.toList()
                assertEquals(listOf("i1", "i2", "late2"), out)
                assertEquals(listOf("i1", "finally1", "i2", "late2", "finally2"), events)
            }
        }

        // A synchronous upstream burst: every superseded inner is cancelled at
        // its first suspension; only the last inner completes.
        test("flatMapLatestBurstOnlyLastInnerCompletes") {
            runBlocking {
                val out = flowOf(1, 2, 3).flatMapLatest { v ->
                    flow {
                        emit("i" + v)
                        delay(10)
                        emit("done" + v)
                    }
                }.toList()
                assertEquals(listOf("i1", "i2", "i3", "done3"), out)
            }
        }

        // transformLatest: the block may emit several values per input; a new
        // input cancels the in-flight block.
        test("transformLatestEmitsMultiplePerValue") {
            runBlocking {
                val out = flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }.transformLatest { v ->
                    emit("a" + v)
                    delay(30)
                    emit("b" + v)
                }.toList()
                assertEquals(listOf("a1", "a2", "b2"), out)
            }
        }

        // mapLatest: a superseded transform is cancelled mid-flight; only the
        // last value's transform result is emitted.
        test("mapLatestCancelsSupersededTransform") {
            runBlocking {
                val out = flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }.mapLatest { v ->
                    delay(30)
                    v * 10
                }.toList()
                assertEquals(listOf(20), out)
            }
        }

        // collectLatest restarts the action on each value: superseded actions
        // never reach their end, the last one completes.
        test("collectLatestRestartsAction") {
            runBlocking {
                val log = ArrayList<String>()
                flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }.collectLatest { v ->
                    log.add("start" + v)
                    delay(30)
                    log.add("end" + v)
                }
                assertEquals(listOf("start1", "start2", "end2"), log)
            }
        }

        // combine: no emission until BOTH sides have a value; then an emission
        // on EVERY update from either side, pairing with the other side's latest.
        test("combineEmitsOnEachUpdateAfterBothHaveValues") {
            runBlocking {
                val f1 = flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }
                val f2 = flow {
                    emit("a")
                    delay(30)
                    emit("b")
                }
                val out = combine(f1, f2) { a, b -> "" + a + b }.toList()
                assertEquals(listOf("1a", "2a", "2b"), out)
            }
        }

        // A side that completes early keeps its LATEST value paired against the
        // other side's later updates; values it emitted before the other side's
        // first value are conflated away by the has-both gate.
        test("combineLatestValueSticksAfterEarlyCompletion") {
            runBlocking {
                val f1 = flowOf(1, 2)
                val f2 = flow {
                    delay(1)
                    emit("z")
                }
                val out = combine(f1, f2) { a, b -> "" + a + b }.toList()
                assertEquals(listOf("2z"), out)
            }
        }

        // conflate under a synchronous burst delivers only the latest value
        // (single-context shape: the producer runs to completion before the
        // drain starts — the conflated buffer holds exactly the last value).
        test("conflateDeliversLatestUnderBurst") {
            runBlocking {
                val out = flowOf(1, 2, 3, 4, 5).conflate().toList()
                assertEquals(listOf(5), out)
            }
        }

        // A collector that keeps up sees every value — conflation only drops
        // values the collector never had time to take.
        test("conflateFastCollectorSeesAllValues") {
            runBlocking {
                val out = flow {
                    emit(1)
                    delay(1)
                    emit(2)
                }.conflate().toList()
                assertEquals(listOf(1, 2), out)
            }
        }

        // A slow collector skips intermediate values: while it processes 1,
        // values 2 and 3 arrive and conflate — it next sees only 3.
        test("conflateSlowCollectorSkipsToLatest") {
            runBlocking {
                val received = ArrayList<Int>()
                flow {
                    emit(1)
                    delay(1)
                    emit(2)
                    emit(3)
                }.conflate().collect { v ->
                    received.add(v)
                    delay(30)
                }
                assertEquals(listOf(1, 3), received)
            }
        }

        // Upstream failure after a delivered value: the value arrived, then the
        // failure propagates as itself (pins FlowChannel close-with-cause and
        // the drain's wake-from-park on close, via the conflate surface).
        test("conflateUpstreamFailureDeliversThenThrows") {
            runBlocking {
                val received = ArrayList<Int>()
                var propagated: Throwable? = null
                try {
                    flow {
                        emit(1)
                        delay(1)
                        throw IllegalArgumentException("boom")
                    }.conflate().collect { v -> received.add(v) }
                } catch (e: IllegalArgumentException) {
                    propagated = e
                }
                assertEquals(listOf(1), received)
                assertTrue(propagated is IllegalArgumentException, "upstream failure must propagate through conflate")
                assertEquals("boom", (propagated as IllegalArgumentException).message)
            }
        }
    }
}
