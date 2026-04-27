// Expected: BRS_CREATE_OBJECT_INVALID_TYPE — const val resolves to "roWidget" which is not a known type
import kotlin.brs.createObject

const val BAD_TYPE = "roWidget"

fun test(): Any = createObject(<!BRS_CREATE_OBJECT_INVALID_TYPE!>BAD_TYPE<!>)
