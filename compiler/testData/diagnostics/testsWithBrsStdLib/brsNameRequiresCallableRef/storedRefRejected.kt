// Expected: BRS_BRSNAME_REQUIRES_CALLABLE_REF — stored callable reference; IR lowering
// cannot extract the function name from a variable (IrGetValue, not IrFunctionReference)
import kotlin.brs.brsName

fun topFun() {}

fun test() {
    val ref: () -> Unit = ::topFun
    val name = brsName(<!BRS_BRSNAME_REQUIRES_CALLABLE_REF!>ref<!>)
}
