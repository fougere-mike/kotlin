// Expected: BRS_ADDFIELD_INVALID_TYPE — const val holding invalid type "String" is caught at compile time
import kotlin.brs.roku.RoSGNode

const val FIELD_TYPE = "String"

fun test(node: RoSGNode) {
    node.addField("x", <!BRS_ADDFIELD_INVALID_TYPE!>FIELD_TYPE<!>, false)
}
