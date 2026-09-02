// Pins statement-position `&&` conditions whose RHS is a NEGATED suspend-call
// result inside a suspend function: `if (a && !suspendCall()) return`. The
// state-machine emission dropped the guarded-temp wiring — the resumed value
// was negated as a bare statement (`not ARGUMENT` — a BrightScript syntax
// error) and the branch degenerated to `if true then false` / `if invalid`.
// Found by the Flow cold core (FirstCollector.emit's predicate guard).

suspend fun pred(x: Int): Boolean {
    return x > 1
}

suspend fun check(x: Int): Int {
    if (x != 0 && !pred(x)) return -1
    return 1
}
