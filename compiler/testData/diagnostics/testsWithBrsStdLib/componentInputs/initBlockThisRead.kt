// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the explicit-receiver read. `this.airingId` and
// the bare `airingId` are the SAME resolution in K2: init blocks and property initializers see
// only the PURE primary-constructor parameter scope (parameters that are not properties —
// BodyResolveContext.withAnonymousInitializer / forPropertyInitializer), so a `val` parameter's
// bare name resolves to the PROPERTY with an implicit `this`, and the dispatch-receiver-`this`
// clause catches both spellings (Task 2 review note 2: CAUGHT — no value-parameter clause needed).
// A read through a HANDLE (`peer?.airingId`) is an ordinary node-field read and stays clean.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        println(<!BRS_COMPONENT_INPUT_READ_IN_INIT!>this.airingId<!>)
    }
    private val peer: Screen? = null
    private val peerId: String? = peer?.airingId
}
