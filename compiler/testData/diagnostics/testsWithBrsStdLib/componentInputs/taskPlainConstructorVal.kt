// Expected: BRS_TASK_STATE_NOT_FIELD — the constructor-parameter (fake-source) skip is gone: a
// plain constructor val on a task is m-state and is lost across the task-thread clone.
import kotlin.brs.TaskComponent

class FetchTask(val <!BRS_TASK_STATE_NOT_FIELD!>retries<!>: Int) : TaskComponent() {
    override fun run() {}
}
