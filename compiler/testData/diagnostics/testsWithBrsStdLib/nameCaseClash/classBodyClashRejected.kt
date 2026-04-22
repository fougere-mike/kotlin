// Test that two class-body members differing only in case are rejected.
// Expected: BRS_NAME_CASE_CLASH on both method names.

class Container {
    fun <!BRS_NAME_CASE_CLASH!>handle<!>() {}
    fun <!BRS_NAME_CASE_CLASH!>Handle<!>() {}
}
