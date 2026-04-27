// Expected: BRS_STATIC_OVERLOAD on the unsuppressed overload only. Verifies
// per-member suppression granularity — silencing one member does not silence
// peers.
import kotlin.brs.BrsStatic

@Suppress("BRS_STATIC_OVERLOAD")
@BrsStatic
fun format(value: String): String = value

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Int): String = value.toString()
