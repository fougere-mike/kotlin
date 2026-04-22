// Test that a mutable-variable reference inside a string template is rejected,
// with the diagnostic pinpointed at the template argument (not the full string).
// Expected: BRS_INTRINSIC_LITERAL_REQUIRED on the `v` reference.

import kotlin.brs.brs

fun useBrsWithTemplate(v: String) {
    brs("print ${<!BRS_INTRINSIC_LITERAL_REQUIRED!>v<!>}")
}
