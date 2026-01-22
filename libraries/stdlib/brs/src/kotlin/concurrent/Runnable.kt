/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.concurrent

/**
 * A functional interface representing a unit of work that can be executed.
 *
 * This is the BrightScript equivalent of Java's Runnable interface, used
 * for scheduling work in coroutine dispatchers and other asynchronous contexts.
 */
public fun interface Runnable {
    /**
     * Executes this runnable's action.
     */
    public fun run()
}
