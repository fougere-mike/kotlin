// Test inline BrightScript code using brs() function
// Note: These declarations mirror kotlin.brs.* for testing without the full stdlib
package kotlin.brs

external interface Dynamic

external fun brs(code: String): Dynamic

fun testBrsExpression(): Dynamic {
    return brs("1 + 2")
}

fun testBrsAssignment() {
    val x = brs("3 * 4")
}

fun testBrsMethodCall() {
    brs("m.top.visible = true")
}

fun testBrsReturnStatement(): Dynamic {
    return brs("return 42")
}
