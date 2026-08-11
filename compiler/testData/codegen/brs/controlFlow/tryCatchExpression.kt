// try/catch in expression position: initializer, return value, call argument.
fun throwingLength(s: String?): Int {
    if (s == null) throw IllegalStateException("null input")
    return s.length
}

fun tryInInitializer(s: String?): Int {
    val n = try {
        throwingLength(s)
    } catch (e: Throwable) {
        -1
    }
    return n
}

fun tryInReturn(s: String?): String {
    return try {
        "ok:" + throwingLength(s)
    } catch (e: Throwable) {
        "fallback"
    }
}

fun wrap(n: Int): String {
    return "v" + n
}

fun tryInArgument(s: String?): String {
    return wrap(try { throwingLength(s) } catch (e: Throwable) { 0 })
}

private fun boomA(): Nothing {
    throw IllegalStateException("boomA")
}

private fun boomB(): Nothing {
    throw IllegalStateException("boomB")
}

// All-Nothing try in expression position: both arms throw, so the try's type
// is Nothing. It must still lower to a statement-level try (bare unassigned
// throwing arms, dead temp) instead of leaking `x = try ...`.
fun tryAllNothing(): Int {
    val x = try {
        boomA()
    } catch (e: Throwable) {
        boomB()
    }
    return 0
}

// Nothing-typed try arm with a value-producing catch arm: the try arm stays
// unwrapped, the catch arm assigns the temp.
fun tryNothingArm(s: String?): Int {
    return try {
        boomA()
    } catch (e: Throwable) {
        -1
    }
}
