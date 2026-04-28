// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on a sealed class is a no-op at IR.
// Sealed classes have ClassKind.CLASS (Modality.SEALED is separate from ClassKind), so
// the rendered message says "Found on class declaration."
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>sealed class Result<!>
