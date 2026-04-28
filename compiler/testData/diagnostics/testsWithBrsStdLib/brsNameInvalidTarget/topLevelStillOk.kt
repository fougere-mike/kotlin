// Expected: no diagnostic — top-level function ref is unaffected by the new check.
// Pairs with the existing brsNameRequiresCallableRef/topLevelRefOk.kt; this one
// guards against the new INVALID_TARGET branch accidentally rejecting valid targets.
import kotlin.brs.brsName

fun topLevelFun() {}

fun test() {
    val name = brsName(::topLevelFun)
}
