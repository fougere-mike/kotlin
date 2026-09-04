package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.runBlocking

// Coroutine classes share ONE BrightScript AA with their CoroutineImpl base, so
// a suspend lambda parameter, a named suspend function parameter, or a captured
// local whose emitted field key matched a base field — `state`, `result`,
// `exception`, `exceptionState`, `_context`, … — used to overwrite the state
// machine's own slot: `collectLatest { state -> ... }` died with "Type Mismatch.
// Unable to cast roAssociativeArray to Integer" the moment the lambda ran
// (TestScreen flagship, 2026-09-04). The lowering now renames the shadowing
// subclass field (golden: coroutines/coroutineFieldNameShadowing). These tests
// pin the runtime behaviour in the runBlocking regime — the clobber was inside
// the state machine itself, so no dispatcher is needed to reproduce it.

private suspend fun applyTwice(value: Int, block: suspend (Int) -> Int): Int {
    val once = block(value)
    return block(once)
}

private suspend fun withNamedParams(state: Int, result: Int): Int {
    yield()
    return state * 10 + result
}

fun TestRunner.coroutineFieldShadowingTests() {
    suite("Coroutine field name shadowing") {

        test("suspend lambda parameter named state survives the state machine") {
            runBlocking {
                val out = applyTwice(1) { state ->
                    yield()
                    state + 1
                }
                assertEquals(3, out)
            }
        }

        test("suspend lambda parameters named result and exception") {
            runBlocking {
                val r = applyTwice(5) { result ->
                    yield()
                    result * 2
                }
                val e = applyTwice(7) { exception ->
                    yield()
                    exception - 1
                }
                assertEquals(20, r)
                assertEquals(5, e)
            }
        }

        test("named suspend function parameters named state and result") {
            runBlocking {
                assertEquals(42, withNamedParams(4, 2))
            }
        }

        test("captured local named context does not shadow the coroutine context") {
            runBlocking {
                val context = "ctx"
                val out = applyTwice(1) { n ->
                    yield()
                    // Reading coroutineContext goes through the base's context slot
                    // AFTER the `_context` capture field has been stored on the object.
                    val hasJob = coroutineContext[Job] != null
                    if (hasJob && context == "ctx") n + 1 else -100
                }
                assertEquals(3, out)
            }
        }
    }
}
