// runTask<T>{} call sites are rewritten to runTaskImpl(brsCreateComponent<T>(), configure)
// by BrsRunTaskCallLowering: klib inline functions are never inlined by this backend, so
// the reified component name must be resolved at the call site (the same mechanism family
// as the createComponent intrinsic). The emission shows CreateObject("roSGNode", ...) as
// runTaskImpl's first argument inside the caller's state machine.
import kotlin.brs.SGStringField
import kotlin.brs.TaskComponent
import kotlin.coroutines.task.runTask

class EchoCallTask : TaskComponent() {
    @SGStringField
    var input: String = ""

    @SGStringField
    var result: String = ""

    override fun run() {
        result = "echo:" + input
    }
}

suspend fun launchEcho(): String {
    val task = runTask<EchoCallTask> { input = "ping" }
    return task.result
}
