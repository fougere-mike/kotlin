// Expected: BRS_STATIC_INVALID_TARGET — generic-typed receiver still resolves through
// classId.shortClassName ("List"), so the diagnostic argument extraction is well-defined.
import kotlin.brs.BrsStatic

@BrsStatic
fun List<String>.<!BRS_STATIC_INVALID_TARGET!>joinSpaces<!>(): String = joinToString(" ")
