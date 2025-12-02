/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.runtime

/**
 * Base class for function adapters in BrightScript.
 * Used to wrap Kotlin lambdas/function references for interop.
 *
 * In generated BrightScript, functions are stored as roFunction references.
 * This adapter provides a bridge between Kotlin's function types and BrightScript's
 * function handling mechanism.
 */
internal abstract class FunctionAdapter {
    /**
     * Invokes the adapted function with the given arguments.
     * The backend will generate appropriate BrightScript code for this.
     */
    abstract fun invoke(vararg args: Any?): Any?
}

/**
 * Adapter for functions with 0 parameters.
 */
internal abstract class Function0Adapter<R> : FunctionAdapter() {
    abstract fun invoke(): R
    override fun invoke(vararg args: Any?): Any? = invoke()
}

/**
 * Adapter for functions with 1 parameter.
 */
internal abstract class Function1Adapter<P1, R> : FunctionAdapter() {
    abstract fun invoke(p1: P1): R
    @Suppress("UNCHECKED_CAST")
    override fun invoke(vararg args: Any?): Any? = invoke(args[0] as P1)
}

/**
 * Adapter for functions with 2 parameters.
 */
internal abstract class Function2Adapter<P1, P2, R> : FunctionAdapter() {
    abstract fun invoke(p1: P1, p2: P2): R
    @Suppress("UNCHECKED_CAST")
    override fun invoke(vararg args: Any?): Any? = invoke(args[0] as P1, args[1] as P2)
}

/**
 * Adapter for functions with 3 parameters.
 */
internal abstract class Function3Adapter<P1, P2, P3, R> : FunctionAdapter() {
    abstract fun invoke(p1: P1, p2: P2, p3: P3): R
    @Suppress("UNCHECKED_CAST")
    override fun invoke(vararg args: Any?): Any? = invoke(args[0] as P1, args[1] as P2, args[2] as P3)
}
