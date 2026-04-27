// Expected: BRS_STATIC_OVERLOAD on all THREE functions (verifies count = 3 renders
// correctly and every member of the duplicate group lights up).
import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: String): String = value

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Int): String = value.toString()

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>format<!>(value: Boolean): String = value.toString()
