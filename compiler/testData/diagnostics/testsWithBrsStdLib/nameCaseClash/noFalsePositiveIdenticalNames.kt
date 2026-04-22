// Test that legitimate Kotlin overloads (identical names, distinct signatures)
// do NOT trigger BRS_NAME_CASE_CLASH. Overloads share a Kotlin name, so they
// don't collide under case-insensitive resolution — upstream handles any real
// signature clash via CONFLICTING_OVERLOADS, which doesn't fire for distinct
// parameter types.
// Expected: no BRS diagnostics.

fun foo(x: Int): Int = x
fun foo(x: String): String = x
