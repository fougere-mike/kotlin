// BRS_SHARED_THROUGH_COPYING_CHANNEL — a SharedService-typed value into a copying channel:
// setField value arguments, callFunc arguments, and @SG*Field declarations on task components.
// Ordinary channels COPY, and a copy of a shared instance is a husk — data keys survive, every
// method slot is stripped, and it is NOT the shared instance (device-pinned, scope-handle
// spike Q1d). Shared instances cross by reference only: pass the stash NODE and acquire with
// sharedFrom<T>() on the other side.
import kotlin.brs.SGAssocArrayField
import kotlin.brs.SharedService
import kotlin.brs.TaskComponent
import kotlin.brs.asDynamic
import kotlin.brs.roku.RoSGNode
import kotlin.brs.sharedFrom

abstract class Vm : SharedService() {
    var n: Int = 0
}

class ChatVm : Vm()

// Case 1: setField value argument, concrete shared static type — ERROR
fun sendConcrete(node: RoSGNode, vm: ChatVm) {
    node.setField("payload", <!BRS_SHARED_THROUGH_COPYING_CHANNEL!>vm<!>)
}

// Case 2: setField value argument, ABSTRACT-BASE static type — ERROR (full transitive walk)
fun sendBase(node: RoSGNode, vm: Vm) {
    node.setField("payload", <!BRS_SHARED_THROUGH_COPYING_CHANNEL!>vm<!>)
}

// Case 3: callFunc argument — ERROR. The Dynamic-typed parameter forces asDynamic() at the
// call site; the checker unwraps the cast to the underlying shared value.
fun sendViaCallFunc(node: RoSGNode, vm: ChatVm) {
    node.callFunc("accept", <!BRS_SHARED_THROUGH_COPYING_CHANNEL!>vm.asDynamic()<!>)
}

// Case 4: @SG*Field declaration on a task component — ERROR at the DECLARATION (the field
// write clones across the render/task thread boundary). The SceneGraph field-type rule also
// rejects the annotation/type mismatch on the same declaration — both fire.
class FetchVmTask : TaskComponent() {
    @SGAssocArrayField
    var <!BRS_SHARED_THROUGH_COPYING_CHANNEL!><!BRS_SCENEGRAPH_FIELD_TYPE!>vm<!><!>: ChatVm? = null

    override fun run() {
    }
}

// Case 5: the sanctioned pattern — pass the stash NODE, acquire with sharedFrom — CLEAN
fun sendNode(node: RoSGNode, stashNode: RoSGNode) {
    node.setField("stash", stashNode)
}

fun acquire(stashNode: RoSGNode): ChatVm {
    return sharedFrom<ChatVm>(stashNode)
}

// Case 6: plain data OUT OF a shared instance — CLEAN (the value is data, not the instance)
fun sendData(node: RoSGNode, vm: ChatVm) {
    node.setField("count", vm.n)
}

// Case 7: suppression escape — CLEAN (deliberate husk, review-visible)
@Suppress("BRS_SHARED_THROUGH_COPYING_CHANNEL")
fun sendDeliberateHusk(node: RoSGNode, vm: ChatVm) {
    node.setField("payload", vm)
}
