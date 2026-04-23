// Expected: two BRS_SCENEGRAPH_FIELD_TYPE diagnostics — one per annotation, both mismatched
import kotlin.brs.SGIntegerField
import kotlin.brs.SGStringField

class MyComponent {
    @SGIntegerField
    @SGStringField
    // Two nested markers: both @SGIntegerField and @SGStringField mismatch Boolean; harness records both opens against identifier x.
    var <!BRS_SCENEGRAPH_FIELD_TYPE!><!BRS_SCENEGRAPH_FIELD_TYPE!>x<!><!>: Boolean = false
}
