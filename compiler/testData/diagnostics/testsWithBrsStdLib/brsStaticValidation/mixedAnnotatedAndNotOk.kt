// Expected: no diagnostic — only one of the overloads is @BrsStatic. The rule is
// "two or more @BrsStatic functions sharing a name", not "one @BrsStatic shares
// a name with another non-static peer".
import kotlin.brs.BrsStatic

@BrsStatic
fun format(value: String): String = value

fun format(value: Int): String = value.toString()
