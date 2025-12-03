/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.artifacts

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.brs.KotlinBrsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KLIB_TYPE
import org.jetbrains.kotlin.gradle.utils.libsDirectory

internal val KotlinBrsKlibArtifact = KotlinTargetArtifact { target, apiElements, runtimeElements ->
    if (target !is KotlinBrsIrTarget) return@KotlinTargetArtifact

    val brsKlibTask = target.createArtifactsTask {
        it.from(target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME).output.allOutputs)
        it.archiveExtension.set(KLIB_TYPE)
        it.destinationDirectory.set(target.project.libsDirectory)
    }

    target.createPublishArtifact(brsKlibTask, KLIB_TYPE, apiElements, runtimeElements)
}
