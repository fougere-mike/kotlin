// Component with state management
class Counter {
    private var count = 0

    fun increment() {
        count++
    }

    fun getCount(): Int {
        return count
    }
}

fun createCounter(): Counter {
    return Counter()
}
