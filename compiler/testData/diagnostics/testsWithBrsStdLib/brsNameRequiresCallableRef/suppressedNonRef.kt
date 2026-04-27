// Expected: no diagnostic — BRS_BRSNAME_REQUIRES_CALLABLE_REF suppressed on enclosing function
import kotlin.brs.brsName

@Suppress("BRS_BRSNAME_REQUIRES_CALLABLE_REF")
fun test() {
    val name = brsName({ })
}
