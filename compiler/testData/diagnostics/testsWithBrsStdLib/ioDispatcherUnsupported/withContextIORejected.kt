// Expected: BOTH diagnostics — BRS_IO_DISPATCHER_UNSUPPORTED on Dispatchers.IO
// itself, and the ioWorkerCapture checker's finding on the non-serializable
// capture inside the block. The two checkers stay independent.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

suspend fun fetch(): String {
    val list: List<Int> = listOf(1, 2, 3)
    return withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
        "${<!BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE!>list<!>.size}"
    }
}
