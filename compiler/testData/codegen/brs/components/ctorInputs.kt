// A constructor-parameter @SG property (spec 2026-09-04-component-lifecycle §3):
// the extractor emits the XML field as for any @SG property; init() must NOT
// emit `m.top.airingId = airingId` (an undefined identifier inside sub init()),
// and the type gains the boolean ready-marker field `__kotlinInputsReady`.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.SGIntegerField

class InputScreen(
    @SGStringField val airingId: String,
    @SGIntegerField val row: Int,
) : GroupComponent() {
    @SGStringField
    var status: String = "idle"

    override suspend fun onStart() {
        status = airingId + ":" + row
    }
}
