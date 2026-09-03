/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.test

import kotlin.test.Test

/**
 * Golden file tests for BrightScript code generation.
 *
 * These tests verify that Kotlin source compiles to expected BrightScript output.
 * Run with -PupdateGoldenFiles=true to regenerate expected output after intentional changes.
 */

// ==================== Declaration Tests ====================

class BrsDeclarationGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun simpleFunction() = runTest("declarations/simpleFunction.kt")

    @Test
    fun simpleClass() = runTest("declarations/simpleClass.kt")

    @Test
    fun dataClass() = runTest("declarations/dataClass.kt")

    @Test
    fun dataClassWithCompanion() = runTest("declarations/dataClassWithCompanion.kt")

    // Pins direct-member-read property access on data classes — the evidence behind
    // BRS_SCOPE_RESULT_NOT_DATA's WARNING severity (see the fixture header comment).
    @Test
    fun dataClassPropertyRead() = runTest("declarations/dataClassPropertyRead.kt")

    @Test
    fun namedArgsSkipDefaults() = runTest("declarations/namedArgsSkipDefaults.kt")

    @Test
    fun initBlocks() = runTest("declarations/initBlocks.kt")
}

// ==================== Expression Tests ====================

class BrsExpressionGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun binaryOperators() = runTest("expressions/binaryOperators.kt")

    @Test
    fun whenExpression() = runTest("expressions/whenExpression.kt")

    @Test
    fun nullability() = runTest("expressions/nullability.kt")

    @Test
    fun safeCast() = runTest("expressions/safeCast.kt")

    @Test
    fun externalBrsNameOverloads() = runTest("expressions/externalBrsNameOverloads.kt")

    @Test
    fun arrayFactories() = runTest("expressions/arrayFactories.kt")

    @Test
    fun shortCircuit() = runTest("expressions/shortCircuit.kt")

    @Test
    fun emptyVarargCall() = runTest("expressions/emptyVarargCall.kt")
}

// ==================== Control Flow Tests ====================

class BrsControlFlowGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun loops() = runTest("controlFlow/loops.kt")

    @Test
    fun conditionals() = runTest("controlFlow/conditionals.kt")

    @Test
    fun doWhileConditionScope() = runTest("controlFlow/doWhileConditionScope.kt")

    @Test
    fun tryCatchExpression() = runTest("controlFlow/tryCatchExpression.kt")
}

// ==================== Class Tests ====================

class BrsClassGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun inheritance() = runTest("classes/inheritance.kt")

    @Test
    fun interfaces() = runTest("classes/interfaces.kt")

    @Test
    fun interfaceDefaultMethods() = runTest("classes/interfaceDefaultMethods.kt")

    @Test
    fun enumConstructorProperties() = runTest("classes/enumConstructorProperties.kt")
}

// ==================== Closure Tests ====================

class BrsClosureGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun lambdaCapture() = runTest("closures/lambdaCapture.kt")

    @Test
    fun capturedVarInAnonymousClass() = runTest("closures/capturedVarInAnonymousClass.kt")

    @Test
    fun nestedLambdaCaptureHoisting() = runTest("closures/nestedLambdaCaptureHoisting.kt")

    @Test
    fun nestedLambdaSuspendCaptureWrite() = runTest("closures/nestedLambdaSuspendCaptureWrite.kt")
}

// ==================== Coroutine Tests ====================

class BrsCoroutineGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun simpleSuspend() = runTest("coroutines/simpleSuspend.kt")

    @Test
    fun suspendResumeValue() = runTest("coroutines/suspendResumeValue.kt")

    @Test
    fun suspendStatementDiscard() = runTest("coroutines/suspendStatementDiscard.kt")

    @Test
    fun suspendInStringTemplate() = runTest("coroutines/suspendInStringTemplate.kt")

    @Test
    fun suspendInTryCatch() = runTest("coroutines/suspendInTryCatch.kt")

    @Test
    fun suspendTryDiscardedResult() = runTest("coroutines/suspendTryDiscardedResult.kt")

    @Test
    fun suspendStatementArgHoist() = runTest("coroutines/suspendStatementArgHoist.kt")

    @Test
    fun suspendFunctionReference() = runTest("coroutines/suspendFunctionReference.kt")

    @Test
    fun suspendMultiCatch() = runTest("coroutines/suspendMultiCatch.kt")

    // Typed catch clauses in a suspend state machine must is-dispatch and
    // rethrow non-matching exceptions; exact-Throwable stays unconditional
    // (task 3b — the state machine used to run every clause as a catch-all).
    @Test
    fun suspendTypedCatch() = runTest("coroutines/suspendTypedCatch.kt")

    // Multi-catch inside a LAMBDA: the merged __caught variable must get a
    // parent, or callable-reference lowering crashes the compile (task 3b).
    @Test
    fun multiCatchInLambda() = runTest("coroutines/multiCatchInLambda.kt")

    // try/finally around a suspension: the finally-lowering's synthesized
    // Any?-typed clause must stay an UNCONDITIONAL catch-all — an is-check
    // there skips the finally on the exceptional path (task 3b near-miss).
    @Test
    fun suspendTryFinally() = runTest("coroutines/suspendTryFinally.kt")

    @Test
    fun suspendMemberStateMachine() = runTest("coroutines/suspendMemberStateMachine.kt")

    // TRY_RESULT wrapping vs terminal-assignment arms: an arm typed non-Unit
    // only via generic LUB inference (T = Any) whose terminal statement is an
    // assignment must NOT be wrapped — the wrap swallows the write
    // (m.TRY_RESULT = (m._result.value = ...) renders as a comparison; task 3c).
    @Test
    fun suspendWrapAssignmentArm() = runTest("coroutines/suspendWrapAssignmentArm.kt")

    // The visitWhen sibling of the same hole: per-branch WHEN_RESULT wrapping
    // must skip a branch whose terminal statement is an assignment (task 3c).
    @Test
    fun suspendWhenBranchAssignment() = runTest("coroutines/suspendWhenBranchAssignment.kt")

    // Suspension INSIDE a typed catch clause body: the dispatch-state
    // save/restore around the clause body keeps the else-rethrow's edges on
    // the catch state (task 3b review rider).
    @Test
    fun suspendInTypedCatchBody() = runTest("coroutines/suspendInTypedCatchBody.kt")

    // break/continue in a NON-suspendable if, targeting a loop WITH suspension
    // points: the suspendable-nodes collector must mark such jumps (JS
    // visitBreakContinue rule) so the state machine rewrites them into state
    // dispatches. Pre-fix the orphaned IrBreak crashed LivenessAnalysis:
    // "Break from an unknown loop" (flow concurrent-operators defect).
    @Test
    fun suspendLoopJumpInPlainBranch() = runTest("coroutines/suspendLoopJumpInPlainBranch.kt")

    // A Unit-typed value parameter (generic instantiated at Unit — the
    // Flow<Unit>.collect { } shape) must emit "as Dynamic", never "as Void":
    // Void is return-position-only in BrightScript — a Void parameter is a
    // device-side compile error (flow concurrent-operators defect).
    @Test
    fun unitParameterType() = runTest("coroutines/unitParameterType.kt")

    // IR-special local names (<iterator>) lifted to state-machine fields must be
    // sanitized — the raw name is a BrightScript syntax error (flow cold core defect).
    @Test
    fun suspendLambdaForLoopIterator() = runTest("flow/suspendLambdaForLoopIterator.kt")

    // `if (a && !suspendCall())` in a suspend body — the guarded-temp wiring must
    // survive the state-machine split (flow cold core defect: FirstCollector).
    @Test
    fun suspendNegatedAndCondition() = runTest("flow/suspendNegatedAndCondition.kt")

    // Shared-box coroutine fields must be keyed by field SYMBOL, not raw
    // class-name strings — same-named locals in two suspend lambdas collide
    // (flow cold core defect: FlowCore flowEmitsValuesInOrder device failure).
    @Test
    fun sharedBoxFieldKeyCollision() = runTest("flow/sharedBoxFieldKeyCollision.kt")

    // Class TYPE names mangled into function names must be sanitized: a
    // "$"-named suspend-lambda SAM wrapper typing a coroutine's __this
    // parameter leaked "$" into the create function's name — a BrightScript
    // syntax error (flow simple-operators defect: OperatorsKt device compile).
    @Test
    fun samWrapperCoroutineCreateName() = runTest("flow/samWrapperCoroutineCreateName.kt")

    // Ordering comparisons on shared-box primitives must emit native operators:
    // the box read degrades the expression type to anyN, and the emitter fell
    // back to a compareTo METHOD call on a bare integer — "Member function not
    // found" on device (flow simple-operators defect: take/drop counters).
    @Test
    fun sharedBoxPrimitiveComparison() = runTest("flow/sharedBoxPrimitiveComparison.kt")

    // The spec-§9 operator-chain golden: a component collecting
    // flowOf().map{}.filter{}.flatMapLatest{}.onCompletion{} in launch {} —
    // user-side suspend-chain codegen against the flow prebuilt (operator call
    // shapes, nested SAM lambda classes, lambda-scope @SG writes, pump attach).
    @Test
    fun operatorChain() = runTest("flow/operatorChain.kt")
}

// ==================== Inline BrightScript Tests ====================

class BrsInlineGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun brsFunction() = runTest("inline/brsFunction.kt")

    @Test
    fun brsConstVal() = runTest("inline/brsConstVal.kt")
}

// ==================== SceneGraph Component Tests ====================

class BrsComponentGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun basicComponent() = runTest("components/basicComponent.kt")

    @Test
    fun sceneComponent() = runTest("components/sceneComponent.kt")

    @Test
    fun componentWithMAccess() = runTest("components/componentWithMAccess.kt")

    @Test
    fun inheritedField() = runTest("components/inheritedField.kt")

    @Test
    fun inheritedExport() = runTest("components/inheritedExport.kt")

    @Test
    fun inheritedOnChangeHandler() = runTest("components/inheritedOnChangeHandler.kt")

    @Test
    fun overriddenFieldKeepsBaseAnnotation() = runTest("components/overriddenFieldKeepsBaseAnnotation.kt")

    @Test
    fun overriddenFieldChangesAnnotation() = runTest("components/overriddenFieldChangesAnnotation.kt")

    @Test
    fun deepTransitiveDeps() = runTest("components/deepTransitiveDeps.kt")

    @Test
    fun projectHelperTransitiveDeps() = runMultiFileTest("components/projectHelperTransitiveDeps")

    @Test
    fun taskComponentBasic() = runTest("components/taskComponentBasic.kt")

    @Test
    fun taskComponentCallSite() = runTest("components/taskComponentCallSite.kt")

    @Test
    fun taskComponentInheritedRun() = runTest("components/taskComponentInheritedRun.kt")

    @Test
    fun runTaskCallSite() = runTest("components/runTaskCallSite.kt")

    @Test
    fun componentExternalFieldAccess() = runTest("components/componentExternalFieldAccess.kt")

    @Test
    fun lambdaSelfWriteToField() = runTest("components/lambdaSelfWriteToField.kt")

    @Test
    fun lambdaComponentScopeRead() = runTest("components/lambdaComponentScopeRead.kt")

    @Test
    fun plainTopNameNoHijack() = runTest("components/plainTopNameNoHijack.kt")

    @Test
    fun brsFieldNamedArgs() = runTest("components/brsFieldNamedArgs.kt")

    @Test
    fun nodeFieldNodeType() = runTest("components/nodeFieldNodeType.kt")

    @Test
    fun layoutStubAccess() = runTest("components/layoutStubAccess.kt")

    @Test
    fun componentLaunchExtension() = runTest("components/componentLaunchExtension.kt")

    @Test
    fun componentPumpAttachMethodUse() = runTest("components/componentPumpAttachMethodUse.kt")

    @Test
    fun componentNoCoroutinesNoPumpAttach() = runTest("components/componentNoCoroutinesNoPumpAttach.kt")
}

// ==================== Intrinsics Tests ====================

class BrsIntrinsicsGoldenFileTests : AbstractBrsGoldenFileTest() {
    // brsName test disabled pending implementation of brsName() for callable references
    // @Test
    // fun brsName() = runTest("intrinsics/brsName.kt")
}

// ==================== @BrsConstant Tests ====================

class BrsConstantGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun brsConstantBasic() = runTest("constants/brsConstantBasic.kt")

    @Test
    fun brsConstantTransitive() = runTest("constants/brsConstantTransitive.kt")

    @Test
    fun brsConstantArithmetic() = runTest("constants/brsConstantArithmetic.kt")

    @Test
    fun brsConstantBoolean() = runTest("constants/brsConstantBoolean.kt")

    @Test
    fun brsConstantEnum() = runTest("constants/brsConstantEnum.kt")
}

// ==================== ScopeHandle Tests ====================

class BrsScopeHandleGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun runBlockLowering() = runTest("scopehandle/runBlockLowering.kt")

    @Test
    fun bindingTableInjection() = runTest("scopehandle/bindingTableInjection.kt")
}

// ==================== SharedService Tests ====================

class BrsSharedServiceGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun sharedFromLowering() = runTest("shared/sharedFromLowering.kt")

    @Test
    fun sharedEmission() = runTest("shared/sharedEmission.kt")

    @Test
    fun sharedDispatch() = runTest("shared/sharedDispatch.kt")

    @Test
    fun sharedDataClass() = runTest("shared/sharedDataClass.kt")

    @Test
    fun sharedDispatchDataLeaf() = runTest("shared/sharedDispatchDataLeaf.kt")
}

// ==================== Reflection Tests ====================

class BrsReflectionGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun classReference() = runTest("reflection/classReference.kt")

    @Test
    fun getClass() = runTest("reflection/getClass.kt")

    @Test
    fun kclassEquality() = runTest("reflection/kclassEquality.kt")

    @Test
    fun nativeTypeClass() = runTest("reflection/nativeTypeClass.kt")
}
