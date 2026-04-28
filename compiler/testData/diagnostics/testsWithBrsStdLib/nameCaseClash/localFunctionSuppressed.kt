// Test that @Suppress("BRS_NAME_CASE_CLASH") on local function declarations silences
// the diagnostic. Both clashing members must be suppressed for no diagnostic to fire.

fun outer() {
    @Suppress("BRS_NAME_CASE_CLASH")
    fun innerOne() {}
    @Suppress("BRS_NAME_CASE_CLASH")
    fun innerone() {}
}
