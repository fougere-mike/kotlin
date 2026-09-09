// Expected: clean — @Suppress opts out of the init-read rule.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        @Suppress("BRS_COMPONENT_INPUT_READ_IN_INIT")
        println(airingId)
    }
}
