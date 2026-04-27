// Expected: BRS_BRSCONSTANT_VAR x3 — every var in the same @BrsConstant object fires independently.
import kotlin.brs.BrsConstant

@BrsConstant
object Settings {
    <!BRS_BRSCONSTANT_VAR!>var first = 1<!>
    <!BRS_BRSCONSTANT_VAR!>var second = "two"<!>
    <!BRS_BRSCONSTANT_VAR!>var third = true<!>
}
