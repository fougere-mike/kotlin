// Every concrete render component gets bare-named __kotlinRetire/__kotlinRevive
// entries + XML <function> entries (spec 2026-09-04-component-lifecycle §5.6).
// __kotlinRetire calls the onStop slot (try/caught) only when the hierarchy
// overrides it; __kotlinRevive relaunches the synthesized onStart driver only
// when one exists.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class FullLifecycle : GroupComponent() {
    @SGStringField
    var state: String = ""

    override suspend fun onStart() {
        state = "started"
    }

    override fun onStop() {
        state = "stopped"
    }
}

class NoHooks : GroupComponent() {
    @SGStringField
    var label: String = ""
}
