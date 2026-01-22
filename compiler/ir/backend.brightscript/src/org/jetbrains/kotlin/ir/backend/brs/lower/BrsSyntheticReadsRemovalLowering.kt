/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Removes synthetic read composites that were added by BrsCapturedWriteOnlyVariablesLowering.
 *
 * BrsCapturedWriteOnlyVariablesLowering adds synthetic IrGetValue reads wrapped in IrComposite
 * to trigger field creation in LocalDeclarationsLowering. After LDL runs, these become
 * IrComposite containing IrGetField statements, which produce invalid BrightScript when
 * transformed (standalone expressions like `m._result.value` are syntax errors).
 *
 * This lowering runs AFTER LocalDeclarationsLowering and removes these synthetic composites.
 * We identify them by:
 * 1. They are IrComposite with unit type
 * 2. They contain only IrGetField expressions
 * 3. They appear at the start of a function body
 */
class BrsSyntheticReadsRemovalLowering(
    @Suppress("unused")
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        // Visit all functions in the file, including those nested inside other functions
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunction(declaration: IrFunction) {
                val body = declaration.body as? IrBlockBody
                if (body != null) {
                    removeSyntheticReads(body)
                }
                // Continue visiting nested elements (like local classes inside this function)
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun removeSyntheticReads(body: IrBlockBody) {
        // Remove composites at the start of the body that only contain get operations
        val iterator = body.statements.listIterator()
        while (iterator.hasNext()) {
            val statement = iterator.next()
            if (isSyntheticReadComposite(statement)) {
                iterator.remove()
            } else {
                // Stop after the first non-synthetic statement
                // (synthetic reads are always at the start)
                break
            }
        }
    }

    private fun isSyntheticReadComposite(element: IrElement): Boolean {
        // Must be a composite
        if (element !is IrComposite) return false

        // Must have unit type (void effect)
        if (!element.type.isUnit()) return false

        // Must contain only read operations (IrGetField or IrGetValue)
        // These are side-effect-free expressions that serve no purpose as statements
        return element.statements.all { statement ->
            statement is IrGetField || statement is IrGetValue
        }
    }
}
