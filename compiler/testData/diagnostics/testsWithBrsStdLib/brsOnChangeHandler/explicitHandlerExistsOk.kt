// Expected: no diagnostic — handler "onXChanged" exists as a member function
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0

    fun onXChanged() {}
}
