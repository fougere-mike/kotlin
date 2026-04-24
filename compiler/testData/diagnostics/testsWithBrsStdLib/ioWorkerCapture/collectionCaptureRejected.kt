// Expected: one diagnostic on `list` — List<Int> is not in the serializable whitelist.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

suspend fun run() {
    val list: List<Int> = listOf(1, 2, 3)
    withContext(Dispatchers.IO) {
        println(<!BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE!>list<!>.size)
    }
}
