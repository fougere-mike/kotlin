// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the init-block read — init() runs inside
// CreateObject before any field is written, so the read sees the declared default.
// The bare name resolves to the PRIMARY-CONSTRUCTOR VALUE PARAMETER in FIR (init blocks and
// property initializers see the constructor parameter scope), not to the property — the
// checker classifies that path too (Task 2 review note 2).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        println(<!BRS_COMPONENT_INPUT_READ_IN_INIT!>airingId<!>)
    }
}
