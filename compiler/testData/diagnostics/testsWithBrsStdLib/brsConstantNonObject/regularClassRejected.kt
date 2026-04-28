// Expected: BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on a non-object class
// is silently ignored by the IR lowering (BrsConstantEvaluationLowering.kt:64-65).
// FIR reports it explicitly so the misuse can't slip past review.
//
// Note: the inner `var` is NOT additionally flagged with BRS_BRSCONSTANT_VAR — once
// the class-level diagnostic fires, the checker early-returns before walking members,
// preventing duplicate noise on the same root cause.
import kotlin.brs.BrsConstant

@BrsConstant
<!BRS_BRSCONSTANT_NON_OBJECT!>class IgnoredByLowering<!> {
    var ignored = 1
}
