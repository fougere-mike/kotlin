// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE — the stdlib base classes themselves are
// abstract; instantiate a user subclass, not TaskComponent
import kotlin.brs.TaskComponent
import kotlin.brs.createComponent

fun launch(): TaskComponent = createComponent<<!BRS_CREATE_COMPONENT_INVALID_TYPE!>TaskComponent<!>>()
