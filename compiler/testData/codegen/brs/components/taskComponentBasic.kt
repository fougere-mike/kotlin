// Typed task component: inherited kotlinTask* protocol fields land in the XML,
// the compiler emits the __kotlinTaskMain wrapper (spike-proven shape) and wires
// m.top.functionName in init(). Reading kotlinTaskId inside run() exercises
// inherited-annotated-field access through the klib fake override.
import kotlin.brs.SGStringField
import kotlin.brs.TaskComponent

class BasicTask : TaskComponent() {
    @SGStringField
    var inputText: String = ""

    @SGStringField
    var outputText: String = ""

    override fun run() {
        val id = kotlinTaskId
        outputText = "echo:" + inputText
        if (id > 0) {
            outputText = outputText + "!"
        }
    }
}
