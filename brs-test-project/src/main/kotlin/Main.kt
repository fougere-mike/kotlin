// Main entry point for BrightScript test app

fun main() {
    // Test basic output
    val greeting = "BrightScript Test App"

    // Test data class
    val user = User(1, "Alice", "alice@example.com")
    val userStr = user.toString()

    // Test enum
    val status = Status.ACTIVE
    val statusCode = status.code

    // Test singleton
    val apiUrl = AppConfig.apiUrl
    val timeout = AppConfig.timeout

    // Test closure
    val doubler = createMultiplier(2)
    val result = doubler(5)  // Should be 10

    // Test function reference
    val squareRef = ::square
    val squared = squareRef(4)  // Should be 16

    // Test try/catch
    testTryCatch()
}

fun square(x: Int): Int = x * x

fun testTryCatch() {
    try {
        val value = riskyOperation()
    } catch (e: Throwable) {
        val errorHandled = true
    }
}

fun riskyOperation(): Int {
    return 42
}
