/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

/**
 * Marker class used by the compiler to distinguish default constructor calls.
 * This is used internally by the BrightScript backend for constructor lowering.
 */
@Suppress("UNUSED_PARAMETER")
internal class DefaultConstructorMarker private constructor()
