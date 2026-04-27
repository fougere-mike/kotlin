// Expected: no diagnostic — @Suppress silences BRS_BRSCONSTANT_VAR at the property.
// The IR-phase defensive error still fires but is filtered by AbstractBrsDiagnosticTest's
// ignoredMessagePatterns (^\[IR] @BrsConstant.*).
import kotlin.brs.BrsConstant

@BrsConstant
object Suppressed {
    val keep = 1
    @Suppress("BRS_BRSCONSTANT_VAR")
    var allowed = 2
}
