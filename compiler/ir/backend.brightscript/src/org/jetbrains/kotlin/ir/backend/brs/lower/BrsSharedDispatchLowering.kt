/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.brs.backend.ast.BrsAAEntry
import org.jetbrains.kotlin.brs.backend.ast.BrsAALiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsBinaryOp
import org.jetbrains.kotlin.brs.backend.ast.BrsBinaryOperator
import org.jetbrains.kotlin.brs.backend.ast.BrsBlock
import org.jetbrains.kotlin.brs.backend.ast.BrsDeclaration
import org.jetbrains.kotlin.brs.backend.ast.BrsDotAccess
import org.jetbrains.kotlin.brs.backend.ast.BrsExpression
import org.jetbrains.kotlin.brs.backend.ast.BrsExpressionStatement
import org.jetbrains.kotlin.brs.backend.ast.BrsFunction
import org.jetbrains.kotlin.brs.backend.ast.BrsFunctionCall
import org.jetbrains.kotlin.brs.backend.ast.BrsIdentifier
import org.jetbrains.kotlin.brs.backend.ast.BrsIf
import org.jetbrains.kotlin.brs.backend.ast.BrsIndexAccess
import org.jetbrains.kotlin.brs.backend.ast.BrsIntLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsParameter
import org.jetbrains.kotlin.brs.backend.ast.BrsReturn
import org.jetbrains.kotlin.brs.backend.ast.BrsStatement
import org.jetbrains.kotlin.brs.backend.ast.BrsStringLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsSub
import org.jetbrains.kotlin.brs.backend.ast.BrsThrow
import org.jetbrains.kotlin.brs.backend.ast.BrsVariable
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.resolveFakeOverride

/**
 * SharedService static dispatch (design §4, Task 6 of the SharedService
 * program): call sites whose receiver reaches `kotlin.brs.SharedService`
 * never read fn slots — they compile to direct static calls to the
 * extension-shaped implementations Task 5 emits (`C_f_<mangle>(recv,
 * args...)`, receiver FIRST), to generated `__proto`-name dispatchers for
 * open/abstract members reached through a non-final static type
 * (`Base_f_<mangle>__dispatch(recv, args...)`), or to direct member
 * reads/writes for simple val/var accessors. `super.f()` compiles to a
 * direct static call to the resolved superclass implementation (pre-Task-6
 * it emitted a self-recursive slot call).
 *
 * WHY THIS IS NOT A PIPELINE FileLoweringPass (the plan's original shape —
 * "register after BrsScopeRunBlockLowering, before UpgradeCallableReferences"
 * — was written before Task 5's implementation choice and cannot work):
 * - The static targets have no IR symbols. Task 5 injects the receiver
 *   parameter at EMISSION time ([IrToBrsTransformer.transformFunction]);
 *   there is no top-level IrSimpleFunction an early IrCall rewrite could be
 *   retargeted to.
 * - A synthesized IR stub cannot be named at phase 0.05x: suspend members'
 *   final mangled names (the Continuation parameter is part of the mangle,
 *   e.g. `slowHook_I_ContinuationI_k_`) only exist after the continuation
 *   lowerings at phase 16.5.x — the same mangle-changing transform
 *   `BrsCompiler.populateScopeBindingTables` documents and works around by
 *   resolving names from CURRENT declarations.
 * - Marking stubs `external` to suppress duplicate emission would skip
 *   `recordFunctionDependency` at the call site, silently breaking the
 *   include-closure recording static dispatch is supposed to provide.
 *
 * The pieces therefore live where the names live:
 * - [BrsSharedDispatchRegistry]: the whole-module registry (shared classes →
 *   concrete descendants), built by [org.jetbrains.kotlin.ir.backend.brs.BrsCompiler]
 *   after lowering + the manifest pass (so class-name uniquification order is
 *   untouched), stored on the context.
 * - [classifySharedCall]: call-site classification, consulted by
 *   [IrExpressionToBrsTransformer] where the receiver-first extension call
 *   shape already lives.
 * - [buildSharedDispatcher]: dispatcher generation, invoked next to the
 *   declaring implementation's emission (the wrapper-slot single-source
 *   precedent) so each dispatcher is defined exactly once per module, in the
 *   declaring class's file.
 */

/**
 * Dispatcher name = the implementation's emitted name + this suffix.
 * Collision-free after the mangled `_k_` for the same reason the wrapper
 * slots' `__slot` is: user-code globals always end in `_k_`.
 */
internal const val SHARED_DISPATCH_SUFFIX = "__dispatch"

