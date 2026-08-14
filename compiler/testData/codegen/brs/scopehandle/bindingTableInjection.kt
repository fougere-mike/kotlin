// Binding-table injection — locks the OWNER half of the compiler-lowered
// run{} surface: a component whose file calls exposeScope gets, in generated
// init() AFTER the pump attach,
//   m.__kotlinScopeBindings = {"<fileFq>#<n>": <liftedFn>, ...}
//   __kotlinScopeBindingsInstall(m.__kotlinScopeBindings)
// with one entry per lifted run-block whose FILE is in this component's
// include closure (here: the component's own file — blocks #1 and #2).
// Request-name keys contain '#', so they must render as QUOTED AA-literal
// keys. The install call hands the table to the stdlib's per-component
// holder (GetGlobalAA domain — the __kotlinPumpAttach idiom); owner dispatch
// consults it after the hand-registered map, on both the wire and the
// same-component fast path.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.exposeScope
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scopeHandleOf

private fun describe(node: RoSGNode): String = "node:" + node.subtype()

class BindingTableInjection : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        exposeScope {}
        launch {
            val self = scopeHandleOf(top)
            val node: RoSGNode = top
            val zero = self.run { 7 }
            val described = self.run { describe(node) }
            status = "" + zero + described
        }
    }
}
