// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGLongIntegerField requires Long, not Int
import kotlin.brs.SGLongIntegerField

class MyComponent {
    @SGLongIntegerField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: Int = 0
}
