// Test that two file-level declarations differing only in case are rejected.
// Expected: BRS_NAME_CASE_CLASH on both declaration names.

val <!BRS_NAME_CASE_CLASH!>foo<!> = 1
val <!BRS_NAME_CASE_CLASH!>Foo<!> = 2
