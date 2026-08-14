// BRS_TRY_FINALLY_UNSUPPORTED — try/finally whose nearest containing callable is not
// suspend: non-suspend visitTry silently drops the finally block at BRS codegen, so
// the FIR stopgap errors at the source. Suspend bodies compile finally correctly
// (state-machine path) and stay clean; a non-suspend lambda INSIDE a suspend function
// compiles as its own non-suspend BRS function, so the nearest callable wins.

// Case 1: non-suspend function — ERROR
fun nonSuspendFinally(): Int {
    <!BRS_TRY_FINALLY_UNSUPPORTED!>try {
        return 1
    } finally {
        println("cleanup")
    }<!>
}

// Case 2: suspend function — CLEAN (finally works inside suspend state machines)
suspend fun suspendFinallyOk(): Int {
    try {
        return 1
    } finally {
        println("cleanup")
    }
}

// Case 3: non-suspend lambda inside a suspend function — ERROR (the lambda compiles
// as its own non-suspend BRS function; nearest callable wins)
suspend fun lambdaTrap() {
    val f = {
        <!BRS_TRY_FINALLY_UNSUPPORTED!>try {
            println("a")
        } finally {
            println("b")
        }<!>
    }
    f()
}

// Case 4: try/catch WITHOUT finally, non-suspend — CLEAN
fun tryCatchOk() {
    try {
        println("a")
    } catch (e: Exception) {
        println("b")
    }
}

// Case 5: suppression escape — CLEAN
@Suppress("BRS_TRY_FINALLY_UNSUPPORTED")
fun deliberate() {
    try {
        println("a")
    } finally {
        println("b")
    }
}

// Case 6: property initializer — no containing callable at all, so the checker's
// else->false fall-through classifies it non-suspend — ERROR. Pins the initializer
// path against future refactors of the nearest-callable walk (ledgered gap).
val propInit: Int = <!BRS_TRY_FINALLY_UNSUPPORTED!>try {
    1
} finally {
    println("cleanup")
}<!>