/**
 * True when [function] emits EXTENSION-SHAPED because it is a member of a
 * SharedService-reaching class (design §4.1, the static-dispatch program):
 * the implementation global takes the receiver as an explicit FIRST
 * parameter named `m` (the existing extension convention — suspend members
 * therefore order `(m, params..., _completion)`), and the constructor
 * attaches a thin forwarding wrapper slot in its place (see
 * [IrToBrsTransformer.buildSharedSlotWrapper]) so residual slot-shaped call
 * sites keep working while normal call sites go static ([classifySharedCall]).
 *
 * Deliberately NOT treated:
 * - SIMPLE property accessors (default accessor of a final, non-overriding,
 *   non-delegated property with a backing field): their call sites become
 *   direct member reads under static dispatch (data-class precedent), so
 *   their m-reading globals stay directly attached, byte-identical.
 * - Member EXTENSION functions (dispatch + extension receiver): their
 *   emitted `m` parameter is the extension receiver; a second injected
 *   receiver would collide. They remain slot-dispatched (documented
 *   residual, same bucket as Any/interface-typed receivers).
 * - DATA-CLASS generated-member NAMES (equals/hashCode/toString/copy/
 *   componentN) of a shared data class: the data-class emitters generate
 *   these as simple-named m-reading globals and skip same-named class
 *   members BY NAME — no extension-shaped impl exists, so call sites must
 *   stay on the simple-named slot shapes the data-class ctor attaches
 *   (treating them compiled call sites to NONEXISTENT mangled statics —
 *   the Task 6 review's C1). The exclusion is name-based, not origin-based,
 *   deliberately: a HAND-WRITTEN equals/hashCode/toString in a data class is
 *   also name-skipped at emission (the generated global wins), so an
 *   origin-only check would re-open the same nonexistent-target hole for it.
 *   Other hand-written members of shared data classes ARE treated, since
 *   they flow through transformFunction like any other member.
 */
internal fun isSharedExtensionShapedFunction(function: IrFunction, context: BrsIrBackendContext): Boolean {
    if (function !is IrSimpleFunction) return false
    if (function.isFakeOverride || function.isExternal) return false
    if (function.dispatchReceiverParameter == null) return false
    if (function.extensionReceiverParameter != null) return false
    val parentClass = function.parentClassOrNull ?: return false
    if (!context.intrinsics.isSharedServiceClass(parentClass)) return false
    if (parentClass.isData && isDataClassGeneratedMemberName(function.name.asString())) return false
    val property = function.correspondingPropertySymbol?.owner
    if (property != null && isSimpleSharedAccessor(function, property)) return false
    return true
}

/**
 * Member names the data-class emitters OWN: they generate simple-named
 * m-reading globals for these (`C_equals`, `C_copy`, `C_component1`, ...)
 * and skip same-named class members at emission. componentN is digit-checked
 * — `component1` is generated, `componentFoo` is an ordinary member. The
 * single source for the shared-dispatch exclusion above AND the emission/
 * attachment skips in IrToBrsTransformer (keeping all three aligned is what
 * prevents call sites targeting functions that were never emitted).
 */
internal fun isDataClassGeneratedMemberName(name: String): Boolean {
    if (name == "equals" || name == "hashCode" || name == "toString" || name == "copy") return true
    return name.startsWith("component") && name.removePrefix("component").toIntOrNull() != null
}

/**
 * A simple accessor: the default accessor of a final, non-overriding,
 * non-delegated property with a backing field — reads/writes the field and
 * nothing else, so no override can ever change its behavior and static
 * dispatch replaces its call sites with direct member access.
 */
internal fun isSimpleSharedAccessor(accessor: IrSimpleFunction, property: IrProperty): Boolean {
    return accessor.origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR &&
        property.backingField != null &&
        !property.isDelegated &&
        accessor.overriddenSymbols.isEmpty() &&
        accessor.modality == Modality.FINAL
}

/**
 * True when [function] needs a `__proto`-name dispatcher: an extension-shaped
 * shared member that is open/abstract AND declared in a class subtypes can
 * see it through (non-final). A member declared in a FINAL class needs no
 * dispatcher — every call site's receiver static type is that class, so
 * every call site compiles to a direct static call.
 */
internal fun sharedDispatcherCandidate(function: IrFunction, context: BrsIrBackendContext): Boolean {
    if (function !is IrSimpleFunction) return false
    if (!isSharedExtensionShapedFunction(function, context)) return false
    if (function.modality != Modality.OPEN && function.modality != Modality.ABSTRACT) return false
    val parentClass = function.parentClassOrNull ?: return false
    return parentClass.modality != Modality.FINAL
}

