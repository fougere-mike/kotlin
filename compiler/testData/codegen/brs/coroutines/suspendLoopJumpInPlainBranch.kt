// A break/continue nested in a NON-suspendable if, targeting a loop that
// contains suspension points. The loop is dissolved into states, so the jump
// must be rewritten into a state dispatch — which requires the suspendable-
// nodes collector to mark jumps targeting suspendable loops (the JS
// SuspendableNodesCollector.visitBreakContinue rule; the BRS port had dropped
// it). Pre-fix, the orphaned IrBreak survived into the state-machine body and
// crashed LivenessAnalysis: "Break from an unknown loop" (flow concurrent-
// operators defect: drainTo's `if (next === CLOSED) break` receive loop).

suspend fun next(): Any? = null

suspend fun drain(): Int {
    var taken = 0
    while (true) {
        val value = next()
        if (value == null) break
        taken = taken + 1
    }
    return taken
}

suspend fun pump(limit: Int): Int {
    var count = 0
    var i = 0
    while (i < limit) {
        i = i + 1
        val value = next()
        if (value == null) continue
        count = count + 1
    }
    return count
}
