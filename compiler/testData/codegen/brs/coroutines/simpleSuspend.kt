// Simple suspend function - should compile to a state machine
suspend fun simpleSuspend(): Int {
    return 42
}

// Suspend function that calls another suspend function
suspend fun callingSuspend(): Int {
    val result = simpleSuspend()
    return result + 1
}

// Suspend function with parameters
suspend fun suspendWithParams(a: Int, b: String): String {
    return "$b: $a"
}
