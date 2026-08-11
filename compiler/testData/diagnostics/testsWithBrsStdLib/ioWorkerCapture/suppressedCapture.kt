// Expected: no CAPTURE diagnostic (the IO-dispatcher error still marks Dispatchers.IO) — @Suppress("BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE") on the
// enclosing function silences the FIR-phase error for a non-serializable local capture.
// The IR-backend lowering emits only a WARNING for local captures (not an error), so
// suppressing the FIR diagnostic leaves the compilation error-free.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Thing

@Suppress("BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE")
suspend fun load() {
    val t = Thing()
    withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
        println(t)
    }
}
