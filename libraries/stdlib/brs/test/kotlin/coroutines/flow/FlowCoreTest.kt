package test.coroutines.flow

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.flow.*

fun TestRunner.flowCoreTests() {
    suite("FlowCore") {

        test("flowColdBodyReExecutesPerCollect") {
            runBlocking {
                var executions = 0
                val f = flow {
                    executions++
                    emit(executions)
                }
                assertEquals(listOf(1), f.toList())
                assertEquals(listOf(2), f.toList())
                assertEquals(2, executions)
            }
        }

        test("flowEmitsValuesInOrder") {
            runBlocking {
                val f = flow {
                    emit(1)
                    emit(2)
                    emit(3)
                }
                val received = ArrayList<Int>()
                f.collect { value ->
                    received.add(value)
                }
                assertEquals(listOf(1, 2, 3), received)
            }
        }

        // Known-defect probe (spec §12): primitive varargs at a generic vararg site.
        test("flowOfPrimitiveVarargsToList") {
            runBlocking {
                assertEquals(listOf(1, 2, 3), flowOf(1, 2, 3).toList())
            }
        }

        // Known-defect probe (spec §12): empty varargs.
        test("flowOfEmptyVarargs") {
            runBlocking {
                assertTrue(flowOf<Int>().toList().isEmpty())
            }
        }

        test("iterableAsFlowEmitsAll") {
            runBlocking {
                assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).asFlow().toList())
            }
        }

        test("emitAllConcatenates") {
            runBlocking {
                val f = flow {
                    emit(0)
                    emitAll(flowOf(1, 2))
                }
                assertEquals(listOf(0, 1, 2), f.toList())
            }
        }

        // first() aborts the upstream producer: the side-effect counter stops at 1,
        // and the abort machinery (AbortFlowException) never leaks to the caller.
        test("firstStopsProducer") {
            runBlocking {
                var produced = 0
                val first = flow {
                    var i = 1
                    while (i <= 3) {
                        produced++
                        emit(i)
                        i++
                    }
                }.first()
                assertEquals(1, first)
                assertEquals(1, produced)
            }
        }

        test("firstPredicateSelectsMatch") {
            runBlocking {
                val v = flowOf(1, 2, 3).first { value -> value > 1 }
                assertEquals(2, v)
            }
        }

        // Catch is deliberately typed NoSuchElementException: a leaked
        // AbortFlowException would escape and fail the test loudly.
        test("firstOnEmptyFlowThrowsNoSuchElement") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    flowOf<Int>().first()
                } catch (e: NoSuchElementException) {
                    thrown = e
                }
                assertTrue(thrown != null, "first() on an empty flow must throw NoSuchElementException")
            }
        }

        test("firstPredicateNoMatchThrowsNoSuchElement") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    flowOf(1, 2).first { value -> value > 10 }
                } catch (e: NoSuchElementException) {
                    thrown = e
                }
                assertTrue(thrown != null, "first(predicate) with no match must throw NoSuchElementException")
            }
        }

        test("firstOrNullEmptyAndNonEmpty") {
            runBlocking {
                assertNull(flowOf<Int>().firstOrNull())
                assertEquals(1, flowOf(1, 2).firstOrNull())
            }
        }

        // launchIn collects in the given scope; values are ignored, side effects
        // in the flow body prove the collection ran.
        test("launchInCollectsInScope") {
            runBlocking {
                val hits = ArrayList<Int>()
                val job = flow {
                    hits.add(1)
                    emit(1)
                    hits.add(2)
                    emit(2)
                }.launchIn(this@runBlocking)
                job.join()
                assertEquals(listOf(1, 2), hits)
            }
        }

        // Cancelling the collector mid-collect: the next emit's entry check throws
        // CancellationException, which unwinds through the flow body's try/finally.
        // The downstream action never sees a value emitted after cancellation.
        test("cancellationMidCollectRunsFinally") {
            runBlocking {
                var finallyRan = false
                var received = 0
                val job = launch {
                    val self = coroutineContext[Job]!!
                    flow {
                        try {
                            emit(1)
                            emit(2)
                        } finally {
                            finallyRan = true
                        }
                    }.collect { value ->
                        received++
                        if (value == 1) self.cancel()
                    }
                }
                job.join()
                assertTrue(finallyRan, "flow body's finally must run when the collector is cancelled")
                assertEquals(1, received)
            }
        }

        // Task 9b deliverable 3: a STATEMENT-POSITION try/finally between two
        // emit suspension points, with a Unit-returning CALL as the try arm's
        // terminal (the statementTryInFlowLambda golden's exact shape).
        // FinallyBlocksLowering's returnable block used to hit the do-while
        // body-duplication emission plus a void-sub result assignment — a
        // device-side compile error in the generated state machine; this pins
        // the fixed shape actually executing.
        test("statementTryFinallyBetweenEmits") {
            runBlocking {
                stmtTryMarks = ""
                flow {
                    emit("started")
                    try {
                        var i = 0
                        while (i < 3) {
                            i = i + 1
                        }
                        stmtTryMark("done")
                    } finally {
                        stmtTryMark("cleanup")
                    }
                    emit("finished")
                }.collect { v -> stmtTryMark(v) }
                assertEquals(":started:done:cleanup:finished", stmtTryMarks)
            }
        }
    }
}

// Top-level accumulator + Unit-returning helper: the try arm must TERMINATE in
// a Unit CALL (not a captured-var assignment) to pin the void-sub result-temp
// split alongside the loop shape — mirrors the golden fixture exactly.
private var stmtTryMarks = ""

private fun stmtTryMark(tag: String) {
    stmtTryMarks = stmtTryMarks + ":" + tag
}
