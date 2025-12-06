// Lambda and closure capture test
fun simpleCapture(): Int {
    val multiplier = 5
    val multiply = { x: Int -> x * multiplier }
    return multiply(10)
}

fun mutableCapture(): Int {
    var counter = 0
    val increment = { counter = counter + 1 }
    increment()
    increment()
    return counter
}

fun returnedClosure(): () -> Int {
    var count = 0
    return {
        count = count + 1
        count
    }
}

fun higherOrderFunction(value: Int, transform: (Int) -> Int): Int {
    return transform(value)
}

fun testHigherOrder(): Int {
    return higherOrderFunction(5) { it * 2 }
}
