// Expected: no diagnostic — zero-arg handler is a valid signature
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0

    fun onXChanged() {}
}
