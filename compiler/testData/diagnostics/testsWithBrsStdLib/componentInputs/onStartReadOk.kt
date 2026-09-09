// Expected: clean — onStart runs after the ready gate; every input is set by then.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    override suspend fun onStart() {
        println(airingId)
    }
}
