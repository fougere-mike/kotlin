// Expected: clean — a method body is not init; it runs on a live node whose inputs are set.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    fun describe(): String = "Airing " + airingId
}
