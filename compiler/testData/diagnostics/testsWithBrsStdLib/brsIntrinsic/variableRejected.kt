// Test that passing a non-const variable to `brs()` is rejected at FIR phase.
// Expected: BRS_INTRINSIC_LITERAL_REQUIRED on the argument reference.

import kotlin.brs.brs

fun useBrsWithVariable(code: String) {
    brs(<!BRS_INTRINSIC_LITERAL_REQUIRED!>code<!>)
}
