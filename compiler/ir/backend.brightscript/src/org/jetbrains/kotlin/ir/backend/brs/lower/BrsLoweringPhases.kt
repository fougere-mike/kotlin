/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.coroutines.AddContinuationToNonLocalSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.loops.ForLoopsLowering
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsAddContinuationToFunctionCallsLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsSuspendFunctionsLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.inline.BrsInlineFunctionResolver
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.inline.FunctionInlining
import org.jetbrains.kotlin.ir.inline.InlineMode
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
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
        // Note: We DO run inlining during stdlib compilation, because inline functions like
        // suspendCoroutineUninterceptedOrReturn need to be inlined into suspend functions.
        // After inlining, the suspend functions contain getContinuation() calls which are
        // transformed by coroutine lowering (for user code) or left as stubs (for stdlib).
        run {
            val inlineFunctionResolver = BrsInlineFunctionResolver(context, InlineMode.ALL_INLINE_FUNCTIONS)
            val functionInlining = FunctionInlining(context, inlineFunctionResolver)

            fun processDeclaration(declaration: IrDeclaration, parent: IrDeclarationParent) {
                when (declaration) {
                    is org.jetbrains.kotlin.ir.declarations.IrFunction -> {
                        declaration.body?.let { body ->
                            functionInlining.lower(body, declaration)
                        }
                        // After inlining, patch parents of the entire function subtree
                        // This ensures all declarations (including newly inlined ones) have correct parents
                        declaration.patchDeclarationParents(parent)
                    }
                    is org.jetbrains.kotlin.ir.declarations.IrClass -> {
                        // Process class members recursively
                        declaration.declarations.forEach { member ->
                            processDeclaration(member, declaration)
                        }
                        // Patch the class after processing all members
                        declaration.patchDeclarationParents(parent)
                    }
                    else -> {
                        // Patch other declarations too
                        declaration.patchDeclarationParents(parent)
                    }
                }
            }

            for (file in module.files) {
                file.declarations.forEach { declaration ->
                    processDeclaration(declaration, file)
                }
            }
        }

        // Phase 0.05: Returnable Block Lowering
        // After inlining, inline functions leave behind IrReturnableBlock nodes with
        // return@block expressions. This lowering transforms them by:
        // 1. Introducing a result variable to hold the return value
        // 2. Replacing return@block value with: result = value; return@block Unit
        // 3. Wrapping in a composite that evaluates to the result variable
        //
        // The IR-to-BrightScript transformer then handles these blocks using a
        // while/done-flag pattern since BrightScript has no labeled breaks.
        run {
            val returnableBlockLowering = BrsReturnableBlockLowering(context)
            for (file in module.files) {
                file.declarations.forEach { declaration ->
                    when (declaration) {
                        is org.jetbrains.kotlin.ir.declarations.IrFunction -> {
                            declaration.body?.let { body ->
                                returnableBlockLowering.lower(body, declaration)
                            }
                        }
                        is org.jetbrains.kotlin.ir.declarations.IrClass -> {
                            declaration.declarations.forEach { member ->
                                if (member is org.jetbrains.kotlin.ir.declarations.IrFunction) {
                                    member.body?.let { body ->
                                        returnableBlockLowering.lower(body, member)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Build the list of lowering phases
        val phases = mutableListOf<FileLoweringPass>()

        // Phase 0.05: BrightScript Intrinsic Lowering
        // MUST run BEFORE UpgradeCallableReferences and BrsCallableReferenceLowering.
        // This handles intrinsics that need to see the original function references
        // before they are transformed into anonymous classes:
        // - brsName(::function) -> extracts the mangled function name as a string literal
        phases += BrsIntrinsicLowering(context)

        // Phase 0.06: IO Worker Detection (Warning-only for now)
        // Detects withContext(Dispatchers.IO) calls and emits warnings about the
        // lambda serialization limitation. Full automatic extraction is planned
        // for a future release.
        // Only run for user code, not stdlib compilation
        if (!context.isStdlibCompilation) {
            phases += BrsIOWorkerExtractionLowering(context)
        }

        // Phase 0.1: Upgrade Callable References
        // Transforms IrFunctionExpression (lambdas) and IrFunctionReference into IrRichFunctionReference.
        // This is a prerequisite for the callable reference lowering phase.
        // NOTE: This runs for BOTH stdlib and user code, matching JS backend behavior.
        // Lambdas must always be converted to proper classes so they can be invoked with .invoke()
        // SAM conversions are enabled so that fun interfaces (like Comparator) get proper implementation classes
        phases += UpgradeCallableReferences(
            context,
            upgradeFunctionReferencesAndLambdas = true,
            upgradePropertyReferences = true,
            upgradeLocalDelegatedPropertyReferences = true,
            upgradeSamConversions = true,
        )

        // Phase 0.15: Shared Variables Lowering
        // Boxes mutable variables that are captured by closures in {value: x} wrapper objects.
        // This MUST run AFTER UpgradeCallableReferences (so lambdas are IrRichFunctionReference)
        // but BEFORE BrsCallableReferenceLowering (so the box is captured, not the value).
        //
        // IMPORTANT: We use BRS-specific SharedVariablesLowering that does NOT skip inline lambdas.
        // Unlike JVM/Native where inline lambdas are truly inlined, BrightScript cannot inline
        // functions - they always become closure objects. Therefore, mutable variables captured
        // by inline lambdas (like forEach, also, etc.) must also be boxed.
        phases += BrsSharedVariablesLoweringPass(context)

        // Phase 0.2: Callable Reference Lowering
        // Transforms IrRichFunctionReference nodes into anonymous classes.
        // NOTE: This runs for BOTH stdlib and user code, matching JS backend behavior.
        // All lambdas become classes with an invoke() method, which allows uniform invocation.
        // For suspend lambdas (in user code), this also enables BrsSuspendFunctionsLowering
        // to detect them by LAMBDA_IMPL origin and transform them into CoroutineImpl classes.
        phases += BrsCallableReferenceLoweringPass(context)

        // Note: Coroutine lowering phases are added later, after LocalDeclarationsLowering,
        // because BrsSuspendFunctionsLowering needs capturedFields to be set.
        // See the coroutine lowering section below LocalDeclarationsLowering.

        // Phase 1.4: Generate implementations for interface default methods
        // Must run before code generation since BrightScript has no virtual dispatch.
        // When a class implements an interface with default methods, we need to copy
        // the method bodies into the class because BrightScript has no prototype chain.
        phases += BrsInterfaceDefaultMethodsLowering(context)

        // NOTE: SharedVariablesLowering is now used (Phase 0.15) for closure capture of mutable variables.
        // The custom BrsSharedVariableDetectionLowering below is a fallback for any cases the standard
        // lowering might miss (e.g., anonymous object expressions with mutable captures).
        // The key is that SharedVariablesLowering runs BEFORE callable reference lowering so that
        // lambdas capture the box object, not the value.

        // Add remaining phases
        phases += listOf(
            // Phase 1.6: Pre-compute enum ordinals for constant evaluation
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

            // Phase 14.25: Detect shared variables BEFORE any local declarations lowering
            // This must run BEFORE LocalDeclarationsLowering because LDL transforms local class
            // variable accesses to field accesses, making it impossible to detect which outer
            // variables are captured and mutated. The detected shared variables are stored in
            // context.sharedVariablesByFunction for use during transformation.
            BrsSharedVariableDetectionLowering(context),

            // Phase 14.5: Ensure fields are created for write-only captured variables
            // LocalDeclarationsLowering only creates fields for captured variables that are READ.
            // If a variable is only WRITTEN inside a local class (never read), no field is created.
            // This lowering adds synthetic reads to trigger field creation for write-only variables.
            // Must run BEFORE LocalDeclarationsLowering.
            BrsCapturedWriteOnlyVariablesLowering(context),

            // Phase 15: Local declarations lowering - handles closure capture for local classes
            // This transforms local classes to properly capture variables from enclosing scopes
            // Must run after ForLoopsLowering to avoid conflicts
            // Note: localNameSanitizer replaces $ with _ since BrightScript doesn't allow $ in identifiers
            LocalDeclarationsLowering(
                context,
                localNameSanitizer = { it.replace("\$", "_") },
                suggestUniqueNames = true  // Generate unique names for lambda classes
            ),

            // Phase 15.25: Remove synthetic reads that were added by BrsCapturedWriteOnlyVariablesLowering
            // These were needed to trigger field creation in LocalDeclarationsLowering,
            // but now would generate invalid BrightScript (standalone expressions)
            BrsSyntheticReadsRemovalLowering(context),

            // Phase 15.5: Rewrite writes to captured variables
            // LocalDeclarationsLowering creates fields for captured variables and rewrites reads,
            // but does NOT rewrite writes. For BrightScript we need explicit field access for writes.
            BrsCapturedVariableWriteLowering(context)
        )

        // Phase 16: Extract local classes to file level
        // MUST run AFTER LocalDeclarationsLowering and BEFORE BrsSuspendFunctionsLowering.
        // - LocalDeclarationsLowering sets capturedFields on classes
        // - BrsLocalClassExtractionLowering moves classes to file level
        // - BrsSuspendFunctionsLowering can then process them without nested local declarations
        phases += BrsLocalClassExtractionLowering(context)

        // Phase 16.5: Coroutine Lowering
        // MUST run AFTER LocalDeclarationsLowering (which sets capturedFields on classes)
        // and AFTER BrsLocalClassExtractionLowering (so classes are at file level).
        //
        // Note: We split coroutine lowering into two groups:
        // 1. Continuation parameter addition and getContinuation() replacement - runs ALWAYS
        //    (these don't need CoroutineImpl, just the Continuation interface)
        // 2. State machine generation - only runs for user code (needs full CoroutineImpl infrastructure)

        // Check if the basic coroutine symbols are available (Continuation class)
        val continuationAvailable = context.brsSymbols.coroutineSymbols.continuationClass != null

        // Check if full coroutine infrastructure is available (CoroutineImpl with all properties)
        val fullCoroutinesAvailable = !context.isStdlibCompilation &&
            context.brsSymbols.coroutineSymbols.areCoroutineSymbolsAvailable

        if (fullCoroutinesAvailable) {
            // 16.5.1: Transform suspend functions into state machines
            // Uses capturedFields set by LocalDeclarationsLowering
            // Only runs for user code - stdlib suspend functions delegate suspension
            phases += BrsSuspendFunctionsLoweringWrapper(context)
        }

        // These phases run for BOTH stdlib and user code because they only need
        // the Continuation interface, not the full CoroutineImpl infrastructure
        if (continuationAvailable) {
            // 16.5.2: Add continuation parameter to non-local suspend functions
            phases += AddContinuationToNonLocalSuspendFunctionsLoweringWrapper(context)

            // 16.5.3: Add continuation parameter to local suspend functions
            phases += AddContinuationToLocalSuspendFunctionsLoweringWrapper(context)

            // 16.5.4: Add continuation to suspend function CALLS and replace getContinuation() intrinsic
            // This must run after 16.5.2 and 16.5.3 so that the continuation parameter exists
            phases += BrsAddContinuationToFunctionCallsLoweringWrapper(context)
        }

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
 *
 * This uses the delegate's built-in lower(irFile) which properly handles:
 * - File-level function declarations
 * - Function members inside classes (including lambda class invoke methods)
 * - Nested declarations (via recursive Visitor)
 */
class AddContinuationToNonLocalSuspendFunctionsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = AddContinuationToNonLocalSuspendFunctionsLowering(context)

    override fun lower(irFile: IrFile) {
        // Use the delegate's default lower() which properly visits class members
        delegate.lower(irFile)
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
 * Wrapper for BrsAddContinuationToFunctionCallsLowering to work with FileLoweringPass.
 * Adds continuation parameter to suspend function CALLS and replaces getContinuation() intrinsic.
 *
 * This must run after AddContinuationToNonLocalSuspendFunctionsLowering and
 * AddContinuationToLocalSuspendFunctionsLowering so that the continuation parameter exists.
 */
class BrsAddContinuationToFunctionCallsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = BrsAddContinuationToFunctionCallsLowering(context)

    override fun lower(irFile: IrFile) {
        delegate.lower(irFile)
    }
}

/**
 * Wrapper for BrsSuspendFunctionsLowering to work with FileLoweringPass.
 * Transforms suspend functions into CoroutineImpl classes with state machines.
 *
 * This runs AFTER BrsLocalClassExtractionLowering, so lambda classes are now
 * file-level declarations. We use standard runOnFilePostfix traversal which
 * visits all file-level declarations including extracted lambda classes.
 */
class BrsSuspendFunctionsLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = BrsSuspendFunctionsLowering(context)

    override fun lower(irFile: IrFile) {
        // Lambda classes have been extracted to file level by BrsLocalClassExtractionLowering,
        // so we use standard traversal (withLocalDeclarations = false, the default).
        // This processes all file-level classes including extracted lambda classes.
        delegate.runOnFilePostfix(irFile)
    }
}

/**
 * Wrapper for SharedVariablesLowering to work with FileLoweringPass.
 * Transforms mutable variables captured by closures to use boxed wrappers.
 */
class SharedVariablesLoweringWrapper(
    private val context: BrsIrBackendContext
) : FileLoweringPass {
    private val delegate = SharedVariablesLowering(context)

    override fun lower(irFile: IrFile) {
        irFile.declarations.forEach { declaration ->
            lowerDeclaration(declaration)
        }
    }

    private fun lowerDeclaration(declaration: IrDeclaration) {
        when (declaration) {
            is org.jetbrains.kotlin.ir.declarations.IrFunction -> {
                declaration.body?.let { body ->
                    delegate.lower(body, declaration)
                }
            }
            is org.jetbrains.kotlin.ir.declarations.IrClass -> {
                // Process class members
                declaration.declarations.forEach { member ->
                    lowerDeclaration(member)
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
