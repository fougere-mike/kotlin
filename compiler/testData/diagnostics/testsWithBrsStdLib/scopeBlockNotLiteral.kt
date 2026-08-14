// BRS_SCOPE_BLOCK_NOT_LITERAL — ScopeHandle.run(block) requires a literal lambda at the
// call site: the compiler lifts the block into a named request (runLowered). A stored
// function value or a function reference has no call-site body to lift; without this
// error the un-rewritten call hits the stdlib backstop ISE at runtime.
// The run(request, args...) overloads take ScopeRequest objects and are never checked.

import kotlin.brs.ScopeHandle
import kotlin.brs.ScopeRequest

object RefreshThing : ScopeRequest<Int>("RefreshThing")

// Case 1: literal lambda — CLEAN (the lowering lifts it)
suspend fun literalOk(owner: ScopeHandle): Int {
    return owner.run { 1 + 2 }
}

// Case 2: stored function value — ERROR
suspend fun storedValueRejected(owner: ScopeHandle): Int {
    val f: suspend () -> Int = { 3 }
    return owner.run(<!BRS_SCOPE_BLOCK_NOT_LITERAL!>f<!>)
}

// Case 3: function reference — ERROR (no call-site body to lift)
suspend fun refTarget(): Int = 4

suspend fun referenceRejected(owner: ScopeHandle): Int {
    return owner.run(<!BRS_SCOPE_BLOCK_NOT_LITERAL!>::refTarget<!>)
}

// Case 4: hand-written request overload — CLEAN (never lowered, never checked)
suspend fun requestOverloadOk(owner: ScopeHandle): Int {
    return owner.run(RefreshThing)
}

// Case 5: suppression escape — CLEAN (deliberate opt-in to the runtime backstop)
@Suppress("BRS_SCOPE_BLOCK_NOT_LITERAL")
suspend fun deliberate(owner: ScopeHandle): Int {
    val f: suspend () -> Int = { 5 }
    return owner.run(f)
}
