// Self-access to @SG*Field state through a LAMBDA-CAPTURED `this` must route
// through the node handle (<capture>.top.<field>), not the captured component
// object: at runtime the captured `this` (the this$0 field LocalDeclarationsLowering
// creates) is the component's m-scope AA, so a bare `.field` write on it is a
// silent AA-key write that never reaches the node interface field (no observer
// fires, no error). Reads are equally wrong (they read the stale AA key).
// Un-annotated internal state keeps the accessor-call path: those accessors are
// attached to the component m, which IS the captured object.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.builders.launch
import kotlin.coroutines.dispatchers.Dispatchers

class LambdaSelfWrite : GroupComponent() {
    @SGStringField
    var status: String = ""

    private var internalCount: Int = 0

    init {
        CoroutineScope(Dispatchers.Main).launch {
            status = "from-coroutine"
            val echo = status
            status = echo + "!"
            internalCount = internalCount + 1
        }

        val block = {
            status = "from-lambda"
        }
        block()
    }
}
