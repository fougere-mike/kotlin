// Pins shared-box classification of state-machine fields against NAME-KEY
// collisions. sharedVariableFields used to be keyed by class-name + field-name
// STRINGS — but sibling suspend lambdas at the same nesting path share a raw
// class name, so a boxed captured var lifted to a coroutine field in ONE lambda
// (shared0 in the second block below) falsely reclassified the SAME-NAMED plain
// field in the sibling lambda: its accesses emitted `m.shared0.value` with no
// box ever created — device error "Invalid value for left-side of expression".
// Coroutine box fields are now keyed by field SYMBOL.
// Found by the Flow cold core device suite (FlowCore flowEmitsValuesInOrder:
// two test bodies in one suite are exactly such sibling lambdas).

suspend fun tick(): Int {
    return 1
}

fun hold(block: suspend () -> Int): suspend () -> Int {
    return block
}

fun driver(): Int {
    // Plain local val, live across a suspension -> coroutine field shared0 of
    // the first sibling lambda; must stay a plain field (no .value indirection).
    // The inner closure capturing it pins the LOCAL-CLASS half of the same
    // defect: the sibling boxed lambda's closure registers its _shared field as
    // a box, and name-string keys reclassified THIS closure's plain _shared
    // capture too (device error "Interface not a member of BrightScript
    // Component" on the .value deref). Local-class box fields are now keyed by
    // class IDENTITY + field name.
    val plain = hold {
        val shared = tick()
        val note = { shared + 1 }
        tick()
        note()
        shared
    }

    // Captured mutable var (mutated by the nested closure) -> boxed; lifted to
    // coroutine field shared0 of the second sibling lambda (a box), and captured
    // as a box field _shared on the closure class.
    val boxed = hold {
        var shared = 0
        val bump = { shared += 1 }
        tick()
        bump()
        shared
    }

    if (plain === boxed) return 0
    return 1
}
