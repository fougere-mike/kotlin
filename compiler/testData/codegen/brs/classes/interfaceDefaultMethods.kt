// Interface default methods test
// Verifies that classes properly inherit default method implementations from interfaces

// Simple interface with a default method
interface Greeter {
    fun greet(): String = "Hello"
}

// Class that inherits default method without override
class SimpleGreeter : Greeter

// Class that overrides the default method
class CustomGreeter : Greeter {
    override fun greet(): String = "Hi there"
}

// Interface with multiple default methods
interface Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun multiply(a: Int, b: Int): Int = a * b
    fun subtract(a: Int, b: Int): Int  // abstract method
}

// Class that implements abstract method but uses default for others
class BasicCalculator : Calculator {
    override fun subtract(a: Int, b: Int): Int = a - b
}

// Multiple interface inheritance with defaults
interface Named {
    fun getName(): String = "Unknown"
}

interface Aged {
    fun getAge(): Int = 0
}

class Person(val name: String, val age: Int) : Named, Aged {
    override fun getName(): String = name
    override fun getAge(): Int = age
}

// Uses default from one interface, overrides the other
class AnonymousPerson(val age: Int) : Named, Aged {
    // Uses default getName()
    override fun getAge(): Int = age
}

// Diamond inheritance pattern
interface Base {
    fun value(): Int = 42
}

interface Left : Base {
    // Inherits value() from Base
}

interface Right : Base {
    // Also inherits value() from Base
}

class Diamond : Left, Right {
    // Should get value() from Base
}

// Default method with parameters using receiver
interface Describable {
    val description: String
    fun describe(): String = "Description: $description"
}

class Item(override val description: String) : Describable

// Entry point functions for testing
fun testSimpleGreeter(): String {
    val g = SimpleGreeter()
    return g.greet()
}

fun testCustomGreeter(): String {
    val g = CustomGreeter()
    return g.greet()
}

fun testCalculator(): Int {
    val c = BasicCalculator()
    return c.add(2, 3) + c.multiply(2, 3) - c.subtract(10, 5)
}

fun testDiamond(): Int {
    val d = Diamond()
    return d.value()
}

fun testItem(): String {
    val item = Item("A test item")
    return item.describe()
}
