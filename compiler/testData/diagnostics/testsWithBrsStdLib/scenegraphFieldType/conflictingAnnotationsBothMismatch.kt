// Expected: one BRS_SCENEGRAPH_FIELD_CONFLICT diagnostic — two SG*Field annotations conflict; TYPE errors suppressed
import kotlin.brs.SGIntegerField
import kotlin.brs.SGStringField

class MyComponent {
    @SGIntegerField
    @SGStringField
    var <!BRS_SCENEGRAPH_FIELD_CONFLICT!>x<!>: Boolean = false
}
