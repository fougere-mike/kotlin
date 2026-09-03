package test.coroutines.flow

import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.*
import kotlin.coroutines.pump.kotlinAmbientGlobalOrNull
import kotlin.coroutines.pump.kotlinAmbientTopOrNull
import kotlin.coroutines.task.spawnTask
import kotlin.test.*

// Task-lift runtime coverage REACHABLE in the runBlocking (main-thread)
// regime: the guided render-context ISEs and the envelope protocol's pure
// logic. The lift's happy path (a task node actually running the upstream)
// REQUIRES render-thread component context by v1 law and cannot execute here
// — that is E2E Suite 10a's job (flow-program Task 9).
fun TestRunner.flowTaskLiftTests() {
    suite("FlowTaskLift") {

        // The two public ambient accessors (added for the lift's guards; the
        // hot tier reuses them): no component context on the main-thread
        // driver, so both answer null.
        test("ambientAccessorsNullOffComponent") {
            assertTrue(kotlinAmbientTopOrNull() == null, "ambient top must be null off-component")
            assertTrue(kotlinAmbientGlobalOrNull() == null, "ambient global must be null off-component")
        }

        // v1 context law, flowOn form: collecting a lifted flow off-component
        // throws the guided ISE (the guard fires BEFORE any node exists).
        test("flowOnCollectOffComponentThrowsGuidedIse") {
            runBlocking {
                var message = ""
                try {
                    flowOf(1, 2).flowOn(Dispatchers.Task).collect { }
                } catch (e: IllegalStateException) {
                    message = "${e.message}"
                }
                assertTrue(
                    message.contains("render-thread component context"),
                    "expected the guided context-law ISE, got: '$message'"
                )
            }
        }

        // v1 context law, spawnTask form (same guard sentence family).
        test("spawnTaskOffComponentThrowsGuidedIse") {
            runBlocking {
                var message = ""
                try {
                    spawnTask { 21 + 21 }
                } catch (e: IllegalStateException) {
                    message = "${e.message}"
                }
                assertTrue(
                    message.contains("render-thread component context"),
                    "expected the guided context-law ISE, got: '$message'"
                )
            }
        }

        // Envelope protocol round trips (public builders/parsers because the
        // test module has no friend wiring into the flow klib — the ScopeWire
        // buildScopeErrorOutcome precedent).
        test("emitEnvelopeRoundTrip") {
            val env = buildFlowEmitEnvelope(3, "payload")
            assertEquals("emit", "${env.lookup("kind")}")
            assertEquals("3", "${env.lookup("seq")}")
            assertEquals("payload", "${env.lookup("value")}")
        }

        test("completeEnvelopeCarriesSpawnResult") {
            val env = buildFlowCompleteEnvelope(1, 42)
            assertEquals("complete", "${env.lookup("kind")}")
            assertEquals("42", "${env.lookup("value")}")
        }

        test("errorEnvelopeToTaskException") {
            val env = buildFlowErrorEnvelope(7, "boom", 12, null)
            assertEquals("error", "${env.lookup("kind")}")
            val e = flowTaskExceptionFrom(env)
            assertEquals("boom", e.message)
            assertEquals(12, e.number)
        }

        // flowErrorEnvelopeFrom reads the caught value's runtime AA data keys
        // (never getter slots) — a Kotlin exception instance carries `message`
        // there, the same device truth ScopeWire's error marshalling rides.
        // Device fact (pinned by this test's first run, 2026-09-03): a Kotlin
        // exception instance ALSO carries `number` = 40 (&h28, the BrightScript
        // user-thrown-error stamp) — the extractor reports the platform's
        // stamp faithfully rather than defaulting to 0.
        test("errorEnvelopeFromKotlinException") {
            val env = flowErrorEnvelopeFrom(1, IllegalStateException("kaput"))
            assertEquals("error", "${env.lookup("kind")}")
            val e = flowTaskExceptionFrom(env)
            assertEquals("kaput", e.message)
            assertEquals(40, e.number)
        }

        // Parser defaults: an error envelope missing its data keys still maps
        // to a usable TaskException (guided default message, number 0).
        test("errorEnvelopeParserDefaults") {
            val bare = kotlin.brs.roku.RoAssociativeArray.create()
            bare.addReplace("kind", "error")
            val e = flowTaskExceptionFrom(bare)
            assertEquals("flow task failed", e.message)
            assertEquals(0, e.number)
        }
    }
}
