// Expected: clean — @Suppress opts out of the has-inputs rule.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.createComponent

class Screen(@SGStringField val airingId: String) : GroupComponent()

class Host : GroupComponent() {
    @Suppress("BRS_CREATE_COMPONENT_HAS_INPUTS")
    fun open() {
        createComponent<Screen>()
    }
}
