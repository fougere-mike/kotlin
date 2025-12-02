/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.collections.*
import kotlin.test.*

class LinkedHashMapTest {
    @Test
    fun testInsertionOrder() {
        val map = LinkedHashMap<String, Int>()
        map["three"] = 3
        map["one"] = 1
        map["two"] = 2

        val keys = map.keys.toList()
        assertEquals(listOf("three", "one", "two"), keys)

        val values = map.values.toList()
        assertEquals(listOf(3, 1, 2), values)
    }

    @Test
    fun testInsertionOrderWithUpdates() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        map["three"] = 3
        map["two"] = 22  // Update existing key

        // Order should remain the same after update
        val keys = map.keys.toList()
        assertEquals(listOf("one", "two", "three"), keys)
        assertEquals(22, map["two"])
    }

    @Test
    fun testConstructorWithMap() {
        val original = mapOf("a" to 1, "b" to 2, "c" to 3)
        val linked = LinkedHashMap(original)

        assertEquals(3, linked.size)
        assertTrue(linked.containsKey("a"))
        assertTrue(linked.containsKey("b"))
        assertTrue(linked.containsKey("c"))
    }

    @Test
    fun testPutAndGet() {
        val map = LinkedHashMap<Int, String>()
        assertNull(map.put(1, "one"))
        assertEquals("one", map.put(1, "ONE"))
        assertEquals("ONE", map[1])
    }

    @Test
    fun testRemove() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        map["three"] = 3

        assertEquals(2, map.remove("two"))
        assertFalse(map.containsKey("two"))
        assertEquals(2, map.size)

        val keys = map.keys.toList()
        assertEquals(listOf("one", "three"), keys)
    }

    @Test
    fun testClear() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2

        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun testIteratorRemove() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        map["three"] = 3

        val iter = map.keys.iterator()
        while (iter.hasNext()) {
            if (iter.next() == "two") {
                iter.remove()
            }
        }

        assertEquals(2, map.size)
        assertFalse(map.containsKey("two"))
    }

    @Test
    fun testEntrySet() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2

        val entries = map.entries.toList()
        assertEquals(2, entries.size)
        assertEquals("one", entries[0].key)
        assertEquals(1, entries[0].value)
    }

    @Test
    fun testPutAll() {
        val map = LinkedHashMap<String, Int>()
        map["one"] = 1

        val other = mapOf("two" to 2, "three" to 3)
        map.putAll(other)

        assertEquals(3, map.size)
        assertEquals(listOf("one", "two", "three"), map.keys.toList())
    }
}

class LinkedHashSetTest {
    @Test
    fun testInsertionOrder() {
        val set = LinkedHashSet<String>()
        set.add("three")
        set.add("one")
        set.add("two")

        val list = set.toList()
        assertEquals(listOf("three", "one", "two"), list)
    }

    @Test
    fun testNoDuplicates() {
        val set = LinkedHashSet<Int>()
        assertTrue(set.add(1))
        assertTrue(set.add(2))
        assertFalse(set.add(1))  // Duplicate

        assertEquals(2, set.size)
        assertEquals(listOf(1, 2), set.toList())
    }

    @Test
    fun testConstructorWithCollection() {
        val list = listOf(3, 1, 2, 1, 3)  // Has duplicates
        val set = LinkedHashSet(list)

        assertEquals(3, set.size)
        assertEquals(listOf(3, 1, 2), set.toList())  // Maintains first occurrence order
    }

    @Test
    fun testRemove() {
        val set = LinkedHashSet<String>()
        set.add("one")
        set.add("two")
        set.add("three")

        assertTrue(set.remove("two"))
        assertFalse(set.remove("four"))

        assertEquals(2, set.size)
        assertEquals(listOf("one", "three"), set.toList())
    }

    @Test
    fun testContains() {
        val set = LinkedHashSet<Int>()
        set.add(1)
        set.add(2)
        set.add(3)

        assertTrue(set.contains(2))
        assertFalse(set.contains(5))
    }

