// Expected: BRS_STATIC_OVERLOAD on both — same name inside the same `object`.
import kotlin.brs.BrsStatic

object Util {
    @BrsStatic
    fun <!BRS_STATIC_OVERLOAD!>format<!>(value: String): String = value

    @BrsStatic
    fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Int): String = value.toString()
}
