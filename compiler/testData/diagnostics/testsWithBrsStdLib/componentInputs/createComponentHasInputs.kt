// Expected: BRS_CREATE_COMPONENT_HAS_INPUTS — an input-bearing class must be constructed with
// its constructor (Screen(...)) so every input is written before onStart(); createComponent<T>()
// would leave the inputs at their defaults and the ready gate never opens.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.createComponent

class Screen(@SGStringField val airingId: String) : GroupComponent()

class Host : GroupComponent() {
    fun open() {
        createComponent<<!BRS_CREATE_COMPONENT_HAS_INPUTS!>Screen<!>>()
    }
}
