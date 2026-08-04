import kotlin.brs.BrsComponent
import kotlin.brs.SGNodeField
import kotlin.brs.roku.RoSGNode

// @SGNodeField(nodeType = ...) narrows the accepted node type in the XML field
// declaration; without it the field accepts any node.
@BrsComponent
class NodeTypedFieldComponent {
    @SGNodeField(nodeType = "ContentNode")
    var content: RoSGNode? = null

    @SGNodeField
    var anyNode: RoSGNode? = null
}
