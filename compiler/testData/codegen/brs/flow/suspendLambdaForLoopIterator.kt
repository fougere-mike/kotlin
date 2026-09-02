// Pins state-machine field naming for IR-special local names that are live
// across a suspension point. A for-loop's implicit <iterator> temporary lifted
// to a CoroutineImpl field must be sanitized: emitting the raw name produced
// `m.<iterator>0 = ...` — a BrightScript syntax error, caught on device as
// "Syntax Error. (compile error &h02)". Found by the Flow cold core (the
// flowOf/asFlow loop-emit builders are exactly this shape).

suspend fun consume(value: Int): Int {
    return value * 2
}

// The failing shape: suspend lambda, loop over a List, suspend call in the body —
// the iterator temporary is live across the suspension and lifts to a field.
fun makeSummingBlock(items: List<Int>): suspend () -> Int {
    return {
        var total = 0
        for (item in items) {
            total = total + consume(item)
        }
        total
    }
}

// Same shape in a named suspend function (same lowering path).
suspend fun sumDirect(items: List<Int>): Int {
    var total = 0
    for (item in items) {
        total = total + consume(item)
    }
    return total
}