/**
 * Whole-module registry: every SharedService-reaching class declared in the
 * CURRENT compilation, and for each of them the concrete (instantiable)
 * classes that are it-or-its-descendants — the dispatcher if-chain domain.
 * Closed world by design (single-module, the ScopeHandle run{}-block
 * documented constraint); descendants are sorted by emitted class name for
 * deterministic dispatcher output ("source-name order").
 *
 * Built AFTER the function-manifest pass: [BrsIrBackendContext.getBrsName]
 * assigns uniquifying suffixes first-come, so the first exhaustive walk over
 * the module must stay the manifest pass (which visits every class in file
 * order) — this registry only re-reads cached names.
 */
class BrsSharedDispatchRegistry private constructor(
    private val moduleSharedClasses: Set<IrClass>,
    private val concreteDescendants: Map<IrClass, List<IrClass>>,
) {
    /** Whether [irClass] is a shared class DECLARED IN THIS COMPILATION (dependency-klib classes answer false). */
    fun isModuleClass(irClass: IrClass): Boolean = irClass in moduleSharedClasses

    /** Concrete self-or-descendants of [irClass] in this compilation, sorted by emitted class name. */
    fun concreteDescendantsOf(irClass: IrClass): List<IrClass> = concreteDescendants[irClass] ?: emptyList()

    companion object {
        fun build(module: IrModuleFragment, context: BrsIrBackendContext): BrsSharedDispatchRegistry {
            val shared = mutableSetOf<IrClass>()
            fun collect(declarations: List<IrDeclaration>) {
                for (declaration in declarations) {
                    if (declaration is IrClass) {
                        if (context.intrinsics.isSharedServiceClass(declaration)) shared.add(declaration)
                        collect(declaration.declarations)
                    }
                }
            }
            for (file in module.files) collect(file.declarations)

            val descendants = mutableMapOf<IrClass, MutableList<IrClass>>()
            for (irClass in shared) {
                // Concrete = instantiable: abstract and sealed classes never
                // appear as a runtime __proto head of their own.
                if (irClass.modality == Modality.ABSTRACT || irClass.modality == Modality.SEALED) continue
                var current: IrClass? = irClass
                while (current != null && context.intrinsics.isSharedServiceClass(current)) {
                    // Dependency-klib ancestors (e.g. SharedService itself) are
                    // walked over but get no entry: dispatchers are only
                    // generated for classes emitted by this compilation.
                    if (current in shared) descendants.getOrPut(current) { mutableListOf() }.add(irClass)
                    current = superclassOf(current)
                }
            }
            for (list in descendants.values) list.sortBy { context.getBrsName(it) }
            return BrsSharedDispatchRegistry(shared, descendants)
        }
    }
}

private fun superclassOf(irClass: IrClass): IrClass? =
    irClass.superTypes.firstNotNullOfOrNull { superType ->
        superType.classOrNull?.owner?.takeIf { !it.isInterface }
    }

/** How a shared-receiver call site compiles ([classifySharedCall]). */
internal sealed class SharedCallShape {
    /** Simple val/var accessor → direct member read/write on the receiver. */
    class DirectFieldAccess(val property: IrProperty, val isSetter: Boolean) : SharedCallShape()

    /** Statically-known implementation → `C_f_<mangle>(recv, args...)`. */
    class StaticImpl(val target: IrSimpleFunction) : SharedCallShape()

    /** Open/abstract member through a non-final static type → `Base_f_<mangle>__dispatch(recv, args...)`. */
    class Dispatch(val declaration: IrSimpleFunction) : SharedCallShape()
}

/**
 * Classify a dispatch-receiver call for SharedService static dispatch, or
 * null when the call stays on today's path. Null (documented residuals):
 * receivers statically typed as an interface or Any (the callee's parent is
 * not a shared class), member extensions and synthetic data-class members
 * (not extension-shaped), and open/abstract members of DEPENDENCY-KLIB
 * shared classes (their dispatcher would live in a compilation that cannot
 * see this module's leaves — closed world; the stdlib's own SharedService
 * has only final members, so this residual is currently unreachable).
 *
 * The target of every static rung is `resolveFakeOverride()` of the callee:
 * IR call symbols are statically resolved, so the nearest real declaration
 * up the receiver's static class chain IS the runtime implementation
 * whenever no subtype can override it (super calls; final members; final
 * receiver classes — rule 1 makes concrete leaves final).
 */
