// Expected: no diagnostic — convention path (no @BrsOnChange annotation) is not checked
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    var x: Int = 0

    fun onXChanged(value: Int) {}
}
