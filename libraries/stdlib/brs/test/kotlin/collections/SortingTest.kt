/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for sorting operations.
 */
fun TestRunner.sortingTests() {
    suite("Sorting") {
        test("sort integers") {
            val list = mutableListOf(5, 2, 8, 1, 9, 3)
            list.sort()
            assertEquals(listOf(1, 2, 3, 5, 8, 9), list)
        }

        test("sort strings") {
            val list = mutableListOf("dog", "cat", "apple", "zebra")
            list.sort()
            assertEquals(listOf("apple", "cat", "dog", "zebra"), list)
        }

        test("sort with comparator") {
            val list = mutableListOf(5, 2, 8, 1, 9, 3)
            list.sortWith(reverseOrder())
            assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
        }

        test("sort descending") {
            val list = mutableListOf(5, 2, 8, 1, 9, 3)
            list.sortDescending()
            assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
        }

        test("sortBy") {
            val list = mutableListOf("apple", "pie", "a", "zoo")
            list.sortBy { it.length }
            assertEquals(listOf("a", "pie", "zoo", "apple"), list)
        }

        test("sortByDescending") {
            val list = mutableListOf("apple", "pie", "a", "zoo")
            list.sortByDescending { it.length }
            assertEquals(listOf("apple", "pie", "zoo", "a"), list)
        }

        test("sorted (immutable)") {
            val list = listOf(5, 2, 8, 1, 9, 3)
            val sorted = list.sorted()

            // Original list unchanged
            assertEquals(listOf(5, 2, 8, 1, 9, 3), list)
            // Sorted list is new
            assertEquals(listOf(1, 2, 3, 5, 8, 9), sorted)
        }

        test("sortedWith") {
            val list = listOf(5, 2, 8, 1, 9, 3)
            val sorted = list.sortedWith(reverseOrder())
            assertEquals(listOf(9, 8, 5, 3, 2, 1), sorted)
        }

        test("sortedDescending") {
            val list = listOf(5, 2, 8, 1, 9, 3)
            val sorted = list.sortedDescending()
            assertEquals(listOf(9, 8, 5, 3, 2, 1), sorted)
        }

        test("sortedBy") {
            val list = listOf("apple", "pie", "a", "zoo")
            val sorted = list.sortedBy { it.length }
            assertEquals(listOf("a", "pie", "zoo", "apple"), sorted)
        }

        test("sortedByDescending") {
            val list = listOf("apple", "pie", "a", "zoo")
            val sorted = list.sortedByDescending { it.length }
            assertEquals(listOf("apple", "pie", "zoo", "a"), sorted)
        }

        test("sort stability") {
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

        test("sort empty list") {
            val list = mutableListOf<Int>()
            list.sort()
            assertEquals(emptyList(), list)
        }

        test("sort single element") {
            val list = mutableListOf(42)
            list.sort()
            assertEquals(listOf(42), list)
        }

        test("sort large list") {
            val list = mutableListOf<Int>()
            for (i in 100 downTo 1) {
                list.add(i)
            }

            list.sort()

            for (i in 0 until 100) {
                assertEquals(i + 1, list[i])
            }
        }

        test("compareBy") {
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

        test("comparator reversed") {
            val list = mutableListOf(5, 2, 8, 1, 9, 3)
            list.sortWith(naturalOrder<Int>().reversed())
            assertEquals(listOf(9, 8, 5, 3, 2, 1), list)
        }

        test("compareValues") {
            assertEquals(0, compareValues(5, 5))
            assertTrue(compareValues(3, 5) < 0)
            assertTrue(compareValues(5, 3) > 0)
            assertTrue(compareValues<Int>(null, 5) < 0)
            assertTrue(compareValues<Int>(5, null) > 0)
            assertEquals(0, compareValues<Int>(null, null))
        }
    }
}
