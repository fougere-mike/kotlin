// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGFloatField requires Float, not Int
import kotlin.brs.SGFloatField

class MyComponent {
    @SGFloatField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: Int = 0
}
