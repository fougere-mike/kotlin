// Test that a plain literal `brs("...")` call is accepted at FIR phase.
// Expected: no diagnostics.

import kotlin.brs.brs

fun useBrsWithLiteral() {
    brs("1 + 2")
}
