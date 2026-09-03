package test.coroutines.flow

import kotlin.test.*
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.flow.*

// StateFlow — the runBlocking-regime slice ONLY (flow-program spec §6).
//
// HONESTY NOTE: this suite runs on the app MAIN thread (runBlocking), where
// there is no render-thread component context — so it can exercise exactly:
// construction, `value` reads (legal anywhere), `asStateFlow` view shape, the
// equality gate's position BEFORE the render-context guard, and the guided
// ISEs for off-context emit/collect (including guard-before-store: the value
// is unchanged after a failed emit). Everything BEHAVIORAL — dedup delivery,
// conflation, doorbell wakes, multi-collector fanout, stateIn bridging —
// needs a live component context and lives in E2E Suite 10b
// (stateFlowEqualityDedup, stateInBridgesFlow, Task 11).
fun TestRunner.flowStateTests() {
    suite("FlowState") {

        test("stateFlowValueGetReturnsInitial") {
            val f = MutableStateFlow(42)
            assertEquals(42, f.value)
        }

        // Construction works anywhere (no component context on this thread),
        // including nullable and reference-typed states.
        test("stateFlowConstructionAnywhere") {
            val s = MutableStateFlow("loading")
            assertEquals("loading", s.value)
            val n = MutableStateFlow<String?>(null)
            assertNull(n.value)
        }

        test("asStateFlowViewReadsSourceValue") {
            val source = MutableStateFlow("a")
            val view = source.asStateFlow()
            assertEquals("a", view.value)
            // The view is a distinct read-only object, not the source itself.
            assertFalse(view === source)
            assertTrue(view !is MutableStateFlow<*>)
        }

        // The emit protocol's ORDER is law: equality gate BEFORE the
        // render-context guard — assigning the CURRENT value is a no-op
        // everywhere, even where a distinct emit would throw.
        test("stateFlowEqualEmitIsNoOpOffContext") {
            val f = MutableStateFlow(7)
            f.value = 7
            assertEquals(7, f.value)
        }

        test("stateFlowOffContextEmitThrowsGuidedIse") {
            val f = MutableStateFlow(1)
            var message = ""
            try {
                f.value = 2
            } catch (e: IllegalStateException) {
                message = "${e.message}"
            }
            assertTrue(
                message.contains("render-thread"),
                "expected the guided emit-law ISE, got: '$message'"
            )
        }

        // GUARD BEFORE STORE: the failed emit above must not have mutated a
        // value collectors were never told about.
        test("stateFlowValueUnchangedAfterFailedEmit") {
            val f = MutableStateFlow(1)
            try {
                f.value = 2
            } catch (e: IllegalStateException) {
                // expected off-context
            }
            assertEquals(1, f.value)
        }

        // Collection is context-gated too; the terminal routes through
        // flowCollectDispatch into the doorbell protocol, whose guard fires
        // before any registry/bind work.
        test("stateFlowOffContextCollectThrowsGuidedIse") {
            runBlocking {
                val f = MutableStateFlow(5)
                var message = ""
                try {
                    f.first()
                } catch (e: IllegalStateException) {
                    message = "${e.message}"
                }
                assertTrue(
                    message.contains("render-thread"),
                    "expected the guided collect-law ISE, got: '$message'"
                )
            }
        }
    }
}
