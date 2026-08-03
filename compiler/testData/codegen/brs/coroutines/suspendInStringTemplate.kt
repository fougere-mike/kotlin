// transformArguments shape (task-15 known gap, confirmed on device by task-16):
// a suspend call nested inside a string template. The IrStringConcatenation
// survives to the state machine builder (StringConcatenationLowering is a
// no-op), so without a visitStringConcatenation override the whole template -
// suspend call included - was emitted bare in one state: no suspendResult
// store, no COROUTINE_SUSPENDED check, no state split.
//
// simpleTemplate: pure prefix (val parameter) + one suspend call.
// mixedTemplate: non-pure prefix (mutated var, must be hoisted to a temp to
// preserve evaluation order across the suspension) + two suspend calls.

suspend fun produceText(): String {
    return "x"
}

suspend fun simpleTemplate(prefix: String): String {
    val msg = "$prefix ${produceText()}"
    return msg
}

suspend fun mixedTemplate(): String {
    var p = "p"
    val s = "$p ${produceText()} ${produceText()}"
    p = "q"
    return "$p $s"
}
