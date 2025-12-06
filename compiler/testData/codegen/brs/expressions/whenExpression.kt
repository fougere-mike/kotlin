// When expression test
fun describeNumber(x: Int): String {
    return when (x) {
        0 -> "zero"
        1 -> "one"
        2 -> "two"
        else -> "many"
    }
}

fun getGrade(score: Int): String {
    return when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        score >= 70 -> "C"
        score >= 60 -> "D"
        else -> "F"
    }
}

fun categorize(x: Int): String {
    return when (x) {
        1, 2, 3 -> "small"
        4, 5, 6 -> "medium"
        else -> "large"
    }
}
