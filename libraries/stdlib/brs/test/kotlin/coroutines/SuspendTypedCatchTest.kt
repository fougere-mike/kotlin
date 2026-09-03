package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.runBlocking

// Typed catch clauses inside SUSPEND state machines (task 3b). The state
// machine used to emit every catch clause as a catch-all: a typed
// `catch (e: IllegalStateException)` silently swallowed exceptions of every
// type. The fixed emission is-dispatches on the clause type and rethrows
// non-matching exceptions. Each try body contains a real suspension point so
// the try goes through BrsStateMachineBuilder.visitTry (a suspension-free try
// in a suspend function takes the non-suspend emission path instead).

private suspend fun raiseAfterYield(kind: String): Int {
    yield()
    if (kind == "ise") {
        throw IllegalStateException("ise-boom")
    }
    if (kind == "iae") {
        throw IllegalArgumentException("iae-boom")
    }
    return 7
}

private fun isIse(kind: String): Boolean = kind == "ise"

private fun plainSeven(): Int = 7

fun TestRunner.suspendTypedCatchTests() {
    suite("Suspend typed catch") {

        test("typed catch catches a matching exception") {
            runBlocking {
                var result = "none"
                try {
                    raiseAfterYield("ise")
                    result = "unreached"
                } catch (e: IllegalStateException) {
                    result = "caught:" + (e.message ?: "")
                }
                assertEquals("caught:ise-boom", result)
            }
        }

        test("typed catch does not swallow a non-matching exception") {
            runBlocking {
                var result = "none"
                try {
                    try {
                        raiseAfterYield("iae")
                        result = "unreached"
                    } catch (e: IllegalStateException) {
                        result = "swallowed"
                    }
                } catch (e: Throwable) {
                    result = if (e is IllegalArgumentException) {
                        "propagated:" + (e.message ?: "")
                    } else {
                        "wrong-type"
                    }
                }
                assertEquals("propagated:iae-boom", result)
            }
        }

        test("non-matching exception propagates out of the suspend function") {
            var result = "none"
            try {
                runBlocking {
                    try {
                        raiseAfterYield("iae")
                    } catch (e: IllegalStateException) {
                        result = "swallowed"
                    }
                }
            } catch (e: Throwable) {
                result = if (e is IllegalArgumentException) {
                    "escaped:" + (e.message ?: "")
                } else {
                    "wrong-type"
                }
            }
            assertEquals("escaped:iae-boom", result)
        }

        test("second clause is selected by type") {
            runBlocking {
                var result = "none"
                try {
                    raiseAfterYield("iae")
                    result = "unreached"
                } catch (e: IllegalStateException) {
                    result = "ise"
                } catch (e: IllegalArgumentException) {
                    result = "iae:" + (e.message ?: "")
                }
                assertEquals("iae:iae-boom", result)
            }
        }

        test("Throwable catch-all still catches everything") {
            runBlocking {
                var result = "none"
                try {
                    raiseAfterYield("iae")
                    result = "unreached"
                } catch (e: Throwable) {
                    result = "caught-all:" + (e.message ?: "")
                }
                assertEquals("caught-all:iae-boom", result)
            }
        }

        // The wrap write-loss family (task 3c; was red-guarded by 3b): when a
        // generic builder like runBlocking infers the block's T as Any, an
        // arm/branch whose terminal statement is an ASSIGNMENT gets a non-Unit
        // static type from the LUB coercion, and the result-temp wrap used to
        // make the assignment the RHS of the temp set — BrightScript renders
        // that as a COMPARISON, silently losing the write
        // (m.__try_tmp = (m._result.value = "caught")). Fixed by the
        // terminal-statement wrap guard (armTerminalProducesValue in
        // BrsTryExpressionLowering.kt, shared with BrsWhenExpressionLowering,
        // mirrored in BrsStateMachineBuilder.producesValue): such arms run
        // unwrapped and the temp reads invalid — the BRS mapping of Unit.
        test("typed catch body writes a captured outer var") {
            var result = "none"
            runBlocking {
                try {
                    raiseAfterYield("ise")
                } catch (e: IllegalStateException) {
                    result = "caught"
                }
            }
            assertEquals("caught", result)
        }

        // The try-arm side of the same hole: terminal assignment in the TRY
        // arm, the catch arm carrying the genuine value.
        test("try arm terminal assignment still writes") {
            var result = "none"
            runBlocking {
                try {
                    result = "r:" + raiseAfterYield("none")
                } catch (e: IllegalStateException) {
                    -1
                }
            }
            assertEquals("r:7", result)
        }

        // The visitWhen sibling: a suspendable value-typed when (T = Any from
        // the LUB of an Int branch and a Unit branch) whose taken branch ends
        // in an assignment — the WHEN_RESULT wrap used to swallow it.
        test("value-typed when assignment branch still writes") {
            var result = "none"
            runBlocking {
                if (isIse("iae")) {
                    raiseAfterYield("none")
                } else {
                    result = "else"
                }
            }
            assertEquals("else", result)
        }

        // A suspension INSIDE the typed catch clause body (task 3b review
        // rider): the clause body suspends after the is-dispatch; the catch
        // parameter must survive into the resume state (e.message read after
        // yield), and the else-rethrow edges stay on the catch state.
        test("typed catch body itself suspends before writing") {
            runBlocking {
                var result = "none"
                try {
                    raiseAfterYield("ise")
                } catch (e: IllegalStateException) {
                    yield()
                    result = "caught:" + (e.message ?: "")
                }
                assertEquals("caught:ise-boom", result)
            }
        }

        test("typed catch in value-position try") {
            runBlocking {
                val r = try {
                    raiseAfterYield("ise")
                } catch (e: IllegalStateException) {
                    -1
                }
                assertEquals(0, r + 1)
            }
        }
    }

    // The result-temp wrap (BrsTryExpressionLowering / BrsWhenExpressionLowering)
    // is shared by suspend and non-suspend code — the write-loss family above is
    // NOT coroutine-specific. These pin the plain non-suspend side: an explicit
    // Any ascription is enough to coerce a terminal-assignment arm to non-Unit.
    suite("Result-temp wrap guard (non-suspend)") {

        test("non-suspend when: assignment branch still writes") {
            var result = "none"
            val x: Any = if (isIse("iae")) 5 else {
                result = "else"
            }
            assertEquals("else", result)
            assertEquals(false, x is Int)
        }

        test("non-suspend try: assignment arm still writes") {
            var result = "none"
            val x: Any = try {
                result = "t:" + plainSeven()
            } catch (e: IllegalStateException) {
                -1
            }
            assertEquals("t:7", result)
            assertEquals(false, x is Int)
        }
    }
}
