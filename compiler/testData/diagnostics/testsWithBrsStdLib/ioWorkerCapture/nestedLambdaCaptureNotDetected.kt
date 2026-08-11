// Expected: no CAPTURE diagnostic (the IO-dispatcher error still marks Dispatchers.IO) — the reference to `t` is inside a nested lambda passed to
// forEach, which is a separate FirAnonymousFunction. The checker stops descent into
// nested lambdas to avoid complexity. This is a known limitation: captures inside
// nested lambdas within withContext(Dispatchers.IO) are not currently detected.
// The IR lowering catches this at compile time regardless.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Thing

suspend fun run() {
    val t = Thing()
    withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
        listOf(1, 2, 3).forEach { _ ->
            println(t)
        }
    }
}
