// Test that passing a function-call result to `brs()` is rejected at FIR phase.
// Expected: BRS_INTRINSIC_LITERAL_REQUIRED on the call expression.

import kotlin.brs.brs

fun makeCode(): String = "1 + 2"

fun useBrsWithCall() {
    brs(<!BRS_INTRINSIC_LITERAL_REQUIRED!>makeCode()<!>)
}
