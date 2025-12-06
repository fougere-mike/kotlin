// Inheritance test
open class Animal(val name: String) {
    open fun speak(): String {
        return "..."
    }

    fun introduce(): String {
        return "I am $name"
    }
}

class Dog(name: String) : Animal(name) {
    override fun speak(): String {
        return "Woof!"
    }
}

class Cat(name: String) : Animal(name) {
    override fun speak(): String {
        return "Meow!"
    }
}

fun createDog(): Dog {
    return Dog("Buddy")
}

fun createCat(): Cat {
    return Cat("Whiskers")
}

fun testDogSpeak(): String {
    val dog = Dog("Rex")
    return dog.speak()
}
