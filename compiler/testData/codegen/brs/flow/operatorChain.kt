// The spec-§9 representative operator chain: a COMPONENT collecting
// flowOf().map{}.filter{}.flatMapLatest{}.onCompletion{} inside launch {} —
// pins user-side suspend-chain codegen against the flow prebuilt klib: the
// operator extension call shapes (receiver-first _k_ globals), the nested
// suspend-lambda SAM classes with sanitized create/mangle names, the
// captured-self routing for @SG field writes in lambda scope (m.this_0.top),
// and the injected __kotlinPumpAttach from the file-level coroutine scan.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.coroutines.flow.*

class OperatorChain : GroupComponent() {
    @SGStringField
    var result: String = ""

    init {
        launch {
            flowOf(1, 2, 3)
                .map { it * 10 }
                .filter { it > 10 }
                .flatMapLatest { v -> flowOf(v, v + 1) }
                .onCompletion { result = result + "|done" }
                .collect { v -> result = result + "," + v }
        }
    }
}
