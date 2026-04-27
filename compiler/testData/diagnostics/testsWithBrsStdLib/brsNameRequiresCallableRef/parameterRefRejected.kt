// Expected: BRS_BRSNAME_REQUIRES_CALLABLE_REF — function-typed parameter is a property
// reference at FIR (and an IrGetValue at IR) — not a syntactic ::ref
import kotlin.brs.brsName

fun outer(f: () -> Unit) {
    val name = brsName(<!BRS_BRSNAME_REQUIRES_CALLABLE_REF!>f<!>)
}
