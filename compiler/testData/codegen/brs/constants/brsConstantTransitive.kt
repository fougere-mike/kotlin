// Test transitive @BrsConstant references

// Local annotation definition for test (mirrors kotlin.brs.BrsConstant)
package kotlin.brs
annotation class BrsConstant

@BrsConstant
object Base {
    val VALUE = 10
    val PREFIX = "item_"
}

@BrsConstant
object Derived {
    val DOUBLE = Base.VALUE * 2
    val LABEL = Base.PREFIX + "name"
    val COMBINED = Base.PREFIX + Base.VALUE
}

fun getDouble(): Int = Derived.DOUBLE
fun getLabel(): String = Derived.LABEL
fun getCombined(): String = Derived.COMBINED
