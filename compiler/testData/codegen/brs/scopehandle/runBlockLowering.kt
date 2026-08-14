// ScopeHandle.run{block} lowering — locks the compiler-lowered surface:
// (1) zero-capture block → runLowered("<fileFq>#1", invalid) + lifted top-level
//     suspend function, (2) capturing block (shelfId: Int, node: RoSGNode) →
//     captures AA built at the call site by name + lifted function reading the
//     same names from `captures` at entry, (3) the hand-written request call is
//     the control and must NOT be touched by the lowering, (4) the lifted
//     functions ride ordinary top-level emission — visible in the file manifest
//     and the component's deps.json exactly like any generated function.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.ScopeHandle
import kotlin.brs.ScopeRequest
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scopeHandleOf

object ControlReq : ScopeRequest<Int>("ControlReq")

private fun loadShelf(shelfId: Int, node: RoSGNode): Int = shelfId + 1

class RunBlockLowering : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        launch {
            val owner = scopeHandleOf(top)
            val shelfId = 41
            val node: RoSGNode = top
            val zeroCapture = owner.run { 7 }
            val withCaptures = owner.run { loadShelf(shelfId, node) }
            val control = owner.run(ControlReq)
            status = "" + zeroCapture + withCaptures + control
        }
    }
}
