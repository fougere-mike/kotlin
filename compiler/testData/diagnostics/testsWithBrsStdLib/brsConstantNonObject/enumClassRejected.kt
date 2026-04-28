// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on an enum class is a no-op at IR.
// The annotation sits on the enum class declaration (ClassKind.ENUM_CLASS), not on entries.
// The rendered message says "Found on enum class declaration."
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>enum class Color<!> {
    RED, GREEN, BLUE
}
