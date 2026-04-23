// Expected: no diagnostic — @Suppress("BRS_SCENEGRAPH_FIELD_TYPE") suppresses the error
import kotlin.brs.SGIntegerField

class MyComponent {
    @SGIntegerField
    @Suppress("BRS_SCENEGRAPH_FIELD_TYPE")
    var x: String = ""
}
