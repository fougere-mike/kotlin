// Utility functions and closures

// Function that returns a closure
fun createMultiplier(factor: Int): (Int) -> Int {
    return { value -> value * factor }
}

// Function with local function (closure)
fun outerFunction(x: Int): Int {
    var captured = x

    fun inner(y: Int): Int {
        return captured + y
    }

    return inner(10)
}

// Lambda stored in variable
fun createAdder(base: Int): (Int) -> Int {
    val addBase = { x: Int -> x + base }
    return addBase
}

// Higher-order function
fun applyOperation(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// Simple utility functions
fun add(a: Int, b: Int): Int = a + b
fun subtract(a: Int, b: Int): Int = a - b
fun multiply(a: Int, b: Int): Int = a * b

fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}

fun min(a: Int, b: Int): Int {
    return if (a < b) a else b
}

fun clamp(value: Int, minVal: Int, maxVal: Int): Int {
    return if (value < minVal) minVal
    else if (value > maxVal) maxVal
    else value
}

// String utilities
fun greet(name: String): String {
    return "Hello, " + name + "!"
}

fun formatMessage(template: String, value: Int): String {
    return template + ": " + value
}
