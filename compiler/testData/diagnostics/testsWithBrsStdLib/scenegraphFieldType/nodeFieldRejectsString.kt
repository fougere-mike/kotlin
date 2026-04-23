// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGNodeField requires RoSGNode or subtype, not String
import kotlin.brs.SGNodeField

class MyComponent {
    @SGNodeField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: String = ""
}
