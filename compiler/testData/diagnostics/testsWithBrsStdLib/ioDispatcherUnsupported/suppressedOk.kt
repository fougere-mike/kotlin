// Expected: no diagnostics — the error is suppressible (stdlib internals and
// deliberate opt-ins into the quarantined pipeline use this).
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.dispatchers.Dispatchers

@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED")
fun makeScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
