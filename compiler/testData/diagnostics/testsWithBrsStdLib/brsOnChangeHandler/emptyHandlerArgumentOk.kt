// Expected: no diagnostic — empty string is treated as unset sentinel; no handler lookup performed
import kotlin.brs.BrsOnChange
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @BrsOnChange("")
    var x: Int = 0
}
