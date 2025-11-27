// Simple function test for BrightScript compilation

fun add(a: Int, b: Int): Int {
    return a + b
}

fun greet(name: String): String {
    return "Hello, " + name
}

fun main() {
    val result = add(1, 2)
    val message = greet("Roku")
    println(message)
}
