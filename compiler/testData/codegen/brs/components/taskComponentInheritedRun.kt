// Inherited-run task: the __kotlinTaskMain wrapper is emitted in the class that
// DECLARES the run() override (here the abstract intermediate), so the wrapper's
// call never crosses component script files. The concrete leaf inherits the
// wrapper through SceneGraph XML inheritance (extends="BaseEchoTask") and only
// wires m.top.functionName in its own init().
import kotlin.brs.SGStringField
import kotlin.brs.TaskComponent

abstract class BaseEchoTask : TaskComponent() {
    @SGStringField
    var result: String = ""

    override fun run() {
        result = "ran-in-base"
    }
}

class LeafTask : BaseEchoTask()
