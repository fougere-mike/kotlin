// Expected: no diagnostic — inherited zero-arg handler is valid
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

abstract class Base {
    fun onXChanged() {}
}

class MyComponent : Base() {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0
}
