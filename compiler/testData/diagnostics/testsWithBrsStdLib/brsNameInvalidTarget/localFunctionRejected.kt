// Expected: BRS_BRSNAME_INVALID_TARGET — local function gets hoisted by LocalFunctionLowering
// after BrsIntrinsicLowering captures the name; the resulting string does not match the
// post-hoist module-level function actually emitted.
import kotlin.brs.brsName

fun outer() {
    fun innerFun() {}
    val name = brsName(<!BRS_BRSNAME_INVALID_TARGET!>::innerFun<!>)
}
