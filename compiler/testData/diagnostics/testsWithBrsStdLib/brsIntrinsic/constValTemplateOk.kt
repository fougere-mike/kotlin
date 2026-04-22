// Test that a `const val` reference inside a string template argument is accepted
// at FIR phase. `canBeEvaluatedAtCompileTime` folds const-val refs, so the FIR
// checker sees a compile-time constant.
//
// NOTE: the IR-backend's `foldBrsCodeString` does not yet walk IrGetValue for
// const-val references (see B4(e) followup). This fixture exercises only the
// FIR checker's acceptance. IR-phase collateral errors are tolerated — the
// harness only verifies error-name equality for declared markers.

import kotlin.brs.brs

const val EXPR = "1 + 2"

fun useBrsWithConstVal() {
    brs("${EXPR}")
}