internal fun classifySharedCall(expression: IrCall, context: BrsIrBackendContext): SharedCallShape? {
    val callee = expression.symbol.owner
    val real = (if (callee.isFakeOverride) callee.resolveFakeOverride() else null) ?: callee
    val declClass = real.parentClassOrNull ?: return null
    if (!context.intrinsics.isSharedServiceClass(declClass)) return null

    val property = real.correspondingPropertySymbol?.owner
    if (property != null && isSimpleSharedAccessor(real, property)) {
        return SharedCallShape.DirectFieldAccess(property, isSetter = real === property.setter)
    }
    if (!isSharedExtensionShapedFunction(real, context)) return null

    if (expression.superQualifierSymbol != null) return SharedCallShape.StaticImpl(real)
    if (real.modality == Modality.FINAL) return SharedCallShape.StaticImpl(real)
    val staticClass = callee.parentClassOrNull
    if (staticClass != null && staticClass.modality == Modality.FINAL) return SharedCallShape.StaticImpl(real)

    val registry = context.sharedDispatchRegistry
    if (registry == null || !registry.isModuleClass(declClass)) return null
    return SharedCallShape.Dispatch(real)
}

/**
 * The `__proto`-name dispatcher for an open/abstract shared member: a global
 * next to the declaring implementation (same file — defined exactly once per
 * module), reading the receiver's runtime class name from `__proto[0]` (pure
 * data — the ctor emits the class's own name at the head) and if-chaining to
 * the concrete implementation, in emitted-class-name order:
 *
 * ```brightscript
 * function Base_f_Str_k___dispatch(recv as Object, tag as String) as String
 *     runtimeClass = recv.__proto[0]
 *     if runtimeClass = "LeafA" then
 *         return LeafA_f_Str_k_(recv, tag)
 *     else if runtimeClass = "LeafB" then
 *         return Base_f_Str_k_(recv, tag)   ' non-overriding leaf → declaring impl
 *     else
 *         exc = { type: "IllegalStateException", message: ... }
 *         throw exc
 *     end if
 * end function
 * ```
 *
 * The signature is derived from the EMITTED implementation's final parameter
 * list — never re-derived from IR — so arity (suspend `_completion`
 * included) cannot drift from the implementations it forwards to (the same
 * single-source rule as the wrapper slots). The else-arm is the guided
 * closed-world error; an abstract member with no concrete descendants in the
 * compilation gets ONLY the guided error.
 */
internal fun buildSharedDispatcher(
    declaration: IrSimpleFunction,
    impl: BrsDeclaration,
    context: BrsIrBackendContext,
): BrsDeclaration {
    val implName: String
    val implParams: List<BrsParameter>
    when (impl) {
        is BrsFunction -> {
            implName = impl.name
            implParams = impl.parameters
        }
        is BrsSub -> {
            implName = impl.name
            implParams = impl.parameters
        }
        else -> compilationException(
            "SharedService dispatcher: unexpected implementation shape ${impl::class.simpleName}",
            declaration
        )
    }
    check(implParams.firstOrNull()?.name == "m") {
        "SharedService dispatcher: implementation $implName has no leading receiver parameter"
    }
    val forwarded = implParams.drop(1)

    var recvName = "recv"
    while (forwarded.any { it.name.equals(recvName, ignoreCase = true) }) recvName += "_"
    var protoVar = "runtimeClass"
    while (forwarded.any { it.name.equals(protoVar, ignoreCase = true) }) protoVar += "_"

    val params = mutableListOf(BrsParameter(recvName, implParams[0].type))
    forwarded.mapTo(params) { BrsParameter(it.name, it.type, it.defaultValue?.deepCopy()) }

    val declaringClass = declaration.parentClassOrNull
        ?: compilationException("SharedService dispatcher: declaration has no parent class", declaration)
    val leaves = context.sharedDispatchRegistry?.concreteDescendantsOf(declaringClass) ?: emptyList()

    val statements = mutableListOf<BrsStatement>(
        BrsVariable(
            protoVar,
            initializer = BrsIndexAccess(BrsDotAccess(BrsIdentifier(recvName), "__proto"), BrsIntLiteral(0))
        )
    )

    // Guided closed-world else-arm: names the member and the runtime class,
    // and says what fixes it (stdlib exception-helper AA shape).
    val rawName = declaration.name.asString()
    val memberDisplay = when {
        rawName.startsWith("<get-") -> rawName.removePrefix("<get-").removeSuffix(">")
        rawName.startsWith("<set-") -> rawName.removePrefix("<set-").removeSuffix(">")
        else -> rawName
    }
    val message = BrsBinaryOp(
        BrsBinaryOp(
            BrsStringLiteral(
                "SharedService dispatch: no compiled implementation of " +
                    "${declaringClass.name.asString()}.$memberDisplay for runtime class '"
            ),
            BrsBinaryOperator.ADD,
            BrsIdentifier(protoVar)
        ),
        BrsBinaryOperator.ADD,
        BrsStringLiteral(
            "' — dispatchers enumerate this compilation's concrete shared classes " +
                "(closed world, single module); recompile with the module containing it"
        )
    )
    val throwStatements = listOf<BrsStatement>(
        BrsVariable(
            "exc",
            initializer = BrsAALiteral(
                mutableListOf(
                    BrsAAEntry("type", BrsStringLiteral("IllegalStateException")),
                    BrsAAEntry("message", message)
                )
            )
        ),
        BrsThrow(BrsIdentifier("exc"))
    )

    if (leaves.isEmpty()) {
        statements.addAll(throwStatements)
    } else {
        var chain: BrsStatement = BrsBlock(throwStatements.toMutableList())
        for (leaf in leaves.asReversed()) {
            val target = resolveSharedImplIn(leaf, declaration, context)
            val callArgs = mutableListOf<BrsExpression>(BrsIdentifier(recvName))
            forwarded.mapTo(callArgs) { BrsIdentifier(it.name) }
            val targetName = context.getBrsName(target)
            context.recordFunctionDependency(targetName)
            val call = BrsFunctionCall(BrsIdentifier(targetName), callArgs)
            val branch = if (impl is BrsFunction) {
                BrsBlock(mutableListOf(BrsReturn(call)))
            } else {
                BrsBlock(mutableListOf(BrsExpressionStatement(call)))
            }
            chain = BrsIf(
                BrsBinaryOp(BrsIdentifier(protoVar), BrsBinaryOperator.EQ, BrsStringLiteral(context.getBrsName(leaf))),
                branch,
                chain
            )
        }
        statements.add(chain)
    }

    val dispatcherName = implName + SHARED_DISPATCH_SUFFIX
    return when (impl) {
        is BrsFunction -> BrsFunction(dispatcherName, params, impl.returnType, BrsBlock(statements))
        is BrsSub -> BrsSub(dispatcherName, params, BrsBlock(statements))
        else -> compilationException("unreachable", declaration)
    }
}

