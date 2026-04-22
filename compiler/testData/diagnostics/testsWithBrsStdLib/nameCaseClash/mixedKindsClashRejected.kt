// Test that a case-insensitive name clash across kinds (property + function)
// is rejected. Verifies the checker groups across declaration kinds, not just
// within same-kind members.
// Expected: BRS_NAME_CASE_CLASH on both names.

val <!BRS_NAME_CASE_CLASH!>handler<!> = 1
fun <!BRS_NAME_CASE_CLASH!>Handler<!>() {}
