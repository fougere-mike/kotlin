// Expected: clean — @Suppress on the class opts out of the task-constructor-input rule.
import kotlin.brs.TaskComponent
import kotlin.brs.SGIntegerField

@Suppress("BRS_TASK_CONSTRUCTOR_INPUT")
class FetchTask(@SGIntegerField val count: Int) : TaskComponent() {
    override fun run() {}
}
