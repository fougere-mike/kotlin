package test.standard

import kotlin.test.*

/**
 * Task 9b deliverable 1 — the mangle length fallback vs dispatch contracts.
 *
 * The interface method mangles `probe_AnyN_k_` (type parameter erased) and the
 * call site dispatches that slot on the interface-typed receiver. The
 * implementing class's name is deliberately long enough that the OLD
 * class-prefixed length measurement crossed the 100-char cliff: the
 * implementation then attached a HASH-mangled slot while the call site kept
 * dispatching the readable interface mangle — "Member function not found" at
 * first dispatch (the defect that killed every task lift in a
 * package-qualified file). The fix measures the class-prefix-STRIPPED slot
 * name, so the decision is identical across the override family.
 */
private interface MangleContractProbe<T> {
    fun probe(value: T): String
}

private class MangleContractImplWithADeliberatelyVeryLongClassNameThatPushesThePrefixedMangleFarPastTheCliff :
    MangleContractProbe<Any?> {
    override fun probe(value: Any?): String = "ok:$value"
}

fun TestRunner.mangleContractTests() {
    suite("MangleContract") {

        test("interfaceDispatchSurvivesLongImplClassName") {
            val p: MangleContractProbe<Any?> =
                MangleContractImplWithADeliberatelyVeryLongClassNameThatPushesThePrefixedMangleFarPastTheCliff()
            var out = ""
            try {
                out = p.probe(1)
            } catch (e: Throwable) {
                out = "dispatch crashed: ${e.message}"
            }
            assertEquals("ok:1", out)
        }
    }
}
