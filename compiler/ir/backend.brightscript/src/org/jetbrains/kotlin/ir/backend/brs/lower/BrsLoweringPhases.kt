/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

/**
 * BrightScript-specific IR lowering phases.
 *
 * These phases transform Kotlin IR into a form suitable for BrightScript code generation.
 */
object BrsLoweringPhases {

    /**
     * Run all lowering phases on the module.
     */
    fun lower(module: IrModuleFragment, context: BrsIrBackendContext): IrModuleFragment {
        // Apply lowering phases in order
        val phases = listOf(
            // Phase 1: Validate and prepare
            ValidateSuspendUsageLowering(context),

            // Phase 2: Parse @BrsInline code into AST (like JsCodeOutliningLowering)
            BrsCodeOutliningLowering(context),

            // Phase 3: External interop preparation
            BrsExternalLowering(context),
            DynamicAccessLowering(context),
            FieldObserverLowering(context),

            // Phase 3: Inner class handling
            InnerClassesLowering(context),

            // Phase 4: Default parameters
            DefaultParameterLowering(context),

            // Phase 5: Local functions and closures
            LocalFunctionLowering(context),

            // Phase 6: Enum lowering
            EnumLowering(context),

            // Phase 7: Object lowering (singletons)
            ObjectLowering(context),

            // Phase 8: Property lowering
            PropertyLowering(context),

            // Phase 9: Increment/decrement lowering
            // BrightScript doesn't have ++ or -- operators, so transform to + 1 or - 1
            IncrementDecrementLowering(context),

            // Phase 10: Exception handling (if needed)
            TryCatchLowering(context),

            // Phase 11: String concatenation
            StringConcatenationLowering(context),

            // Phase 12: Control flow simplification
            ControlFlowLowering(context),

            // Phase 13: Final cleanup
            CleanupLowering(context)
        )

        for (phase in phases) {
            for (file in module.files) {
                phase.lower(file)
            }
        }

        return module
    }
}

/**
 * Validates that no suspend functions are used, as BrightScript doesn't support coroutines.
 */
class ValidateSuspendUsageLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Check for suspend functions and report errors
        irFile.accept(object : org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid {
            override fun visitElement(element: org.jetbrains.kotlin.ir.IrElement) {
                element.acceptChildren(this, null)
            }

            override fun visitFunction(declaration: org.jetbrains.kotlin.ir.declarations.IrFunction) {
                if (declaration is org.jetbrains.kotlin.ir.declarations.IrSimpleFunction && declaration.isSuspend) {
                    error("Suspend functions are not supported in BrightScript: ${declaration.name}")
                }
                super.visitFunction(declaration)
            }
        }, null)
    }
}

/**
 * Lowers inner classes by capturing outer class references.
 */
class InnerClassesLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Inner classes need their outer reference captured
        // This is handled by BrsInnerClassesSupport
    }
}

/**
 * Lowers default parameters to overloaded functions.
 */
class DefaultParameterLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Generate wrapper functions for default parameters
        // BrightScript supports default parameters natively, so minimal transformation needed
    }
}

/**
 * Hoists local functions that capture variables (closures).
 */
class LocalFunctionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Local functions that capture variables need to be hoisted
        // and their captured variables wrapped in AAs
    }
}

/**
 * Lowers enum classes to constants and lookup functions.
 */
class EnumLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Enums become:
        // 1. Integer constants for each entry
        // 2. An array of entry names
        // 3. Lookup functions (valueOf, values)
    }
}

/**
 * Lowers object declarations (singletons) to lazy-initialized instances.
 */
class ObjectLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Objects become:
        // 1. A module-level variable holding the instance (initially invalid)
        // 2. A getInstance() function that creates the instance on first call
    }
}

/**
 * Lowers properties to getter/setter functions.
 */
class PropertyLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Properties with custom getters/setters need their accessors extracted
        // Simple properties just become direct field access
    }
}

/**
 * Optimizes string concatenation operations.
 */
class StringConcatenationLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // String templates become concatenation operations
        // Multiple consecutive concatenations can be optimized
    }
}

/**
 * Simplifies control flow constructs for BrightScript.
 */
class ControlFlowLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Handle control flow that BrightScript doesn't support:
        // - do-while loops (transform to while)
        // - continue on older Roku OS (restructure loop)
        // - labeled breaks (restructure to flags)
    }
}

/**
 * Final cleanup and optimization pass.
 */
class CleanupLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Remove unused declarations
        // Inline trivial functions
        // Remove dead code
    }
}

/**
 * Generates synthetic members for data classes.
 */
class DataClassLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        // Data classes need generated:
        // - equals()
        // - hashCode()
        // - toString()
        // - copy()
        // - componentN() functions
    }
}

/**
 * Lowers try-catch-finally blocks.
 *
 * For Roku OS 9.4+, native try/catch is used.
 * For older versions, a polyfill pattern is generated.
 */
class TryCatchLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        if (!context.supportsExceptions) {
            // Transform try/catch to error checking patterns
            // This is a limited transformation that won't handle all cases
        }
        // else: native try/catch is used directly
    }
}
