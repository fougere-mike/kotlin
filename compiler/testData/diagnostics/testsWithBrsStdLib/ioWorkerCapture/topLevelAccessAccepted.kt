// Expected: no CAPTURE diagnostic (the IO-dispatcher error still marks Dispatchers.IO) — top-level property access is not a capture.
// Top-level properties are not local; the checker only flags local captures
// and this-captures of the enclosing class.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Thing

val topLevelThing = Thing()

suspend fun run() {
    withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
        println(topLevelThing)
    }
}
