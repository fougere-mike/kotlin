// Statement-position try/catch whose try body ends in a DISCARDED non-Unit
// suspend call (Task 6 device repro: try { d.await() } catch { thrown = e }).
// The try's type is non-Unit (LUB of the Int arm and the Unit catch arm), so
// BrsStateMachineBuilder materializes TRY_RESULT — and its unconsumed
// value-exposure read must NOT survive as a bare m.TRY_RESULT statement
// (BrightScript syntax error, device compile failure).

suspend fun fetchValue(flag: Boolean): Int {
    if (flag) {
        throw IllegalStateException("boom")
    }
    return 7
}

suspend fun guardedDiscard(flag: Boolean): Int {
    var sawError = 0
    try {
        fetchValue(flag)
    } catch (e: Throwable) {
        sawError = 1
    }
    return sawError
}
