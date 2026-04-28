// Test that a local `val` and a local `fun` whose effective BrightScript names differ
// only in case are both rejected. BrightScript names are case-insensitive; a property
// `count` and a function `COUNT` occupy the same runtime name.

fun outer() {
    val <!BRS_NAME_CASE_CLASH!>count<!> = 42
    fun <!BRS_NAME_CASE_CLASH!>COUNT<!>() {}
}
