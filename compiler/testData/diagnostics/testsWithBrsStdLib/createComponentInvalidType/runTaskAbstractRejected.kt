// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE — runTask<T>{} joins componentFactoryCallables:
// an abstract task class has no XML component of its own, so the node creation inside
// runTask would return invalid at runtime
import kotlin.brs.SGStringField
import kotlin.brs.TaskComponent
import kotlin.coroutines.task.runTask

abstract class AbsRunTask : TaskComponent() {
    @SGStringField
    var input: String = ""
}

suspend fun launchAbs(): AbsRunTask = runTask<<!BRS_CREATE_COMPONENT_INVALID_TYPE!>AbsRunTask<!>> { input = "x" }
