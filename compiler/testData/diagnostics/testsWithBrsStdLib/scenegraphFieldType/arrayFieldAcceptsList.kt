// Expected: no diagnostic — List<String> is accepted by @SGArrayField
import kotlin.brs.SGArrayField

class MyComponent {
    @SGArrayField
    var x: List<String> = emptyList()
}
