// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND — "onXChangd" is a typo; only "onXChanged" exists
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChangd")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>x<!>: Int = 0

    fun onXChanged() {}
}
