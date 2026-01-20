// Class reference tests
class Person(val name: String)

fun getPersonClass() = Person::class

fun getClassName(): String? {
    val klass = Person::class
    return klass.simpleName
}

fun useClassInfo(klass: kotlin.reflect.KClass<*>): String? {
    return klass.simpleName
}

fun testClassLiteral(): String? {
    return useClassInfo(Person::class)
}
