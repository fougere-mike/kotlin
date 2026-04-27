// Expected: no diagnostic — named type arg with valid canonical string
import kotlin.brs.createObject

fun test(): Any = createObject(type = "roSGNode")
