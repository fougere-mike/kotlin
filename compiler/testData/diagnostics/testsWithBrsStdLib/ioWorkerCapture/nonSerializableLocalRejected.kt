// Expected: BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE on `config` — type Config is not in the serializable whitelist.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Config(val url: String)

suspend fun run() {
    val config = Config("x")
    withContext(Dispatchers.IO) {
        println(<!BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE!>config<!>.url)
    }
}
