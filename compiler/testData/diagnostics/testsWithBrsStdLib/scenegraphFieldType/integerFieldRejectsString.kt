// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGIntegerField requires Int, not String
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: String = ""
}
