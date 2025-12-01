// Singleton objects and classes

object AppConfig {
    val apiUrl = "https://api.example.com"
    val timeout = 30
    val maxRetries = 3
}

object Logger {
    val level = "INFO"

    fun log(message: String): String {
        return "[" + level + "] " + message
    }
}

// Regular class
class Counter {
    private var _count: Int = 0

    var count: Int
        get() = _count
        set(value) {
            if (value >= 0) {
                _count = value
            }
        }

    fun increment() {
        _count = _count + 1
    }

    fun decrement() {
        if (_count > 0) {
            _count = _count - 1
        }
    }
}

// Class with constructor parameters
class Rectangle(val width: Int, val height: Int) {
    fun area(): Int = width * height
    fun perimeter(): Int = 2 * (width + height)
}

// Inner class example
class Outer(val value: Int) {
    inner class Inner(val offset: Int) {
        fun compute(): Int = value + offset
    }

    fun createInner(offset: Int): Inner = Inner(offset)
}
