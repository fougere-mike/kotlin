// Short-circuit semantics: Kotlin && / || must not evaluate an effectful RHS
// when the LHS decides the result. BRS `and`/`or` evaluate both operands, so
// impure RHS operands are hoisted to a guarded temp; pure operands keep the
// compact `and`/`or` form.
class Holder(val flag: Boolean) {
    fun check(): Boolean = flag
}

fun pureOperands(a: Boolean, b: Boolean): Boolean {
    return a && b || !a
}

fun nullGuardAnd(h: Holder?): Boolean {
    return h != null && h.check()
}

fun nullGuardOr(h: Holder?): Boolean {
    return h == null || h.check()
}

fun guardInIf(h: Holder?): String {
    if (h != null && h.check()) return "yes"
    return "no"
}

fun guardInWhile(items: ArrayList<Int>): Int {
    var n = 0
    while (items.size > 0 && items[0] > 0) {
        items.removeAt(0)
        n = n + 1
    }
    return n
}

fun mixedChain(h: Holder?, enabled: Boolean): Boolean {
    return enabled && h != null && h.check()
}

class Scored(val score: Int) : Comparable<Scored> {
    override fun compareTo(other: Scored): Int = score - other.score
}

// A non-first when-branch condition with an impure RHS: the guard must be
// nested under the preceding condition's else, NOT hoisted before the chain
// (Kotlin only evaluates it after earlier conditions were false).
fun whenMultiBranch(a: Boolean, h: Holder?): String {
    return when {
        a -> "first"
        h != null && h.check() -> "second"
        else -> "third"
    }
}

// User-defined Comparable: x < y compiles to a compareTo METHOD CALL, which
// runs user code — it must hoist, never stay inside a bare `and`.
fun comparableGuard(x: Scored?, y: Scored): Boolean {
    return x != null && x < y
}

// continue must not skip the per-iteration re-evaluation of the hoisted
// condition guard.
fun continueInWhile(items: ArrayList<Int>): Int {
    var n = 0
    while (items.size > 0 && items[0] > 0) {
        val v = items.removeAt(0)
        if (v == 2) continue
        n = n + 1
    }
    return n
}
