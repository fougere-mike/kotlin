// Expected: BRS_STATIC_INVALID_TARGET — abstract class is still ClassKind.CLASS.
import kotlin.brs.BrsStatic

abstract class Foo {
    @BrsStatic
    open fun <!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
}
