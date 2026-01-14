// Test boolean operations in @BrsConstant

// Local annotation definition for test (mirrors kotlin.brs.BrsConstant)
package kotlin.brs
annotation class BrsConstant

@BrsConstant
object Flags {
    val DEBUG = true
    val RELEASE = false
    val AND_RESULT = DEBUG && RELEASE
    val OR_RESULT = DEBUG || RELEASE
    val NOT_DEBUG = !DEBUG
}

fun isDebug(): Boolean = Flags.DEBUG
fun isRelease(): Boolean = Flags.RELEASE
fun getAndResult(): Boolean = Flags.AND_RESULT
fun getOrResult(): Boolean = Flags.OR_RESULT
fun isNotDebug(): Boolean = Flags.NOT_DEBUG
