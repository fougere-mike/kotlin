// Expected: BRS_TASK_STATE_NOT_FIELD on progress — un-annotated backing-field var in a concrete
// task compiles to plain m-state; the write from run() lands in the task thread's clone and is
// silently lost (device-proven: spikes/task-node-spike, m_writeback_isolation)
import kotlin.brs.TaskComponent

class FetchTask : TaskComponent() {
    var <!BRS_TASK_STATE_NOT_FIELD!>progress<!>: Int = 0

    override fun run() {
        progress = 100
    }
}
