// Expected: no diagnostic — @BrsStatic on a member of `object` is valid.
import kotlin.brs.BrsStatic

object Util {
    @BrsStatic
    fun format(value: String): String = value
}
