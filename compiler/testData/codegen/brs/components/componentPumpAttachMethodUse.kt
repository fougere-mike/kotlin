// The pump-attach injection predicate is FILE-level: coroutine use only inside
// a method (not init) must still inject __kotlinPumpAttach into init(), because
// the scheduler has to be attached before the method's launch ever runs.
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
