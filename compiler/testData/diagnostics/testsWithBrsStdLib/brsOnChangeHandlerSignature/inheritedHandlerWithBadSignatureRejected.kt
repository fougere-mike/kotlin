// Expected: BRS_ONCHANGE_HANDLER_SIGNATURE — inherited handler has wrong param type
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

abstract class Base {
    fun onXChanged(value: Int) {}
}

class MyComponent : Base() {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var <!BRS_ONCHANGE_HANDLER_SIGNATURE!>x<!>: Int = 0
}
