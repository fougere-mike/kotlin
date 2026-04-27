// Expected: BRS_CREATE_OBJECT_INVALID_TYPE — "ROARRAY" (all-caps) is not canonical; requires "roArray"
import kotlin.brs.createObject

fun test(): Any = createObject(<!BRS_CREATE_OBJECT_INVALID_TYPE!>"ROARRAY"<!>)
