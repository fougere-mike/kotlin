// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGAssocArrayField requires RoAssociativeArray or Map, not List
import kotlin.brs.SGAssocArrayField

class MyComponent {
    @SGAssocArrayField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: List<String> = emptyList()
}
