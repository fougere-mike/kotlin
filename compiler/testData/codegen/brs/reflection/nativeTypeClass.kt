// Native type KClass tests - basic class reference only
// Note: Full native type tests require stdlib and are in runtime tests

class SimpleData(val value: Int)

// Test getting simpleName from a regular class
fun getSimpleClassName(): String? {
    return SimpleData::class.simpleName
}

// Test KClass.isInstance method
fun testIsInstance(): Boolean {
    val data = SimpleData(42)
    return SimpleData::class.isInstance(data)
}

// Test KClass.isInstance with non-matching type
fun testIsInstanceFalse(): Boolean {
    class Other
    val other = Other()
    return SimpleData::class.isInstance(other)
}
