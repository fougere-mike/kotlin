// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on an interface is a no-op at IR.
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>interface IConfig<!> {
    val timeout: Int
}
