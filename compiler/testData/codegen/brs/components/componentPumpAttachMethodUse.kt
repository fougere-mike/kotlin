// Coroutine use only inside a method (not init) still has the scheduler
// attached before the method's launch ever runs: the lifecycle attach
// (__kotlinComponentAttach, which performs the pump attach) is injected
// UNCONDITIONALLY as init()'s first statement, and the coroutine scripts land
// in the component's includes via the method's dependency recording.
import kotlin.brs.BrsOnChange
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.roku.RoSGNodeEvent

class MethodOnlyCoroutines : GroupComponent() {
    @SGStringField
    @BrsOnChange("onTrigger")
    var trigger: String = ""

    @SGStringField
    var status: String = ""

    private fun onTrigger(msg: RoSGNodeEvent) {
        launch {
            status = "triggered"
        }
    }
}
