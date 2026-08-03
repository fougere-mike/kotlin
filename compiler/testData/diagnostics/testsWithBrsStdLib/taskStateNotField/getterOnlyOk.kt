// Expected: no diagnostic — an accessor-only property has no backing field, so no m-state
// is generated for it; it recomputes on each access on whichever thread calls it
import kotlin.brs.TaskComponent
import kotlin.brs.SGStringField

class StatusTask : TaskComponent() {
    @SGStringField
    var rawState: String = ""

    val isDone: Boolean
        get() = rawState == "done"

    override fun run() {
        rawState = "done"
    }
}
