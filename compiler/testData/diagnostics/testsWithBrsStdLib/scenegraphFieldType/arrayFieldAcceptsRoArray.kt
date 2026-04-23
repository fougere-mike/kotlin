// Expected: no diagnostic — RoArray is accepted by @SGArrayField
import kotlin.brs.SGArrayField
import kotlin.brs.roku.RoArray

class MyComponent {
    @SGArrayField
    var x: RoArray? = null
}
