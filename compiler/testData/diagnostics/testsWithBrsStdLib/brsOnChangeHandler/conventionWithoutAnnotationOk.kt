// Expected: no diagnostic — the on{PropName}Changed convention path is not validated by this checker
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    var x: Int = 0
}
