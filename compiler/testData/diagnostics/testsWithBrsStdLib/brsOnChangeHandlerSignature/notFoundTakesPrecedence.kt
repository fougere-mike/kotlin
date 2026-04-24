// Expected: BRS_ONCHANGE_HANDLER_NOT_FOUND only — short-circuit when handler doesn't exist
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("nonExistent")
    var <!BRS_ONCHANGE_HANDLER_NOT_FOUND!>x<!>: Int = 0

    fun onXChanged(value: Int) {}
}
