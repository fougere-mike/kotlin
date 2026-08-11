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
