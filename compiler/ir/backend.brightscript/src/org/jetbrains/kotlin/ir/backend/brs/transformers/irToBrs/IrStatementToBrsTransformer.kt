/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.backend.common.capturedFields
import org.jetbrains.kotlin.backend.common.lower.AbstractSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.parseBrightScriptStatements
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsCodeOutliningLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInlineCallTransformer
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.io.File

/**
 * Transforms IR statements to BrightScript statements.
 */
class IrStatementToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext,
    private val genCtx: BrsGenerationContext,
) : IrVisitor<BrsStatement?, Unit>() {

    override fun visitElement(element: IrElement, data: Unit): BrsStatement? = null

    override fun visitCall(expression: IrCall, data: Unit): BrsStatement {
        return visitCallAsStatement(expression)
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Unit): BrsStatement? {
        // Handle IMPLICIT_COERCION_TO_UNIT by unwrapping to the inner expression/block
        // This handles cases like `toIndex++` used as a statement, where the increment block
        // is wrapped in IMPLICIT_COERCION_TO_UNIT because the Int result is discarded
        if (expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
            val innerArg = expression.argument
            return when (innerArg) {
                is IrBlock -> visitBlock(innerArg, data)
                is IrWhen -> visitWhen(innerArg, data)
                is IrCall -> visitCall(innerArg, data)
                else -> {
                    // Side-effect-free reads in statement position (e.g. the suspendResult
                    // read the coroutine state machine leaves in a continue state when the
                    // value is discarded) would emit a bare identifier - invalid BrightScript.
                    if (isDiscardablePureExpression(innerArg)) return BrsEmpty()
                    // For other expressions, transform and wrap as expression statement
                    val expr = parent.transformExpression(innerArg)
                    val hoisted = genCtx.takeHoistedStatements()
                    if (hoisted.isNotEmpty()) {
                        BrsBlock((hoisted + BrsExpressionStatement(expr)).toMutableList())
                    } else {
                        BrsExpressionStatement(expr)
                    }
                }
            }
        }
        return null
    }

    /**
     * True for expressions that are side-effect free and therefore produce no code when
     * used in statement position (their value is discarded). BrightScript cannot emit a
     * bare identifier/constant as a statement, so such statements must be dropped.
     *
     * IrGetField with a pure receiver is included because the suspend lowering promotes
     * state-machine locals to continuation fields (BrsLiveLocalsTransformer): the
     * unconsumed TRY_RESULT value-exposure read that BrsStateMachineBuilder.visitTry
     * leaves behind arrives here as IrGetField(m, TRY_RESULT), and rendering it emits a
     * bare `m.TRY_RESULT` line — a BrightScript syntax error (Task 6 device repro).
     */
    private fun isDiscardablePureExpression(expression: IrExpression): Boolean = when (expression) {
        is IrGetValue -> true
        is IrConst -> true
        is IrGetField -> expression.receiver.let { it == null || it is IrGetValue }
        is IrTypeOperatorCall ->
            (expression.operator == IrTypeOperator.IMPLICIT_CAST ||
                expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) &&
                isDiscardablePureExpression(expression.argument)
        else -> false
    }

    override fun visitVariable(declaration: IrVariable, data: Unit): BrsStatement {
        val initializer = declaration.initializer

        // Check if this variable is shared (captured by closure and mutable)
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val isShared = declaration.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER ||
                       declaration.symbol in genCtx.sharedVariables

        // Get a unique variable name to avoid collision with other variables
        // that may have the same name in the IR (e.g., from multiple inline expansions)
        val baseName = sanitizeParameterName(declaration.name.asString())
        val uniqueName = genCtx.getUniqueVariableName(declaration.symbol, baseName)

        // Handle block initializers specially - BrightScript doesn't have block expressions
        // so we need to flatten the block's statements before the variable assignment
        if (initializer is IrBlock && initializer.statements.size > 1) {
            val statements = mutableListOf<BrsStatement>()

            // Flatten all statements from the block recursively
            flattenBlockStatements(initializer.statements, statements, data)

            // The last added statement should be the variable assignment
            // Replace the last expression statement with the actual variable declaration
            if (statements.isNotEmpty()) {
                val last = statements.removeLast()
                var lastExpr: BrsExpression = when (last) {
                    is BrsExpressionStatement -> last.expression
                    is BrsVariable -> last.initializer ?: BrsInvalidLiteral()
                    else -> BrsInvalidLiteral()
                }
                // Box shared variables
                if (isShared) {
                    lastExpr = BrsAALiteral(mutableListOf(BrsAAEntry("value", lastExpr)))
                }
                statements.add(BrsVariable(
                    name = uniqueName,
                    type = mapTypeToBrs(declaration.type),
                    initializer = lastExpr
                ))
            } else {
                var initExpr: BrsExpression = BrsInvalidLiteral()
                if (isShared) {
                    initExpr = BrsAALiteral(mutableListOf(BrsAAEntry("value", initExpr)))
                }
                statements.add(BrsVariable(
                    name = uniqueName,
                    type = mapTypeToBrs(declaration.type),
                    initializer = initExpr
                ))
            }

            return BrsBlock(statements)
        }

        // Simple case - the expression transformer handles when-lowered blocks via hoisting
        // Transform the initializer, which may add hoisted statements
        val transformedInit = initializer?.let { parent.transformExpression(it) }

        // If shared (already checked above), wrap the initializer in {value: ...} box
        val finalInit = if (isShared && transformedInit != null) {
            // Box the value: {value: initializer}
            BrsAALiteral(mutableListOf(BrsAAEntry("value", transformedInit)))
        } else {
            transformedInit
        }

        // Check for hoisted statements from when-lowered blocks
        val hoisted = genCtx.takeHoistedStatements()
        return if (hoisted.isEmpty()) {
            BrsVariable(
                name = uniqueName,
                type = mapTypeToBrs(declaration.type),
                initializer = finalInit
            )
        } else {
            // Prepend hoisted statements before the variable declaration
            val allStatements = hoisted.toMutableList()
            allStatements.add(BrsVariable(
                name = uniqueName,
                type = mapTypeToBrs(declaration.type),
                initializer = finalInit
            ))
            BrsBlock(allStatements)
        }
    }

    /**
     * Recursively flatten statements from an IrBlock, handling nested blocks.
     * The last statement in each block is treated as an expression value.
     */
    private fun flattenBlockStatements(
        stmts: List<IrStatement>,
        output: MutableList<BrsStatement>,
        data: Unit
    ) {
        for (i in stmts.indices) {
            val stmt = stmts[i]
            val isLast = i == stmts.lastIndex

            when {
                stmt is IrBlock && stmt.statements.isNotEmpty() -> {
                    // Check if this is a FOR_LOOP block - if so, increment the nesting counter
                    // so that any break statements inside get "exit while" instead of "exit for"
                    val isForLoop = stmt.origin == IrStatementOrigin.FOR_LOOP
                    if (isForLoop) {
                        genCtx.forLoopToWhileNestingDepth++
                    }
                    try {
                        // Recursively flatten nested blocks
                        flattenBlockStatements(stmt.statements, output, data)
                    } finally {
                        if (isForLoop) {
                            genCtx.forLoopToWhileNestingDepth--
                        }
                    }
                }
                stmt is IrVariable -> {
                    output.add(visitVariable(stmt, data))
                }
                stmt is IrWhen -> {
                    // When statement - transform using statement transformer
                    parent.transformStatement(stmt)?.let { output.add(it) }
                }
                stmt is IrExpression -> {
                    val expr = parent.transformExpression(stmt)
                    // For the LAST expression, always add it as an expression statement
                    // because it may be the value of the block (e.g., __when_tmp variable read)
                    // For non-last expressions, skip side-effect-free ones
                    if (isLast) {
                        // Always add last expression - it's the block's value
                        output.add(BrsExpressionStatement(expr))
                    } else if (expr !is BrsIdentifier && expr !is BrsInvalidLiteral &&
                        expr !is BrsIntLiteral && expr !is BrsDoubleLiteral &&
                        expr !is BrsStringLiteral && expr !is BrsBooleanLiteral) {
                        output.add(BrsExpressionStatement(expr))
                    }
                }
                else -> {
                    parent.transformStatement(stmt)?.let { output.add(it) }
                }
            }
        }
    }

    /**
     * Handle local function declarations.
     *
     * Local functions are transformed into closure objects when they capture variables
     * from the outer scope. BrightScript anonymous functions cannot access outer scope
     * variables, so we create an object with captured variable fields and an invoke method.
     */
    override fun visitFunction(declaration: IrFunction, data: Unit): BrsStatement {
        if (declaration is IrSimpleFunction && declaration.isFakeOverride) return BrsEmpty()

        val name = declaration.name.asString()
        val rawParameters = declaration.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type)
            )
        }
        val parameters = deduplicateParameterNames(rawParameters)

        val returnType = mapTypeToBrs(declaration.returnType)

        // For subs (void return), use VOID return type
        val effectiveReturnType = if (declaration.returnType.isUnit() || declaration.returnType.isNothing()) {
            BrsType.VOID
        } else {
            returnType
        }

        // Detect captured variables for local functions
        val capturedVars = if (declaration is IrSimpleFunction) {
            genCtx.detectCapturedVariables(declaration)
        } else {
            emptyList()
        }

        // Save previous closure context
        val previousContext = genCtx.currentClosureContext

        // Set closure context for body transformation (if there are captures)
        if (capturedVars.isNotEmpty()) {
            genCtx.currentClosureContext = capturedVars
        }

        // Transform body with closure context active
        val body = declaration.body?.let { parent.transformBody(it) } ?: BrsBlock()

        // Restore previous context
        genCtx.currentClosureContext = previousContext

        // Build closure object for local function (always, for consistent .invoke() usage)
        val entries = mutableListOf<BrsAAEntry>()

        for (capturedVar in capturedVars) {
            val varValue = BrsIdentifier(capturedVar.name)
            val fieldValue = if (capturedVar.isMutable) {
                // Wrap mutable captures in { value: x } for mutation to propagate
                BrsAALiteral(mutableListOf(BrsAAEntry("value", varValue)))
            } else {
                // Read-only captures can be stored directly
                varValue
            }
            entries.add(BrsAAEntry(capturedVar.name, fieldValue))
        }

        // Add the invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), effectiveReturnType, body)
        entries.add(BrsAAEntry("invoke", invokeFunction))

        val closureObject = BrsAALiteral(entries)

        return BrsVariable(
            name = name,
            type = BrsType.OBJECT,
            initializer = closureObject
        )
    }

    override fun visitReturn(expression: IrReturn, data: Unit): BrsStatement {
        // Transform the expression - hoisted statements from when-lowered blocks go to current scope
        val transformedValue = parent.transformExpression(expression.value)
        val hoisted = genCtx.takeHoistedStatements()

        // If the transformed value is a BrsStatementAsExpression wrapping a control-flow statement
        // (throw, return, exit, continue), we should emit that statement directly instead of
        // wrapping it in a return. These statements don't produce values and "return throw x" is invalid.
        if (transformedValue is BrsStatementAsExpression) {
            val innerStmt = transformedValue.statement
            if (innerStmt is BrsThrow || innerStmt is BrsReturn || innerStmt is BrsExit || innerStmt is BrsContinue) {
                return if (hoisted.isNotEmpty()) {
                    val allStatements = hoisted.toMutableList()
                    allStatements.add(innerStmt)
                    BrsBlock(allStatements)
                } else {
                    innerStmt
                }
            }
        }

        val returnTarget = expression.returnTargetSymbol.owner

        // Returns to returnable blocks (from inlined functions) after lowering
        // The BrsReturnableBlockLowering transforms:
        //   return@block value  ->  result = value; return@block Unit
        // And wraps the block in: while true { ... exit while } end while
        //
        // So when we see return@block Unit, we should emit "exit while" to break out of the wrapper loop.
        // The result variable has already been set by the lowering.
        //
        // IMPORTANT: If we're using flag-based approach (because the return is inside a while loop),
        // we need to set the flag before exiting.
        if (returnTarget !is IrFunction) {
            // This is return@block Unit (after lowering) - emit exit while
            val statements = hoisted.toMutableList()

            // If there's a flag on the stack, we're inside a flag-based returnable block
            val flagName = genCtx.returnableBlockFlagStack.lastOrNull()
            if (flagName != null) {
                statements.add(BrsExpressionStatement(
                    BrsBinaryOp(BrsIdentifier(flagName), BrsBinaryOperator.EQ, BrsBooleanLiteral(true))
                ))
            }
            statements.add(BrsExit(BrsExitKind.WHILE))
            return if (statements.size == 1) statements[0] else BrsBlock(statements)
        }

        // Check if the function we're returning from expects a value
        // (e.g., suspend functions have their return type changed from Unit to Any?)
        val functionReturnsValue = !returnTarget.returnType.isUnit() && !returnTarget.returnType.isNothing()

        // For Unit return types, we still need to execute the expression (it may have side effects)
        // but we don't return a value. Emit the expression as a statement followed by empty return.
        // EXCEPTION: If the function expects a return value (like suspend functions returning Any?),
        // we must return the expression result even if it's Unit-typed, because the actual runtime
        // value may be COROUTINE_SUSPENDED which needs to propagate to the caller.
        if (expression.value.type.isUnit()) {
            if (functionReturnsValue) {
                // Function expects a return value - return the expression result even if it's Unit-typed
                // This is critical for suspend functions where Unit expressions may actually carry
                // suspension signals (COROUTINE_SUSPENDED) at runtime
                val statements = hoisted.toMutableList()
                statements.add(BrsReturn(value = transformedValue))
                return if (statements.size == 1) statements[0] else BrsBlock(statements)
            } else {
                // Function doesn't expect return value - emit as statement then return nothing
                val statements = hoisted.toMutableList()
                // Only add the expression as a statement if it's not just a literal/identifier
                // (to avoid emitting standalone "invalid" statements)
                if (transformedValue !is BrsInvalidLiteral && transformedValue !is BrsIdentifier) {
                    statements.add(BrsExpressionStatement(transformedValue))
                }
                statements.add(BrsReturn(value = null))
                return if (statements.size == 1) statements[0] else BrsBlock(statements)
            }
        }

        return if (hoisted.isNotEmpty()) {
            val allStatements = hoisted.toMutableList()
            allStatements.add(BrsReturn(value = transformedValue))
            BrsBlock(allStatements)
        } else {
            BrsReturn(value = transformedValue)
        }
    }

    override fun visitThrow(expression: IrThrow, data: Unit): BrsStatement {
        return if (context.supportsExceptions) {
            BrsThrow(parent.transformExpression(expression.value))
        } else {
            // Fallback for older Roku OS versions - use stop
            BrsExpressionStatement(
                BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(
                        BrsStringLiteral("Error: "),
                        parent.transformExpression(expression.value)
                    )
                )
            )
        }
    }

    override fun visitTry(aTry: IrTry, data: Unit): BrsStatement {
        // Helper to transform a statement/expression inside try/catch blocks.
        // IrWhen needs special handling because it's technically an IrExpression
        // but when used in statement context (like `if (x) return`) it should be
        // transformed as a statement, not an expression (which returns BrsInvalidLiteral for Unit).
        //
        // CRITICAL: each statement's transformation may hoist statements into
        // genCtx (elvis machinery, nested lowered try/when blocks). Those must
        // be spliced immediately BEFORE the statement, INSIDE this try/catch
        // block — an isolated scope per statement, mirroring the
        // block-as-expression consumers. Without this the hoists leak to the
        // next consumer OUTSIDE the try: a catch arm's elvis on the catch
        // parameter was emitted above the try (reading `e` before it exists),
        // and a nested try-expression's statements escaped the outer try
        // entirely (device miscompiles, fix round 3).
        fun transformBlockStatement(stmt: IrStatement): List<BrsStatement> {
            genCtx.pushHoistedScope()
            val transformed = when (stmt) {
                is IrWhen -> visitWhen(stmt, data) // Transform as statement (if-then-else)
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
            val hoisted = genCtx.popHoistedScope()
            return if (transformed != null) hoisted + transformed else hoisted
        }

        fun transformArmBlock(arm: IrExpression): BrsBlock = when (arm) {
            is IrBlock -> BrsBlock(arm.statements.flatMap { transformBlockStatement(it) }.toMutableList())
            else -> BrsBlock(transformBlockStatement(arm).toMutableList())
        }

        return if (context.supportsExceptions) {
            // Transform try block - tryResult is an IrExpression (often IrBlock)
            val tryBlock = transformArmBlock(aTry.tryResult)

            // Transform catch block
            val catchBlock = aTry.catches.firstOrNull()?.let { transformArmBlock(it.result) }
            val catchVar = aTry.catches.firstOrNull()?.catchParameter?.name?.asString()

            BrsTry(tryBlock, catchVar, catchBlock)
        } else {
            // Without exception support, just execute the try block
            transformArmBlock(aTry.tryResult)
        }
    }

    override fun visitWhen(expression: IrWhen, data: Unit): BrsStatement {
        // Collect statements that must precede the if chain (e.g., variable declarations
        // from ELVIS blocks that are referenced in conditions)
        val precedingStatements = mutableListOf<BrsStatement>()

        // Transform to if/else chain
        var result: BrsIf? = null
        var current: BrsIf? = null

        for (branch in expression.branches) {
            val condition = parent.transformExpression(branch.condition)

            // CRITICAL: Consume hoisted statements from condition transformation.
            // This handles cases like elvis operator where the condition references
            // a variable that was declared in an enclosing ELVIS block, and
            // short-circuit guards hoisted from impure && / || conditions.
            // Kotlin evaluates a branch condition only after all earlier branch
            // conditions were false: the FIRST branch's hoists are unconditional
            // and may precede the chain, but a later branch's hoists must run
            // inside the preceding branch's else (nested below), never before
            // the chain.
            val conditionHoisted = genCtx.takeHoistedStatements()

            // Handle branch result - certain IR nodes need statement transformation
            val bodyStatement: BrsStatement = when (val branchResult = branch.result) {
                is IrReturn -> parent.transformStatement(branchResult) ?: BrsEmpty()
                is IrThrow -> visitThrow(branchResult, data)
                is IrBreak -> visitBreak(branchResult, data)
                is IrContinue -> visitContinue(branchResult, data)
                is IrSetValue -> visitSetValue(branchResult, data)
                is IrSetField -> visitSetField(branchResult, data)
                is IrBlock -> {
                    // Transform block which may contain returns
                    val transformed = transformBlockOrStatement(branchResult)
                    transformed
                }
                is IrComposite -> {
                    // Transform composite (used in coroutine state machine branches)
                    val transformed = transformBlockOrStatement(branchResult)
                    transformed
                }
                // IrTypeOperatorCall with IMPLICIT_COERCION_TO_UNIT wraps expressions used as statements
                // Expression fallbacks go through expressionStatementWithHoisted: a branch
                // result whose argument transformation hoists statements (safe-call/elvis
                // machinery) must keep those hoists INSIDE the branch body, before the call —
                // otherwise they leak past the whole if and run unconditionally, and the
                // branch body reads the machinery's temp before it is computed (task 6.6,
                // brace-less `if (flag) note("cond", s?.length ?: -3)` shape).
                is IrTypeOperatorCall -> {
                    if (branchResult.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                        val innerArg = branchResult.argument
                        when (innerArg) {
                            is IrBlock -> transformBlockOrStatement(innerArg)
                            is IrCall -> expressionStatementWithHoisted(innerArg)
                            else -> expressionStatementWithHoisted(branchResult)
                        }
                    } else {
                        expressionStatementWithHoisted(branchResult)
                    }
                }
                else -> {
                    // Null constants in statement context should produce no code.
                    // This handles safe-call null branches: a?.foo() where null branch does nothing.
                    if (branchResult is IrConst && (branchResult as IrConst).value == null) {
                        BrsEmpty()
                    } else {
                        expressionStatementWithHoisted(branchResult)
                    }
                }
            }

            val ifStmt = BrsIf(
                condition = condition,
                thenBranch = bodyStatement
            )

            if (result == null) {
                precedingStatements.addAll(conditionHoisted)
                result = ifStmt
                current = ifStmt
            } else {
                current?.elseBranch = if (conditionHoisted.isEmpty()) {
                    ifStmt
                } else {
                    BrsBlock((conditionHoisted + ifStmt).toMutableList())
                }
                current = ifStmt
            }
        }

        val ifChain = result ?: BrsEmpty()

        // If we have preceding statements (e.g., variable declarations from ELVIS blocks),
        // wrap the if chain in a block so they're emitted first
        return if (precedingStatements.isNotEmpty()) {
            precedingStatements.add(ifChain)
            BrsBlock(precedingStatements.toMutableList())
        } else {
            ifChain
        }
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsStatement {
        val condition = parent.transformExpression(loop.condition)
        // Take hoisted statements from condition transformation (e.g., from inlined returnable blocks)
        val conditionHoisted = genCtx.takeHoistedStatements()
        val bodyContainsContinue = containsContinueFor(loop.body, loop)

        // The continue wrapper is needed when the target OS lacks native
        // continue, and ALSO when the condition produced hoisted statements:
        // native `continue while` would jump past the loop-tail re-evaluation
        // of the hoisted condition, resuming with a stale temp. The wrapper
        // turns continue into `exit while` on the inner loop, which falls
        // through to the re-evaluation below.
        if (bodyContainsContinue && (!context.supportsContinue || conditionHoisted.isNotEmpty())) {
            // Wrap body in inner while(true) loop to simulate continue.
            // continue becomes "exit while" which exits the inner loop, and the outer loop continues.
            // break needs special handling: set a flag, exit inner, check flag after inner loop.
            val bodyContainsBreak = containsBreakFor(loop.body, loop)
            val breakFlagName = if (bodyContainsBreak) "__break${genCtx.nextTempId()}" else null

            // Register this loop as having a continue wrapper
            genCtx.loopsWithContinueWrapper.add(loop)
            if (breakFlagName != null) {
                genCtx.loopBreakFlags[loop] = breakFlagName
            }

            // Push this loop onto the stack BEFORE transforming its body.
            // This ensures that only IrContinue/IrBreak that are encountered
            // during the body transformation will be converted to exit while.
            genCtx.continueWrapperLoopStack.add(loop)

            try {
                // Transform the body (continue/break will be handled by visitContinue/visitBreak)
                val innerBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()

                // Ensure inner loop always exits at the end (normal flow)
                val innerBodyStatements = when (innerBody) {
                    is BrsBlock -> innerBody.statements.toMutableList()
                    else -> mutableListOf(innerBody)
                }
                // Add exit while at end if not already terminating
                if (innerBodyStatements.isEmpty() || !isTerminating(innerBodyStatements.last())) {
                    innerBodyStatements.add(BrsExit(BrsExitKind.WHILE))
                }

                val innerLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(innerBodyStatements)
                )

                // Build outer loop body
                val outerBodyStatements = mutableListOf<BrsStatement>()
                if (breakFlagName != null) {
                    // Initialize break flag to false at start of each iteration
                    outerBodyStatements.add(BrsVariable(breakFlagName, null, BrsBooleanLiteral(false)))
                }
                outerBodyStatements.add(innerLoop)
                if (breakFlagName != null) {
                    // Check break flag after inner loop
                    outerBodyStatements.add(
                        BrsIf(
                            condition = BrsIdentifier(breakFlagName),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                }

                // Handle hoisted statements from complex while conditions
                if (conditionHoisted.isNotEmpty()) {
                    // Add condition check at start and re-evaluation at end
                    val fullBodyStatements = mutableListOf<BrsStatement>()
                    // Condition check
                    fullBodyStatements.add(
                        BrsIf(
                            condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                    fullBodyStatements.addAll(outerBodyStatements)
                    // Re-evaluate condition
                    fullBodyStatements.addAll(conditionHoisted)

                    val whileLoop = BrsWhile(
                        condition = BrsBooleanLiteral(true),
                        body = BrsBlock(fullBodyStatements)
                    )
                    return BrsBlock((conditionHoisted + whileLoop).toMutableList())
                }

                return BrsWhile(
                    condition = condition,
                    body = BrsBlock(outerBodyStatements)
                )
            } finally {
                // Clean up tracking - remove from stack and sets
                genCtx.continueWrapperLoopStack.remove(loop)
                genCtx.loopsWithContinueWrapper.remove(loop)
                if (breakFlagName != null) {
                    genCtx.loopBreakFlags.remove(loop)
                }
            }
        } else {
            // Handle hoisted statements from complex while conditions (e.g., inlined returnable blocks)
            // For conditions like `while (queue.isNotEmpty())` where isNotEmpty is an inlined function,
            // the condition transformation produces hoisted statements that set up a temporary variable.
            // We need to:
            // 1. Run the hoisted statements before the loop (initial condition evaluation)
            // 2. Check the condition at the start of the loop
            // 3. Run the hoisted statements at the end of the loop body (re-evaluate for next iteration)
            if (conditionHoisted.isNotEmpty()) {
                val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
                val bodyStatements = when (body) {
                    is BrsBlock -> body.statements.toMutableList()
                    else -> mutableListOf(body)
                }

                // Build: while true { if not condition then exit while end if; body; hoisted }
                val loopBody = mutableListOf<BrsStatement>()
                // Condition check at start of loop
                loopBody.add(
                    BrsIf(
                        condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                        thenBranch = BrsExit(BrsExitKind.WHILE),
                        elseBranch = null
                    )
                )
                // Original body
                loopBody.addAll(bodyStatements)
                // Re-evaluate condition for next iteration
                loopBody.addAll(conditionHoisted)

                val whileLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(loopBody)
                )

                // Return: hoisted statements + while loop
                return BrsBlock((conditionHoisted + whileLoop).toMutableList())
            }

            return BrsWhile(
                condition = condition,
                body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
            )
        }
    }

    /**
     * Checks if a statement is terminating (return, exit, throw, etc.)
     */
    private fun isTerminating(stmt: BrsStatement): Boolean {
        return when (stmt) {
            is BrsReturn -> true
            is BrsExit -> true
            is BrsThrow -> true
            is BrsContinue -> true
            is BrsBlock -> stmt.statements.isNotEmpty() && isTerminating(stmt.statements.last())
            else -> false
        }
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsStatement {
        // Coroutine root loops have special handling - use simple transformation
        // The loop is structured as: do { try { state machine } catch { ... } } while(true)
        // Continue statements become "exit while" (handled in visitContinue)
        // which exits the try block and re-enters the while loop from the top.
        if (loop.origin == BrsStatementOrigins.COROUTINE_ROOT_LOOP) {
            val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
            val condition = parent.transformExpression(loop.condition)
            // do { body } while(true) → while(true) { body }
            // For coroutines, condition is always true, so we just use a while(true)
            return BrsWhile(condition, body)
        }

        // BrightScript doesn't have do-while, transform to while with entry guard
        // First execution runs unconditionally, then subsequent iterations check condition
        val bodyContainsContinue = containsContinueFor(loop.body, loop)

        // Transform the body FIRST: do-while is the one loop whose condition can
        // legally reference variables declared in the body, and reads resolve
        // through getVariableName, which only knows a variable's (possibly
        // collision-renamed) unique name once its declaration was transformed.
        // This transform doubles as the first (unconditional) execution both
        // paths below need; the wrapper path re-transforms the body for the
        // loop iterations. Hoisted statements the body leaves pending belong to
        // the enclosing consumer, not the condition — keep them separate.
        val firstBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
        val bodyPending = genCtx.takeHoistedStatements()

        // Consume the condition's hoisted statements (elvis temps, short-circuit
        // guards) — previously they leaked into the enclosing hoist queue — and
        // let them steer the wrapper routing below, mirroring visitWhileLoop.
        val condition = parent.transformExpression(loop.condition)
        val conditionHoisted = genCtx.takeHoistedStatements()
        bodyPending.forEach { genCtx.addHoistedStatement(it) }

        if (bodyContainsContinue && (!context.supportsContinue || conditionHoisted.isNotEmpty())) {
            // Similar to while loop, but do-while executes body first, then checks condition
            // We transform to: body; while(condition) { body }
            // But with continue wrapper for the while part
            val bodyContainsBreak = containsBreakFor(loop.body, loop)
            val breakFlagName = if (bodyContainsBreak) "__break${genCtx.nextTempId()}" else null

            // Register this loop as having a continue wrapper
            genCtx.loopsWithContinueWrapper.add(loop)
            if (breakFlagName != null) {
                genCtx.loopBreakFlags[loop] = breakFlagName
            }

            // firstBody (transformed above, before the stack push) is the first
            // (unconditional) execution: it is NOT inside the loop, so its
            // continues were deliberately not converted to exit while.
            // Now push to the stack for the while loop body transformation.
            genCtx.continueWrapperLoopStack.add(loop)

            try {
                // Transform the body again for the while loop (with continue wrapper)
                val innerBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()

                // Ensure inner loop always exits at the end
                val innerBodyStatements = when (innerBody) {
                    is BrsBlock -> innerBody.statements.toMutableList()
                    else -> mutableListOf(innerBody)
                }
                if (innerBodyStatements.isEmpty() || !isTerminating(innerBodyStatements.last())) {
                    innerBodyStatements.add(BrsExit(BrsExitKind.WHILE))
                }

                val innerLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(innerBodyStatements)
                )

                // Build while loop body
                val whileBodyStatements = mutableListOf<BrsStatement>()
                if (breakFlagName != null) {
                    whileBodyStatements.add(BrsVariable(breakFlagName, null, BrsBooleanLiteral(false)))
                }
                whileBodyStatements.add(innerLoop)
                if (breakFlagName != null) {
                    whileBodyStatements.add(
                        BrsIf(
                            condition = BrsIdentifier(breakFlagName),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                }

                if (conditionHoisted.isNotEmpty()) {
                    // do-while checks the condition AFTER the body: evaluate the
                    // hoisted guard after the first unconditional execution and
                    // re-evaluate at the end of every iteration. continue (exit
                    // while on the inner loop) falls through to the re-evaluation.
                    val fullBodyStatements = mutableListOf<BrsStatement>()
                    fullBodyStatements.add(
                        BrsIf(
                            condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                    fullBodyStatements.addAll(whileBodyStatements)
                    fullBodyStatements.addAll(conditionHoisted)

                    val whileLoop = BrsWhile(
                        condition = BrsBooleanLiteral(true),
                        body = BrsBlock(fullBodyStatements)
                    )
                    return BrsBlock((listOf(firstBody) + conditionHoisted + whileLoop).toMutableList())
                }

                val whileLoop = BrsWhile(
                    condition = condition,
                    body = BrsBlock(whileBodyStatements)
                )

                // For do-while with break in first body execution, we need similar handling
                // But it's tricky since there's no outer loop. For now, handle simple case.
                return BrsBlock(mutableListOf(firstBody, whileLoop))
            } finally {
                // Clean up tracking - remove from stack and sets
                genCtx.continueWrapperLoopStack.remove(loop)
                genCtx.loopsWithContinueWrapper.remove(loop)
                if (breakFlagName != null) {
                    genCtx.loopBreakFlags.remove(loop)
                }
            }
        } else {
            // Original transformation without continue wrapper; the single body
            // transform above is the unconditional first execution.
            val body = firstBody

            if (conditionHoisted.isNotEmpty()) {
                // do-while checks the condition AFTER the body — run the hoisted
                // guard right after the body, then test. No body duplication.
                // continue never reaches here: hoisted + continue routes through
                // the wrapper path above.
                val bodyStatements = when (body) {
                    is BrsBlock -> body.statements.toMutableList()
                    else -> mutableListOf<BrsStatement>(body)
                }
                bodyStatements.addAll(conditionHoisted)
                bodyStatements.add(
                    BrsIf(
                        condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                        thenBranch = BrsExit(BrsExitKind.WHILE),
                        elseBranch = null
                    )
                )
                return BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(bodyStatements)
                )
            }

            return BrsBlock(mutableListOf(
                body,
                BrsWhile(condition, body.deepCopy())
            ))
        }
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsStatement {
        // Check if this break targets a loop with a continue wrapper (simulated continue)
        // In this case, we need to set the break flag and exit the inner wrapper loop.
        // The outer loop will check the flag and exit.
        // We check the stack to ensure we're inside the loop body transformation.
        val breakFlagName = genCtx.loopBreakFlags[jump.loop]
        if (breakFlagName != null && genCtx.continueWrapperLoopStack.contains(jump.loop)) {
            // Set break flag to true, then exit the inner while loop
            return BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier(breakFlagName),
                        BrsBinaryOperator.EQ,
                        BrsBooleanLiteral(true)
                    )
                ),
                BrsExit(BrsExitKind.WHILE)
            ))
        }

        // Check if the loop this break references was transformed to a while loop
        // This happens when a for-each loop uses Strategy 4 (iterator protocol),
        // which generates a while loop instead of a native for-each
        // First check: are we inside a FOR_LOOP that's being transformed to while?
        // This handles cases where inlining creates new loop instances that we can't track by identity.
        if (genCtx.forLoopToWhileNestingDepth > 0 &&
            (jump.loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE ||
             jump.loop.origin == IrStatementOrigin.FOR_LOOP)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // Second check: is this specific loop instance in our tracking set?
        if (genCtx.loopsTransformedToWhile.contains(jump.loop)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // Determine exit kind based on the loop type
        // FOR_LOOP origin indicates a for or for-each loop
        val exitKind = when (jump.loop.origin) {
            IrStatementOrigin.FOR_LOOP,
            IrStatementOrigin.FOR_LOOP_INNER_WHILE -> BrsExitKind.FOR
            else -> BrsExitKind.WHILE
        }
        return BrsExit(exitKind)
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsStatement {
        // Coroutine root loops use while(true) with try/catch inside.
        // The state machine uses continue to re-enter the loop from any state.
        // When a suspend call returns immediately (doesn't suspend), we need to
        // continue the loop to re-read the state and execute the next state.
        // This requires "continue while" to restart the loop iteration.
        if (jump.loop.origin == BrsStatementOrigins.COROUTINE_ROOT_LOOP) {
            return BrsContinue(BrsContinueKind.WHILE)
        }

        // Check if this continue targets a loop with a continue wrapper
        // In this case, emit "exit while" to exit the inner wrapper loop,
        // which allows the outer loop to continue naturally.
        // We check the stack (not just the set) to ensure we're actually inside
        // the loop body transformation. This is important for coroutines where
        // IrContinue statements may exist both inside and outside the loop.
        if (genCtx.continueWrapperLoopStack.contains(jump.loop)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // First check: are we inside a FOR_LOOP that's being transformed to while?
        if (genCtx.forLoopToWhileNestingDepth > 0 &&
            (jump.loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE ||
             jump.loop.origin == IrStatementOrigin.FOR_LOOP)) {
            return if (context.supportsContinue) {
                BrsContinue(BrsContinueKind.WHILE)
            } else {
                BrsComment("continue not supported on target Roku OS")
            }
        }

        // Second check: is this specific loop instance in our tracking set?
        if (genCtx.loopsTransformedToWhile.contains(jump.loop)) {
            return if (context.supportsContinue) {
                BrsContinue(BrsContinueKind.WHILE)
            } else {
                BrsComment("continue not supported on target Roku OS")
            }
        }

        // Determine continue kind based on the loop type
        val continueKind = when (jump.loop.origin) {
            IrStatementOrigin.FOR_LOOP,
            IrStatementOrigin.FOR_LOOP_INNER_WHILE -> BrsContinueKind.FOR
            else -> BrsContinueKind.WHILE
        }
        return if (context.supportsContinue) {
            BrsContinue(continueKind)
        } else {
            // For older Roku OS, we'd need to restructure the loop
            BrsComment("continue not supported on target Roku OS")
        }
    }

    override fun visitBlockBody(body: IrBlockBody, data: Unit): BrsStatement {
        return parent.transformBody(body)
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsStatement {
        // Check if this is an IrReturnableBlock (from inlined functions)
        // After BrsReturnableBlockLowering, the block contains:
        // - Statements that may include "return@block Unit" (which becomes exit while)
        // We wrap this in "while true { ... }" so exit while can break out of it.
        if (expression is IrReturnableBlock) {
            return transformReturnableBlock(expression)
        }

        // Check if this is a FOR loop that was lowered to a while loop
        val isForLoop = expression.origin == IrStatementOrigin.FOR_LOOP
        if (isForLoop) {
            val forLoopResult = tryTransformForLoop(expression)
            if (forLoopResult != null) {
                return forLoopResult
            }
            // If tryTransformForLoop returns null, fall through to general processing.
            // This can happen after inlining changes the block structure.
            // Register any inner while loops so that visitBreak generates "exit while" instead of "exit for".
            // We need to do this BEFORE general processing transforms the body.
            // Recursively find ALL while loops in the block, including those nested inside
            // when expressions, type operators, etc.
            fun registerInnerLoops(element: IrElement) {
                when (element) {
                    is IrWhileLoop -> {
                        // This while loop was originally a for-loop's inner while
                        // Any break statements targeting it should use "exit while"
                        genCtx.loopsTransformedToWhile.add(element)
                        // Also recurse into the loop body
                        element.body?.let { registerInnerLoops(it) }
                    }
                    is IrBlock -> element.statements.forEach { registerInnerLoops(it) }
                    is IrContainerExpression -> element.statements.forEach { registerInnerLoops(it) }
                    is IrWhen -> element.branches.forEach { branch ->
                        registerInnerLoops(branch.condition)
                        registerInnerLoops(branch.result)
                    }
                    is IrTypeOperatorCall -> registerInnerLoops(element.argument)
                    is IrReturn -> registerInnerLoops(element.value)
                    is IrSetValue -> registerInnerLoops(element.value)
                    is IrSetField -> registerInnerLoops(element.value)
                    is IrVariable -> element.initializer?.let { registerInnerLoops(it) }
                    is IrCall -> {
                        element.dispatchReceiver?.let { registerInnerLoops(it) }
                        element.extensionReceiver?.let { registerInnerLoops(it) }
                        for (i in 0 until element.valueArgumentsCount) {
                            element.getValueArgument(i)?.let { registerInnerLoops(it) }
                        }
                    }
                }
            }
            expression.statements.forEach { registerInnerLoops(it) }
        }

        // Check if this is an increment/decrement block
        // These have structure: { val <unary> = old; var = <unary>.inc(); <unary> }
        // When used as statement, we can simplify to just the assignment
        if (expression.origin == IrStatementOrigin.POSTFIX_INCR ||
            expression.origin == IrStatementOrigin.POSTFIX_DECR ||
            expression.origin == IrStatementOrigin.PREFIX_INCR ||
            expression.origin == IrStatementOrigin.PREFIX_DECR) {
            val result = transformIncrementDecrementBlock(expression)
            if (result != null) {
                return result
            }
        }

        // Check for when-lowered blocks in statement context
        // Structure: { var __when_tmp; if/when { ... -> __when_tmp = ... }; __when_tmp }
        // Or: { var __when_tmp; if/when { ... -> __when_tmp = ... }; var result = __when_tmp }
        // We need to properly flatten these into their component statements
        val blockStatements = expression.statements
        if (blockStatements.size >= 3) {
            val firstStmt = blockStatements.firstOrNull()
            val lastStmt = blockStatements.lastOrNull()

            // Detect when-lowered block by checking for __when_tmp variable
            val firstIsWhenTmp = firstStmt is IrVariable &&
                firstStmt.name.asString().startsWith("__when_tmp")

            // Last can be either:
            // 1. IrGetValue(__when_tmp) - when used as expression
            // 2. IrVariable whose initializer is IrGetValue(__when_tmp) - when result is assigned to a variable
            val lastIsWhenTmpRef = lastStmt is IrGetValue &&
                lastStmt.symbol.owner.name.asString().startsWith("__when_tmp")
            val lastIsVarWithWhenTmpInit = lastStmt is IrVariable &&
                (lastStmt.initializer as? IrGetValue)?.symbol?.owner?.name?.asString()?.startsWith("__when_tmp") == true

            val isWhenLoweredBlock = firstIsWhenTmp && (lastIsWhenTmpRef || lastIsVarWithWhenTmpInit)

            if (isWhenLoweredBlock) {
                // Transform all statements
                // - Skip the last if it's just IrGetValue(__when_tmp) (return value, not needed)
                // - Include the last if it's IrVariable (the result variable assignment)
                //
                // We push a new hoisting scope so nested transformations don't interfere with
                // outer scopes. This block directly returns its statements (not hoisted).
                genCtx.pushHoistedScope()
                val resultStatements = mutableListOf<BrsStatement>()
                val lastIndex = if (lastIsWhenTmpRef) blockStatements.size - 1 else blockStatements.size

                for (i in 0 until lastIndex) {
                    val stmt = blockStatements[i]
                    val transformed: BrsStatement? = when (stmt) {
                        is IrVariable -> {
                            val init = stmt.initializer?.let { parent.transformExpression(it) }
                            // Consume any hoisted statements from nested when-lowered blocks in the initializer
                            val hoisted = genCtx.takeHoistedStatements()
                            resultStatements.addAll(hoisted)
                            BrsVariable(stmt.name.asString(), mapTypeToBrs(stmt.type), init)
                        }
                        is IrWhen -> {
                            val whenStmt = visitWhen(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            whenStmt
                        }
                        is IrWhileLoop -> {
                            val loopStmt = visitWhileLoop(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            loopStmt
                        }
                        is IrDoWhileLoop -> {
                            val loopStmt = visitDoWhileLoop(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            loopStmt
                        }
                        is IrBlock -> {
                            val blockStmt = visitBlock(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            blockStmt
                        }
                        is IrSetValue -> {
                            val setStmt = visitSetValue(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            setStmt
                        }
                        is IrSetField -> {
                            val setStmt = visitSetField(stmt, Unit)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            setStmt
                        }
                        else -> {
                            val transformed = parent.transformStatement(stmt)
                            resultStatements.addAll(genCtx.takeHoistedStatements())
                            transformed
                        }
                    }
                    if (transformed != null) {
                        resultStatements.add(transformed)
                    }
                }

                // Pop our scope (should be empty now) and discard
                genCtx.popHoistedScope()

                return BrsBlock(resultStatements.toMutableList())
            }
        }

        val statements = expression.statements.flatMap { stmt ->
            // Helper to consume and prepend hoisted statements
            fun prependHoisted(stmts: List<BrsStatement>): List<BrsStatement> {
                val hoisted = genCtx.takeHoistedStatements()
                return if (hoisted.isNotEmpty()) hoisted + stmts else stmts
            }

            when (stmt) {
                // Route IrReturn to statement transformer (has visitReturn handler)
                // IrReturn extends IrExpression but needs statement-level handling
                is IrReturn -> {
                    val transformed = parent.transformStatement(stmt)
                    prependHoisted(listOfNotNull(transformed))
                }
                // These are handled during constructor/enum transformation, skip here
                is IrDelegatingConstructorCall -> emptyList()
                is IrInstanceInitializerCall -> emptyList()
                is IrEnumConstructorCall -> emptyList()
                // Skip temp variable REFERENCES (IrGetValue) from increment/decrement blocks
                // and when-lowering blocks. These are return values that shouldn't be statements.
                is IrGetValue -> {
                    val varName = stmt.symbol.owner.name.asString()
                    if (varName.startsWith("<") && varName.endsWith(">")) {
                        emptyList()  // Skip temp variable returns like <unary>
                    } else if (varName.startsWith("__when_tmp")) {
                        emptyList()  // Skip when-lowering temp variable returns
                    } else {
                        val expr = parent.transformExpression(stmt)
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                // Keep temp variable DECLARATIONS - they're needed by the setter call
                is IrVariable -> {
                    val varName = stmt.name.asString()
                    // Sanitize the name for BrightScript - handle special names and escape reserved keywords
                    val sanitizedName = sanitizeParameterName(varName)
                    val init = stmt.initializer?.let { parent.transformExpression(it) }
                    // Check for hoisted statements from when-lowered blocks in the initializer
                    val hoisted = genCtx.takeHoistedStatements()
                    // Shared (closure-captured mutable) variables live in a {value: ...} box
                    // created at their declaration - same logic as visitVariable
                    val isShared = stmt.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER ||
                                   stmt.symbol in genCtx.sharedVariables
                    val finalInit = if (isShared) {
                        BrsAALiteral(mutableListOf(BrsAAEntry("value", init ?: BrsInvalidLiteral())))
                    } else {
                        init ?: BrsInvalidLiteral()
                    }
                    // Always create the variable declaration (use invalid for uninitialized vars)
                    val varDecl = BrsVariable(sanitizedName, mapTypeToBrs(stmt.type), finalInit)
                    if (hoisted.isNotEmpty()) {
                        // Prepend hoisted statements before the variable declaration
                        hoisted + varDecl
                    } else {
                        listOf(varDecl)
                    }
                }
                // For nested blocks/composites, flatten them to extract all statements
                // This is important for postfix increment/decrement which use blocks
                is IrBlock -> {
                    val nested = visitBlock(stmt, Unit)
                    val stmts = if (nested is BrsBlock) nested.statements else listOf(nested)
                    prependHoisted(stmts)
                }
                is IrComposite -> {
                    // Process each statement in the composite
                    // In statement context, the last element might be a result value that should be discarded
                    val compositeStmts = stmt.statements.flatMapIndexed { index, nestedStmt ->
                        val isLast = index == stmt.statements.lastIndex
                        when (nestedStmt) {
                            is IrBlock -> {
                                val nested = visitBlock(nestedStmt, Unit)
                                if (nested is BrsBlock) nested.statements else listOf(nested)
                            }
                            // Loops are expressions (extend IrExpression) but should be transformed as statements
                            is IrWhileLoop -> listOf(visitWhileLoop(nestedStmt, Unit))
                            is IrDoWhileLoop -> listOf(visitDoWhileLoop(nestedStmt, Unit))
                            // IrWhen (if/when) should be transformed as statements
                            is IrWhen -> listOf(visitWhen(nestedStmt, Unit))
                            // Control flow must be handled as statements, not expressions
                            is IrThrow -> listOf(visitThrow(nestedStmt, Unit))
                            is IrBreak -> listOf(visitBreak(nestedStmt, Unit))
                            is IrContinue -> listOf(visitContinue(nestedStmt, Unit))
                            // IrReturn needs special handling - use statement transformer which has visitReturn
                            is IrReturn -> listOfNotNull(parent.transformStatement(nestedStmt))
                            // Skip pure value reads at the end (result values discarded in statement context)
                            is IrGetValue -> if (isLast) emptyList() else listOf(BrsExpressionStatement(parent.transformExpression(nestedStmt)))
                            // IrSetValue needs statement visitor to handle hoisting from nested composites properly
                            is IrSetValue -> {
                                val transformed = visitSetValue(nestedStmt, Unit)
                                if (transformed is BrsBlock) transformed.statements else listOf(transformed)
                            }
                            is IrExpression -> listOf(BrsExpressionStatement(parent.transformExpression(nestedStmt)))
                            else -> listOfNotNull(parent.transformStatement(nestedStmt))
                        }
                    }
                    prependHoisted(compositeStmts)
                }
                // Loops are expressions (extend IrExpression) but should be transformed as statements
                is IrWhileLoop -> {
                    val transformed = visitWhileLoop(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrDoWhileLoop -> {
                    val transformed = visitDoWhileLoop(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // IrWhen (if/when) with Unit type should be transformed as statements, not expressions
                is IrWhen -> {
                    val transformed = visitWhen(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // Control flow must be handled as statements, not expressions
                is IrThrow -> {
                    val transformed = visitThrow(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrBreak -> {
                    val transformed = visitBreak(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrContinue -> {
                    val transformed = visitContinue(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // Handle type operator calls (like IMPLICIT_COERCION_TO_UNIT wrapping increment blocks)
                is IrTypeOperatorCall -> {
                    val transformed = visitTypeOperator(stmt, Unit)
                    if (transformed != null) {
                        prependHoisted(listOf(transformed))
                    } else {
                        // Fallback to expression transformer
                        val expr = parent.transformExpression(stmt)
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                is IrExpression -> {
                    val expr = parent.transformExpression(stmt)
                    // Skip BrsInvalidLiteral - these come from empty IrComposite left behind
                    // when LocalDeclarationsLowering hoists local functions
                    if (expr is BrsInvalidLiteral) {
                        emptyList()
                    } else {
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                else -> {
                    val transformed = parent.transformStatement(stmt)
                    prependHoisted(listOfNotNull(transformed))
                }
            }
        }
        // Consume any remaining hoisted statements and prepend them to the block
        val remainingHoisted = genCtx.takeHoistedStatements()
        val finalStatements = if (remainingHoisted.isNotEmpty()) {
            remainingHoisted + statements
        } else {
            statements
        }

        // If this was a FOR_LOOP that fell through to general processing,
        // convert any "exit for" to "exit while" since the loop may have been
        // transformed to a while loop during inlining
        val convertedStatements = if (isForLoop) {
            convertForControlToWhile(finalStatements)
        } else {
            finalStatements
        }

        return BrsBlock(convertedStatements.toMutableList())
    }

    /**
     * Check if any returns within the block target this returnable block.
     * After BrsReturnableBlockLowering, these will be "return@block Unit" statements.
     */
    private fun hasReturnsTargetingBlock(block: IrReturnableBlock): Boolean {
        var hasReturns = false
        block.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!hasReturns) {
                    element.acceptChildrenVoid(this)
                }
            }

            override fun visitReturn(expression: IrReturn) {
                if (expression.returnTargetSymbol == block.symbol) {
                    hasReturns = true
                }
                // Don't recurse into nested returns
            }
        })
        return hasReturns
    }

    /**
     * Check if any returns targeting this block are inside a while loop.
     * This is critical because BrightScript's "exit while" only exits the innermost
     * while loop, so if a return@block is inside a while loop, we need to use a
     * flag-based approach instead of just "exit while".
     */
    private fun hasReturnsInsideWhileLoop(block: IrReturnableBlock): Boolean {
        var hasReturnsInWhile = false
        var insideWhileLoop = 0

        block.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!hasReturnsInWhile) {
                    element.acceptChildrenVoid(this)
                }
            }

            override fun visitWhileLoop(loop: IrWhileLoop) {
                insideWhileLoop++
                loop.acceptChildrenVoid(this)
                insideWhileLoop--
            }

            override fun visitDoWhileLoop(loop: IrDoWhileLoop) {
                insideWhileLoop++
                loop.acceptChildrenVoid(this)
                insideWhileLoop--
            }

            override fun visitReturn(expression: IrReturn) {
                if (expression.returnTargetSymbol == block.symbol && insideWhileLoop > 0) {
                    hasReturnsInWhile = true
                }
            }
        })
        return hasReturnsInWhile
    }

    /**
     * Check if a BrsStatement contains a BrsWhile loop (at any nesting level).
     * This is used to determine if we need to insert flag checks after a statement
     * in the flag-based returnable block approach.
     */
    private fun containsBrsWhileLoop(stmt: BrsStatement): Boolean {
        return when (stmt) {
            is BrsWhile -> true
            is BrsBlock -> stmt.statements.any { containsBrsWhileLoop(it) }
            is BrsIf -> {
                containsBrsWhileLoop(stmt.thenBranch) ||
                (stmt.elseBranch?.let { containsBrsWhileLoop(it) } ?: false)
            }
            is BrsFor -> containsBrsWhileLoop(stmt.body)
            is BrsForEach -> containsBrsWhileLoop(stmt.body)
            is BrsTry -> {
                containsBrsWhileLoop(stmt.tryBlock) ||
                (stmt.catchBlock?.let { containsBrsWhileLoop(it) } ?: false)
            }
            else -> false
        }
    }

    /**
     * Insert flag checks after any BrsWhile statements found at any nesting level.
     * This post-processes a BrsStatement to add "if flagName then exit while" after
     * each while loop, ensuring early exit propagates correctly through nested loops.
     *
     * @param stmt The statement to process
     * @param flagName The flag variable name to check
     * @return A new statement with flag checks inserted
     */
    private fun insertFlagChecksAfterWhileLoops(stmt: BrsStatement, flagName: String): BrsStatement {
        return when (stmt) {
            is BrsBlock -> {
                val newStatements = mutableListOf<BrsStatement>()
                for (s in stmt.statements) {
                    val processed = insertFlagChecksAfterWhileLoops(s, flagName)
                    newStatements.add(processed)
                    // Add flag check after while loops
                    if (processed is BrsWhile) {
                        newStatements.add(
                            BrsIf(
                                condition = BrsIdentifier(flagName),
                                thenBranch = BrsExit(BrsExitKind.WHILE),
                                elseBranch = null
                            )
                        )
                    }
                }
                BrsBlock(newStatements)
            }
            is BrsIf -> BrsIf(
                condition = stmt.condition,
                thenBranch = insertFlagChecksAfterWhileLoops(stmt.thenBranch, flagName),
                elseBranch = stmt.elseBranch?.let { insertFlagChecksAfterWhileLoops(it, flagName) }
            )
            is BrsFor -> BrsFor(
                variable = stmt.variable,
                start = stmt.start,
                end = stmt.end,
                step = stmt.step,
                body = insertFlagChecksAfterWhileLoops(stmt.body, flagName)
            )
            is BrsForEach -> BrsForEach(
                variable = stmt.variable,
                iterable = stmt.iterable,
                body = insertFlagChecksAfterWhileLoops(stmt.body, flagName)
            )
            is BrsTry -> BrsTry(
                tryBlock = insertFlagChecksAfterWhileLoops(stmt.tryBlock, flagName),
                catchVariable = stmt.catchVariable,
                catchBlock = stmt.catchBlock?.let { insertFlagChecksAfterWhileLoops(it, flagName) }
            )
            is BrsWhile -> {
                // Don't recurse into while loop body - we only care about while loops
                // at the returnable block level, not nested while loops inside those
                stmt
            }
            else -> stmt
        }
    }

    /**
     * Transform a returnable block (from inlined functions).
     *
     * After BrsReturnableBlockLowering, the returnable block contains:
     * - Statements that may include "return@block Unit" (converted to exit while)
     * - The actual return value has been hoisted to a result variable by the lowering
     *
     * We wrap this in "while true { ... exit while }" so that return@block Unit
     * (which becomes "exit while") can break out of the block.
     *
     * IMPORTANT: If there are NO returns targeting this block, we don't need the
     * while wrapper at all - just emit the statements directly. This avoids
     * generating nested while loops for cases like `let { return it }` where
     * the return is a function return, not a block return.
     *
     * CRITICAL: When returns are inside while loops, we use a flag-based approach
     * because BrightScript's "exit while" only exits the innermost loop.
     * The flag approach: set __done = true; exit while, then after each inner
     * while loop check: if __done then exit while.
     */
    private fun transformReturnableBlock(block: IrReturnableBlock): BrsStatement {
        // Check if any returns actually target this block
        // If not, we don't need the while wrapper - just emit statements directly
        val needsWhileWrapper = hasReturnsTargetingBlock(block)

        if (!needsWhileWrapper) {
            // No returns target this block - just transform statements directly
            val bodyStatements = mutableListOf<BrsStatement>()
            for (stmt in block.statements) {
                val transformed = when (stmt) {
                    is IrExpression -> {
                        genCtx.pushHoistedScope()
                        val result = when (stmt) {
                            is IrWhen -> visitWhen(stmt, Unit)
                            is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                            is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                            is IrBlock -> visitBlock(stmt, Unit)
                            is IrReturn -> visitReturn(stmt, Unit)
                            else -> BrsExpressionStatement(parent.transformExpression(stmt))
                        }
                        val hoisted = genCtx.popHoistedScope()
                        if (hoisted.isNotEmpty()) {
                            bodyStatements.addAll(hoisted)
                        }
                        result
                    }
                    else -> parent.transformStatement(stmt)
                }
                if (transformed != null) {
                    bodyStatements.add(transformed)
                }
            }
            return if (bodyStatements.size == 1) bodyStatements[0] else BrsBlock(bodyStatements)
        }

        // Check if any returns targeting this block are inside while loops
        // If so, we need to use a flag-based approach
        val needsFlagApproach = hasReturnsInsideWhileLoop(block)

        // Generate flag name if needed and push onto stack
        val flagName = if (needsFlagApproach) {
            val name = "__ret_done_${genCtx.returnableBlockFlagCounter++}"
            genCtx.returnableBlockFlagStack.add(name)
            name
        } else null

        try {
            // Transform the block body
            val bodyStatements = mutableListOf<BrsStatement>()

            for (stmt in block.statements) {
                val transformed = when (stmt) {
                    is IrExpression -> {
                        genCtx.pushHoistedScope()
                        val result = when (stmt) {
                            is IrWhen -> visitWhen(stmt, Unit)
                            is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                            is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                            is IrBlock -> visitBlock(stmt, Unit)
                            is IrReturn -> visitReturn(stmt, Unit)
                            else -> BrsExpressionStatement(parent.transformExpression(stmt))
                        }
                        val hoisted = genCtx.popHoistedScope()
                        if (hoisted.isNotEmpty()) {
                            bodyStatements.addAll(hoisted)
                        }
                        result
                    }
                    else -> parent.transformStatement(stmt)
                }
                if (transformed != null) {
                    // If the result is a BrsBlock, flatten its statements into bodyStatements
                    // This ensures proper ordering when nested blocks contain variable declarations
                    // and assignments that depend on each other
                    if (transformed is BrsBlock) {
                        bodyStatements.addAll(transformed.statements)
                    } else {
                        bodyStatements.add(transformed)
                    }
                }
            }

            // Add "exit while" at the end to ensure we exit after all statements complete
            // (in case there's no early return), but only if the last statement isn't already terminating
            if (bodyStatements.isEmpty() || !isTerminating(bodyStatements.last())) {
                if (flagName != null) {
                    // Set flag before exiting
                    bodyStatements.add(BrsExpressionStatement(
                        BrsBinaryOp(BrsIdentifier(flagName), BrsBinaryOperator.EQ, BrsBooleanLiteral(true))
                    ))
                }
                bodyStatements.add(BrsExit(BrsExitKind.WHILE))
            }

            // Build the while wrapper body
            var whileBody: BrsStatement = BrsBlock(bodyStatements)

            // If using flag approach, post-process the body to insert flag checks after each while loop
            // at any nesting level. This ensures early exit propagates correctly.
            if (flagName != null) {
                whileBody = insertFlagChecksAfterWhileLoops(whileBody, flagName)
            }

            val whileLoop = BrsWhile(
                condition = BrsBooleanLiteral(true),
                body = whileBody
            )

            // If using flag approach, prepend flag declaration
            return if (flagName != null) {
                BrsBlock(mutableListOf<BrsStatement>(
                    BrsVariable(flagName, BrsType.BOOLEAN, BrsBooleanLiteral(false)),
                    whileLoop
                ))
            } else {
                whileLoop
            }
        } finally {
            if (flagName != null) {
                genCtx.returnableBlockFlagStack.removeLast()
            }
        }
    }

    /**
     * Transform an increment/decrement block.
     *
     * Increment/decrement blocks have structure:
     * IrBlock(origin=POSTFIX_INCR/DECR or PREFIX_INCR/DECR) {
     *   val <unary> = oldValue       // temp variable (for postfix)
     *   assignment                    // IrSetValue, IrSetField, or IrCall to setter
     *   <unary> or newValue          // return value - discarded when used as statement
     * }
     *
     * When used as a statement (result discarded), we output:
     * 1. All statements EXCEPT the final value return
     *
     * Returns null if the pattern doesn't match.
     */
    private fun transformIncrementDecrementBlock(block: IrBlock): BrsStatement? {
        val statements = block.statements
        if (statements.isEmpty()) return null

        // Process all statements except the last one (which is the return value)
        // For postfix: [val <unary> = old, setter(<unary>+1), <unary>]
        // For prefix: [val <unary> = var+1, setter(<unary>), <unary>]
        // We want to output just the setter call, skipping temp var and return value

        // Find the temp variable and its initializer for substitution
        // This allows us to inline the temp var references in the assignment
        val tempVar = statements.filterIsInstance<IrVariable>().firstOrNull {
            it.name.asString().startsWith("<") && it.name.asString().endsWith(">")
        }
        val tempVarInitializer = tempVar?.initializer

        // Find statements that are NOT just getters (temp var returns)
        // Include setter calls (IrCall to <set-*>), IrSetValue, IrSetField
        val effectfulStatements = statements.filter { stmt ->
            when (stmt) {
                is IrGetValue -> false  // Skip value returns
                is IrVariable -> false  // Skip temp variable declarations
                is IrSetValue -> true   // Include direct assignments
                is IrSetField -> true   // Include field assignments
                is IrCall -> true       // Include all calls (including setter calls)
                else -> false
            }
        }

        if (effectfulStatements.isEmpty()) return null

        // Set up temp var substitution if we found one
        if (tempVar != null && tempVarInitializer != null) {
            genCtx.pushTempVarSubstitution(tempVar.symbol, tempVarInitializer)
        }

        try {
            // Transform each effectful statement
            val brsStatements = effectfulStatements.mapNotNull { stmt ->
                when (stmt) {
                    is IrSetValue -> visitSetValue(stmt, Unit)
                    is IrSetField -> visitSetField(stmt, Unit)
                    is IrCall -> BrsExpressionStatement(parent.transformExpression(stmt))
                    is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                    else -> parent.transformStatement(stmt)
                }
            }

            return if (brsStatements.isEmpty()) {
                null
            } else if (brsStatements.size == 1) {
                brsStatements.first()
            } else {
                BrsBlock(brsStatements.toMutableList())
            }
        } finally {
            // Clean up temp var substitution
            if (tempVar != null) {
                genCtx.popTempVarSubstitution(tempVar.symbol)
            }
        }
    }

    /**
     * Try to transform a lowered FOR loop back to a BrightScript for/forEach loop.
     * Returns null if the pattern doesn't match and we should fall back to while loop.
     */
    private fun tryTransformForLoop(block: IrBlock): BrsStatement? {
        // Structure of a lowered FOR loop:
        // IrBlock(origin=FOR_LOOP) {
        //   IrVariable(origin=FOR_LOOP_ITERATOR) = <iterable>.iterator()
        //   IrWhileLoop(origin=FOR_LOOP_INNER_WHILE) {
        //     condition: iterator.hasNext()
        //     body: {
        //       val loopVar = iterator.next()
        //       // actual body
        //     }
        //   }
        // }

        val statements = block.statements
        if (statements.size < 2) {
            return null
        }

        // Find the iterator variable - may be at top level or inside an IrComposite
        var iteratorVar = statements.firstOrNull {
            it is IrVariable && it.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR
        } as? IrVariable

        // If not found at top level, check inside IrComposite (happens after inlining)
        if (iteratorVar == null) {
            for (stmt in statements) {
                if (stmt is IrComposite) {
                    iteratorVar = stmt.statements.firstOrNull {
                        it is IrVariable && (it as? IrVariable)?.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR
                    } as? IrVariable
                    if (iteratorVar != null) break
                }
            }
        }

        if (iteratorVar == null) {
            // Still not found - this FOR_LOOP block has an unexpected structure
            return null
        }

        // Find the while loop
        val whileLoop = statements.lastOrNull {
            it is IrWhileLoop && it.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE
        } as? IrWhileLoop
        if (whileLoop == null) {
            return null
        }

        // Try to extract the iterable from the iterator initialization
        val iteratorInit = iteratorVar.initializer as? IrCall
        if (iteratorInit == null) {
            return null
        }
        val iterableExpr = iteratorInit.dispatchReceiver ?: iteratorInit.extensionReceiver
        if (iterableExpr == null) {
            return null
        }

        // Get the loop body
        val loopBody = whileLoop.body as? IrContainerExpression
        if (loopBody == null) {
            return null
        }
        val bodyStatements = loopBody.statements

        if (bodyStatements.isEmpty()) {
            return null
        }

        // First statement should be the loop variable assignment from iterator.next()
        val loopVarDecl = bodyStatements.firstOrNull() as? IrVariable
        if (loopVarDecl == null) {
            return null
        }
        val loopVarName = loopVarDecl.name.asString()

        // Register the while loop as "transformed to while" BEFORE transforming the body.
        // This ensures that any IrBreak statements inside will generate "exit while" instead of "exit for".
        // We don't know yet which strategy we'll use (it depends on iterable type analysis below),
        // but it's safe to register - if we use native for-each (Strategy 1-3, 5), there won't be
        // any break statements targeting this while loop in the output anyway.
        genCtx.loopsTransformedToWhile.add(whileLoop)

        // Increment nesting counter so that any nested for-loops (from inlining) also get "exit while"
        genCtx.forLoopToWhileNestingDepth++
        val actualBody: List<BrsStatement>
        try {
            // Transform the remaining body statements (skip the loop variable declaration)
            actualBody = bodyStatements.drop(1).mapNotNull { stmt ->
            when (stmt) {
                // IrWhen (if statements) should use visitWhen directly to handle returns properly
                is IrWhen -> visitWhen(stmt, Unit)
                // IrWhileLoop and IrDoWhileLoop need explicit handling
                is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                // IrBlock handling - check if it's a wrapper around a single IrWhen
                is IrBlock -> {
                    // Unwrap single-statement blocks that contain IrWhen
                    // These wrappers are created by various lowering passes
                    val innerStatements = stmt.statements
                    if (innerStatements.size == 1 && innerStatements[0] is IrWhen) {
                        visitWhen(innerStatements[0] as IrWhen, Unit)
                    } else {
                        transformBlockOrStatement(stmt)
                    }
                }
                // IrTypeOperatorCall with IMPLICIT_COERCION_TO_UNIT wraps expressions used as statements
                // Unwrap to get to the inner when/block
                is IrTypeOperatorCall -> {
                    if (stmt.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                        val innerArg = stmt.argument
                        when (innerArg) {
                            is IrWhen -> visitWhen(innerArg, Unit)
                            is IrBlock -> {
                                // Check if the block contains a single IrWhen
                                if (innerArg.statements.size == 1 && innerArg.statements[0] is IrWhen) {
                                    visitWhen(innerArg.statements[0] as IrWhen, Unit)
                                } else {
                                    transformBlockOrStatement(innerArg)
                                }
                            }
                            else -> transformBlockOrStatement(stmt)
                        }
                    } else {
                        transformBlockOrStatement(stmt)
                    }
                }
                // Expressions - transform and check for hoisted when-lowered blocks
                is IrExpression -> {
                    if (stmt.type.isUnit()) {
                        parent.transformStatement(stmt)
                    } else if (stmt is IrWhen) {
                        // When expressions should always be statements in for loop body,
                        // even if they have a non-Unit type (e.g., StringBuilder.append())
                        visitWhen(stmt, Unit)
                    } else if (stmt is IrTypeOperatorCall && stmt.argument is IrWhen) {
                        // Unwrap type operators around when expressions
                        visitWhen(stmt.argument as IrWhen, Unit)
                    } else {
                        // Transform expression - when-lowered blocks add to hoisted queue
                        val expr = parent.transformExpression(stmt)
                        val hoisted = genCtx.takeHoistedStatements()
                        // In statement context, skip __when_tmp references (just temp var values)
                        val skipFinalExpr = expr is BrsIdentifier && expr.name.startsWith("__when_tmp")
                        if (hoisted.isNotEmpty()) {
                            val stmts = hoisted.toMutableList()
                            if (!skipFinalExpr && expr !is BrsInvalidLiteral) {
                                stmts.add(BrsExpressionStatement(expr))
                            }
                            if (stmts.isEmpty()) null
                            else if (stmts.size == 1) stmts.first()
                            else BrsBlock(stmts)
                        } else if (!skipFinalExpr) {
                            BrsExpressionStatement(expr)
                        } else {
                            null
                        }
                    }
                }
                else -> parent.transformStatement(stmt)
            }
        }
        } finally {
            genCtx.forLoopToWhileNestingDepth--
        }

        // Transform the iterable expression
        val iterableBrs = parent.transformExpression(iterableExpr)

        // For Kotlin collection types (ArrayList, MutableList, etc.), we need to iterate
        // over the underlying array, not the wrapper object. In BrightScript, 'for each'
        // on an AA iterates over keys, not values. Collections store items in .array property.
        val iterableType = iterableExpr.type
        val iterableClassName = iterableType.classOrNull?.owner?.name?.asString() ?: ""

        // =============================================================================
        // Strategy 1: Check if type implements NativeIterable (e.g., RoArray, RoAssociativeArray)
        // These types support native BrightScript for-each iteration directly.
        // =============================================================================
        if (context.intrinsics.isNativeIterable(iterableType)) {
            return BrsForEach(
                variable = loopVarName,
                iterable = iterableBrs,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 2: Check if iterator() returns NativeArrayIterator
        // This signals that the type supports native BrightScript for-each.
        // =============================================================================
        val iteratorMethod = findIteratorMethod(iterableType)
        if (iteratorMethod != null && context.intrinsics.returnsNativeArrayIterator(iteratorMethod)) {
            return BrsForEach(
                variable = loopVarName,
                iterable = iterableBrs,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 3: Stdlib collections with __get_array() property
        // These concrete stdlib collection types wrap roArray and expose it via __get_array().
        // =============================================================================
        val hasGetArray = iterableClassName in listOf(
            // Array-backed collections
            "ArrayList", "ArrayDeque", "CharArray", "IntArray", "LongArray",
            "FloatArray", "DoubleArray", "BooleanArray", "ByteArray", "ShortArray",
            // HashMap view collections
            "KeySet", "ValueCollection", "EntrySet",
            // LinkedHashMap view collections
            "LinkedKeySet", "LinkedValueCollection", "LinkedEntrySet"
        )

        if (hasGetArray) {
            // Call __get_array() method for iteration - this works for concrete stdlib collections
            val arrayIterable = BrsFunctionCall(BrsDotAccess(iterableBrs, "__get_array"), mutableListOf())
            return BrsForEach(
                variable = loopVarName,
                iterable = arrayIterable,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 4: Kotlin Iterable interface types → iterator protocol
        // Generate a while loop with iterator_k_(), hasNext_k_(), next_k_() calls.
        // =============================================================================
        val isCollectionInterface = iterableClassName in listOf(
            // Interface types
            "Iterable", "MutableIterable", "Collection", "MutableCollection",
            "List", "MutableList", "Set", "MutableSet",
            // Sequence types - need iterator protocol, not native for-each (which iterates AA keys)
            "Sequence",
            // Concrete collection classes that need iterator-based iteration
            // (these don't have get_array() - they use hash-based storage)
            "HashSet", "LinkedHashSet", "HashMap", "LinkedHashMap"
        )

        if (isCollectionInterface) {
            // Generate: __iter = iterable.iterator_k_(); while __iter.hasNext_k_() { loopVar = __iter.next_k_(); body }
            // Note: Method names do NOT include return types (like Java) to support polymorphism
            val iterVarName = "__iter_${genCtx.nextTempId()}"
            val iterVar = BrsIdentifier(iterVarName)

            // All iterator methods use the same name regardless of mutable/non-mutable
            // (return types are not part of method name mangling)
            val iteratorMethodName = "iterator_k_"

            // __iter = iterable.iterator_k_()
            val iteratorCall = BrsFunctionCall(BrsDotAccess(iterableBrs, iteratorMethodName), mutableListOf())
            val iterAssign = BrsVariable(iterVarName, null, iteratorCall)

            // __iter.hasNext_k_()  (returns Boolean, but return type not in name)
            val hasNextCall = BrsFunctionCall(BrsDotAccess(iterVar, "hasNext_k_"), mutableListOf())

            // loopVar = __iter.next_k_()  (returns T which erases to Any?, but return type not in name)
            val nextCall = BrsFunctionCall(BrsDotAccess(iterVar, "next_k_"), mutableListOf())
            val loopVarAssign = BrsVariable(loopVarName, null, nextCall)

            // Build while body: assignment + original body
            val whileBody = mutableListOf<BrsStatement>()
            whileBody.add(loopVarAssign)
            whileBody.addAll(actualBody)

            // Convert any "exit for" to "exit while" and "continue for" to continue pattern
            val convertedBody = convertForControlToWhile(whileBody)

            return BrsBlock(mutableListOf(
                iterAssign,
                BrsWhile(hasNextCall, BrsBlock(convertedBody.toMutableList()))
            ))
        }

        // =============================================================================
        // Strategy 5: Default - native for-each
        // For native arrays or other types, use for-each directly.
        // =============================================================================
        return BrsForEach(
            variable = loopVarName,
            iterable = iterableBrs,
            body = BrsBlock(actualBody.toMutableList())
        )
    }

    /**
     * Find the iterator() method on a type.
     * Returns the IrSimpleFunction if found, null otherwise.
     */
    private fun findIteratorMethod(type: IrType): IrSimpleFunction? {
        val classSymbol = type.classOrNull ?: return null
        return classSymbol.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == "iterator" && it.valueParameters.isEmpty() }
    }

    /**
     * Convert "exit for" to "exit while" in statements that were originally in a for-each
     * but are now in a while loop due to iterator-based conversion.
     */
    private fun convertForControlToWhile(statements: List<BrsStatement>): List<BrsStatement> {
        return statements.map { stmt -> convertForControlToWhileStmt(stmt) }
    }

    private fun convertForControlToWhileStmt(stmt: BrsStatement): BrsStatement {
        return when (stmt) {
            is BrsExit -> if (stmt.kind == BrsExitKind.FOR) BrsExit(BrsExitKind.WHILE) else stmt
            is BrsContinue -> if (stmt.kind == BrsContinueKind.FOR) BrsContinue(BrsContinueKind.WHILE) else stmt
            is BrsBlock -> BrsBlock(convertForControlToWhile(stmt.statements).toMutableList())
            is BrsIf -> BrsIf(
                condition = stmt.condition,
                thenBranch = convertForControlToWhileStmt(stmt.thenBranch),
                elseBranch = stmt.elseBranch?.let { convertForControlToWhileStmt(it) }
            )
            is BrsTry -> BrsTry(
                tryBlock = convertForControlToWhileStmt(stmt.tryBlock),
                catchVariable = stmt.catchVariable,
                catchBlock = stmt.catchBlock?.let { convertForControlToWhileStmt(it) }
            )
            is BrsWhile -> BrsWhile(
                condition = stmt.condition,
                body = stmt.body // Don't recurse into nested while - it has its own scope
            )
            is BrsForEach -> stmt // Don't recurse into nested for - it has its own scope
            is BrsFor -> stmt // Don't recurse into nested for - it has its own scope
            // Handle expression statements that might contain nested structures
            is BrsExpressionStatement -> stmt
            is BrsVariable -> stmt
            is BrsReturn -> stmt
            is BrsThrow -> stmt
            is BrsComment -> stmt
            is BrsEmpty -> stmt
            else -> stmt
        }
    }

    /**
     * Recursively scan an expression tree for IrBlocks that need hoisting.
     * Collects the blocks and returns them so their statements can be emitted before the expression.
     */
    private fun collectBlocksFromExpression(expr: IrExpression): List<IrBlock> {
        val blocks = mutableListOf<IrBlock>()
        collectBlocksRecursive(expr, blocks)
        return blocks
    }

    private fun collectBlocksRecursive(expr: IrExpression?, blocks: MutableList<IrBlock>) {
        when (expr) {
            null -> {}
            is IrBlock -> {
                if (expr.statements.size > 1 &&
                    expr.origin != IrStatementOrigin.POSTFIX_INCR &&
                    expr.origin != IrStatementOrigin.POSTFIX_DECR &&
                    expr.origin != IrStatementOrigin.PREFIX_INCR &&
                    expr.origin != IrStatementOrigin.PREFIX_DECR) {
                    blocks.add(expr)
                }
                // Also check statements within the block
                for (stmt in expr.statements) {
                    if (stmt is IrExpression) {
                        collectBlocksRecursive(stmt, blocks)
                    }
                    // Note: IrVariable initializers are handled by hoistBlockStatements which
                    // consumes hoisted statements from transformExpression calls
                }
            }
            is IrCall -> {
                collectBlocksRecursive(expr.dispatchReceiver, blocks)
                collectBlocksRecursive(expr.extensionReceiver, blocks)
                for (i in 0 until expr.valueArgumentsCount) {
                    collectBlocksRecursive(expr.getValueArgument(i), blocks)
                }
            }
            is IrConstructorCall -> {
                for (i in 0 until expr.valueArgumentsCount) {
                    collectBlocksRecursive(expr.getValueArgument(i), blocks)
                }
            }
            is IrWhen -> {
                // Only recurse into conditions, NOT branch results
                // Branch result blocks should stay as branch bodies and be handled by visitWhen
                // They don't need hoisting - only blocks in true expression context do
                for (branch in expr.branches) {
                    collectBlocksRecursive(branch.condition, blocks)
                    // Don't recurse into branch.result - it's a branch body, not a hoistable block
                }
            }
            is IrTypeOperatorCall -> {
                collectBlocksRecursive(expr.argument, blocks)
            }
            is IrStringConcatenation -> {
                for (arg in expr.arguments) {
                    collectBlocksRecursive(arg, blocks)
                }
            }
            is IrGetValue, is IrConst, is IrGetField -> {
                // Leaf nodes - no children to process
            }
            else -> {
                // For other expression types, try to get children via reflection or skip
            }
        }
    }

    /**
     * Hoist the statements from a block (all except the last, which is the value).
     * The last statement will be returned by the expression transformer when the block is visited.
     */
    private fun hoistBlockStatements(block: IrBlock, precedingStatements: MutableList<BrsStatement>) {
        val blockStatements = block.statements
        if (blockStatements.size <= 1) return

        // Transform all statements except the last one as statements
        for (i in 0 until blockStatements.size - 1) {
            val stmt = blockStatements[i]
            val transformed = when (stmt) {
                is IrVariable -> {
                    val varName = stmt.name.asString()
                    // Sanitize the name for BrightScript - handle special names and escape reserved keywords
                    val sanitizedVarName = sanitizeParameterName(varName)
                    val init = stmt.initializer?.let { parent.transformExpression(it) }
                    // Consume any hoisted statements from nested when-lowered blocks in the initializer
                    val hoisted = genCtx.takeHoistedStatements()
                    precedingStatements.addAll(hoisted)
                    if (init != null) BrsVariable(sanitizedVarName, mapTypeToBrs(stmt.type), init) else null
                }
                is IrWhen -> visitWhen(stmt, Unit)
                is IrSetValue -> visitSetValue(stmt, Unit)
                is IrSetField -> visitSetField(stmt, Unit)
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
            if (transformed != null) {
                precedingStatements.add(transformed)
            }
        }
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsStatement {
        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = genCtx.getCapturedVariable(expression.symbol)

        val sanitizedName = when {
            rawName.startsWith("<set-") && rawName.endsWith(">") -> "value"
            rawName.startsWith("<") && rawName.endsWith(">") ->
                rawName.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> sanitizeParameterName(rawName)
        }

        // Check if this is a shared variable (boxed for closure capture)
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val owner = expression.symbol.owner
        val isSharedVariable = (owner is IrVariable && owner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                               expression.symbol in genCtx.sharedVariables

        // The state machine hoists shared variable declarations and re-emits their
        // initialization as an assignment tagged SHARED_BOX_INIT - that assignment
        // creates the box; every other write goes through .value
        val isBoxInit = isSharedVariable && expression.origin == BrsStatementOrigins.SHARED_BOX_INIT

        // Build the target expression (LHS of assignment)
        val target = if (capturedVar != null && capturedVar.isMutable) {
            // Rewrite to assign via m (the closure object): m.varName.value = newValue
            BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value")
        } else if (isSharedVariable && !isBoxInit) {
            // Shared variable accessed outside closure: varName.value = newValue
            // Note: capturedVar is null here due to the first condition being false
            BrsDotAccess(BrsIdentifier(sanitizeParameterName(sanitizedName)), "value")
        } else if (owner is IrVariable) {
            // For local variables, use the unique name to avoid collisions from inline expansion
            BrsIdentifier(genCtx.getVariableName(expression.symbol, sanitizedName))
        } else {
            BrsIdentifier(sanitizedName)
        }

        // Transform the expression - when-lowered blocks will add to hoisted queue
        val transformedValue = parent.transformExpression(expression.value)
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = genCtx.takeHoistedStatements()

        val finalValue = if (isBoxInit) {
            BrsAALiteral(mutableListOf(BrsAAEntry("value", transformedValue)))
        } else {
            transformedValue
        }

        val assignment = BrsExpressionStatement(
            BrsBinaryOp(target, BrsBinaryOperator.EQ, finalValue)
        )

        return if (hoisted.isNotEmpty()) {
            BrsBlock((hoisted + assignment).toMutableList())
        } else {
            assignment
        }
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsStatement {
        val receiver = expression.receiver?.let { parent.transformExpression(it) }
            ?: BrsMRef()

        val field = expression.symbol.owner
        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // @SG*Field-annotated component fields live on the NODE, not the component
        // m-scope object: backing-field writes (setter bodies) must write m.top.field.
        // Delegated properties are excluded — their backing field holds the delegate.
        val parentClass = field.parent as? IrClass
        val fieldProperty = field.correspondingPropertySymbol?.owner
        if (parentClass != null && fieldProperty != null &&
            !fieldProperty.isDelegated &&
            context.intrinsics.isSceneGraphComponent(parentClass) &&
            hasInterfaceFieldAnnotation(fieldProperty)
        ) {
            val interfaceFieldValue = parent.transformExpression(expression.value)
            val interfaceFieldHoisted = genCtx.takeHoistedStatements()
            val interfaceFieldAssignment = BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsDotAccess(receiver, "top"), fieldName),
                    BrsBinaryOperator.EQ,
                    interfaceFieldValue
                )
            )
            return if (interfaceFieldHoisted.isNotEmpty()) {
                BrsBlock((interfaceFieldHoisted + interfaceFieldAssignment).toMutableList())
            } else {
                interfaceFieldAssignment
            }
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // If so, we need to write to field.value instead of field
        // EXCEPTION: In constructor body, we're initializing the field with the box itself,
        // so we write directly to the field, not field.value
        val className = parentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields
        val isInConstructor = genCtx.isInConstructorBody

        // A shared variable moved to a coroutine field keeps its SHARED_BOX_INIT-tagged
        // initializing assignment (BrsLiveLocalsTransformer preserves the origin) - the box
        // is created there; all other writes go through .value
        val isBoxInit = isSharedVariableField && expression.origin == BrsStatementOrigins.SHARED_BOX_INIT

        val target = if (isSharedVariableField && !isInConstructor && !isBoxInit) {
            // Write to the box's value: m.fieldName.value = newValue
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }

        // Transform the expression - when-lowered blocks will add to hoisted queue
        val transformedValue = parent.transformExpression(expression.value)
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = genCtx.takeHoistedStatements()

        val finalValue = if (isBoxInit) {
            BrsAALiteral(mutableListOf(BrsAAEntry("value", transformedValue)))
        } else {
            transformedValue
        }

        val assignment = BrsExpressionStatement(
            BrsBinaryOp(target, BrsBinaryOperator.EQ, finalValue)
        )

        return if (hoisted.isNotEmpty()) {
            BrsBlock((hoisted + assignment).toMutableList())
        } else {
            assignment
        }
    }

    /**
     * Handle function calls as statements, extracting block arguments from when expression lowering.
     *
     * When a function call has a block argument (from when expression lowering), we need to:
     * 1. Emit the block's statements (variable declaration, if/else) before the call
     * 2. Replace the block argument with just the final value (the temp variable)
     */
    fun visitCallAsStatement(expression: IrCall): BrsStatement {
        // Transform the expression - when-lowered blocks will add to hoisted queue
        val transformedCall = parent.transformExpression(expression)
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = genCtx.takeHoistedStatements()

        val callStmt = BrsExpressionStatement(transformedCall)

        return if (hoisted.isNotEmpty()) {
            BrsBlock((hoisted + callStmt).toMutableList())
        } else {
            callStmt
        }
    }

    private fun transformBlockOrStatement(element: IrElement): BrsStatement {
        // Pure reads in statement position emit invalid BrightScript (bare identifier);
        // their value is discarded, so drop them entirely.
        if (element is IrExpression && isDiscardablePureExpression(element)) return BrsEmpty()
        return when (element) {
            is IrBlock -> visitBlock(element, Unit)
            is IrBlockBody -> visitBlockBody(element, Unit)
            // Loops need explicit handling since they're IrExpressions but should be transformed as statements
            is IrWhileLoop -> visitWhileLoop(element, Unit)
            is IrDoWhileLoop -> visitDoWhileLoop(element, Unit)
            // IrWhen (if/when) needs explicit handling for proper statement transformation
            is IrWhen -> visitWhen(element, Unit)
            // Control flow must be handled as statements, not expressions
            is IrThrow -> visitThrow(element, Unit)
            is IrBreak -> visitBreak(element, Unit)
            is IrContinue -> visitContinue(element, Unit)
            // Type operators (like IMPLICIT_COERCION_TO_UNIT) - recursively unwrap to find the inner statement
            is IrTypeOperatorCall -> {
                val innerArg = element.argument
                when (innerArg) {
                    is IrWhen -> visitWhen(innerArg, Unit)
                    is IrBlock -> visitBlock(innerArg, Unit)
                    is IrTypeOperatorCall -> transformBlockOrStatement(innerArg)  // Recursive unwrap
                    is IrComposite -> {
                        // Composite might wrap a when - process statements individually
                        val compositeStmts = innerArg.statements
                        if (compositeStmts.size == 1 && compositeStmts[0] is IrWhen) {
                            visitWhen(compositeStmts[0] as IrWhen, Unit)
                        } else {
                            // Transform all statements and wrap in block
                            val brsStatements = compositeStmts.mapNotNull { transformBlockOrStatement(it) }
                            BrsBlock(brsStatements.toMutableList())
                        }
                    }
                    else -> expressionStatementWithHoisted(element)
                }
            }
            // Composite expressions might contain when statements
            is IrComposite -> {
                val compositeStmts = element.statements
                if (compositeStmts.size == 1 && compositeStmts[0] is IrWhen) {
                    visitWhen(compositeStmts[0] as IrWhen, Unit)
                } else {
                    // Transform all statements and wrap in block
                    val brsStatements = compositeStmts.mapNotNull { transformBlockOrStatement(it) }
                    BrsBlock(brsStatements.toMutableList())
                }
            }
            is IrExpression -> expressionStatementWithHoisted(element)
            is IrStatement -> parent.transformStatement(element) ?: BrsEmpty()
            else -> BrsEmpty()
        }
    }

    /**
     * Emit a statement-position expression together with whatever its transformation
     * hoisted (safe-call/elvis machinery, block-as-expression temps). The hoisted
     * statements go BEFORE the expression statement — never INSTEAD of it: the old
     * code returned hoisted-only, silently dropping the consuming call (state-machine
     * states emitted `assertEquals("boom", thrown?.message)` as bare safe-call
     * machinery — the assertion never executed and the test passed vacuously).
     * A residual bare identifier or literal IS skipped (matching flattenBlockStatements):
     * that is a when-lowered block's value read whose effect lives entirely in the
     * hoisted machinery, and a bare identifier/literal statement is invalid BrightScript.
     */
    private fun expressionStatementWithHoisted(element: IrExpression): BrsStatement {
        val expr = parent.transformExpression(element)
        val hoisted = genCtx.takeHoistedStatements()
        if (hoisted.isEmpty()) return BrsExpressionStatement(expr)
        val keepExpr = expr !is BrsIdentifier && expr !is BrsInvalidLiteral &&
            expr !is BrsIntLiteral && expr !is BrsDoubleLiteral &&
            expr !is BrsStringLiteral && expr !is BrsBooleanLiteral
        val stmts = hoisted.toMutableList()
        if (keepExpr) stmts.add(BrsExpressionStatement(expr))
        return if (stmts.size == 1) stmts.first() else BrsBlock(stmts)
    }
}
