// Control flow loops test
fun forLoopSum(): Int {
    var sum = 0
    for (i in 1..10) {
        sum = sum + i
    }
    return sum
}

fun whileLoopSum(): Int {
    var sum = 0
    var i = 0
    while (i < 10) {
        sum = sum + i
        i = i + 1
    }
    return sum
}

fun breakContinueSum(): Int {
    var sum = 0
    for (i in 1..10) {
        if (i == 3) continue
        if (i == 7) break
        sum = sum + i
    }
    return sum
}
