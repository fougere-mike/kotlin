// Pins type-name sanitization in mangled function names: a suspend lambda
// SAM-converted to a fun interface (here Sink) becomes a class named after the
// lambda ("chain$slambda$slambda"); when its suspend method needs a state
// machine, the coroutine create function's __this parameter mangles that class
// TYPE into the function name. The raw "$" separators are invalid
// mid-identifier in BrightScript — device compile error in OperatorsKt.brs
// (flow simple-operators defect: every map/filter/onEach/catch/take/drop
// collect lambda hit it).

fun interface Sink {
    suspend fun push(value: Any?)
}

suspend fun identity(value: Any?): Any? {
    return value
}

suspend fun feed(sink: Sink) {
    sink.push(1)
}

fun build(block: suspend () -> Unit): suspend () -> Unit {
    return block
}

fun chain(transform: suspend (Any?) -> Any?): suspend () -> Unit {
    return build {
        feed { value ->
            identity(transform(value))
        }
    }
}
