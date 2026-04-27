// Expected: no diagnostic — canonical lowercase "roArray" is valid
import kotlin.brs.createObject
import kotlin.brs.roku.RoArray

fun test(): RoArray = createObject("roArray", 0, true)
