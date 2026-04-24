// Expected: no diagnostic — suppressed via @Suppress on the enclosing function
import kotlin.brs.roku.RoSGNode

@Suppress("BRS_ADDFIELD_INVALID_TYPE")
fun test(node: RoSGNode) {
    node.addField("x", "widget", false)
}
