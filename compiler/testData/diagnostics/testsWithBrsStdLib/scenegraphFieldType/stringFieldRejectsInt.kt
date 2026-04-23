// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGStringField requires String, not Int
import kotlin.brs.SGStringField

class MyComponent {
    @SGStringField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: Int = 0
}
