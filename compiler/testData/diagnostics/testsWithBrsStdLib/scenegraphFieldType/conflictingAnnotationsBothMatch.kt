// Expected: BRS_SCENEGRAPH_FIELD_CONFLICT fires even when both annotations agree on the property type
// (both SGStringField and SGUriField want String; the conflict is the dual annotation, not a type mismatch)
import kotlin.brs.SGStringField
import kotlin.brs.SGUriField

class MyComponent {
    @SGStringField
    @SGUriField
    var <!BRS_SCENEGRAPH_FIELD_CONFLICT!>name<!>: String = ""
}
