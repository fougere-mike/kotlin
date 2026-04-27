// Expected: BRS_INTRINSIC_USER_DEFINED — non-external user function carrying @BrsIntrinsic (annotation silently ignored today)
import kotlin.brs.BrsIntrinsic

@BrsIntrinsic("brsIntrinsicBar")
fun <!BRS_INTRINSIC_USER_DEFINED!>userNonExternal<!>(): String = "hello"
