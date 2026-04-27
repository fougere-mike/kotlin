// Expected: BRS_BRSCONSTANT_VAR x2 — only the two var properties fire; vals are clean.
import kotlin.brs.BrsConstant

@BrsConstant
object Mixed {
    val constantA = 10
    <!BRS_BRSCONSTANT_VAR!>var mutableB = 20<!>
    val constantC = "hello"
    <!BRS_BRSCONSTANT_VAR!>var mutableD = false<!>
    val constantE = 3.14
}
