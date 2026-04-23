// Expected: BRS_SCENEGRAPH_FIELD_TYPE on x — @SGArrayField requires RoArray or List, not IntArray
import kotlin.brs.SGArrayField

class MyComponent {
    @SGArrayField
    var <!BRS_SCENEGRAPH_FIELD_TYPE!>x<!>: IntArray = intArrayOf()
}
