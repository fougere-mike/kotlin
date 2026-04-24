// Expected: no diagnostic — reordered named args with valid type "string"
import kotlin.brs.roku.RoSGNode

fun test(node: RoSGNode) {
    node.addField(alwaysNotify = false, type = "string", fieldName = "x")
}
