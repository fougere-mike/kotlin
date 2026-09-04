// Pins the field-name defence for coroutine classes
// (BrsSuspendFunctionsLowering.renameFieldsShadowingCoroutineBase). A coroutine
// class is flattened onto ONE BrightScript AA together with its CoroutineImpl
// base, so a suspend lambda's parameter stored under its raw name shadowed the
// base's own slot: `i.state = <value>` overwrote the state-machine dispatch
// index and the next `__get_state()` threw "Type Mismatch. Unable to cast
// roAssociativeArray to Integer" (device finding 2026-09-04, TestScreen
// flagship: `collectLatest { state -> ... }`). BrightScript is case-insensitive,
// so `Result` collides with `result` too. Every shadowing subclass field gets
// `_` appended: lambda parameters, a named suspend function's parameters, and
// a LocalDeclarationsLowering capture whose `$context` field is emitted as
// `_context` — InterceptedCoroutine/CoroutineImpl's own context slot.

suspend fun pause(): Int {
    return 1
}

suspend fun applyTwice(value: Int, block: suspend (Int) -> Int): Int {
    val once = block(value)
    return block(once)
}

// Lambda parameters named after CoroutineImpl fields.
suspend fun lambdaParams(): Int {
    val a = applyTwice(1) { state -> pause() + state }
    val b = applyTwice(2) { Result -> pause() + Result }
    val c = applyTwice(3) { exception -> pause() + exception }
    return a + b + c
}

// A named suspend function's parameters store onto its COROUTINE_ class the same way.
suspend fun namedParams(state: Int, exceptionState: Int): Int {
    val first = pause()
    return first + state + exceptionState
}

// A captured local named `context` becomes the `$context` capture field (emitted `_context`).
fun capturedContext(context: String): suspend () -> String {
    return {
        val n = pause()
        context + n
    }
}
