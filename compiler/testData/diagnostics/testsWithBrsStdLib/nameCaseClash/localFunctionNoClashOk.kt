// Test that local functions with distinct BrightScript names do not trigger BRS_NAME_CASE_CLASH.

fun outer() {
    fun alpha() {}
    fun beta() {}
    fun gamma() {}
}
