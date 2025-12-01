// Simple test file for BrightScript compilation

fun add(a: Int, b: Int): Int {
    return a + b
}

fun greet(name: String): String {
    return "Hello, " + name
}

fun main() {
    val result = add(1, 2)
    val message = greet("Roku")

    // Test singleton access
    val config = AppConfig.setting

    // Test enum
    val myColor = Color.RED
}

object AppConfig {
    val setting = "default"
}

enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

data class Person(val name: String, val age: Int)

// Test local function with closure
fun outerFunction(x: Int): Int {
    var captured = x

    fun inner(y: Int): Int {
        return captured + y
    }

    return inner(10)
}

// Test lambda with closure
fun lambdaExample(multiplier: Int): (Int) -> Int {
    return { value -> value * multiplier }
}

// Test inner class
class Outer(val value: Int) {
    inner class Inner(val offset: Int) {
        fun compute(): Int = value + offset
    }

    fun createInner(offset: Int): Inner = Inner(offset)
}

// Test function reference
fun double(x: Int): Int = x * 2

fun testFunctionReference() {
    // Unbound function reference
    val ref = ::double
    val result = ref(5)  // Should be 10
}

// Test try/catch
fun testTryCatch() {
    try {
        val result = 10 / 0
    } catch (e: Throwable) {
        // Handle error
        val errorMsg = "Error occurred"
    }
}

// Test custom property accessors
class Counter {
    private var _count: Int = 0

    var count: Int
        get() = _count
        set(value) {
            if (value >= 0) {
                _count = value
            }
        }
}

// Test simple comparison
fun testComparison(x: Int): Boolean {
    return x >= 0
}
