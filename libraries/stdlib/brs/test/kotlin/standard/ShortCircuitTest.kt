package test.standard

import kotlin.test.*

private class SideEffectCounter {
    var calls = 0
    fun hitTrue(): Boolean {
        calls = calls + 1
        return true
    }
    fun hitFalse(): Boolean {
        calls = calls + 1
        return false
    }
}

/**
 * Device-pinned Kotlin semantics for && / || on the BRS backend. The probe
 * test at the bottom records the platform truth for bare BRS `and` (Roku OS
 * 15.3.4: it short-circuited — see CLAUDE.md, BrightScript Language Notes);
 * the compiler must deliver Kotlin short-circuit semantics regardless of
 * platform behavior or OS level.
 */
fun TestRunner.shortCircuitTests() {
    suite("Short-circuit semantics") {

        test("&& skips effectful RHS when LHS is false") {
            val c = SideEffectCounter()
            var lhs = false
            var entered = false
            if (lhs && c.hitTrue()) {
                entered = true
            }
            assertFalse(entered)
            assertEquals(0, c.calls)
        }

        test("&& evaluates RHS exactly once when LHS is true") {
            val c = SideEffectCounter()
            var lhs = true
            val result = lhs && c.hitTrue()
            assertTrue(result)
            assertEquals(1, c.calls)
        }

        test("|| skips effectful RHS when LHS is true") {
            val c = SideEffectCounter()
            var lhs = true
            val result = lhs || c.hitTrue()
            assertTrue(result)
            assertEquals(0, c.calls)
        }

        test("|| evaluates RHS when LHS is false") {
            val c = SideEffectCounter()
            var lhs = false
            val result = lhs || c.hitFalse()
            assertFalse(result)
            assertEquals(1, c.calls)
        }

        test("null-guard && protects member access") {
            val list: ArrayList<Int>? = null
            var entered = false
            if (list != null && list.size > 0) {
                entered = true
            }
            assertFalse(entered)
        }

        test("null-guard || protects member access") {
            val list: ArrayList<Int>? = null
            var empty = false
            if (list == null || list.size == 0) {
                empty = true
            }
            assertTrue(empty)
        }

        test("chain evaluates left to right and stops at first decider") {
            val c = SideEffectCounter()
            val list: ArrayList<Int>? = null
            var entered = false
            if (c.hitFalse() && list != null && list.size > 0) {
                entered = true
            }
            assertFalse(entered)
            assertEquals(1, c.calls)
        }

        test("&& in while condition re-evaluates guard each iteration") {
            val items = ArrayList<Int>()
            items.add(2)
            items.add(1)
            items.add(-5)
            var n = 0
            while (items.size > 0 && items[0] > 0) {
                items.removeAt(0)
                n = n + 1
            }
            assertEquals(2, n)
            assertEquals(1, items.size)
        }

        test("&& in while condition re-evaluates after continue") {
            val items = ArrayList<Int>()
            items.add(2)
            items.add(0)   // skipped by continue, must still re-evaluate guard
            items.add(1)
            items.add(-5)
            var kept = 0
            while (items.size > 0 && items[0] > -1) {
                val head = items.removeAt(0)
                if (head == 0) continue
                kept = kept + 1
            }
            assertEquals(2, kept)
            assertEquals(1, items.size)
        }

        // PLATFORM PROBE (0a): records whether bare BRS `and` short-circuits on
        // this device. Asserts nothing about the platform — prints the truth
        // for CLAUDE.md. Kotlin locals share the generated function's scope
        // with brs() splice lines (ShelfView precedent).
        test("PROBE bare BRS and with invalid-receiver RHS") {
            var probeArr: ArrayList<Int>? = null
            var probeOutcome = "unset"
            try {
                // Expression splice bound to a local (the documented
                // `val x = brs("expr")` form) — emits the bare `and` expression
                // verbatim as the initializer. If BRS `and` evaluates the RHS,
                // .count() on invalid crashes -> catch arm. NB: bare-identifier
                // assignment and if-statement splices are silently dropped by
                // the intrinsic (BrsVariable/BrsIf hit the statement fallback).
                val hit = brs("probeArr <> invalid and probeArr.count() > 0")
                if (probeOutcome == "unset") {
                    probeOutcome = "short-circuited-or-skipped"
                }
            } catch (e: Throwable) {
                probeOutcome = "crashed-no-short-circuit"
            }
            println("[PROBE] bare BRS and, invalid receiver on RHS: " + probeOutcome)
            assertTrue(probeOutcome != "unset")
        }
    }
}
