// A concrete user base overrides onKeyEvent; the leaf does not. The leaf's
// generated SceneGraph-facing `function onKeyEvent` must reach the inherited
// override through the slot the BASE's init attached (m.onKeyEvent_Str_Z_k_),
// not shadow it with `return false` (spec 2026-09-04-component-lifecycle §5.5).
import kotlin.brs.GroupComponent

open class KeyBase : GroupComponent() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean = press && key == "OK"
}

class KeyLeaf : KeyBase()
