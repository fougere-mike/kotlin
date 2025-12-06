// Nullability test
fun safeCallIncrement(value: Int?): Int? {
    return if (value != null) value + 1 else null
}

fun elvisOperator(value: Int?): Int {
    return value ?: 0
}

fun notNullAssertion(value: Int?): Int {
    return value!! + 1
}

fun nullCheck(value: Int?): String {
    return if (value != null) {
        "Has value"
    } else {
        "Is null"
    }
}

fun chainedSafeCall(holder: Holder?): Int? {
    return holder?.inner?.value
}

class Holder(val inner: Inner?)
class Inner(val value: Int)
