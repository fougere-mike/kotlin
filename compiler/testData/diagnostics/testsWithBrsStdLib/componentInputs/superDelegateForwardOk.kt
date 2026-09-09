// Expected: clean by design — a parameter forward in constructor-header context: the delegated
// super call reads the `val` parameter as a VALUE PARAMETER (the checker deliberately ignores
// non-property callees), and forwarding it to the base constructor is not an init read of the
// field. DISCLOSED (R34 backlog, out of plan-B v1 scope): the inherited-input shape itself — a
// leaf that forwards to an input-bearing base while declaring no input of its own is invisible to
// both createComponent-has-inputs and the extractor's requiredInputs (the silent-watchdog shape).
// Also clean, and belonging in the same R34-widening bundle: a SUBCLASS init reading an INHERITED
// input (`class Leaf : Screen("x") { init { println(airingId) } }`) — the rule scopes to the
// owner's identity (the accessed context's containing class must be the input's declaring class).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

abstract class BaseScreen(@SGStringField open val airingId: String) : GroupComponent()

class Screen(@SGStringField override val airingId: String) : BaseScreen(airingId)
