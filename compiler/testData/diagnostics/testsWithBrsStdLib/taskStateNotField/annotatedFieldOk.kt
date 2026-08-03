// Expected: no diagnostic — @SG*Field properties are real node fields and cross the task
// thread boundary; this is the canonical typed-task input/output shape
import kotlin.brs.TaskComponent
import kotlin.brs.SGStringField
import kotlin.brs.SGIntegerField

class DownloadTask : TaskComponent() {
    @SGStringField
    var url: String = ""

    @SGIntegerField
    var bytesFetched: Int = 0

    override fun run() {
        bytesFetched = url.length
    }
}
