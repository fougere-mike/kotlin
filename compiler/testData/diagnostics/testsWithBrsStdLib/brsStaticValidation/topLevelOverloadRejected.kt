// Expected: BRS_STATIC_OVERLOAD on BOTH functions (top-level scope, two @BrsStatic
// functions sharing the name `format`).
import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: String): String = value

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Int): String = value.toString()
