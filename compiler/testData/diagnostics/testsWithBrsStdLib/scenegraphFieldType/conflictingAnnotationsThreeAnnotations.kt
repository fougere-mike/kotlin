// Expected: BRS_SCENEGRAPH_FIELD_CONFLICT with three annotations listed in source order
import kotlin.brs.SGBooleanField
import kotlin.brs.SGIntegerField
import kotlin.brs.SGStringField

class MyComponent {
    @SGStringField
    @SGIntegerField
    @SGBooleanField
    var <!BRS_SCENEGRAPH_FIELD_CONFLICT!>x<!>: String = ""
}
