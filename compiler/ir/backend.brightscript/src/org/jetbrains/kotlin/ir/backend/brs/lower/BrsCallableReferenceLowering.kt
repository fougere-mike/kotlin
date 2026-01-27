/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrRichFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor

/**
 * Transforms lambda expressions and function references into anonymous classes.
 *
 * This is essential for coroutine support because BrsSuspendFunctionsLowering
 * detects suspend lambdas by checking for LAMBDA_IMPL class origin. Without this
 * lowering, lambdas go directly to IrToBrsTransformer which emits them as AA literals
 * instead of proper classes with `invoke` methods.
 *
 * This lowering must run BEFORE coroutine lowering phases so that:
 * 1. Lambda expressions become classes with LAMBDA_IMPL origin
 * 2. BrsSuspendFunctionsLowering can detect suspend lambdas and transform them
 *    into CoroutineImpl classes with `create` and `doResume` methods
 *
 * Based on the JS backend implementation (JsCallableReferenceLowering).
 */
class BrsCallableReferenceLowering(
    private val brsContext: BrsIrBackendContext
) : WebCallableReferenceLowering(brsContext) {

    /**
     * Check if the lambda is a suspend lambda that needs coroutine support.
     * Suspend lambdas need to extend CoroutineImpl and have a continuation parameter.
     */
    private val IrRichFunctionReference.isSuspendLambda: Boolean
        get() = isLambda && invokeFunction.isSuspend

    override fun getConstructorCallOrigin(reference: IrRichFunctionReference): IrStatementOrigin? = null

    override fun getClassOrigin(reference: IrRichFunctionReference): IrDeclarationOrigin {
        return if (reference.isKReference || !reference.isLambda)
            FUNCTION_REFERENCE_IMPL
        else
            LAMBDA_IMPL
    }

    override fun getSuperClassType(reference: IrRichFunctionReference): IrType {
        // Suspend lambdas must extend CoroutineImpl so that BrsSuspendFunctionsLowering
        // can find the create() method to override and build the state machine.
        return if (reference.isSuspendLambda) {
            // Check if CoroutineImpl is available (not during stdlib compilation)
            brsContext.brsSymbols.coroutineSymbols.coroutineImpl?.owner?.defaultType
                ?: context.irBuiltIns.anyType
        } else {
            context.irBuiltIns.anyType
        }
    }

    override fun getExtraConstructorParameters(constructor: IrConstructor, reference: IrRichFunctionReference): List<IrValueParameter> {
        // Suspend lambdas need a continuation parameter to pass to CoroutineImpl's constructor
        if (!reference.isSuspendLambda) return emptyList()

        val coroutineImpl = brsContext.brsSymbols.coroutineSymbols.coroutineImpl?.owner
            ?: return emptyList()

        val superContinuation = coroutineImpl.primaryConstructor?.parameters?.singleOrNull()
            ?: return emptyList()

        return listOf(
            buildValueParameter(constructor) {
                name = superContinuation.name
                type = superContinuation.type
                origin = IrDeclarationOrigin.CONTINUATION
                kind = IrParameterKind.Regular
            }
        )
    }

    override fun IrBuilderWithScope.generateSuperClassConstructorCall(
        constructor: IrConstructor,
        superClassType: IrType,
        functionReference: IrRichFunctionReference,
    ): IrDelegatingConstructorCall {
        val superConstructor = superClassType.classOrFail.owner.primaryConstructor
            ?: error("Missing primary constructor for ${superClassType.classOrFail.owner.name}")
        return irDelegatingConstructorCall(superConstructor).apply {
            // For suspend lambdas, pass the continuation parameter to CoroutineImpl's constructor
            if (functionReference.isSuspendLambda) {
                val continuation = constructor.parameters.singleOrNull { it.origin == IrDeclarationOrigin.CONTINUATION }
                if (continuation != null) {
                    arguments[0] = IrGetValueImpl(
                        startOffset = UNDEFINED_OFFSET,
                        endOffset = UNDEFINED_OFFSET,
                        type = continuation.type,
                        symbol = continuation.symbol
                    )
                }
            }
        }
    }
}

/**
 * Wrapper to integrate BrsCallableReferenceLowering into the lowering pipeline
 * as a FileLoweringPass.
 *
 * Uses the standard BodyLoweringPass traversal which properly visits all function
 * bodies including those in nested classes (e.g., constructors, property initializers).
 */
class BrsCallableReferenceLoweringPass(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val lowering = BrsCallableReferenceLowering(context)

    override fun lower(irFile: IrFile) {
        // Use the standard BodyLoweringPass traversal mechanism which properly
        // visits all bodies including those nested inside classes.
        lowering.runOnFilePostfix(irFile)
    }
}
