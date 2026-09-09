// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the init-block read — init() runs inside
// CreateObject before any field is written, so the read sees the declared default.
// In K2 the bare `val` constructor-parameter name resolves to the PROPERTY here, with an implicit
// `this` dispatch receiver: init blocks and property initializers see only the PURE parameter
// scope (parameters that are NOT properties — BodyResolveContext.withAnonymousInitializer /
// forPropertyInitializer), so this is exactly the R1 dispatch-receiver-`this` shape that
// BRS_COMPONENT_INPUT_READ_IN_INIT catches (Task 2 review note 2: CAUGHT; see initBlockThisRead.kt).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        println(<!BRS_COMPONENT_INPUT_READ_IN_INIT!>airingId<!>)
    }
}
