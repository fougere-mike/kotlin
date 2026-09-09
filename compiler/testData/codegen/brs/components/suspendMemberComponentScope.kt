// Component-scope access AFTER a suspension point inside a suspend MEMBER of a
// SceneGraph component (a real state machine — NOT a lambda). Inside
// `X__onStartCOROUTINE__doResume_k_` the receiver `m` is the COROUTINE object;
// the component self is the constructor-captured `__this` field (origin
// COROUTINE_IMPL, the suspend lowering's parameter field for `<this>`). Post-
// suspension code that touches component scope must route through it exactly
// like the lambda capture `this_0` does (lambdaComponentScopeRead /
// lambdaSelfWriteToField): reads `top`/`global`/`m` → m.__this.top /
// m.__this.global / m.__this; @SG interface-field reads and writes →
// m.__this.top.<field>. The pre-fix emission was `m.global` (crashed on device:
// "'Dot' Operator attempted on invalid BrightScript Component", Suite 11
// LifecycleSuperProbe) and `m.__this.<field> = v` (a dead AA key on the
// coroutine object, LifecycleTaskProbe). Both device shapes are mirrored here:
// the leaf's `super.onStart()` is the suspension point; the awaitReady() class
// covers a stdlib suspend call as the suspension point.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.awaitReady
import kotlin.brs.roku.RoSGNode

fun mark(g: RoSGNode, entry: String) {
    g.setField("mark", entry)
}

open class SuspendSelfBase : GroupComponent() {
    @SGStringField
    var status: String = ""

    override suspend fun onStart() {
        status = "base"                     // no suspension: plain fn, m.top.status
    }
}

class SuspendSelfLeaf : SuspendSelfBase() {
    private var internalCount: Int = 0

    private fun tag(): String = "leaf"

    override suspend fun onStart() {
        super.onStart()                     // real suspension point
        mark(global, "after-super")         // m.__this.global
        val node = top                      // m.__this.top
        node.setFocus(true)
        m.addReplace("marker", true)        // m.__this.addReplace(...)
        val prev = status                   // @SG read: m.__this.top.status
        status = prev + "+" + tag()         // @SG write: m.__this.top.status = ...; member call: m.__this.tag_k_()
        internalCount = internalCount + 1   // un-annotated state: accessor calls on the captured self (lambdaSelfWriteToField parity)
    }
}

class SuspendSelfAwait : GroupComponent() {
    @SGStringField
    var status: String = ""

    override suspend fun onStart() {
        awaitReady()                        // stdlib suspend call as the suspension point
        status = "ready"                    // m.__this.top.status = "ready"
        mark(global, status)                // m.__this.global, m.__this.top.status
    }
}
