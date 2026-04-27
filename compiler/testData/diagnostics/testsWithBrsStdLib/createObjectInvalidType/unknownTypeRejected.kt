// Expected: BRS_CREATE_OBJECT_INVALID_TYPE — "roFakeType" is not a known Roku object type
import kotlin.brs.createObject

fun test(): Any = createObject(<!BRS_CREATE_OBJECT_INVALID_TYPE!>"roFakeType"<!>)
