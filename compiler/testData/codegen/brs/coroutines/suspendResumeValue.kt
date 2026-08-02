// R1 reproducer: a value-returning suspend call whose result is bound to a local.
// After the suspension point, the resume state must assign the resumed value
// (suspendResult) to the target local, not `invalid`.

suspend fun produceValue(): Int {
    return 42
}

suspend fun consumeValue(): Int {
    val got = produceValue()
    return got + 1
}
