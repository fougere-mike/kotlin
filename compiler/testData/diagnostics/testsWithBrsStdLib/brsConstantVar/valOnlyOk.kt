// Expected: no diagnostic — every property is val.
import kotlin.brs.BrsConstant

@BrsConstant
object Valid {
    val a = 1
    val b = "two"
    val c = true
}
