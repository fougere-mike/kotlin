// Expected: no diagnostic.
//
// @BrsConstant's IR lowering filters with `kind == ClassKind.OBJECT`
// (BrsConstantEvaluationLowering.kt:64-65), so a regular class carrying @BrsConstant
// is a silent no-op at IR. The FIR checker mirrors this filter to avoid surfacing
// diagnostics for code the lowering ignores. A future BRS_BRSCONSTANT_NON_OBJECT
// could shift left this misuse, but is out of scope for BRS_BRSCONSTANT_VAR.
import kotlin.brs.BrsConstant

@BrsConstant
class IgnoredByLowering {
    var ignored = 1
}
