// Simple class test
class Person(val name: String, val age: Int) {
    fun greet(): String {
        return "Hello, my name is $name"
    }

    fun isAdult(): Boolean {
        return age >= 18
    }
}

fun createPerson(): Person {
    return Person("Alice", 30)
}

fun testPerson(): String {
    val person = Person("Bob", 25)
    return person.greet()
}
