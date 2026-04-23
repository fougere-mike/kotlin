// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGColorField requires String, not Int
import kotlin.brs.SGColorField

class MyComponent {
    @SGColorField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: Int = 0
}
