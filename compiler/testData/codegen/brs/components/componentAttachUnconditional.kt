// A component whose file never references coroutine machinery STILL gets the lifecycle attach (__kotlinComponentAttach) as init()'s first statement — the attach is unconditional (spec 2026-09-04-component-lifecycle §5.1) and absorbs the pump attach.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class PlainComponent : GroupComponent() {
    @SGStringField
    var label: String = ""

    init {
        label = "plain"
    }
}
