// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND — companion-object handlers are not in instance scope
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>x<!>: Int = 0

    companion object {
        fun onXChanged() {}
    }
}
