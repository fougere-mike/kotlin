// Expected: BRS_STATIC_INVALID_TARGET — verifies the FIR framework visits nested
// regular classes independently. `Outer` is an object (valid context) but `Inner`
// is a regular class (invalid context); the @BrsStatic on Inner.bar must fire.
import kotlin.brs.BrsStatic

object Outer {
    class Inner {
        @BrsStatic
        fun <!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
    }
}
