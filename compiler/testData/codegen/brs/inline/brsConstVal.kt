// Test that brs() accepts const val references — direct and inside string templates —
// for both top-level and companion-object consts. Exercises B4(e) parity with the FIR checker.
// Declarations mirror kotlin.brs.* for testing without the full stdlib.
package kotlin.brs

external interface Dynamic

external fun brs(code: String): Dynamic

const val TOP_LEVEL_VALUE = "42"

class Config {
    companion object {
        const val COMPANION_VALUE = "99"
    }
}

fun testTopLevelConstValDirect(): Dynamic {
    return brs(TOP_LEVEL_VALUE)
}

fun testCompanionConstValDirect(): Dynamic {
    return brs(Config.COMPANION_VALUE)
}

fun testTopLevelConstValInTemplate(): Dynamic {
    return brs("${TOP_LEVEL_VALUE}")
}

fun testCompanionConstValInTemplate(): Dynamic {
    return brs("${Config.COMPANION_VALUE}")
}
