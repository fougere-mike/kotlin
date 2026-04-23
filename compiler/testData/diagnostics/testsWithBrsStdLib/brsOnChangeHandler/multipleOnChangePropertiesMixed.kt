// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND on y only — x and z have valid handlers, y is a typo
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0

    @SGIntegerField
    @BrsOnChange("onYChangd")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>y<!>: Int = 0

    @SGIntegerField
    @BrsOnChange("onZChanged")
    var z: Int = 0

    fun onXChanged() {}
    fun onYChanged() {}
    fun onZChanged() {}
}
