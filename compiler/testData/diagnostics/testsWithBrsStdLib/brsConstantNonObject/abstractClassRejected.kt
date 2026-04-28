// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on an abstract class is a no-op at IR.
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>abstract class AbstractConfig<!> {
    abstract val value: Int
}
