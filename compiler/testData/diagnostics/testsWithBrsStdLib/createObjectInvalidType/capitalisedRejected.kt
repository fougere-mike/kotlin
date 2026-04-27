// Expected: BRS_CREATE_OBJECT_INVALID_TYPE — "RoArray" (title-case) is not canonical; requires "roArray"
import kotlin.brs.createObject

fun test(): Any = createObject(<!BRS_CREATE_OBJECT_INVALID_TYPE!>"RoArray"<!>)
