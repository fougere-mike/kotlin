// Expected: no diagnostic — @Suppress silences BRS_CREATE_OBJECT_INVALID_TYPE
import kotlin.brs.createObject

@Suppress("BRS_CREATE_OBJECT_INVALID_TYPE")
fun test(): Any = createObject("roWidget")
