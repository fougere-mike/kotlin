// Expected: no diagnostic — one overload has a valid signature (zero-arg)
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0

    fun onXChanged() {}
    fun onXChanged(value: Int) {}
}
