// Expected: no diagnostic — only withContext(Dispatchers.IO) is checked.
// Other dispatchers pass through silently.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

class Thing

suspend fun run() {
    val t = Thing()
    withContext(Dispatchers.Main) { println(t) }
    withContext(Dispatchers.Default) { println(t) }
    withContext(Dispatchers.Unconfined) { println(t) }
}
