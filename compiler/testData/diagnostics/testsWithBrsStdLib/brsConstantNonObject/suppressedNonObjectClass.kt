// Expected: no diagnostic — @Suppress silences BRS_BRSCONSTANT_NON_OBJECT at the class.
import kotlin.brs.BrsConstant

@Suppress("BRS_BRSCONSTANT_NON_OBJECT")
@BrsConstant
class Quiet {
    val x = 1
}
