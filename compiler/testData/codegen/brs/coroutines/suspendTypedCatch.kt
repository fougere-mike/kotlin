// Typed catch clauses in a SUSPEND state machine (task 3b). The catch state
// must discriminate by clause type: `is`-check on the stored exception, with a
// non-matching exception RETHROWN (throw m.__get_exception()) so it propagates
// to the enclosing handler — mirroring BrsMultiCatchLowering's merged dispatch
// and the JS backend's StateMachineBuilder. A clause typed exactly
// kotlin.Throwable stays UNCONDITIONAL (native BrightScript errors carry no
// Kotlin __proto and fail every `is` test, so a Throwable clause must not
// gain an is-check). Pins:
// - typedCaught / typedMissValue: single typed clause emits the is-check +
//   else-rethrow (statement-position try and TRY_RESULT value machinery);
// - secondClauseWins: two typed clauses (merged by BrsMultiCatchLowering
//   before the state machine) select by type in clause order;
// - throwableCatchAll: exact-Throwable clause remains unconditional.

suspend fun tick(): Int {
    return 1
}

suspend fun raise(kind: String): Int {
    if (kind == "ise") {
        throw IllegalStateException("ise")
    }
    if (kind == "iae") {
        throw IllegalArgumentException("iae")
    }
    return 0
}

// (a) Matching typed clause: at runtime the ISE passes the is-check and the
// clause body runs.
suspend fun typedCaught(): Int {
    var r = 0
    try {
        r = raise("ise") + tick()
    } catch (e: IllegalStateException) {
        r = -1
    }
    return r
}

// (b) Non-matching typed clause in VALUE position: at runtime the IAE fails
// the is-check and rethrows out of the try (TRY_RESULT never assigned on that
// path — the exit state is not reached).
suspend fun typedMissValue(): Int {
    val r = try {
        raise("iae")
    } catch (e: IllegalStateException) {
        -1
    }
    return r + 1
}

// (c) Two clauses, the SECOND matches at runtime: BrsMultiCatchLowering merges
// these into one catch-all clause with is-dispatch before the state machine
// runs, so the catch state carries the when-chain and an else-rethrow.
suspend fun secondClauseWins(): String {
    return try {
        raise("iae")
        "ok"
    } catch (e: IllegalStateException) {
        "ise"
    } catch (e: IllegalArgumentException) {
        "iae"
    }
}

// (d) Exact-Throwable clause: unconditional catch-all, no is-check — the shape
// native BrightScript errors rely on.
suspend fun throwableCatchAll(): Int {
    return try {
        raise("ise")
    } catch (e: Throwable) {
        -2
    }
}
