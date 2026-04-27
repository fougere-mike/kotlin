// Expected: BRS_STATIC_INVALID_TARGET — @BrsStatic on a regular class member.
import kotlin.brs.BrsStatic

class Foo {
    @BrsStatic
    fun <!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
}
