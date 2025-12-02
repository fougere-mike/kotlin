/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.brs.internal

import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions

/**
 * Compatibility adapter for deprecated KotlinCommonOptions interface.
 * Delegates to the new KotlinBrsCompilerOptions.
 */
internal class KotlinBrsOptionsCompat(
    override val options: KotlinBrsCompilerOptions
) : KotlinCommonOptions
