package test.standard

import kotlin.test.*

private fun boom(): Int {
    throw IllegalStateException("boom")
}

fun TestRunner.tryExpressionTests() {
    suite("try-expression semantics") {

        test("try expression yields try-arm value") {
            val n = try { "abc".length } catch (e: Throwable) { -1 }
            assertEquals(3, n)
        }

        test("try expression yields catch-arm value on throw") {
            val n = try { boom() } catch (e: Throwable) { -1 }
            assertEquals(-1, n)
        }

        test("try expression in return position") {
            fun f(fail: Boolean): String {
                return try {
                    if (fail) boom()
                    "ok"
                } catch (e: Throwable) {
                    "caught:" + (e.message ?: "?")
                }
            }
            assertEquals("ok", f(false))
            assertEquals("caught:boom", f(true))
        }

        test("try expression as call argument") {
            fun wrap(n: Int): String = "v" + n
            assertEquals("v7", wrap(try { 7 } catch (e: Throwable) { 0 }))
            assertEquals("v0", wrap(try { boom() } catch (e: Throwable) { 0 }))
        }

        test("nested try expressions") {
            val n = try {
                try { boom() } catch (e: Throwable) { boom() }
            } catch (e: Throwable) {
                42
            }
            assertEquals(42, n)
        }
    }
}
