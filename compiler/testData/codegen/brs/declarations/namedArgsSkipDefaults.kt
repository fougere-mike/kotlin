// Named arguments that skip defaulted parameters must keep positional
// alignment at the call site: each skipped slot emits `invalid` and the
// callee's default preamble materializes the default over it.
class C(val a: Int, val b: Boolean = false, val c: Boolean = false, val d: Boolean = false)

// Super-call argument lists must preserve alignment too
class D(x: Int) : C(x, c = true)

// Enum entry initializers route through initEntries, a separate assembly site
enum class E(val x: Int, val flag: Boolean = false, val tag: String = "t") {
    A(1, tag = "z")
}

// Data classes have their own constructor generator; it needs the guard preamble too
data class P(val a: Int, val b: Boolean = false, val c: Boolean = false)

object O {
    fun g(a: Int, b: Boolean = false, c: Boolean = false, d: Boolean = false): Int {
        var r = a
        if (b) r = r + 1
        if (c) r = r + 2
        if (d) r = r + 4
        return r
    }
}

fun f(a: Int, b: Boolean = false, c: Boolean = false, d: Boolean = false): Int {
    var r = a
    if (b) r = r + 1
    if (c) r = r + 2
    if (d) r = r + 4
    return r
}

fun testConstructorSkip(): C {
    return C(1, c = true)
}

fun testFunctionSkip(): Int {
    return f(1, d = true)
}

fun testObjectMethodSkip(): Int {
    return O.g(1, c = true)
}

fun testSuperSkip(): D {
    return D(2)
}

fun testEnumSkip(): String {
    return E.A.tag
}

fun testDataClassSkip(): P {
    return P(1, c = true)
}
