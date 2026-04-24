// Expected: no diagnostic — nullable RoSGNodeEvent? param is accepted
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField
import kotlin.brs.roku.RoSGNodeEvent

class MyComponent {
    @SGIntegerField
    @BrsOnChange("onXChanged")
    var x: Int = 0

    fun onXChanged(event: RoSGNodeEvent?) {}
}
