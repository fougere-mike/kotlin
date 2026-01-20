/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.reflect

import kotlin.test.*
import kotlin.reflect.KClass
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray

// Test classes for KClass tests
class SimpleClass
open class Animal(val name: String)
class Dog(name: String) : Animal(name)
class Cat(name: String) : Animal(name)

/**
 * Tests for KClass runtime support.
 */
fun TestRunner.kclassTests() {
    suite("KClass") {
        test("simpleName returns class name") {
            assertEquals("SimpleClass", SimpleClass::class.simpleName)
        }

        test("simpleName for Animal") {
            assertEquals("Animal", Animal::class.simpleName)
        }

        test("simpleName for Dog") {
            assertEquals("Dog", Dog::class.simpleName)
        }

        test("same class literals are equal") {
            assertTrue(SimpleClass::class == SimpleClass::class)
        }

        test("different class literals are not equal") {
            assertFalse(Dog::class == Cat::class)
        }

        test("parent and child class literals are not equal") {
            assertFalse(Animal::class == Dog::class)
        }

        test("get class from instance") {
            val dog = Dog("Buddy")
            assertEquals("Dog", dog::class.simpleName)
        }

        test("get class from polymorphic reference") {
            val animal: Animal = Dog("Rex")
            assertEquals("Dog", animal::class.simpleName)
        }

        test("hashCode is consistent with equals") {
            assertEquals(Dog::class.hashCode(), Dog::class.hashCode())
        }

        test("different classes have different hashCodes") {
            // Not guaranteed but should be true for different class names
            assertNotEquals(Dog::class.hashCode(), Cat::class.hashCode())
        }

        test("isInstance returns true for matching type") {
            assertTrue(Dog::class.isInstance(Dog("Buddy")))
        }

        test("isInstance returns true for subclass") {
            assertTrue(Animal::class.isInstance(Dog("Buddy")))
        }

        test("isInstance returns false for non-matching type") {
            assertFalse(Dog::class.isInstance(Cat("Whiskers")))
        }

        test("isInstance returns false for null") {
            assertFalse(Dog::class.isInstance(null))
        }

        test("KClass can be used as map key") {
            val map = mutableMapOf<KClass<*>, String>()
            map[Dog::class] = "dog handler"
            map[Cat::class] = "cat handler"
            assertEquals("dog handler", map[Dog::class])
            assertEquals("cat handler", map[Cat::class])
        }

        test("KClass map lookup with instance class") {
            val map = mutableMapOf<KClass<*>, String>()
            map[Dog::class] = "dog handler"
            map[Cat::class] = "cat handler"

            val dog = Dog("Buddy")
            assertEquals("dog handler", map[dog::class])

            val cat = Cat("Whiskers")
            assertEquals("cat handler", map[cat::class])
        }

        test("qualifiedName returns null") {
            assertNull(SimpleClass::class.qualifiedName)
        }

        test("toString returns class prefix with name") {
            val str = Dog::class.toString()
            assertTrue(str.contains("Dog"))
        }
    }

    suite("KClass Native Types") {
        test("RoArray class literal simpleName") {
            assertEquals("roArray", RoArray::class.simpleName)
        }

        test("RoAssociativeArray class literal simpleName") {
            assertEquals("roAssociativeArray", RoAssociativeArray::class.simpleName)
        }

        test("native type class literals are equal") {
            assertTrue(RoArray::class == RoArray::class)
        }

        test("different native type class literals are not equal") {
            assertFalse(RoArray::class == RoAssociativeArray::class)
        }

        test("native type KClass as map key") {
            val map = mutableMapOf<KClass<*>, String>()
            map[RoArray::class] = "array handler"
            map[RoAssociativeArray::class] = "aa handler"
            assertEquals("array handler", map[RoArray::class])
            assertEquals("aa handler", map[RoAssociativeArray::class])
        }

        test("get class from native RoArray instance") {
            val arr = RoArray.create(0, true)
            assertEquals("roArray", arr::class.simpleName)
        }

        test("native instance class matches class literal") {
            val arr = RoArray.create(0, true)
            assertTrue(arr::class == RoArray::class)
        }

        test("native type KClass map lookup with instance") {
            val map = mutableMapOf<KClass<*>, String>()
            map[RoArray::class] = "array handler"

            val arr = RoArray.create(0, true)
            assertEquals("array handler", map[arr::class])
        }
    }
}
