// BRS_SCOPE_CAPTURE_MUTATION_LOST (warning) — ScopeHandle.run { } captures cross BY
// COPY: the lifted block re-declares each capture as a local read from the captures AA
// (BrsScopeRunBlockLowering), so an assignment to a captured var inside the block writes
// the owner-side copy and never reaches the caller. Fires ONLY inside run{} blocks —
// ordinary lambdas share the caller's frame and mutate the real variable.
import kotlin.brs.ScopeHandle

// Case 1: assignment to a captured var — WARNING (the caller's `total` stays 0)
suspend fun assignmentLost(owner: ScopeHandle): Int {
    var total = 0
    return owner.run {
        <!BRS_SCOPE_CAPTURE_MUTATION_LOST!>total<!> = 5
        total
    }
}

// Case 2: compound assignment — WARNING (desugars to an assignment)
suspend fun compoundLost(owner: ScopeHandle): Int {
    var count = 10
    return owner.run {
        <!BRS_SCOPE_CAPTURE_MUTATION_LOST!>count<!> += 1
        count
    }
}

// Case 3: var declared INSIDE the block — CLEAN (owner-side local, nothing to lose)
suspend fun localVarOk(owner: ScopeHandle): Int {
    return owner.run {
        var acc = 0
        acc += 2
        acc
    }
}

// Case 4: ordinary lambda mutating a captured var — CLEAN (same frame, write is real)
fun ordinaryLambdaOk(): Int {
    var hits = 0
    val bump = { hits += 1 }
    bump()
    return hits
}

// Case 5: read-only capture of a var — CLEAN (reads cross fine; only writes are lost)
suspend fun readOnlyCaptureOk(owner: ScopeHandle): Int {
    var seed = 4
    seed += 1
    return owner.run { seed }
}

// Case 6: suppression escape — CLEAN (deliberate owner-side scratch write)
@Suppress("BRS_SCOPE_CAPTURE_MUTATION_LOST")
suspend fun deliberateWrite(owner: ScopeHandle): Int {
    var flag = 0
    return owner.run {
        flag = 1
        flag
    }
}

// Case 7: write from a lambda NESTED inside the block — still lost (the nested lambda
// closes over the owner-side copy, exactly like a direct write) — WARNING
suspend fun nestedLambdaWriteLost(owner: ScopeHandle): Int {
    var tally = 0
    return owner.run {
        val bump = { <!BRS_SCOPE_CAPTURE_MUTATION_LOST!>tally<!> = 3 }
        bump()
        tally
    }
}
