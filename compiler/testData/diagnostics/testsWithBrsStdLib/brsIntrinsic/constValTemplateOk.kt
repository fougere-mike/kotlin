// Test that a `const val` reference inside a string template argument is accepted
// at FIR phase. `canBeEvaluatedAtCompileTime` folds const-val refs, so the FIR
// checker sees a compile-time constant. The IR-backend's `foldBrsCodeString`
// also walks the reference (see B4(e)), so FIR and IR agree on acceptance.

import kotlin.brs.brs

const val EXPR = "1 + 2"

fun useBrsWithConstVal() {
    brs("${EXPR}")
}
