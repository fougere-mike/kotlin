// Test that @Suppress("BRS_NAME_CASE_CLASH") silences the diagnostic.
// Expected: no BRS diagnostics.

@Suppress("BRS_NAME_CASE_CLASH")
val foo = 1

@Suppress("BRS_NAME_CASE_CLASH")
val Foo = 2
