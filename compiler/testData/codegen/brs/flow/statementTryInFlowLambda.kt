// A STATEMENT-POSITION try/finally directly inside a suspend flow {} lambda
// (Task 9b deliverable 3; Task 9's ensureTaskActiveStops fixture shape): the
// try sits between two suspension points (emit calls), so the state machine
// owns it. Pre-fix the emission produced `tmp_ret_0 = try` — the try rendered
// in EXPRESSION position, a device-side syntax error (compile error &h02) —
// and the fixture had to move the loop into a plain non-suspend helper. Pins
// the try emitted as a STATEMENT inside the state machine, with both arms and
// the finally intact.
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.flow.*

private var marks = ""

private fun mark(tag: String) {
    marks = marks + ":" + tag
}

fun main() {
    runBlocking {
        flow {
            emit("started")
            try {
                var i = 0
                while (i < 3) {
                    i = i + 1
                }
                mark("done")
            } finally {
                mark("cleanup")
            }
            emit("finished")
        }.collect { v -> mark(v) }
    }
    println(marks)
}
