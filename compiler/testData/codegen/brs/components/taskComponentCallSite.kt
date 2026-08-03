// createComponent<T>() lowers to CreateObject("roSGNode", "<ComponentName>");
// typed field access on the returned handle compiles to direct node dot access.
import kotlin.brs.SGStringField
import kotlin.brs.TaskComponent
import kotlin.brs.createComponent

class FetchTask : TaskComponent() {
    @SGStringField
    var url: String = ""

    @SGStringField
    var payload: String = ""

    override fun run() {
        payload = "fetched:" + url
    }
}

fun launchFetch(): String {
    val task = createComponent<FetchTask>()
    task.url = "https://example.com/feed"
    return task.payload
}
