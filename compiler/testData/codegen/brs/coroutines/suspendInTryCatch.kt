// Suspend call inside try/catch (task-15 deferred minor). Exercises
// BrsStateMachineBuilder.visitTry both ways:
// - guardedStatement: suspend assignment inside try (statement form), plus a
//   suspension after the try so the result is live across it;
// - guardedValue: try used as a VALUE (TRY_RESULT temp machinery).
// Also locks the exception trap state: the trap must rethrow the coroutine's
// stored exception (m.__get_exception()), not the out-of-scope catch local.

suspend fun mayFail(flag: Boolean): Int {
    if (flag) {
        throw IllegalStateException("boom")
    }
    return 7
}

suspend fun tock(): Int {
    return 1
}

suspend fun guardedStatement(flag: Boolean): Int {
    var result = 0
    try {
        result = mayFail(flag)
    } catch (e: Exception) {
        result = -1
    }
    return result + tock()
}

suspend fun guardedValue(flag: Boolean): Int {
    val r = try {
        mayFail(flag)
    } catch (e: Exception) {
        -1
    }
    return r + 1
}
