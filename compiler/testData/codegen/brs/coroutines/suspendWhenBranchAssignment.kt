// WHEN_RESULT wrapping vs terminal-assignment branches (task 3c) — the
// visitWhen sibling of the TRY_RESULT wrap hole pinned by
// suspendWrapAssignmentArm.kt. A suspendable when whose static type is
// non-Unit wraps EVERY branch result in a WHEN_RESULT assignment; under
// generic inference (T = Any from the LUB of an Int branch and a Unit
// branch) the Unit branch's terminal statement is an ASSIGNMENT, and
// wrapping it makes that assignment the RHS of the WHEN_RESULT set —
// BrightScript renders it as a COMPARISON and the write is silently lost
// (m.WHEN_RESULT = (m._result.value = "else")). The per-branch wrap guard
// must look at the branch's terminal STATEMENT, not the when's static type.
// An unwrapped branch leaves WHEN_RESULT unassigned; reading it yields
// invalid — the BRS mapping of Unit, the branch's true Kotlin value.

fun <T> runIt(block: suspend () -> T) {
}

suspend fun step(): Int {
    return 1
}

fun kindIsIse(kind: String): Boolean {
    return kind == "ise"
}

fun whenBranchAssignment(kind: String) {
    var result = "none"
    runIt {
        if (kindIsIse(kind)) {
            step()
        } else {
            result = "else"
        }
    }
}
