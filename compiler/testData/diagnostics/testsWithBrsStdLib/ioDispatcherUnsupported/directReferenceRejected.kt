// Expected: BRS_IO_DISPATCHER_UNSUPPORTED on the Dispatchers.IO reference —
// on this platform IO dispatch silently falls back to the render-thread queue
// (TaskPool is quarantined and never initialized), so the name lies about the
// behavior. Background work goes through flowOn(Dispatchers.Task) for streams,
// spawnTask for one-shot blocks, or runTask<T> for typed tasks.
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.dispatchers.Dispatchers

fun makeScope(): CoroutineScope = CoroutineScope(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>)
