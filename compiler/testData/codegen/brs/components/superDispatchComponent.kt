// super.f() inside a SceneGraph component must call the BASE's implementation
// statically (every component class emits its members as globals over the
// shared m; SceneGraph makes base functions callable from the derived scope).
// The slot form (m.f_k_) reaches the override's OWN slot and recurses
// (spec 2026-09-04-component-lifecycle §5.4). The suspend member covers the
// state-machine path: Suite 11 pins super.onStart() — a SUSPEND super call —
// on device.
import kotlin.brs.GroupComponent

open class SuperBase : GroupComponent() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean = key == "back"
    open fun describe(): String = "base"
    open suspend fun load(): String = "base"
}

class SuperLeaf : SuperBase() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean {
        if (press && key == "OK") return true
        return super.onKeyEvent(key, press)
    }
    override fun describe(): String = super.describe() + "+leaf"
    override suspend fun load(): String = super.load() + "+leaf"
}
