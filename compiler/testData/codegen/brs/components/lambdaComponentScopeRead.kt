// Component-scope property reads through a lambda/coroutine-captured `this`.
// Locks the captured-self routing for ComponentBase's scope properties: inside
// a lowered suspend lambda the captured component `this` (m.this_0) IS the
// component's m-scope AA, so `top`/`global` reads emit direct member access
// (m.this_0.top / m.this_0.global) and an `m` access emits the captured value
// itself — NEVER an accessor call: __get_top_k_() and friends are deliberately
// never attached to the component m, so calling one crashes "Member function
// not found" on device (the pre-fix emission). Layout-stub properties ride the
// same routing (m.this_0.layout): the stub lives at m.<name> and layout-class
// accessors are never attached either.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

class LambdaScopeRead_Layout(private val top: RoSGNode) {
    private var _shelfNode: RoSGNode? = null

    val shelfNode: RoSGNode
        get() {
            if (_shelfNode == null) {
                _shelfNode = top.findNode("shelfNode")
                    ?: error("Layout node 'shelfNode' not found in component")
            }
            return _shelfNode!!
        }
}

class LambdaScopeRead : GroupComponent() {
    @SGStringField
    var status: String = ""

    private val layout = LambdaScopeRead_Layout(top)

    init {
        launch {
            val node = top                        // m.this_0.top
            node.setFocus(true)
            val g = global                        // m.this_0.global
            m.addReplace("marker", g.hasField("beacon"))  // m.this_0.addReplace(...)
            val shelf = layout.shelfNode          // m.this_0.layout.__get_shelfNode()
            shelf.setFocus(true)
            status = "done"                       // case-8 contrast: m.this_0.top.status
        }
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            component("ShelfWidget", id = "shelfNode")
        }
    }
}
