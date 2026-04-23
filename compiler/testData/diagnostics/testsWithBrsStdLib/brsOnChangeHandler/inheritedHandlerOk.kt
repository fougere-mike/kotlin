// Expected: no diagnostic — handler "onXChanged" is inherited from Base via unsubstitutedScope
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
