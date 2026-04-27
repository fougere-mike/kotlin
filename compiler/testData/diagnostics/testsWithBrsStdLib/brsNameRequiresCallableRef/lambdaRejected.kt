// Expected: BRS_BRSNAME_REQUIRES_CALLABLE_REF — lambda is not a function reference
import kotlin.brs.brsName

fun test() {
    val name = brsName(<!BRS_BRSNAME_REQUIRES_CALLABLE_REF!>{ }<!>)
}
