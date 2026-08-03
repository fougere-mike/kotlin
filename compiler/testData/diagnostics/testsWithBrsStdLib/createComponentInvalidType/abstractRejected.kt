// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE — an abstract task class has no XML component
// of its own; CreateObject("roSGNode", "AbsTask") would return invalid at runtime
import kotlin.brs.TaskComponent
import kotlin.brs.createComponent

abstract class AbsTask : TaskComponent() {
    abstract fun configure()
}

fun launch(): AbsTask = createComponent<<!BRS_CREATE_COMPONENT_INVALID_TYPE!>AbsTask<!>>()
