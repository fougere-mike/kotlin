/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToNonLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.loops.ForLoopsLowering
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsSuspendFunctionsLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.inline.BrsInlineFunctionResolver
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.inline.FunctionInlining
import org.jetbrains.kotlin.ir.inline.InlineMode
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid

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
        // Phase 0: Inline all inline functions FIRST
        // This must happen before any other lowering because:
        // 1. Inline functions like run, let, also, apply should be inlined at call sites
        // 2. The inlined code may contain constructs that other lowering phases need to process
        // 3. @InlineOnly functions (like stdlib's run, let, etc.) only exist as inline and must be inlined
        //
        // Note: During stdlib compilation, we skip inlining because:
        // - Coroutine symbols aren't available yet
        // - Stdlib functions being inlined are defined in stdlib itself
        // User code compilations DO get inlining since they link against the compiled stdlib
        if (!context.isStdlibCompilation) {
            val inlineFunctionResolver = BrsInlineFunctionResolver(context, InlineMode.ALL_INLINE_FUNCTIONS)
            val functionInlining = FunctionInlining(context, inlineFunctionResolver)
            for (file in module.files) {
                file.declarations.forEach { declaration ->
                    if (declaration is org.jetbrains.kotlin.ir.declarations.IrFunction) {
                        declaration.body?.let { body ->
                            functionInlining.lower(body, declaration)
                        }
                    }
                }
            }
        }

        // Build the list of lowering phases
        val phases = mutableListOf<FileLoweringPass>()

        // Phase 1: Coroutine Lowering (must run early)
        // Skip during stdlib compilation or when coroutine symbols aren't available (e.g., in tests without stdlib)
        val coroutinesAvailable = !context.isStdlibCompilation &&
            context.brsSymbols.coroutineSymbols.areCoroutineSymbolsAvailable
        if (coroutinesAvailable) {
            // 1.1: Add continuation parameter to non-local suspend functions
            phases += AddContinuationToNonLocalSuspendFunctionsLoweringWrapper(context)

            // 1.2: Add continuation parameter to local suspend functions
            phases += AddContinuationToLocalSuspendFunctionsLoweringWrapper(context)

            // 1.3: Transform suspend functions into state machines
            phases += BrsSuspendFunctionsLoweringWrapper(context)
        }

        // Add remaining phases
        phases += listOf(
            // Phase 1.5: Pre-compute enum ordinals for constant evaluation
            // Must run before BrsConstantEvaluationLowering so enum ordinals are available
            BrsEnumOrdinalPrecomputeLowering(context),

            // Phase 1.5: Evaluate @BrsConstant objects
            // Must run early before other lowerings transform the IR
            BrsConstantEvaluationLowering(context),

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

            // Phase 10: Assignment extraction
            // BrightScript doesn't support assignment expressions (assignments are statements only)
            // Extract assignments from expression contexts like: if ((count = count + 1) > 1)
            AssignmentExtractionLowering(context),

            // Phase 11: Exception handling (if needed)
            TryCatchLowering(context),

            // Phase 11: String concatenation
            StringConcatenationLowering(context),

            // Phase 12: When expression lowering
            // BrightScript doesn't support inline if-then-else, so when expressions
            // are transformed to blocks with temp variables
            BrsWhenExpressionLowering(context),

            // Phase 12.5: Destructuring declarations
            // Renames <destruct> placeholder variables to proper temp names
            // This must run before ForLoopsLowering to handle all destructuring cases
            DestructuringDeclarationLowering(context),

            // Phase 13: For loop optimization and destructuring
            // Transforms destructuring declarations in for loops (e.g., for ((a, b) in list))
            // and optimizes iteration over progressions and arrays
            ForLoopsLowering(context),

            // Phase 13: Control flow simplification
            ControlFlowLowering(context),

            // Phase 14: Final cleanup
            CleanupLowering(context),

            // Phase 15: Local declarations lowering - handles closure capture for local classes
            // This transforms local classes to properly capture variables from enclosing scopes
            // Must run after ForLoopsLowering to avoid conflicts
            // Note: localNameSanitizer replaces $ with _ since BrightScript doesn't allow $ in identifiers
            LocalDeclarationsLowering(
                context,
                localNameSanitizer = { it.replace("\$", "_") },
                suggestUniqueNames = false
            ),

            // Phase 16: Extract local classes to file level
            // After LocalDeclarationsLowering handles the closure capture, this pass moves
            // local classes to file level so they can be properly emitted.
            BrsLocalClassExtractionLowering(context)
        )

        for (phase in phases) {
            for (file in module.files) {
                phase.lower(file)
            }
        }

        return module
    }
}

// ==================== Coroutine Lowering Wrappers ====================

/**
 * Wrapper for AddContinuationToNonLocalSuspendFunctionsLowering to work with FileLoweringPass.
 * Adds $completion continuation parameter to non-local suspend functions.
 */
class AddContinuationToNonLocalSuspendFunctionsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = AddContinuationToNonLocalSuspendFunctionsLowering(context)

    override fun lower(irFile: IrFile) {
        irFile.declarations.toList().forEach { declaration ->
            delegate.transformFlat(declaration)?.let { transformed ->
                val index = irFile.declarations.indexOf(declaration)
                if (index >= 0) {
                    irFile.declarations.removeAt(index)
                    irFile.declarations.addAll(index, transformed)
                }
            }
        }
    }
}

/**
 * Wrapper for AddContinuationToLocalSuspendFunctionsLowering to work with FileLoweringPass.
 * Adds $completion continuation parameter to local suspend functions.
 */
class AddContinuationToLocalSuspendFunctionsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = AddContinuationToLocalSuspendFunctionsLowering(context)

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            if (declaration is org.jetbrains.kotlin.ir.declarations.IrFunction) {
                declaration.body?.let { body ->
                    delegate.lower(body, declaration)
                }
            }
        }
    }
}

/**
 * Wrapper for BrsSuspendFunctionsLowering to work with FileLoweringPass.
 * Transforms suspend functions into CoroutineImpl classes with state machines.
 */
class BrsSuspendFunctionsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = BrsSuspendFunctionsLowering(context)

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            if (declaration is org.jetbrains.kotlin.ir.declarations.IrFunction) {
                declaration.body?.let { body ->
                    delegate.lower(body, declaration)
                }
            }
        }
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
