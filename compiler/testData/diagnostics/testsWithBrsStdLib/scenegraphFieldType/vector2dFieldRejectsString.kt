// Expected: BRS_SCENEGRAPH_FIELD_TYPE on xy — @SGVector2DField requires FloatArray, not String
import kotlin.brs.SGVector2DField

class MyComponent {
    @SGVector2DField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>xy<!>: String = ""
}
