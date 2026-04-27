// Expected: BRS_STATIC_INVALID_TARGET — interfaces are ClassKind.INTERFACE,
// which fails the OBJECT-or-companion guard.
import kotlin.brs.BrsStatic

interface I {
    @BrsStatic
    fun <!BRS_STATIC_INVALID_TARGET!>bar<!>()
}
