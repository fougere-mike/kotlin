// Get class from instance tests
open class Animal(val name: String)
class Dog(name: String) : Animal(name)

fun getClassFromInstance(animal: Animal): kotlin.reflect.KClass<out Animal> {
    return animal::class
}

fun testGetClass(): String? {
    val dog = Dog("Buddy")
    val klass = dog::class
    return klass.simpleName
}