    @Test
    fun testIterator() {
        val set = LinkedHashSet<Int>()
        set.add(10)
        set.add(20)
        set.add(30)

        val collected = mutableListOf<Int>()
        for (element in set) {
            collected.add(element)
        }

        assertEquals(listOf(10, 20, 30), collected)
    }

    @Test
    fun testIteratorRemove() {
        val set = LinkedHashSet<String>()
        set.add("a")
        set.add("b")
        set.add("c")

        val iter = set.iterator()
        while (iter.hasNext()) {
            if (iter.next() == "b") {
                iter.remove()
            }
        }

        assertEquals(2, set.size)
        assertEquals(listOf("a", "c"), set.toList())
    }

    @Test
    fun testAddAll() {
        val set = LinkedHashSet<Int>()
        set.add(1)

        val result = set.addAll(listOf(2, 3, 1))  // 1 is duplicate
        assertTrue(result)

        assertEquals(3, set.size)
        assertEquals(listOf(1, 2, 3), set.toList())
    }

    @Test
    fun testRemoveAll() {
        val set = LinkedHashSet<Int>()
        set.addAll(listOf(1, 2, 3, 4, 5))

        val result = set.removeAll(listOf(2, 4))
        assertTrue(result)

        assertEquals(3, set.size)
        assertEquals(listOf(1, 3, 5), set.toList())
    }

    @Test
    fun testRetainAll() {
        val set = LinkedHashSet<Int>()
        set.addAll(listOf(1, 2, 3, 4, 5))

        val result = set.retainAll(listOf(2, 3, 6))
        assertTrue(result)

        assertEquals(2, set.size)
        assertEquals(listOf(2, 3), set.toList())
    }

    @Test
    fun testClear() {
        val set = LinkedHashSet<String>()
        set.addAll(listOf("a", "b", "c"))

        set.clear()

        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }
}

class ArraySortingTest {
    @Test
    fun testSortIntegers() {
        val list = mutableListOf(5, 2, 8, 1, 9, 3)
        list.sort()

        assertEquals(listOf(1, 2, 3, 5, 8, 9), list)
    }

    @Test
    fun testSortStrings() {
        val list = mutableListOf("dog", "cat", "apple", "zebra")
        list.sort()

        assertEquals(listOf("apple", "cat", "dog", "zebra"), list)
    }

    @Test
    fun testSortWithComparator() {
        val list = mutableListOf(5, 2, 8, 1, 9, 3)
        list.sortWith(reverseOrder())

        assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
    }

    @Test
    fun testSortDescending() {
        val list = mutableListOf(5, 2, 8, 1, 9, 3)
        list.sortDescending()

        assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
    }

    @Test
    fun testSortBy() {
        val list = mutableListOf("apple", "pie", "a", "zoo")
        list.sortBy { it.length }

        assertEquals(listOf("a", "pie", "zoo", "apple"), list)
    }

    @Test
    fun testSortByDescending() {
        val list = mutableListOf("apple", "pie", "a", "zoo")
        list.sortByDescending { it.length }

        assertEquals(listOf("apple", "pie", "zoo", "a"), list)
    }

    @Test
    fun testSorted() {
        val list = listOf(5, 2, 8, 1, 9, 3)
        val sorted = list.sorted()

        // Original list unchanged
        assertEquals(listOf(5, 2, 8, 1, 9, 3), list)
        // Sorted list is new
        assertEquals(listOf(1, 2, 3, 5, 8, 9), sorted)
    }

    @Test
    fun testSortedWith() {
        val list = listOf(5, 2, 8, 1, 9, 3)
        val sorted = list.sortedWith(reverseOrder())

        assertEquals(listOf(9, 8, 5, 3, 2, 1), sorted)
    }

    @Test
    fun testSortedDescending() {
        val list = listOf(5, 2, 8, 1, 9, 3)
        val sorted = list.sortedDescending()

        assertEquals(listOf(9, 8, 5, 3, 2, 1), sorted)
    }

