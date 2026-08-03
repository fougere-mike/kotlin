// Expected: no diagnostic — locals in run() live on the task thread's stack, not in m-state;
// making task-private state a local is one of the two suggested fixes in the message
import kotlin.brs.TaskComponent
import kotlin.brs.SGStringField

class ParseTask : TaskComponent() {
    @SGStringField
    var output: String = ""

    override fun run() {
        var buffer = ""
        val parts = mutableListOf<String>()
        buffer = "parsed"
        parts.add(buffer)
        output = buffer
    }
}
