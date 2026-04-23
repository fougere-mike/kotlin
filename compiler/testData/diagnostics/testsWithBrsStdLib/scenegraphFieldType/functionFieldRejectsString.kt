// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGFunctionField requires function type, not String
import kotlin.brs.SGFunctionField

class MyComponent {
    @SGFunctionField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: String = ""
}
