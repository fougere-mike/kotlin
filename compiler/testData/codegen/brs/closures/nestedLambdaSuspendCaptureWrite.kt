// Task-16 R3 acceptance shape: a var declared inside a suspend lambda and
// assigned FROM A SUSPEND CALL inside a lambda nested within it - the device
// shape `scope.launch { x = suspendCall() }`. By state-machine time the write
// is an IrSetField (the nested lambda writes through the captured shared box),
// so the suspension split must come from visitSetField, not visitSetValue.
// Buggy output emitted the whole assignment bare in one state: the box received
// COROUTINE_SUSPENDED itself, the state returned invalid instead of the
// sentinel, and the state id never advanced (device fingerprint:
// "Expected <echo:one>, actual <CoroutineSingletons>").

class Runner {
    fun testAsync(name: String, body: suspend () -> Unit) {
    }
}

fun launchIt(block: suspend () -> Unit) {
}

suspend fun produceValue(): Int {
    return 42
}

suspend fun tick(): Int {
    return 1
}

fun registerSuiteSuspendCaptureWrite(runner: Runner) {
    runner.testAsync("capturedVarFromSuspend") {
        var got: Int? = null
        launchIt {
            got = produceValue()
        }
        val t = tick()
        if (got == null) {
            got = t
        }
    }
}
