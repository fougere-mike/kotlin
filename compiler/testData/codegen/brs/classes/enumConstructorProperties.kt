// Enum constructor val properties: the enum constructor attaches
// this.__get_x = ClassName___get_x_k_ references, and property reads through
// a variable receiver compile to receiver.__get_x() — so the accessor
// functions must be generated (they were silently dropped before).

enum class Fruit(val label: String, internal val rank: Int) {
    APPLE("apple", 1),
    BANANA("banana", 2)
}

fun labelOf(f: Fruit): String = f.label

fun rankOf(f: Fruit): Int = f.rank
