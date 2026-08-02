// R3 reproducer: a var declared INSIDE a suspend lambda and written from a
// lambda nested within it. The var must live in a {value: ...} box created at
// its declaration; the nested lambda captures the box, and all reads/writes in
// the declaring scope go through .value. Buggy output never created the box
// (the nested lambda got a dead copy) and wrote the raw variable, crashing on
// device at suite registration.
//
// Two flavors:
// - registerSuite: no suspension point; the var stays a doResume local.
// - registerSuiteAcrossSuspend: the var is live across a suspend call, so it
//   moves to a coroutine-class field (the exact device shape) - box semantics
//   must survive the field motion.

class Runner {
    fun testAsync(name: String, body: suspend () -> Unit) {
    }
}

fun launchIt(block: () -> Unit) {
    block()
}

suspend fun tick(): Int {
    return 1
}

fun registerSuite(runner: Runner, scene: Any) {
    runner.testAsync("focusMoves") {
        var focused: Any? = null
        launchIt {
            focused = scene
        }
        if (focused == null) {
            focused = scene
        }
    }
}

fun registerSuiteAcrossSuspend(runner: Runner, scene: Any) {
    runner.testAsync("focusMovesAcrossSuspend") {
        var focused: Any? = null
        launchIt {
            focused = scene
        }
        val t = tick()
        if (focused == null) {
            focused = scene
        }
    }
}
