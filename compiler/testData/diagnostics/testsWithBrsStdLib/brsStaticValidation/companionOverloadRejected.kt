// Expected: BRS_STATIC_OVERLOAD on both — same name inside one companion object.
import kotlin.brs.BrsStatic

class Holder {
    companion object {
        @BrsStatic
        fun <!BRS_STATIC_OVERLOAD!>format<!>(value: String): String = value

        @BrsStatic
        fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Int): String = value.toString()
    }
}
