// Safe cast (as?) tests

// Simple identifier - trivial expression, safe to evaluate twice
fun safeCastIdentifier(x: Any): String? {
    return x as? String
}

// Function call - non-trivial expression, must hoist to temp var
fun getValue(): Any = "hello"

fun safeCastFunctionCall(): String? {
    return getValue() as? String
}

// Safe cast with Int type
fun safeCastToInt(x: Any): Int? {
    return x as? Int
}

// Safe cast with nullable parameter
fun safeCastNullable(x: Any?): String? {
    return x as? String
}

// Safe cast in elvis expression
fun safeCastElvis(x: Any): String {
    return (x as? String) ?: "default"
}

// Chained safe casts
fun chainedSafeCast(x: Any): Int? {
    val str = x as? String
    return str?.length
}

// Safe cast with class type
class MyClass(val value: Int)

fun safeCastToClass(x: Any): MyClass? {
    return x as? MyClass
}
