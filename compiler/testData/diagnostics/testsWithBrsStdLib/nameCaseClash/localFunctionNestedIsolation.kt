// Test that a case clash between local functions declared in a NESTED function body
// does not fire on the outer function. Each local function body is its own scope.
// The clash is reported on the inner declarations (inside `middle`), not on `outer`.

fun outer() {
    fun middle() {
        // These two clash with each other at middle's scope — reported on middle's locals.
        fun <!BRS_NAME_CASE_CLASH!>clashA<!>() {}
        fun <!BRS_NAME_CASE_CLASH!>CLASHA<!>() {}
    }
    // `outer` has only `middle` — no clash at outer's scope.
}
