/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

/**
 * An asynchronous cold stream of values, mirroring `kotlinx.coroutines.flow.Flow`.
 *
 * This lives in the separate `kotlin-flow-brs` klib (NOT the stdlib) because flow
 * operator internals need real suspend state machines, and stdlib compilation
 * (`-Xstdlib-compilation`) generates none — kotlinx-style operator bodies would
 * miscompile silently there (flow-program spec decision 10). This klib compiles in
 * USER mode: `-Xallow-kotlin-package` only.
 */
public interface Flow<out T> {
    public suspend fun collect(collector: FlowCollector<T>)
}

/**
 * The consumer side of a [Flow]: producers emit values into it, mirroring
 * `kotlinx.coroutines.flow.FlowCollector`.
 */
public fun interface FlowCollector<in T> {
    public suspend fun emit(value: T)
}
