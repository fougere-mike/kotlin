// Expected: BRS_IO_DISPATCHER_UNSUPPORTED at the aliasing site only — the
// alias USE is not flagged (documented limitation, matching the ioWorker
// capture checker's identity-based detection).
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.dispatchers.CoroutineDispatcher
import kotlin.coroutines.dispatchers.Dispatchers

fun aliased(): CoroutineScope {
    val d: CoroutineDispatcher = <!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>
    return CoroutineScope(d)
}
