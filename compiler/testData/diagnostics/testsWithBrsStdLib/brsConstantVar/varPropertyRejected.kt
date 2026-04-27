// Expected: BRS_BRSCONSTANT_VAR — a single var in a @BrsConstant object fires once.
import kotlin.brs.BrsConstant

@BrsConstant
object Config {
    val API_VERSION = 1
    <!BRS_BRSCONSTANT_VAR!>var counter = 0<!>
}
