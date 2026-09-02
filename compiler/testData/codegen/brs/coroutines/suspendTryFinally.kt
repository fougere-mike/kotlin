// try/finally around a suspension (task 3b near-miss): FinallyBlocksLowering
// rewrites the finally into a SYNTHESIZED catch clause typed
// catchAllThrowableType — kotlin.Any? on this backend, NOT kotlin.Throwable.
// The state machine's typed-catch dispatch must treat Any/Any? as a
// CATCH-ALL (no is-check): an is-check there skips the finally on the
// exceptional path (__proto chains never list "Any"), which on device
// manifested as flow bodies' finally blocks not running under cancellation
// (FlowCore.cancellationMidCollectRunsFinally). Pins the exceptional-path
// finally clause staying unconditional.
//
// NOTE the try body deliberately ENDS with the suspend call: a trailing
// Unit-typed when (`if (flag) throw` as the LAST statement) is swallowed into
// the returnable-block result assignment and dropped from the emission — a
// separate PRE-EXISTING defect (visitSetValue tail-capture over a trailing
// Unit when), ledgered in the task-3b report, out of this golden's scope.

suspend fun beat(): Int {
    return 1
}

suspend fun guardedCleanup(flag: Boolean): Int {
    var cleanups = 0
    try {
        if (flag) {
            throw IllegalStateException("boom")
        }
        beat()
    } finally {
        cleanups = cleanups + 1
    }
    return cleanups
}
