// A component whose file never references coroutine machinery STILL gets the lifecycle attach (__kotlinComponentAttach) as init()'s first statement — the attach is unconditional (spec 2026-09-04-component-lifecycle §5.1) and absorbs the pump attach.
import kotlin.brs.ContentNodeComponent
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class PlainComponent : GroupComponent() {
    @SGStringField
    var label: String = ""

    init {
        label = "plain"
    }
}

// A ContentNode component is NOT a render component: it must NOT get the lifecycle
// attach or the lifecycle include closure (spec §5.1; retire()/revive() on one is a
// guided hasFunc ISE). Pinned here so the attach predicate stays render-only.
// READING THE GOLDEN: ContentCard's deps.json/XML below STILL list the coroutine +
// lifecycle closure — include deps are recorded per SOURCE FILE (BrsCompiler.
// generateComponentOutputForFile computes them once for every component in the file),
// so ContentCard inherits PlainComponent's attach-driven closure by sharing this file.
// The pin is the ABSENT `__kotlinComponentAttach` line (and absent onKeyEvent /
// __kotlinRetire / __kotlinRevive) in ContentCard's init(). A ContentNode component
// alone in its own file gets an EMPTY dependencies list (verified 2026-09-09).
class ContentCard : ContentNodeComponent() {
    @SGStringField var title: String = ""
}
