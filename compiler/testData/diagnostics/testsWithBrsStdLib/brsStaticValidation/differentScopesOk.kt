// Expected: no diagnostic — same name in two different scopes (top-level + object)
// does NOT collide. Each scope is checked independently.
import kotlin.brs.BrsStatic

@BrsStatic
fun format(value: String): String = value

object Util {
    @BrsStatic
    fun format(value: Int): Int = value
}
