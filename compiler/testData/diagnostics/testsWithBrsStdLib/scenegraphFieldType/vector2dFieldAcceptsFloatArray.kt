// Expected: no diagnostic — FloatArray is accepted by @SGVector2DField
import kotlin.brs.SGVector2DField

class MyComponent {
    @SGVector2DField
    var xy: FloatArray = floatArrayOf(0f, 0f)
}
