// Expected: no diagnostic — const val with valid canonical type string is accepted
import kotlin.brs.createObject

const val ROARRAY_TYPE = "roArray"

fun test(): Any = createObject(ROARRAY_TYPE)
