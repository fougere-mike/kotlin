/**
 * Comprehensive BrightScript stdlib tests
 * Tests collections, unsigned types, ranges, sequences, and math functions
 */

import kotlin.io.println

// Test collections
fun testCollections() {
    println("=== Collections Test ===")

    // ArrayList
    val list = ArrayList<Int>()
    list.add(1)
    list.add(2)
    list.add(3)
    println("ArrayList size: ${list.size}")  // Should be 3
    println("First element: ${list[0]}")  // Should be 1
    list.remove(1)
    println("After remove, size: ${list.size}")  // Should be 2

    // HashMap
    val map = HashMap<String, Int>()
    map["one"] = 1
    map["two"] = 2
    map["three"] = 3
    println("HashMap size: ${map.size}")  // Should be 3
    println("Value for 'one': ${map["one"]}")  // Should be 1
    println("Contains 'two': ${map.containsKey("two")}")  // Should be true
    map.remove("two")
    println("After remove, size: ${map.size}")  // Should be 2

    // HashSet
    val set = HashSet<String>()
    set.add("apple")
    set.add("banana")
    set.add("apple")  // Duplicate
    println("HashSet size: ${set.size}")  // Should be 2 (no duplicates)
    println("Contains 'banana': ${set.contains("banana")}")  // Should be true
}

// Test unsigned types
fun testUnsigned() {
    println()
    println("=== Unsigned Types Test ===")

    // UInt
    val a = 42u
    val b = 8u
    val sum = a + b
    val product = a * b
    println("42u + 8u = $sum")  // Should be 50u
    println("42u * 8u = $product")  // Should be 336u
    println("UInt.MAX_VALUE = ${UInt.MAX_VALUE}")

    // UByte
    val x: UByte = 255u
    println("UByte max: $x")
    val y: UByte = 1u
    println("255u (UByte) = $x")

    // ULong
    val big = 1000000000u
    println("ULong value: $big")
}

// Test ranges
fun testRanges() {
    println()
    println("=== Ranges Test ===")

    // Basic range
    print("1..5: ")
    for (i in 1..5) {
        print("$i ")
    }
    println()

    // Step range
    print("0..10 step 2: ")
    for (i in 0..10 step 2) {
        print("$i ")
    }
    println()

    // Downward range
    print("5 downTo 1: ")
    for (i in 5 downTo 1) {
        print("$i ")
    }
    println()

    // Range contains check
    println("5 in 1..10: ${5 in 1..10}")  // Should be true
    println("15 in 1..10: ${15 in 1..10}")  // Should be false
}

// Test sequences
fun testSequence() {
    println()
    println("=== Sequences Test ===")

    val seq = sequenceOf(1, 2, 3, 4, 5)

    print("Doubled: ")
    val doubled = seq.map { it * 2 }
    doubled.forEach { print("$it ") }
    println()

    print("Filtered (even only): ")
    val seq2 = sequenceOf(1, 2, 3, 4, 5, 6)
    val evens = seq2.filter { it % 2 == 0 }
    evens.forEach { print("$it ") }
    println()

    val seq3 = sequenceOf(1, 2, 3, 4, 5)
    val sum = seq3.sum()
    println("Sum of 1-5: $sum")  // Should be 15
}

// Test math functions
fun testMath() {
    println()
    println("=== Math Test ===")

    println("abs(-42) = ${kotlin.math.abs(-42)}")  // Should be 42
    println("max(10, 20) = ${kotlin.math.max(10, 20)}")  // Should be 20
    println("min(10, 20) = ${kotlin.math.min(10, 20)}")  // Should be 10
}

// Test String operations (basic)
fun testStrings() {
    println()
    println("=== Strings Test ===")

    val str = "Hello, World!"
    println("Original: $str")
    println("Length: ${str.length}")
    println("Substring(0, 5): ${str.substring(0, 5)}")  // Should be "Hello"
    println("Substring(7): ${str.substring(7)}")  // Should be "World!"
}

// Test Pair and basic types
fun testPair() {
    println()
    println("=== Pair Test ===")

    val pair = Pair(1, "one")
    println("Pair: ${pair.first}, ${pair.second}")

    val (num, name) = pair  // Destructuring
    println("Destructured: num=$num, name=$name")
}

// Main test runner
fun main() {
    println("╔════════════════════════════════════════╗")
    println("║   BrightScript Stdlib E2E Tests       ║")
    println("╚════════════════════════════════════════╝")

    testCollections()
    testUnsigned()
    testRanges()
    testSequence()
    testMath()
    testStrings()
    testPair()

    println()
    println("╔════════════════════════════════════════╗")
    println("║   All Tests Completed Successfully!   ║")
    println("╚════════════════════════════════════════╝")
}
