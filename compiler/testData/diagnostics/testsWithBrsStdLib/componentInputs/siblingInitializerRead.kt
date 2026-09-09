// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the sibling property initializer's read.
// Property initializers run inside init() (the constructor body), before any input is written.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    val title: String = "Airing " + <!BRS_COMPONENT_INPUT_READ_IN_INIT!>airingId<!>
}
