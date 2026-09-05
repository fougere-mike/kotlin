// ComponentBase.launch{} — the canonical component coroutine entry point.
// Locks three things: (1) the extension call shape — the component `this`
// (the m-scope AA) is passed as the receiver argument, (2) the injected
// __kotlinComponentAttach(m.top, m.global) as the FIRST init() statement (the
// unconditional lifecycle attach, which performs the pump attach), and (3)
// PumpSchedulerKt.brs + ComponentCoroutinesKt.brs landing in
// the component's script includes via dependency recording.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class LaunchExtension : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        launch {
            status = "done"
        }
    }
}
