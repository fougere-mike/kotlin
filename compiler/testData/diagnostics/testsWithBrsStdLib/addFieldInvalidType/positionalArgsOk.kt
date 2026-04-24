// Expected: no diagnostic — positional args with valid type "string"
import kotlin.brs.roku.RoSGNode

fun test(node: RoSGNode) {
    node.addField("x", "string", false)
}
