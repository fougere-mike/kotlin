// Expected: no diagnostic — all captured locals are whitelisted serializable types.
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers

suspend fun run() {
    val i: Int = 7
    val l: Long = 7L
    val f: Float = 1.0f
    val d: Double = 1.0
    val b: Boolean = true
    val s: String = "hi"
    val nullableString: String? = null
    withContext(Dispatchers.IO) {
        println(i)
        println(l)
        println(f)
        println(d)
        println(b)
        println(s)
        println(nullableString)
    }
}
