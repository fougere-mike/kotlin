/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.concurrent.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * A coroutine dispatcher that is not confined to any specific thread.
 *
 * It executes the initial continuation of a coroutine in the current call-frame
 * and lets the coroutine resume in whatever thread that is used by the
 * corresponding suspending function, without mandating any specific threading policy.
 *
 * In BrightScript, this means coroutines resume immediately in the current
 * execution context without any thread switching.
 */
internal object UnconfinedDispatcher : CoroutineDispatcher() {

    /**
     * Returns `false` because unconfined dispatcher does not need to dispatch
     * - it runs directly in the current context.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = false

    /**
     * Runs the block immediately since unconfined dispatcher doesn't actually dispatch.
     * This is called only when [isDispatchNeeded] returns `true`, which it doesn't
     * for this dispatcher. But we implement it to run immediately just in case.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        // Run immediately - unconfined dispatcher doesn't actually dispatch
        block.run()
    }

    override fun toString(): String = "Dispatchers.Unconfined"
}
