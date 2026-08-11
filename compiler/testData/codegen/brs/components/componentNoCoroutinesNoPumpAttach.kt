// Negative control for the pump-attach injection: a component whose file never
// references coroutine machinery gets NO __kotlinPumpAttach in init() and NO
// coroutine scripts in its includes.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class PlainComponent : GroupComponent() {
    @SGStringField
    var label: String = ""

    init {
        label = "plain"
    }
}
