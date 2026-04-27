// Expected: no diagnostic — BRS_INTRINSIC_USER_DEFINED suppressed on the function
import kotlin.brs.BrsIntrinsic

@Suppress("BRS_INTRINSIC_USER_DEFINED")
@BrsIntrinsic("brsIntrinsicFoo")
fun suppressedFun(): Int = 0
