/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.cancellation.CancellationException

/**
 * Control-flow exception flow machinery ([first], [firstOrNull], [take]) throws
 * from its own collector to abort upstream collection once it has its answer.
 * It is caught by the same machinery and never leaks to the caller.
 *
 * [owner] identifies WHOSE abort this is: each catch site swallows only aborts
 * it threw itself (identity check) and rethrows everything else. With two abort
 * users this is a correctness requirement, not hygiene — an inner `take` that
 * swallowed an outer `first()`'s abort would let the enclosing flow body keep
 * running past the point `first()` stopped caring (kotlinx pins the same
 * ownership discipline on its AbortFlowException).
 *
 * Extends [CancellationException] so job machinery treats an in-flight abort as
 * quiet, never as a failure. Note CancellationException extends
 * IllegalStateException on this platform — a `catch (e: IllegalStateException)`
 * inside a flow body would swallow an abort (and real cancellation); flow bodies
 * should catch specific exception types, or use try/finally.
 */
internal class AbortFlowException(
    internal val owner: FlowCollector<*>,
) : CancellationException("flow terminal aborted")
