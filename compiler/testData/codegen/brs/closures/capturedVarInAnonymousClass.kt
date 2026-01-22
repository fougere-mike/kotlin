// Test: Anonymous class capturing mutable variable
interface Callback {
    fun onResult(value: Int)
}

fun testCapture() {
    var result: Int = 0
    val callback = object : Callback {
        override fun onResult(value: Int) {
            result = value
        }
    }
    callback.onResult(42)
}
