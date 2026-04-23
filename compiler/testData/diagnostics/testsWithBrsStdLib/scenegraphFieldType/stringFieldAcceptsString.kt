// Expected: no diagnostic — @SGStringField with String is valid
import kotlin.brs.SGStringField

class MyComponent {
    @SGStringField
    var x: String = ""
}
