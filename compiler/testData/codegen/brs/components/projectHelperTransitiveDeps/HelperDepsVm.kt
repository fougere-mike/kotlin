// Plain-class VM: the component reaches coroutineScope ONLY through this file —
// ScopesKt.brs must land in the component's include closure via the transitive
// walk over this project file's recorded deps (the live-repro shape: a VM class
// calling stdlib suspend helpers the component file never touches directly).
import kotlin.coroutines.coroutineScope
import kotlin.text.StringBuilder

class HelperDepsVm {
    suspend fun load(): String = coroutineScope {
        val sb = StringBuilder()
        sb.append("loaded")
        sb.toString()
    }
}
