// Expected: no diagnostic — Dispatchers.IO + X context composition is NOT recognized
// as an IO dispatch. Documents a known gap matching the IR lowering behavior:
// users must write withContext(Dispatchers.IO) directly for the check to apply.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.EmptyCoroutineContext

class Thing

suspend fun run() {
    val t = Thing()
    withContext(Dispatchers.IO + EmptyCoroutineContext) {
        println(t)
    }
}
