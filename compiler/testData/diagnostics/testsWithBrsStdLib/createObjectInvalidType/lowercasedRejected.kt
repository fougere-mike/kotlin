// Expected: BRS_CREATE_OBJECT_INVALID_TYPE — "roarray" (all-lowercase) is not canonical; requires "roArray"
// Confirms exact canonical-case policy (BrightScript is case-insensitive at runtime, but we require canonical).
import kotlin.brs.createObject

fun test(): Any = createObject(<!BRS_CREATE_OBJECT_INVALID_TYPE!>"roarray"<!>)
