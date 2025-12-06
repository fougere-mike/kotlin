// Control flow conditionals test
fun classifyNumber(x: Int): String {
    if (x > 0) {
        return "positive"
    } else if (x < 0) {
        return "negative"
    } else {
        return "zero"
    }
}

fun ifExpression(x: Int): String {
    return if (x > 0) "positive" else "non-positive"
}

fun nestedIf(a: Int, b: Int): String {
    return if (a > 0) {
        if (b > 0) "both positive"
        else "a positive, b non-positive"
    } else {
        if (b > 0) "a non-positive, b positive"
        else "both non-positive"
    }
}
