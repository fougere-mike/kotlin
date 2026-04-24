// Expected: BRS_ONCHANGE_HANDLER_SIGNATURE — Int param is not RoSGNodeEvent
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_SIGNATURE!>x<!>: Int = 0

    fun onXChanged(value: Int) {}
}
