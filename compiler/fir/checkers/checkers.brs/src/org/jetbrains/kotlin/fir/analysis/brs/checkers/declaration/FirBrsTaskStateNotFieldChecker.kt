/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.declarations.utils.hasBackingField
import org.jetbrains.kotlin.fir.declarations.utils.isAbstract
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId

/**
 * Reports `BRS_TASK_STATE_NOT_FIELD` on a property with a backing field declared in a
 * concrete TaskComponent-derived class that carries no SceneGraph field annotation
 * (`@SG*Field` or `@BrsField`).
 *
 * Rationale (device-proven, see spikes/task-node-spike/FINDINGS.md, `m_clone_in_visibility`
 * and `m_writeback_isolation`): an un-annotated backing-field property compiles to plain
 * `m.<name>` state. When the task thread starts, it receives a clone-in COPY of `m` — reads
 * of init-set values work, which makes the bug invisible in simple testing — but writes
 * from `run()` land in the task thread's copy and are silently lost. Only `@SG*Field`
 * node fields cross the thread boundary.
 *
 * Scope decisions (v1):
 *  - Flags `val` as well as `var`. A read-only `val` initialized before RUN is safe-ish,
 *    but a `val` holding a mutable object (e.g. `val items = mutableListOf(...)`) mutated
 *    from `run()` loses those mutations just the same, and the two cases cannot be cheaply
 *    distinguished. All backing-field m-state on a task is a trap in at least one direction,
 *    so v1 flags both; `@Suppress("BRS_TASK_STATE_NOT_FIELD")` is the opt-out.
 *  - Only properties DECLARED in a concrete TaskComponent-derived class are checked.
 *    Properties declared in an abstract task base are not flagged (the base itself never
 *    runs), and are NOT re-flagged at concrete subclasses that inherit them — a known v1
 *    hole, documented in the inherited-property fixture.
 *  - Annotations are read from the declaration itself; an override must repeat the
 *    annotation (annotations are not inherited in Kotlin).
 *  - Delegated properties, accessor-only properties (no backing field), and local
 *    variables in `run()` are skipped.
 *  - Constructor-parameter properties ARE checked (components take constructor inputs
 *    since the lifecycle program): an @SG-annotated one is a task-input error
 *    (FirBrsTaskConstructorInputChecker), a plain one is m-state and fires here.
 */
object FirBrsTaskStateNotFieldChecker : FirPropertyChecker(MppCheckerKind.Common) {

    private val fieldAnnotationIds: Set<ClassId> =
        BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys + BrsStandardClassIds.Annotations.BrsField

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        if (declaration.delegate != null) return
        if (!declaration.hasBackingField) return

        // Member properties only: for locals the nearest containing declaration is a function.
        val containingClass = context.containingDeclarations.lastOrNull() as? FirClassSymbol<*> ?: return
        if (containingClass.isInterface || containingClass.isAbstract) return

        val session = context.session
        val isTaskDerived = lookupSuperTypes(containingClass, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.lookupTag.classId == BrsStandardClassIds.Components.TaskComponent }
        if (!isTaskDerived) return

        if (declaration.annotations.any { it.toAnnotationClassId(session) in fieldAnnotationIds }) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_TASK_STATE_NOT_FIELD,
            declaration.name.asString(),
            containingClass.classId.shortClassName.asString(),
        )
    }
}
