// Expected: no errors — BRS_SCENEGRAPH_FIELD_CONFLICT suppressed; TYPE loop not reached (no TYPE errors either)
import kotlin.brs.SGIntegerField
import kotlin.brs.SGStringField

class MyComponent {
    @Suppress("BRS_SCENEGRAPH_FIELD_CONFLICT")
    @SGIntegerField
    @SGStringField
    var x: Boolean = false
}
