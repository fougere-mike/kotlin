// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND — checker applies to all @BrsOnChange, not only SG-annotated properties
import kotlin.brs.BrsOnChange

class MyComponent {
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>x<!>: Int = 0
}
