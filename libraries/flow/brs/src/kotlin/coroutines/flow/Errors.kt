/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.cancellation.CancellationException

/**
 * Control-flow exception flow terminals ([first], [firstOrNull] — later `take`)
 * throw from their own collector to abort upstream collection once they have
 * their answer. It is caught by the same terminal's machinery and never leaks
 * to the caller.
 *
 * Extends [CancellationException] so job machinery treats an in-flight abort as
 * quiet, never as a failure. Note CancellationException extends
 * IllegalStateException on this platform — a `catch (e: IllegalStateException)`
 * inside a flow body would swallow an abort (and real cancellation); flow bodies
 * should catch specific exception types, or use try/finally.
 */
internal class AbortFlowException : CancellationException("flow terminal aborted")
