// Expected: no diagnostic — @Suppress("BRS_ONCHANGE_HANDLER_NOT_FOUND") silences the error
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    @Suppress("BRS_ONCHANGE_HANDLER_NOT_FOUND")
    var x: Int = 0
}
