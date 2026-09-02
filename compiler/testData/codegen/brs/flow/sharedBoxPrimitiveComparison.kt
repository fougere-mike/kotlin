// Pins native comparison emission for shared-box primitives: a captured var Int
// mutated by a suspend lambda becomes a {value: ...} box whose reads degrade the
// EXPRESSION type to anyN. The `<`-family emitter classified the operand
// non-primitive and fell back to `a.compareTo(b) >= 0` — a method call on a bare
// BrightScript integer, "Member function not found" at runtime. The resolved
// builtin comparison still DECLARES Int operands, which must rescue the native
// path (flow simple-operators defect: take/drop count comparisons).

suspend fun use(value: Int): Int {
    return value
}

fun runEach(items: List<Int>, action: suspend (Int) -> Unit): suspend () -> Unit {
    return {
        for (item in items) {
            action(item)
        }
    }
}

fun skipper(count: Int): suspend () -> Unit {
    var skipped = 0
    return runEach(listOf(1, 2, 3)) { value ->
        if (skipped >= count) {
            use(value)
        } else {
            skipped++
        }
    }
}
