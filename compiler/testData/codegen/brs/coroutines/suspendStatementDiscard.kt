// R2 reproducer: a non-Unit suspend call in statement position (result discarded).
// The generated code must still store the call result, test it against
// COROUTINE_SUSPENDED, and split the state machine at the call boundary.

suspend fun produceValue(): Int {
    return 42
}

suspend fun discardValue(): Int {
    produceValue()
    return 7
}
