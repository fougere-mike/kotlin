/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * One lifted `ScopeHandle.run { block }` call site: the compiler-synthesized
 * request name (`<fileFq>#<n>`) and the top-level function the block became.
 * The binding-table injection reads these to map names to function pointers
 * in owner components' generated init().
 */
data class ScopeRunBlock(val requestName: String, val liftedFunction: IrSimpleFunction)

/**
 * Rewrites `scopeHandle.run(<literal suspend lambda>)` call sites into
 * `scopeHandle.runLowered("<fileFq>#<n>", <capturesAA>)` plus one lifted
 * top-level suspend function per block.
 *
 * The lifted function takes a single `captures: RoAssociativeArray?` parameter
 * and re-declares each captured value at entry by reading `captures` by name
 * (`val shelfId = captures.lookup("shelfId")`), then runs the original block
 * body. The call site builds the matching AA literal-style block
 * (`CreateObject("roAssociativeArray")` + `addReplace` per capture); a
 * zero-capture block passes `invalid`. Captures therefore cross BY COPY over
 * the mailbox wire — writes to a captured variable inside the block never
 * reach the caller (BRS_SCOPE_CAPTURE_MUTATION_LOST is the planned guard).
 * The capture collection/prologue/remap machinery is shared with the
 * flowOn/spawnTask task lift — see BrsCaptureLift.kt.
 *
 * Name synthesis: `<fileFqName>#<n>` where n is the 1-based ordinal of
 * rewritten call sites within the file in transform order (deterministic
 * within a compilation; both sides — call-site string and binding table —
 * are generated together, so cross-build stability is not required). '#' is
 * reserved: hand-registered request names may not contain it.
 *
 * Runs BEFORE UpgradeCallableReferences, while the block is still an
 * [IrFunctionExpression] with implicit captures (direct IrGetValue of
 * enclosing values), and before any coroutine lowering (one suspend call
 * replaces another — BrsRunTaskCallLowering precedent). The lifted function
 * is appended to the file's declarations, so it rides ordinary top-level
 * emission, the function manifest, and dependency recording exactly like any
 * other generated function ([BrsIrBackendContext.functionManifest] is
 * collected after lowering).
 *
 * A non-literal argument (stored function value, function reference) is left
 * untouched: FIR rejects it (BRS_SCOPE_BLOCK_NOT_LITERAL), and a suppressed
 * call falls through to the stdlib backstop, which throws an ISE naming the
 * rule.
 */
class BrsScopeRunBlockLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val scopeHandleRunFqName = BrsStandardClassIds.Callables.scopeHandleRun.asSingleFqName()

    override fun lower(irFile: IrFile) {
        val runLowered = context.brsSymbols.scopeRunLoweredOrNull ?: return
        val aaClass = context.brsSymbols.roAssociativeArrayClass ?: return
        val iaaClass = context.brsSymbols.iAssociativeArrayClass ?: return
        val aaCreate = aaClass.owner.companionObject()?.functions?.singleOrNull { it.name.asString() == "create" } ?: return
        val aaLookup = iaaClass.owner.functions.singleOrNull { it.name.asString() == "lookup" } ?: return
        val aaAddReplace = iaaClass.owner.functions.singleOrNull { it.name.asString() == "addReplace" } ?: return
        val capturesType = aaClass.owner.defaultType.makeNullable()

        val fileFq = irFile.packageFqName.asString().let { pkg ->
            val fileName = irFile.name.removeSuffix(".kt")
            if (pkg.isEmpty()) fileName else "$pkg.$fileName"
        }
        val sanitizedFileName = irFile.name.removeSuffix(".kt").replace(Regex("[^A-Za-z0-9_]"), "_")

        val liftedFunctions = mutableListOf<IrSimpleFunction>()
        var ordinal = 0

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {

            private var currentFunction: IrFunction? = null

            override fun visitFunction(declaration: IrFunction): IrStatement {
                val previous = currentFunction
                currentFunction = declaration
                val result = super.visitFunction(declaration)
                currentFunction = previous
                return result
            }

            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                val callee = expression.symbol.owner
                if (callee.fqNameWhenAvailable != scopeHandleRunFqName) return expression
                if (callee.valueParameters.size != 1) return expression
                if (!callee.valueParameters[0].type.isSuspendFunctionType()) return expression
                // Non-literal argument: FIR guard errors; a suppressed call keeps the backstop.
                val blockArg = expression.getValueArgument(0) as? IrFunctionExpression ?: return expression
                val receiver = expression.dispatchReceiver ?: return expression
                val resultType = expression.typeArguments.getOrNull(0) ?: return expression
                // run() is suspend, so FIR guarantees every call site sits inside
                // some function (suspend fun or suspend lambda) by this phase; a
                // null here is a broken IR shape — fail loudly, never skip the
                // rewrite silently (the un-lowered call would throw the stdlib
                // backstop ISE at runtime instead of compiling correctly).
                val enclosing = currentFunction
                    ?: compilationException("ScopeHandle.run { } call site outside any function", expression)

                ordinal += 1
                val requestName = "$fileFq#$ordinal"

                val captures = collectLiftCaptures(blockArg.function.body, blockArg.function.parameters)
                val lifted = buildLiftedFunction(
                    "__scopeBlock_${sanitizedFileName}_$ordinal", blockArg.function, captures, capturesType, aaLookup.symbol
                )
                liftedFunctions += lifted
                context.scopeRunBlocks.getOrPut(irFile) { mutableListOf() } += ScopeRunBlock(requestName, lifted)

                return buildRewrittenCall(
                    expression, receiver, requestName, resultType, captures,
                    runLowered, capturesType, aaCreate, aaAddReplace, enclosing
                )
            }
        })

        for (lifted in liftedFunctions) {
            lifted.parent = irFile
            irFile.declarations.add(lifted)
        }
    }

    private fun IrType.isSuspendFunctionType(): Boolean =
        classFqName?.asString()?.startsWith("kotlin.coroutines.SuspendFunction") == true

    /**
     * The lifted top-level suspend function: reads each capture from the
     * `captures` AA at entry (by the same names the call site wrote), then
     * runs the original block body with references remapped to those locals
     * and returns retargeted from the lambda to the lifted function.
     */
    private fun buildLiftedFunction(
        functionName: String,
        lambda: IrSimpleFunction,
        captures: List<LiftCapture>,
        capturesType: IrType,
        aaLookup: IrSimpleFunctionSymbol,
    ): IrSimpleFunction {
        val lifted = context.irFactory.buildFun {
            startOffset = lambda.startOffset
            endOffset = lambda.endOffset
            origin = IrDeclarationOrigin.DEFINED
            name = Name.identifier(functionName)
            visibility = DescriptorVisibilities.PRIVATE
            modality = Modality.FINAL
            returnType = lambda.returnType
            isSuspend = true
        }
        val capturesParam = lifted.addValueParameter {
            name = Name.identifier("captures")
            type = capturesType
        }

        val statements = mutableListOf<IrStatement>()
        val (prologue, remapping) = buildCaptureLocals(context, lifted, capturesParam, captures, aaLookup)
        statements += prologue

        val body = lambda.body as? IrBlockBody
            ?: error("ScopeHandle.run block lambda has no block body: ${lambda.render()}")
        remapLiftedRegion(body, remapping, retargetReturnsFrom = lambda, retargetReturnsTo = lifted)
        statements += body.statements

        lifted.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, statements)
        lifted.body!!.patchDeclarationParents(lifted)
        return lifted
    }

    /**
     * The rewritten call site. Zero captures: a direct
     * `runLowered(name, invalid)` call on the original receiver expression.
     * With captures: a block that evaluates the receiver once, builds the
     * captures AA (`CreateObject("roAssociativeArray")` + one `addReplace`
     * per capture), then calls `runLowered(name, captures)` — receiver-first
     * evaluation order preserved.
     */
    private fun buildRewrittenCall(
        original: IrCall,
        receiver: IrExpression,
        requestName: String,
        resultType: IrType,
        captures: List<LiftCapture>,
        runLowered: IrSimpleFunctionSymbol,
        capturesType: IrType,
        aaCreate: IrSimpleFunction,
        aaAddReplace: IrSimpleFunction,
        enclosing: IrFunction,
    ): IrExpression {
        fun runLoweredCall(receiverExpression: IrExpression, capturesExpression: IrExpression): IrCallImpl =
            IrCallImpl(
                original.startOffset, original.endOffset,
                original.type,
                runLowered,
                typeArgumentsCount = 1,
            ).apply {
                putTypeArgument(0, resultType)
                extensionReceiver = receiverExpression
                putValueArgument(0, liftStringConst(context, requestName))
                putValueArgument(1, capturesExpression)
            }

        if (captures.isEmpty()) {
            return runLoweredCall(receiver, liftNullCaptures(capturesType))
        }

        val receiverVar = buildVariable(
            parent = enclosing,
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
            name = Name.identifier("scopeRecv"),
            type = receiver.type,
        ).apply { initializer = receiver }

        val capturesVar = buildCapturesVariable("scopeCaptures", aaCreate, enclosing)

        return IrBlockImpl(original.startOffset, original.endOffset, original.type).apply {
            statements += receiverVar
            statements += capturesVar
            for (capture in captures) {
                statements += buildAddReplaceCall(context, aaAddReplace, capturesVar, capture)
            }
            statements += runLoweredCall(
                IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, receiverVar.type, receiverVar.symbol),
                IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capturesVar.type, capturesVar.symbol),
            )
        }
    }
}
