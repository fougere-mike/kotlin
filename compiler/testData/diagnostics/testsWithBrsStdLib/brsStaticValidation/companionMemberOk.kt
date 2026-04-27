// Expected: no diagnostic — @BrsStatic on a companion-object member is valid.
import kotlin.brs.BrsStatic

class Holder {
    companion object {
        @BrsStatic
        fun format(value: String): String = value
    }
}
