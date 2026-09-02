/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

/**
 * Suspend-state-machine acceptance probe for the kotlin-flow-brs klib (flow-program
 * spec decision 10).
 *
 * Two sequential suspend calls whose results combine require a real state machine.
 * Under `-Xstdlib-compilation` no state machines are generated and this exact shape
 * miscompiles silently; the stdlib device suite's `flowKlibStateMachineSmoke` test
 * runs it with two `delay(1)`-ing lambdas — passing on device proves this klib
 * compiles in USER mode.
 *
 * PUBLIC, not internal: the stdlib test module is a separate module with no friend
 * wiring, so internal flow-klib declarations are invisible to it.
 */
public suspend fun __smokeTwoSuspends(a: suspend () -> Int, b: suspend () -> Int): Int = a() + b()
