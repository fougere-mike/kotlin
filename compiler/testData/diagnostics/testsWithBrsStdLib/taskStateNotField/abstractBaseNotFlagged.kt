// Expected: no diagnostic — the checker only fires on properties declared in CONCRETE
// TaskComponent-derived classes; an abstract task base never runs itself
import kotlin.brs.TaskComponent

abstract class BaseTask : TaskComponent() {
    var sharedCounter: Int = 0
}
