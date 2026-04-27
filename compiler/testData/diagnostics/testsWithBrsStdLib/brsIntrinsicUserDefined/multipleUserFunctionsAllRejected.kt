// Expected: BRS_INTRINSIC_USER_DEFINED x3 — all three user functions independently rejected
import kotlin.brs.BrsIntrinsic

@BrsIntrinsic("brsIntrinsicOne")
external fun <!BRS_INTRINSIC_USER_DEFINED!>firstFun<!>(): Int

@BrsIntrinsic("brsIntrinsicTwo")
fun <!BRS_INTRINSIC_USER_DEFINED!>secondFun<!>(): String = ""

@BrsIntrinsic("brsIntrinsicThree")
external fun <!BRS_INTRINSIC_USER_DEFINED!>thirdFun<!>(): Boolean
