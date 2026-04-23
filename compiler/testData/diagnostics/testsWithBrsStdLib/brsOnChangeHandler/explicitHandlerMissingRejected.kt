// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND on x — "onXChanged" does not exist in MyComponent
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>x<!>: Int = 0
}
