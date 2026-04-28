/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.utils.isAbstract
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * FIR checker for `kotlin.brs.brsName(function)`.
 *
 * Fires two diagnostics:
 *
 * - [FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF] — the argument is not a syntactic
 *   function reference (`::ref`). Examples: lambda, stored ref, function call result,
 *   function-typed parameter.
 *
 * - [FirBrsErrors.BRS_BRSNAME_INVALID_TARGET] — the argument *is* a `::ref` but the
 *   referenced declaration produces a name that does not correspond to a runtime
 *   BrightScript function. Two cases today:
 *     - `::localFun` — a local function. `LocalFunctionLowering` (Phase 5) hoists it
 *       to a synthesized module-level name *after* `BrsIntrinsicLowering` (Phase 0.05)
 *       has captured the pre-hoist name, so the resulting string never resolves.
 *     - `Iface::abstractMethod` — an abstract member. The BRS backend emits no
 *       module-level function for the abstract declaration; runtime lookup by name
 *       fails.
 *
 * Constructors (`::SomeClass`) and concrete instance methods of regular classes
 * (`MyClass::method`, `this::method`) remain valid.
 *
 * `brsName` extracts the mangled BrightScript name of a function at compile time.
 * The IR-phase lowering (`BrsIntrinsicLowering`) can only resolve `IrFunctionReference`
 * nodes — anything stored in a variable, passed as a parameter, or expressed as a lambda
 * has already been lowered to an `IrGetValue` or `IrBlock` by the time the lowering runs.
 * This FIR check catches such usages early (IDE squiggle) with a clear message.
 *
 * **Passes:**
 * - `brsName(::topLevel)` — direct function reference
 * - `brsName(SomeClass::method)` — qualified reference
 * - `brsName(this::method)` — bound reference
 * - `brsName(::SomeClass)` — constructor reference (IR handles `IrConstructor` via
 *   `generateBrsFunctionName`)
 *
 * **Fires BRS_BRSNAME_REQUIRES_CALLABLE_REF:**
 * - `brsName({ x -> x })` — lambda (kind: "lambda")
 * - `brsName(storedRef)` — stored callable reference variable (kind: "property reference")
 * - `brsName(funcReturningFun())` — function call result (kind: "function call result")
 * - `brsName(param)` — function-typed parameter (kind: "property reference")
 *
 * **Fires BRS_BRSNAME_INVALID_TARGET:**
 * - `brsName(::localFun)` inside an enclosing function (kind: "local function")
 * - `brsName(IFace::abstractMethod)` for any `abstract`/`interface` member (kind: "abstract function")
 */
object FirBrsNameCallableRefChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val brsNameCallableId = BrsStandardClassIds.Callables.brsName
    private val FUNCTION_PARAM = Name.identifier("function")

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != brsNameCallableId) return

        // Prefer named-parameter extraction (by "function") for robustness; fall back to positional.
        val arg = (expression.argumentList as? FirResolvedArgumentList)
            ?.mapping
            ?.entries
            ?.firstOrNull { (_, param) -> param.name == FUNCTION_PARAM }
            ?.key
            ?: expression.arguments.firstOrNull()
            ?: return

        if (arg !is FirCallableReferenceAccess) {
            val actualKind = when (arg) {
                is FirAnonymousFunctionExpression -> "lambda"
                is FirPropertyAccessExpression -> "property reference"
                is FirFunctionCall -> "function call result"
                else -> "expression"
            }
            reporter.reportOn(
                arg.source ?: expression.source,
                FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF,
                actualKind,
            )
            return
        }

        // The argument is a callable reference. Inspect the resolved symbol to confirm
        // the target produces a runtime-resolvable BrightScript function name.
        // Constructors are valid (handled by IrConstructor branch in BrsIrBackendContext.generateBrsFunctionName).
        val targetSymbol = arg.calleeReference.toResolvedCallableSymbol() ?: return
        if (targetSymbol is FirConstructorSymbol) return
        if (targetSymbol !is FirNamedFunctionSymbol) return

        val invalidKind: String? = when {
            targetSymbol.isLocal -> "local function"
            targetSymbol.isAbstract -> "abstract function"
            else -> null
        }
        if (invalidKind == null) return

        reporter.reportOn(
            arg.source ?: expression.source,
            FirBrsErrors.BRS_BRSNAME_INVALID_TARGET,
            invalidKind,
            targetSymbol.name.asString(),
        )
    }
}
