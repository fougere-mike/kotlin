// Test enum ordinal/name in @BrsConstant

// Local annotation definition for test (mirrors kotlin.brs.BrsConstant)
package kotlin.brs
annotation class BrsConstant

enum class Color { RED, GREEN, BLUE }

@BrsConstant
object Constants {
    val DEFAULT_COLOR_ORDINAL = Color.GREEN.ordinal
    val DEFAULT_COLOR_NAME = Color.GREEN.name
    val FIRST_ORDINAL = Color.RED.ordinal
}

fun getDefaultColorOrdinal(): Int = Constants.DEFAULT_COLOR_ORDINAL
fun getDefaultColorName(): String = Constants.DEFAULT_COLOR_NAME
fun getFirstOrdinal(): Int = Constants.FIRST_ORDINAL
