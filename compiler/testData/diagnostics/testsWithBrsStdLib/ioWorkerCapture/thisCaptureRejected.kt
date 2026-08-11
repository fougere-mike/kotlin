// Expected: BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE on `name` resolved via implicit this.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Fetcher {
    val name: String = "x"
    suspend fun load() {
        withContext(<!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!>) {
            val n = <!BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE!>name<!>
            println(n)
        }
    }
}
