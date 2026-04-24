// Expected: BRS_ONCHANGE_HANDLER_SIGNATURE — two-arg handler is not a valid signature
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_SIGNATURE!>x<!>: Int = 0

    fun onXChanged(a: Int, b: Int) {}
}
