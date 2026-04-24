// Expected: BRS_ADDFIELD_INVALID_TYPE — receiver is a RoSGNode variable; "String" is still invalid
import kotlin.brs.roku.RoSGNode

fun test(node: RoSGNode) {
    node.addField("cardId", <!BRS_ADDFIELD_INVALID_TYPE!>"String"<!>, false)
}
