// A Unit-typed VALUE PARAMETER (a generic instantiated at Unit — here a SAM
// lambda for Sink<Unit>, the Flow<Unit>.collect { } shape collectLatest
// produces). BrightScript allows "as Void" in RETURN position only; a Void
// parameter is a device-side compile error ("Type is Invalid", &ha7).
// normalizeParametersForBrs must rewrite a Void parameter type to Dynamic
// (flow concurrent-operators defect: ConcurrentOperatorsKt.brs line 1588,
// collectLatest's empty collect lambda emitted `it as Void`).

fun interface Sink<T> {
    suspend fun push(value: T)
}

fun register(sink: Sink<Unit>): Sink<Unit> = sink

fun setup(): Sink<Unit> {
    return register { }
}
