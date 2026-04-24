// Expected: BRS_ADDFIELD_INVALID_TYPE — "String" (capital S) is not a valid type; requires "string"
// This reproduces the real-world bug in ShelfView.kt:65.
import kotlin.brs.roku.RoSGNode

fun test(node: RoSGNode) {
    node.addField("cardId", <!BRS_ADDFIELD_INVALID_TYPE!>"String"<!>, false)
}
