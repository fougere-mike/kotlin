// Expected: BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE on `cb` — function types are not serializable.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

suspend fun run() {
    val cb: () -> Unit = {}
    withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
        <!BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE!>cb<!>()
    }
}
