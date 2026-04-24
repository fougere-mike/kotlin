// Expected: no diagnostic — suppressed via @Suppress
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    @Suppress("BRS_ONCHANGE_HANDLER_SIGNATURE")
    var x: Int = 0

    fun onXChanged(value: Int) {}
}
