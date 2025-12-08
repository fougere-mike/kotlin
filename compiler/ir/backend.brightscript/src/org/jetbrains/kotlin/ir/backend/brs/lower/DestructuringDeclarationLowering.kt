/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Lowers destructuring declarations by renaming <destruct> placeholder variables
 * to proper temporary variable names.
 *
 * For example:
 *   val (a, b) = pair
 *
 * Is represented in IR as:
 *   val <destruct> = pair
 *   val a = <destruct>.component1()
 *   val b = <destruct>.component2()
 *
 * This lowering renames <destruct> to __destruct_N to avoid invalid BrightScript syntax.
 */
class DestructuringDeclarationLowering(
    @Suppress("UNUSED_PARAMETER") private val context: BrsIrBackendContext
) : FileLoweringPass {

    private var destructCounter = 0

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(DestructuringTransformer())
    }

    private inner class DestructuringTransformer : IrElementTransformerVoid() {

        override fun visitVariable(declaration: IrVariable): IrStatement {
            // Check if this is a <destruct> placeholder variable
            if (declaration.name == SpecialNames.DESTRUCT) {
                // Create a new name for this variable
                val newName = Name.identifier("__destruct_${destructCounter++}")
                declaration.name = newName
            }
            return super.visitVariable(declaration)
        }
    }
}
