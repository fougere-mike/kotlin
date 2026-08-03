// Expected: BRS_CREATE_COMPONENT_INVALID_TYPE — concrete, satisfies the ComponentBase bound,
// but is not a component: no @BrsComponent annotation and no @BrsSceneGraphComponent ancestor
// (ComponentBase itself is not annotated), so no XML component is ever generated for it
import kotlin.brs.ComponentBase
import kotlin.brs.createComponent

class PlainHelper : ComponentBase()

fun make(): PlainHelper = createComponent<<!BRS_CREATE_COMPONENT_INVALID_TYPE!>PlainHelper<!>>()
