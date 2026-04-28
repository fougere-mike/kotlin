// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on an annotation class is a no-op at IR.
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>annotation class Marker<!>
