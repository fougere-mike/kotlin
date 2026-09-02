package test.coroutines.flow

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.__smokeTwoSuspends

fun TestRunner.flowKlibSmokeTests() {
    suite("FlowKlibSmoke") {

        test("flowKlibStateMachineSmoke") {
            runBlocking {
                // Import/type-resolution probe: Flow comes from the kotlin-flow-brs
                // klib, not the stdlib.
                val typeProbe: Flow<Int>? = null
                assertNull(typeProbe)

                // Suspend-state-machine proof (spec decision 10): two sequential
                // suspend calls whose results combine require a real state machine
                // inside the flow klib's __smokeTwoSuspends. Under
                // -Xstdlib-compilation no state machines are generated and this
                // exact shape miscompiles silently; passing on device proves the
                // flow klib compiled in USER mode.
                val result = __smokeTwoSuspends(
                    { delay(1); 2 },
                    { delay(1); 3 }
                )
                assertEquals(5, result)
            }
        }
    }
}
