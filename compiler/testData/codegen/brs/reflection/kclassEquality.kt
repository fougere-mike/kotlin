// KClass equality tests
class Foo
class Bar

fun testClassEquality(): Boolean {
    return Foo::class == Foo::class
}

fun testClassInequality(): Boolean {
    return Foo::class != Bar::class
}

fun testClassHashCode(): Int {
    return Foo::class.hashCode()
}

fun testClassToString(): String {
    return Foo::class.toString()
}
