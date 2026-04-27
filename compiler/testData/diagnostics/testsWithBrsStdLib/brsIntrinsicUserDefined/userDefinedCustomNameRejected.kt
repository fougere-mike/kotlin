// Expected: BRS_INTRINSIC_USER_DEFINED — user function with a non-brsIntrinsic* name still rejected
import kotlin.brs.BrsIntrinsic

@BrsIntrinsic("completelyCustomName")
fun <!BRS_INTRINSIC_USER_DEFINED!>userCustom<!>(): Boolean = true
