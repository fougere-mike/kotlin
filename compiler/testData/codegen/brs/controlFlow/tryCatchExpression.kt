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
