// Expected: no diagnostic — the legacy @BrsField annotation also marks a real node field
import kotlin.brs.TaskComponent
import kotlin.brs.BrsField

class LegacyTask : TaskComponent() {
    @BrsField(type = "string")
    var status: String = ""

    override fun run() {
        status = "done"
    }
}
