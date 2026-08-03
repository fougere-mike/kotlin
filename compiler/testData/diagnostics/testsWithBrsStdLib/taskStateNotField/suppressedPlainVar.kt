// Expected: no diagnostic — @Suppress("BRS_TASK_STATE_NOT_FIELD") is the documented escape
// hatch for state that is intentionally task-thread-only (set and read entirely inside run())
import kotlin.brs.TaskComponent

class ScratchTask : TaskComponent() {
    @Suppress("BRS_TASK_STATE_NOT_FIELD")
    var scratch: Int = 0

    override fun run() {
        scratch = 42
    }
}
