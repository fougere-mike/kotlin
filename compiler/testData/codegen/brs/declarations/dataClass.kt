// Data class test
data class Point(val x: Int, val y: Int)

data class User(val name: String, val age: Int)

fun createPoint(): Point {
    return Point(1, 2)
}

fun copyPoint(p: Point): Point {
    return p.copy(y = 10)
}

fun comparePoints(): Boolean {
    val p1 = Point(1, 2)
    val p2 = Point(1, 2)
    return p1 == p2
}

fun createUser(): User {
    return User("Alice", 30)
}
