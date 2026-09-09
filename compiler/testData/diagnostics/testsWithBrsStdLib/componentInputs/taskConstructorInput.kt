// Expected: BRS_TASK_CONSTRUCTOR_INPUT — task inputs are configured via runTask<T> { field = value };
// constructor inputs are a render-component contract (v1).
import kotlin.brs.TaskComponent
import kotlin.brs.SGIntegerField

class FetchTask(@SGIntegerField val <!BRS_TASK_CONSTRUCTOR_INPUT!>count<!>: Int) : TaskComponent() {
    override fun run() {}
}
