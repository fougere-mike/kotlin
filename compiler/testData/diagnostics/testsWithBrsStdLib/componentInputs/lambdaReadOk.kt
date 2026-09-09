// Expected: clean — a read inside a lambda declared in init (or in a sibling initializer) is
// deferred: the lambda body runs later, once inputs are written. DISCLOSED HOLE (documented
// under-approximation): an inline lambda invoked synchronously in init (`run { airingId }`)
// classifies as deferred too.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    private val describe: () -> String = { "Airing " + airingId }
    init {
        launch { println(airingId) }
    }
}
