// Multiple catch clauses (device-pinned defect, Suite 8 ScopeHandle bring-up):
// clause dispatch was lost on BOTH paths — the suspend state machine ran the
// FIRST clause unconditionally with the remaining clauses emitted as dead code
// (BrsStateMachineBuilder), and the non-suspend emitter dropped every clause
// after the first outright (BrsTry holds a single catch). This golden pins:
// (1) a suspend multi-catch, (2) a non-suspend multi-catch without a catch-all
// (unmatched exceptions must rethrow), (3) a non-suspend multi-catch whose last
// clause is a Throwable catch-all.
suspend fun pause(): Int {
    return 1
}

suspend fun classifySuspend(): String {
    return try {
        pause()
        "ok"
    } catch (e: IllegalStateException) {
        "ise:" + (e.message ?: "")
    } catch (e: Exception) {
        "exc"
    }
}

fun classifyPlain(input: String): String {
    return try {
        if (input == "x") throw IllegalStateException("boom")
        "ok"
    } catch (e: IllegalStateException) {
        "ise"
    } catch (e: Exception) {
        "exc"
    }
}

fun classifyWithCatchAll(input: String): String {
    return try {
        if (input == "x") throw IllegalArgumentException("nope")
        "ok"
    } catch (e: IllegalArgumentException) {
        "iae"
    } catch (e: Throwable) {
        "other"
    }
}
