// Expected: no diagnostic — function type is accepted by @SGFunctionField
import kotlin.brs.SGFunctionField

class MyComponent {
    @SGFunctionField
    var cb: (Int) -> Unit = {}
}
