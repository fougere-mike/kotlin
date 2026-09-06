/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ir.addExtensionReceiver
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.Name

/**
 * Component lifecycle driver synthesis (spec 2026-09-04-component-lifecycle §5.3).
 *
 * For every CONCRETE render component whose user hierarchy overrides
 * `onStart`, adds a member
 *
 * ```kotlin
 * private fun __kotlinStartDriver() {
 *     if (!kotlinLifecycleClaimDriver()) return
 *     launch { awaitReady(); onStart() }
 * }
 * ```
 *
 * and appends an init block calling it (the LAST init statement — the body
 * runs on the next pump tick anyway). Runs at phase 0.054, BEFORE
 * UpgradeCallableReferences and every coroutine lowering, so the suspend
 * lambda is still an IrFunctionExpression that the later passes turn into a
 * real state machine — the whole point: the stdlib cannot host this driver
 * (no state machines in stdlib compilation).
 *
 * `onStart()` is an ordinary dispatch-receiver call, emitted as the
 * `m.onStart_…_k_` slot; slot attachment layers by SceneGraph init order
 * (base first) and the body runs after the whole cascade, so the slot
 * resolves to the most-derived override. A concrete base and its concrete
 * subclass both emit a driver; `kotlinLifecycleClaimDriver` makes the second
 * call a no-op. Abstract classes, task components and ContentNode components
 * get nothing. User-mode modules only: stdlib compilation bails on the OrNull
 * symbols.
 */
class BrsComponentLifecycleLowering(private val context: BrsIrBackendContext) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val launch = context.brsSymbols.componentLaunchOrNull ?: return
        val awaitReady = context.brsSymbols.awaitReadyOrNull ?: return
        val claim = context.brsSymbols.kotlinLifecycleClaimDriverOrNull ?: return
        val coroutineScope = context.brsSymbols.coroutineScopeClassOrNull ?: return

        for (declaration in irFile.declarations.toList()) {
            val irClass = declaration as? IrClass ?: continue
            if (!context.intrinsics.isSceneGraphComponent(irClass)) continue
            if (irClass.modality == Modality.ABSTRACT) continue
            if (!context.intrinsics.componentNeedsOnKeyEvent(irClass)) continue   // render components only
            val onStart = context.intrinsics.hierarchyOverrides(irClass, "onStart") { fn ->
                fn.isSuspend && fn.valueParameters.isEmpty()
            } ?: continue
            if (irClass.functions.any { it.name.asString() == DRIVER_NAME }) continue
            synthesizeDriver(irClass, onStart, launch.owner, awaitReady.owner, claim.owner, coroutineScope.owner)
        }
    }

    private fun synthesizeDriver(
        irClass: IrClass,
        onStartOverride: IrSimpleFunction,
        launch: IrSimpleFunction,
        awaitReady: IrSimpleFunction,
        claim: IrSimpleFunction,
        coroutineScope: IrClass,
    ) {
        val unit = context.irBuiltIns.unitType
        val bool = context.irBuiltIns.booleanType

        // The member. Emitted by the component transformer as a global over m
        // and attached as m.__kotlinStartDriver_k_ like every own member.
        val driver = irClass.addFunction(
            name = DRIVER_NAME,
            returnType = unit,
            modality = Modality.FINAL,
            visibility = DescriptorVisibilities.PRIVATE,
        )
        val driverThis = driver.dispatchReceiverParameter
            ?: error("addFunction did not create a dispatch receiver for ${irClass.name}.$DRIVER_NAME")
        fun thisRead() = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, driverThis.type, driverThis.symbol)

        // The block: suspend CoroutineScope.() -> Unit { awaitReady(this@comp); this@comp.onStart() }
        val lambda = context.irFactory.buildFun {
            startOffset = irClass.startOffset
            endOffset = irClass.endOffset
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            name = Name.special("<anonymous>")
            visibility = DescriptorVisibilities.LOCAL
            modality = Modality.FINAL
            returnType = unit
            isSuspend = true
        }
        lambda.addExtensionReceiver(coroutineScope.defaultType)
        lambda.parent = driver
        val awaitCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, awaitReady.symbol, typeArgumentsCount = 0).apply {
            extensionReceiver = thisRead()
        }
        // The hook resolved against THIS class (a fake override when inherited):
        // the backend emits it as the m-slot call, which is the point. Falls
        // back to the real override found by the hierarchy walk should a class
        // carry no fake override — the dispatch-receiver call emits the same slot.
        val onStartSymbol = irClass.functions
            .firstOrNull { it.name.asString() == "onStart" && it.isSuspend && it.valueParameters.isEmpty() }
            ?.symbol
            ?: onStartOverride.symbol
        val hookCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, onStartSymbol, typeArgumentsCount = 0).apply {
            dispatchReceiver = thisRead()
        }
        lambda.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(awaitCall, hookCall))
        val lambdaType = context.irBuiltIns.suspendFunctionN(1).typeWith(coroutineScope.defaultType, unit)

        // if (!kotlinLifecycleClaimDriver()) return
        val claimCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, bool, claim.symbol, typeArgumentsCount = 0)
        val notClaimed = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, bool, context.irBuiltIns.booleanNotSymbol, typeArgumentsCount = 0).apply {
            dispatchReceiver = claimCall
        }
        val earlyReturn = IrReturnImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.nothingType, driver.symbol,
            IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, context.irBuiltIns.unitClass)
        )
        val guard = IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, IrStatementOrigin.IF).apply {
            branches.add(IrBranchImpl(notClaimed, earlyReturn))
        }

        // launch(this, <default context>, block)
        val launchCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, launch.returnType, launch.symbol, typeArgumentsCount = 0).apply {
            extensionReceiver = thisRead()
            putValueArgument(0, null)   // context: default (EmptyCoroutineContext) — filled callee-side
            putValueArgument(1, IrFunctionExpressionImpl(irClass.startOffset, irClass.endOffset, lambdaType, lambda, IrStatementOrigin.LAMBDA))
        }
        driver.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(guard, launchCall))

        // Init tail: an anonymous initializer appended LAST calling the driver.
        val initializer = context.irFactory.createAnonymousInitializer(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.DEFINED, IrAnonymousInitializerSymbolImpl(), isStatic = false
        )
        initializer.parent = irClass
        val classThis = irClass.thisReceiver ?: error("component ${irClass.name} has no this receiver")
        initializer.body = context.irFactory.createBlockBody(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            listOf(
                IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, driver.symbol, typeArgumentsCount = 0).apply {
                    dispatchReceiver = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, classThis.type, classThis.symbol)
                }
            )
        )
        irClass.declarations.add(initializer)
        irClass.patchDeclarationParents(irClass.parent)
    }

    companion object {
        const val DRIVER_NAME = "__kotlinStartDriver"
    }
}