/**
 * The implementation [leaf] runs for [declaration]: the nearest REAL
 * (non-fake-override) declaration on the superclass chain from [leaf] up
 * that overrides — transitively — the declaring member (or is it). For a
 * concrete leaf this is never abstract (Kotlin requires concrete classes to
 * implement abstract members).
 *
 * Primary match walks `overriddenSymbols`; the fallback matches by emitted
 * slot-name suffix (the name minus the class prefix — exactly the identity
 * today's slot attachment dispatches on), covering declarations whose
 * override links were severed by a lowering that replaced the function
 * (the continuation lowerings rebuild suspend declarations).
 */
private fun resolveSharedImplIn(
    leaf: IrClass,
    declaration: IrSimpleFunction,
    context: BrsIrBackendContext,
): IrSimpleFunction {
    fun membersOf(irClass: IrClass): List<IrSimpleFunction> = irClass.declarations.flatMap {
        when (it) {
            is IrSimpleFunction -> listOf(it)
            is IrProperty -> listOfNotNull(it.getter, it.setter)
            else -> emptyList()
        }
    }

    var current: IrClass? = leaf
    while (current != null) {
        val hit = membersOf(current).firstOrNull {
            !it.isFakeOverride && (it === declaration || it.overridesTransitively(declaration))
        }
        if (hit != null) return hit
        current = superclassOf(current)
    }

    val declClassName = context.getBrsName(declaration.parentClassOrNull!!)
    val wantedSuffix = context.getBrsName(declaration).removePrefix("${declClassName}_")
    current = leaf
    while (current != null) {
        val className = context.getBrsName(current)
        val hit = membersOf(current).firstOrNull {
            !it.isFakeOverride && context.getBrsName(it).removePrefix("${className}_") == wantedSuffix
        }
        if (hit != null) return hit
        current = superclassOf(current)
    }

    compilationException(
        "SharedService dispatcher: no implementation of '${declaration.name}' found for concrete class '${leaf.name}'",
        leaf
    )
}

private fun IrSimpleFunction.overridesTransitively(target: IrSimpleFunction): Boolean {
    val seen = mutableSetOf<IrSimpleFunction>()
    val queue = ArrayDeque(overriddenSymbols.map { it.owner })
    while (queue.isNotEmpty()) {
        val next = queue.removeFirst()
        if (!seen.add(next)) continue
        if (next === target) return true
        queue.addAll(next.overriddenSymbols.map { it.owner })
    }
    return false
}
