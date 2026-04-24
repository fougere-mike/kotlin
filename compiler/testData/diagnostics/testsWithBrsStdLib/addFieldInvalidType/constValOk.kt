// Expected: no diagnostic — const val with valid type "string" is accepted
import kotlin.brs.roku.RoSGNode

const val FIELD_TYPE = "string"

fun test(node: RoSGNode) {
    node.addField("x", FIELD_TYPE, false)
}
