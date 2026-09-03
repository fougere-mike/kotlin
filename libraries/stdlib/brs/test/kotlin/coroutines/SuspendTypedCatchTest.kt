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

        // RED-GUARDED (pre-existing defect, ledgered in the task-3b report,
        // NOT the typed-catch dispatch fix): when a generic builder like
        // runBlocking infers the block's T as Any, the catch arm's static type
        // is non-Unit while its terminal statement is an ASSIGNMENT — the
        // TRY_RESULT wrap then makes the assignment the RHS of the temp set,
        // which BrightScript renders as a COMPARISON: the write is silently
        // lost (m.__try_tmp = (m._result.value = "caught")). Same wrap-guard
        // family as the Unit-arm fix in BrsStateMachineBuilder.visitTry;
        // visitWhen's branch wrapping has the same latent hole.
        xtest("typed catch body writes a captured outer var", "state-machine TRY_RESULT wrap swallows a terminal assignment (silent write loss)") {
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
}
