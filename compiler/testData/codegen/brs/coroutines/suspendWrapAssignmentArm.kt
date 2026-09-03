// TRY_RESULT wrapping vs terminal-assignment arms (task 3c). When a generic
// builder (`fun <T> runIt(block: suspend () -> T)` — the runBlocking shape)
// infers the block's T as Any, an arm whose TERMINAL statement is an
// ASSIGNMENT gets a non-Unit static type — the LUB coercion lies about the
// arm's value (an assignment is Unit-valued in Kotlin no matter what the
// coerced type says). The wrap guard must therefore look at the arm's
// terminal STATEMENT, not its static type: wrapping such an arm makes the
// assignment the RHS of the TRY_RESULT set, and BrightScript renders an
// assignment in expression position as a COMPARISON — the write is silently
// lost (m.TRY_RESULT = (m._result.value = "caught")). Pins BOTH visitTry
// wrap sites:
// - catchArmAssignment: catch arm ends in a captured-var write (IrSetField
//   shared-box form), try arm carries the genuine Int value;
// - tryArmAssignment: try arm ends in the captured-var write, catch arm
//   carries the genuine Int value.
// The unwrapped arm leaves TRY_RESULT unassigned; reading it yields invalid,
// which is exactly the BRS mapping of Unit — the arm's true Kotlin value.

fun <T> runIt(block: suspend () -> T) {
}

suspend fun raise(kind: String): Int {
    if (kind == "ise") {
        throw IllegalStateException("ise")
    }
    return 0
}

fun catchArmAssignment() {
    var result = "none"
    runIt {
        try {
            raise("ise")
        } catch (e: IllegalStateException) {
            result = "caught"
        }
    }
}

fun tryArmAssignment() {
    var result = "none"
    runIt {
        try {
            result = "r:" + raise("ok")
        } catch (e: IllegalStateException) {
            -1
        }
    }
}
