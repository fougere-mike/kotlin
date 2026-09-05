/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.pump.PumpScheduler

/**
 * Component lifecycle runtime (design of record:
 * docs/superpowers/plans/2026-09-04-component-lifecycle-design.md, §4).
 *
 * ONE file by the include-closure law: the compiler-injected attach call below
 * records the edge that pulls this file into every render component's closure,
 * and the name-registered observer handler added in the gate section must live
 * beside its registration.
 */

/** Per-component-instance state (object singletons live on GetGlobalAA — per instance on the render thread). */
internal object LifecycleRegistry {
    /** Set by the compiler-injected attach as init()'s first statement. */
    internal var attached: Boolean = false
}

/**
 * Component-init entry point, injected UNCONDITIONALLY as the first statement
 * of every render component's generated init() (task components excluded).
 * Performs the pump attach internally — this supersedes the coroutine-scan-
 * gated `__kotlinPumpAttach` injection and closes its helper-file hole. Cost:
 * one call, two ref stores; backend resolution and timers stay deferred to
 * the first real wakeup.
 */
@BrsStatic
public fun __kotlinComponentAttach(top: RoSGNode, global: RoSGNode) {
    PumpScheduler.attach(top, global)
    LifecycleRegistry.attached = true
}
