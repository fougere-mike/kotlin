// Expected: no diagnostic — literal-required-narrow policy: dynamic (non-constant) type arg is silently skipped
import kotlin.brs.createObject

fun computeType(): String = "roArray"

fun test(): Any = createObject(computeType())
