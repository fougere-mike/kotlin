// Test arithmetic operations in @BrsConstant

// Local annotation definition for test (mirrors kotlin.brs.BrsConstant)
package kotlin.brs
annotation class BrsConstant

@BrsConstant
object Math {
    val A = 10
    val B = 3
    val SUM = A + B
    val DIFF = A - B
    val PROD = A * B
    val QUOT = A / B
    val COMPLEX = (A + B) * 2 - 4
    val NEGATIVE = -A
}

fun getSum(): Int = Math.SUM
fun getDiff(): Int = Math.DIFF
fun getProd(): Int = Math.PROD
fun getQuot(): Int = Math.QUOT
fun getComplex(): Int = Math.COMPLEX
fun getNegative(): Int = Math.NEGATIVE
