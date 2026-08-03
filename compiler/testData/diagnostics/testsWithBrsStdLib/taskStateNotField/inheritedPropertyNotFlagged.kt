// Expected: no diagnostic — documented v1 hole: a plain backing-field property declared in an
// abstract task base is not flagged there (base never runs) and is NOT re-flagged at the concrete
// subclass that inherits it. The write from run() below is still silently lost at runtime; v1
// only enforces declarations made directly in concrete task classes.
import kotlin.brs.TaskComponent

abstract class CountingBase : TaskComponent() {
    var attempts: Int = 0
}

class RetryTask : CountingBase() {
    override fun run() {
        attempts = attempts + 1
    }
}
