// Expected: no diagnostic — both overloads suppress BRS_STATIC_OVERLOAD.
import kotlin.brs.BrsStatic

@Suppress("BRS_STATIC_OVERLOAD")
@BrsStatic
fun format(value: String): String = value

@Suppress("BRS_STATIC_OVERLOAD")
@BrsStatic
fun format(value: Int): String = value.toString()
