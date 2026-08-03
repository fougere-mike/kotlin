// Expected: no diagnostic — @Suppress("BRS_CREATE_COMPONENT_INVALID_TYPE") silences the FIR
// error. (The IR lowering then reports its own plain-text error for the same call; the
// diagnostic harness ignores IR-phase messages by pattern, mirroring the @BrsStatic precedent.)
import kotlin.brs.TaskComponent
import kotlin.brs.createComponent

abstract class GhostTask : TaskComponent()

@Suppress("BRS_CREATE_COMPONENT_INVALID_TYPE")
fun launch(): GhostTask = createComponent<GhostTask>()
