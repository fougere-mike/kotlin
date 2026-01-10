/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.DeprecatedTargetPresetApi
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTargetPreset

/**
 * Preset for creating Kotlin/BrightScript IR targets.
 *
 * This preset is used to add BrightScript (Roku) target support to Kotlin Multiplatform projects.
 */
@OptIn(DeprecatedTargetPresetApi::class)
open class KotlinBrsIrTargetPreset(
    project: Project,
) : KotlinOnlyTargetPreset<KotlinBrsIrTarget, KotlinBrsIrCompilation>(project) {

    // Report as 'native' platform for IDE compatibility.
    // The bundled Kotlin IDE plugin doesn't include BRS in IdePlatformKind.ALL_KINDS,
    // so using KotlinPlatformType.brs causes "Unsupported platform kind: BRS" errors.
    // Native is the closest match since BRS uses klib format with NATIVE builtins.
    override val platformType: KotlinPlatformType = KotlinPlatformType.native

    override fun instantiateTarget(name: String): KotlinBrsIrTarget =
        project.objects.newInstance(KotlinBrsIrTarget::class.java, project, platformType)

    override fun createKotlinTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinBrsIrTarget> =
        KotlinBrsIrTargetConfigurator()

    override fun createCompilationFactory(forTarget: KotlinBrsIrTarget): KotlinCompilationFactory<KotlinBrsIrCompilation> =
        KotlinBrsIrCompilationFactory(forTarget)

    override fun getName(): String = BRS_PRESET_NAME

    companion object {
        const val BRS_PRESET_NAME = "brs"
    }
}
