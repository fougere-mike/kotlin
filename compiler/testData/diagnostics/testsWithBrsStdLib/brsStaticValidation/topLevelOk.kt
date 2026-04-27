// Expected: no diagnostic — @BrsStatic on a top-level function is always valid.
import kotlin.brs.BrsStatic

@BrsStatic
fun format(value: String): String = value
