// BRS_SCOPE_ARG_NOT_MARSHALLABLE — ScopeRequest1/2 DECLARATION sites: A1/A2 type
// arguments outside the marshallable set are rejected (arguments cross BY COPY as plain
// data in the request envelope; behavior does not survive, and the declared request
// shape is the API to fix — pass plain data or restructure the request). The R position
// is BRS_SCOPE_RESULT_NOT_DATA territory (warning — data-shaped results are usable as
// data; see scopeResultNotData.kt for the severity verification).
import kotlin.brs.ScopeRequest1
import kotlin.brs.ScopeRequest2
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode

data class ArgBox(val id: Int)

// Case 1: kotlin collection A1 — ERROR
object ListArgReq : <!BRS_SCOPE_ARG_NOT_MARSHALLABLE!>ScopeRequest1<List<Int>, Int><!>("ListArgReq")

// Case 2: data-class A2 — ERROR
object DataArgReq : <!BRS_SCOPE_ARG_NOT_MARSHALLABLE!>ScopeRequest2<Int, ArgBox, String><!>("DataArgReq")

// Case 3: both args bad — one ERROR per offending position
object BothBadReq : <!BRS_SCOPE_ARG_NOT_MARSHALLABLE!><!BRS_SCOPE_ARG_NOT_MARSHALLABLE!>ScopeRequest2<List<Int>, ArgBox, Int><!><!>("BothBadReq")

// Case 4: bad arg AND bad result on one declaration — BOTH fire. (Historical note:
// this used to pin the ARG error only, blamed on a per-element WARNING drop; the real
// mechanism was GroupingMessageCollector dropping ALL plain warnings once a compile
// has errors — the harness now passes reportAllWarnings, so the warning is visible.)
object ArgAndResultReq : <!BRS_SCOPE_ARG_NOT_MARSHALLABLE!><!BRS_SCOPE_RESULT_NOT_DATA!>ScopeRequest1<ArgBox, ArgBox><!><!>("ArgAndResultReq")

// Case 5: data-shaped requests — CLEAN
object EchoReq : ScopeRequest1<String, String>("EchoReq")
object StoreReq : ScopeRequest2<String, RoAssociativeArray, Int>("StoreReq")
object NodeArgReq : ScopeRequest1<RoSGNode, Int>("NodeArgReq")
object NullableArgReq : ScopeRequest1<String?, Int>("NullableArgReq")

// Case 6: suppression escape — CLEAN (deliberate data-husk argument)
@Suppress("BRS_SCOPE_ARG_NOT_MARSHALLABLE")
object DeliberateListArg : ScopeRequest1<List<Int>, Int>("DeliberateListArg")
