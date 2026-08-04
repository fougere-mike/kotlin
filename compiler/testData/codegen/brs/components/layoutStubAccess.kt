// Gradle-stub Layout pairing: GenerateLayoutStubsTask emits ClassName_Layout as
// normal Kotlin source, compiled as-is; the component stores the instance in its
// `layout` property and reads nodes through it. Locks the m.layout store/read
// pairing - the access path must NOT use the retired underscore-prefixed slot
// (m._layout), which nothing initializes (dereferenced invalid on device).
import kotlin.brs.GroupComponent
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

class StubbedScreen_Layout(private val top: RoSGNode) {
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

class StubbedScreen : GroupComponent() {
    private val layout = StubbedScreen_Layout(top)

    init {
        layout.shelfNode.setFocus(true)
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            component("ShelfWidget", id = "shelfNode")
        }
    }
}
