// Expected: no diagnostic — a concrete TaskComponent subclass is exactly what
// createComponent<T>() is for
import kotlin.brs.TaskComponent
import kotlin.brs.SGStringField
import kotlin.brs.createComponent

class FetchTask : TaskComponent() {
    @SGStringField
    var url: String = ""

    override fun run() {
        url = url
    }
}

fun launch(): FetchTask = createComponent<FetchTask>()
