// Expected: clean — the one position where a `val` constructor parameter's bare name resolves to
// the VALUE PARAMETER in K2 is the constructor header (the delegated super call here): forwarding
// it to the base constructor is a parameter forward, not an init read of the field, and the
// checker deliberately ignores non-property callees. DISCLOSED (R34 backlog, out of plan-B v1
// scope): the inherited-input shape itself — a leaf that forwards to an input-bearing base while
// declaring no input of its own is invisible to both createComponent-has-inputs and the
// extractor's requiredInputs (the silent-watchdog shape).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

abstract class BaseScreen(@SGStringField open val airingId: String) : GroupComponent()

class Screen(@SGStringField override val airingId: String) : BaseScreen(airingId)
