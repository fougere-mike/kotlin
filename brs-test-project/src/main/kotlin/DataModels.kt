// Data class definitions

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class Product(
    val sku: String,
    val price: Int  // Using Int instead of Double for simplicity
)

data class Point(val x: Int, val y: Int)

// Enum with properties
enum class Status(val code: Int) {
    PENDING(0),
    ACTIVE(1),
    COMPLETED(2),
    CANCELLED(3)
}

enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}
