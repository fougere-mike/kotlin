/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.coroutines.CoroutineContext

/**
 * Groups various coroutine dispatchers available for use.
 *
 * In BrightScript/Roku, coroutines run on either the render thread (SceneGraph)
 * or Task node threads. This object provides access to dispatchers for both.
 */
public object Dispatchers {

    /**
     * The default coroutine dispatcher that is used by all standard builders
     * like [launch][kotlinx.coroutines.launch] and [async][kotlinx.coroutines.async]
     * if no dispatcher or other [ContinuationInterceptor] is specified in their context.
     *
     * In BrightScript, this is the current thread's event loop - either the
     * render thread or the current Task's thread.
     */
    public val Default: CoroutineDispatcher = UnconfinedDispatcher

    /**
     * A coroutine dispatcher that confines coroutine execution to the main/UI thread.
     *
     * In BrightScript, this is the SceneGraph render thread. Use this dispatcher
     * when you need to update UI elements from a coroutine.
     *
     * Note: The Main dispatcher may not be immediately available if the SceneGraph
     * hasn't been fully initialized. In such cases, use [Dispatchers.Default] or
     * a Task dispatcher.
     */
    public val Main: CoroutineDispatcher = UnconfinedDispatcher

    /**
     * A coroutine dispatcher that is not confined to any specific thread.
     *
     * It executes the initial continuation of a coroutine in the current call-frame
     * and lets the coroutine resume in whatever thread that is used by the
     * corresponding suspending function, without mandating any specific threading policy.
     *
     * Note: Use with caution. [Unconfined] dispatcher should not normally be used
     * in code.
     */
    public val Unconfined: CoroutineDispatcher = UnconfinedDispatcher

    /**
     * A coroutine dispatcher that is designed for offloading blocking IO tasks
     * to a shared pool of threads.
     *
     * In BrightScript, this would dispatch to Task nodes for background work.
     * However, since Task management requires SceneGraph context, this currently
     * falls back to [Unconfined].
     *
     * For actual IO operations in BrightScript, consider using [forTask] with
     * an explicit Task node.
     */
    public val IO: CoroutineDispatcher = UnconfinedDispatcher
}
