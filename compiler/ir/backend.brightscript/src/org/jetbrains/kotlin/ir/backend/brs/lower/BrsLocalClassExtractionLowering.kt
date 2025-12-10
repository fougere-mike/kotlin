/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Extracts local classes (including anonymous object expressions) from function bodies
 * and moves them to the file level so they can be properly emitted as BrightScript functions.
 *
 * This is necessary because:
 * 1. Anonymous object expressions like `object : Sequence<T> { ... }` create IrClass nodes
 *    nested inside function bodies
 * 2. The main file transformation only iterates over top-level declarations
 * 3. Without extraction, these classes would be referenced but never defined in BrightScript
 *
 * This pass should run AFTER LocalDeclarationsLowering (which handles closure capture)
 * but BEFORE the code generation phase.
 */
class BrsLocalClassExtractionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val localClasses = mutableListOf<IrClass>()

        // Collect all local classes from function bodies
        irFile.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Visit children first to find nested local classes
                declaration.acceptChildrenVoid(this)

                // Collect local classes (those defined inside functions)
                if (declaration.isLocal) {
                    localClasses.add(declaration)
                }
            }
        })

        // Move local classes to file level
        for (localClass in localClasses) {
            // Change parent to file
            localClass.parent = irFile

            // Add to file declarations if not already there
            if (localClass !in irFile.declarations) {
                irFile.declarations.add(localClass)
            }
        }
    }
}
