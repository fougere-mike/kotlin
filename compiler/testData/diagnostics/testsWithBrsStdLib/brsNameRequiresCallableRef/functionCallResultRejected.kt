// Expected: BRS_BRSNAME_REQUIRES_CALLABLE_REF — runtime function value, not a ::ref
import kotlin.brs.brsName

fun returnsFunction(): () -> Unit = { }

fun test() {
    val name = brsName(<!BRS_BRSNAME_REQUIRES_CALLABLE_REF!>returnsFunction()<!>)
}
