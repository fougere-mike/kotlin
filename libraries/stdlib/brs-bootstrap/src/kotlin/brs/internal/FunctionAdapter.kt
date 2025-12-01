/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.internal

/**
 * Base class for function adapters in BrightScript.
 * Used to wrap Kotlin lambdas/function references for interop.
 *
 * In generated BrightScript, functions are stored as roFunction references.
 */
internal abstract class FunctionAdapter
