// Test that two local function declarations inside a function body whose effective BrightScript
// names differ only in case are rejected. BrightScript identifiers are case-insensitive, so
// `innerOne` and `INNERONE` silently collide at runtime.

fun outer() {
    fun <!BRS_NAME_CASE_CLASH!>innerOne<!>() {}
    fun <!BRS_NAME_CASE_CLASH!>INNERONE<!>() {}
}
