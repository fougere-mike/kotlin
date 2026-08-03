// Expected: no diagnostic — plain m-state is fine in render-thread components (GroupComponent
// etc.); the clone-in/write-back trap only exists across the task thread boundary
import kotlin.brs.GroupComponent

class SidePanel : GroupComponent() {
    var focusCount: Int = 0

    override fun onKeyEvent(key: String, press: Boolean): Boolean {
        if (press && key == "OK") {
            focusCount = focusCount + 1
            return true
        }
        return false
    }
}
