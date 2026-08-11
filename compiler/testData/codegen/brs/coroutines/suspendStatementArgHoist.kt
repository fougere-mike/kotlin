// Statement-position calls whose ARGUMENT hoists statements (safe-call/elvis
// machinery), inside state-machine states (Task 6.6). The hoisted machinery
// must be emitted BEFORE the call — never INSTEAD of it: the broken emission
// dropped the consuming call entirely (AwaitTest's assertEquals ran as
// safe-call machinery + nothing, passing vacuously on device).

suspend fun tick(): Int {
    return 1
}

// Unit-returning consumer: statement lands in transformBlockOrStatement's
// IrExpression branch.
fun note(tag: String, value: Int) {
    println(tag + ":" + value)
}

// Non-Unit consumer used with the value discarded: statement arrives wrapped
// in IMPLICIT_COERCION_TO_UNIT and lands in the type-operator unwrap's else
// branch (the second same-shape drop site).
fun record(tag: String, value: Int): Int {
    println(tag + ":" + value)
    return value
}

suspend fun hoistInState(s: String?): Int {
    tick()
    note("len", s?.length ?: -1)
    record("len2", s?.length ?: -2)
    return tick()
}

suspend fun hoistInIfBody(s: String?, flag: Boolean): Int {
    tick()
    if (flag) note("cond", s?.length ?: -3)
    return 0
}
