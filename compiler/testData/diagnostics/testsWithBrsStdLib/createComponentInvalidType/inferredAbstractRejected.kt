// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE — the type argument is inferred from the
// expected type, not written explicitly; the checker reads the resolved type argument either way
import kotlin.brs.TaskComponent
import kotlin.brs.createComponent

abstract class QueueTask : TaskComponent()

fun launch(): QueueTask {
    val task: QueueTask = <!BRS_CREATE_COMPONENT_INVALID_TYPE!>createComponent()<!>
    return task
}
