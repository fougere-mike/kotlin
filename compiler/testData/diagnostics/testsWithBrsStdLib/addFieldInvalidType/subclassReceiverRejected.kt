// Expected: BRS_ADDFIELD_INVALID_TYPE — checker fires even when receiver is a user-defined subtype of RoSGNode
import kotlin.brs.roku.RoSGNode

external interface MyNode : RoSGNode

fun test(node: MyNode) {
    node.addField("cardId", <!BRS_ADDFIELD_INVALID_TYPE!>"String"<!>, false)
}
