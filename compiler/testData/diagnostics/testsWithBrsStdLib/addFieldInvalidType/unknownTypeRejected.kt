// Expected: BRS_ADDFIELD_INVALID_TYPE — "widget" is not a valid SceneGraph field type
import kotlin.brs.roku.RoSGNode

fun test(node: RoSGNode) {
    node.addField("x", <!BRS_ADDFIELD_INVALID_TYPE!>"widget"<!>, false)
}
