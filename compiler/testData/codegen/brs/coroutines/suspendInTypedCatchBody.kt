// A suspension INSIDE a typed catch clause BODY (task 3b review rider). The
// typed-clause emission is-dispatches on the stored exception; a SUSPENDING
// clause body leaves currentState on its resume state, so the builder saves
// and restores currentState around the clause body (BrsStateMachineBuilder
// visitTry, the visitWhen discipline) — the else-chain's rethrow and its
// successor edges must belong to the CATCH state, not to the body's resume
// state. Pins: the is-check with the suspension split inside the then-branch
// (the clause body's tail — including the catch-parameter read e.message —
// runs in the resume state), and the else-rethrow staying in the catch
// state. Both arms are Unit-typed assignments, so no TRY_RESULT machinery
// entangles the pin.

suspend fun tick(): Int {
    return 1
}

suspend fun raise(kind: String): Int {
    if (kind == "ise") {
        throw IllegalStateException("ise")
    }
    return 0
}

suspend fun typedCatchBodySuspends(): String {
    var r = "none"
    try {
        r = "t:" + raise("ise")
    } catch (e: IllegalStateException) {
        r = "c:" + tick() + ":" + (e.message ?: "")
    }
    return r
}
