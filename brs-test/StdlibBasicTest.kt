/**
 * Basic BrightScript stdlib test
 * Tests only implemented features
 */

import kotlin.io.println

fun testArrayList() {
    println("=== ArrayList Test ===")
    val list = ArrayList<Int>()
    list.add(1)
    list.add(2)
    list.add(3)
    println("ArrayList size: " + list.size.toString())
    println("First element: " + list[0].toString())
}

fun testHashMap() {
    println("=== HashMap Test ===")
    val map = HashMap<String, Int>()
    map.put("one", 1)
    map.put("two", 2)
    println("HashMap size: " + map.size.toString())
    println("Value for 'one': " + map.get("one").toString())
}

fun testUnsigned() {
    println("=== Unsigned Types Test ===")
    val a = 42u
    val b = 8u
    println("42u value: " + a.toString())
    println("8u value: " + b.toString())
}

fun testRanges() {
    println("=== Ranges Test ===")
    var count = 0
    for (i in 1..5) {
        count = count + 1
    }
    println("Counted 1..5: " + count.toString())
}

fun testPair() {
    println("=== Pair Test ===")
    val pair = Pair(1, "one")
    println("Pair first: " + pair.first.toString())
    println("Pair second: " + pair.second)
}

fun main() {
    println("BrightScript Stdlib Basic Tests")
    println("================================")
    testArrayList()
    testHashMap()
    testUnsigned()
    testRanges()
    testPair()
    println("================================")
    println("Tests Completed!")
}
