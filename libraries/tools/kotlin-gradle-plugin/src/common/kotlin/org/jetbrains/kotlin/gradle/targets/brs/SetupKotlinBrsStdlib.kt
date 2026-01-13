/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtensionOrNull
import org.jetbrains.kotlin.gradle.internal.KOTLIN_MODULE_GROUP
import org.jetbrains.kotlin.gradle.internal.KOTLIN_STDLIB_MODULE_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinProjectSetupAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.launch
import org.jetbrains.kotlin.gradle.plugin.mpp.internal
import org.jetbrains.kotlin.gradle.plugin.mpp.resolvableMetadataConfiguration
import org.jetbrains.kotlin.gradle.plugin.sources.*
import org.jetbrains.kotlin.gradle.utils.Future
import org.jetbrains.kotlin.gradle.utils.extrasStoredFuture

/**
 * Excludes kotlin-stdlib from BRS compilation dependencies.
 *
 * BRS targets use their own kotlin-stdlib-brs instead of the regular kotlin-stdlib.
 * The stdlib is added automatically by KGP to commonMain (via the metadata target),
 * which then flows into BRS compilations' classpaths. This exclusion removes it
 * from BRS compile classpaths so resolution doesn't fail looking for
 * kotlin-stdlib:2.2.255-SNAPSHOT which is not published to Maven repositories.
 */
internal val SetupKotlinBrsStdlibExclusion = KotlinProjectSetupAction {
    val kotlin = multiplatformExtensionOrNull ?: return@KotlinProjectSetupAction

    // Exclude from BRS source set metadata configurations
    launch {
        kotlin.awaitSourceSets().forEach { sourceSet ->
            if (sourceSet.isBrsSourceSet.await()) {
                sourceSet.internal
                    .resolvableMetadataConfiguration
                    .exclude(mapOf("group" to KOTLIN_MODULE_GROUP, "module" to KOTLIN_STDLIB_MODULE_NAME))
            }
        }
    }

    // Exclude from BRS compilation compile classpaths
    kotlin.targets.configureEach { target ->
        if (target.platformType == KotlinPlatformType.brs) {
            target.compilations.configureEach { compilation ->
                // Exclude from compileDependencyConfiguration (brsCompileClasspath)
                compilation.internal.configurations.compileDependencyConfiguration.exclude(
                    mapOf("group" to KOTLIN_MODULE_GROUP, "module" to KOTLIN_STDLIB_MODULE_NAME)
                )
            }
        }
    }
}

/**
 * Returns true if this source set is compiled only for BRS targets.
 */
internal val KotlinSourceSet.isBrsSourceSet: Future<Boolean> by extrasStoredFuture {
    val compilations = internal.awaitPlatformCompilations()
    compilations.isNotEmpty() && compilations.all { it.platformType == KotlinPlatformType.brs }
}