    @Test
    fun testSortedBy() {
        val list = listOf("apple", "pie", "a", "zoo")
        val sorted = list.sortedBy { it.length }

        assertEquals(listOf("a", "pie", "zoo", "apple"), sorted)
    }

    @Test
    fun testSortedByDescending() {
        val list = listOf("apple", "pie", "a", "zoo")
        val sorted = list.sortedByDescending { it.length }

        assertEquals(listOf("apple", "pie", "zoo", "a"), sorted)
    }

    @Test
    fun testSortStability() {
        data class Person(val name: String, val age: Int)

        val list = mutableListOf(
            Person("Alice", 30),
            Person("Bob", 25),
            Person("Charlie", 30),
            Person("David", 25)
        )

        // Sort by age - should preserve relative order of same-age people
        list.sortBy { it.age }

        assertEquals(25, list[0].age)
        assertEquals("Bob", list[0].name)
        assertEquals(25, list[1].age)
        assertEquals("David", list[1].name)
        assertEquals(30, list[2].age)
        assertEquals("Alice", list[2].name)
        assertEquals(30, list[3].age)
        assertEquals("Charlie", list[3].name)
    }

    @Test
    fun testSortEmptyList() {
        val list = mutableListOf<Int>()
        list.sort()

        assertEquals(emptyList(), list)
    }

    @Test
    fun testSortSingleElement() {
        val list = mutableListOf(42)
        list.sort()

        assertEquals(listOf(42), list)
    }

    @Test
    fun testSortLargeList() {
        val list = mutableListOf<Int>()
        for (i in 100 downTo 1) {
            list.add(i)
        }

        list.sort()

        for (i in 0 until 100) {
            assertEquals(i + 1, list[i])
        }
    }

    @Test
    fun testCompareBy() {
        data class Person(val name: String, val age: Int)

        val list = mutableListOf(
            Person("Alice", 30),
            Person("Bob", 25),
            Person("Charlie", 30)
        )

        list.sortWith(compareBy { it.age })

        assertEquals("Bob", list[0].name)
        assertEquals("Alice", list[1].name)
        assertEquals("Charlie", list[2].name)
    }

    @Test
    fun testCompareByMultiple() {
        data class Person(val name: String, val age: Int, val height: Int)

        val list = mutableListOf(
            Person("Alice", 30, 170),
            Person("Bob", 25, 180),
            Person("Charlie", 30, 160),
            Person("David", 25, 175)
        )

        // Sort by age first, then by height
        list.sortWith(compareBy({ it.age }, { it.height }))

        assertEquals("David", list[0].name)  // age=25, height=175
        assertEquals("Bob", list[1].name)    // age=25, height=180
        assertEquals("Charlie", list[2].name) // age=30, height=160
        assertEquals("Alice", list[3].name)  // age=30, height=170
    }

    @Test
    fun testComparatorThen() {
        data class Person(val name: String, val age: Int)

        val list = mutableListOf(
            Person("Alice", 30),
            Person("Bob", 25),
            Person("Charlie", 30)
        )

        val ageComparator = compareBy<Person> { it.age }
        val nameComparator = compareBy<Person> { it.name }

        list.sortWith(ageComparator.then(nameComparator))

        assertEquals("Bob", list[0].name)
        assertEquals("Alice", list[1].name)
        assertEquals("Charlie", list[2].name)
    }

    @Test
    fun testComparatorReversed() {
        val list = mutableListOf(5, 2, 8, 1, 9, 3)
        list.sortWith(naturalOrder<Int>().reversed())

        assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
    }

    @Test
    fun testCompareValues() {
        assertEquals(0, compareValues(5, 5))
        assertTrue(compareValues(3, 5) < 0)
        assertTrue(compareValues(5, 3) > 0)
        assertTrue(compareValues<Int>(null, 5) < 0)
        assertTrue(compareValues<Int>(5, null) > 0)
        assertEquals(0, compareValues<Int>(null, null))
    }
}
