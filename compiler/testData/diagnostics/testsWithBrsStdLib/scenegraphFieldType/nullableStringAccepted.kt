// Expected: no diagnostic — nullable String? is accepted by @SGStringField
import kotlin.brs.SGStringField

class MyComponent {
    @SGStringField
    var x: String? = null
}
