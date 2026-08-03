// Expected: no diagnostic — a type-parameter argument is skipped (the concrete class is
// checked at the outer reified call site instead); this is also why the stdlib's own
// createComponent wrapper body compiles clean with the checker active
import kotlin.brs.ComponentBase
import kotlin.brs.GroupComponent
import kotlin.brs.createComponent

class Tile : GroupComponent()

inline fun <reified T : ComponentBase> makeAndTag(): T {
    return createComponent<T>()
}

fun make(): Tile = makeAndTag<Tile>()
