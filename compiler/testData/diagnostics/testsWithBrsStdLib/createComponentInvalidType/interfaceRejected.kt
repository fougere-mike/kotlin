// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE + upstream UPPER_BOUND_VIOLATED — an interface
// can never satisfy the T : ComponentBase bound, so upstream already rejects it; our checker
// still fires with the "it is an interface" reason (defense in depth for future factory
// callables with looser bounds, e.g. Task 12's runTask)
import kotlin.brs.createComponent

interface Renderable

fun make(): Renderable = createComponent<<!UPPER_BOUND_VIOLATED!><!BRS_CREATE_COMPONENT_INVALID_TYPE!>Renderable<!><!>>()
