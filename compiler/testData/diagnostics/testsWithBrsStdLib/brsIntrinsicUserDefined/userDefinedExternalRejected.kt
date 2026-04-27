// Expected: BRS_INTRINSIC_USER_DEFINED — user-defined external function carrying @BrsIntrinsic
import kotlin.brs.BrsIntrinsic

@BrsIntrinsic("brsIntrinsicFoo")
external fun <!BRS_INTRINSIC_USER_DEFINED!>userExternal<!>(): Int
