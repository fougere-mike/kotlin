// The task lift, flowOn form (flow-program spec §5): a component collecting
// flow { emit(<AA>) }.map { ... }.flowOn(Dispatchers.Task) inside launch {},
// with one hoisted capture used in TWO region lambdas. Pins:
// - the call-site rewrite (taskFlowLifted("KotlinFlowTask_flowOnLift_1", <AA>)
//   with the captures AA built at the site; the Dispatchers.Task token GONE),
// - the lifted upstream factory __flowUpstream_flowOnLift_1(captures) with the
//   capture re-declared from the AA and BOTH lambda references remapped to it,
// - the synthesized component: KotlinFlowTask_flowOnLift_1.xml with ALL SIX
//   fields (kotlinTaskState/kotlinTaskError/kotlinTaskId/flowCaptures/flowOut/
//   flowCancel) + extends="Task" + the __kotlinTaskMain export,
// - the synthesized run() passing m.top (never m) to the drivers, and
// - the synthesized deps.json closing over the originating file + flow klib.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.roku.RoAssociativeArray
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.*

class FlowOnLift : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        val label = "shelf"
        launch {
            flow {
                val row = RoAssociativeArray.create()
                row.addReplace("name", label)
                emit(row)
            }
                .map { row -> "mapped:" + label }
                .flowOn(Dispatchers.Task)
                .collect { v -> status = v }
        }
    }
}
