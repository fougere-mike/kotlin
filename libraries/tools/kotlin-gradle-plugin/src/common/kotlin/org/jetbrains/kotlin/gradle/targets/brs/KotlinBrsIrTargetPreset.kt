/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTargetPreset

/**
 * Preset for creating Kotlin/BrightScript IR targets.
 *
 * This preset is used to add BrightScript (Roku) target support to Kotlin Multiplatform projects.
 */
@OptIn(InternalKotlinGradlePluginApi::class)
internal class KotlinBrsIrTargetPreset(
    project: Project,
) : KotlinOnlyTargetPreset<KotlinBrsIrTarget, KotlinBrsIrCompilation>(project) {

    override val platformType: KotlinPlatformType = KotlinPlatformType.brs

    override fun instantiateTarget(name: String): KotlinBrsIrTarget =
        project.objects.newInstance(KotlinBrsIrTarget::class.java, project, platformType)

    override fun createKotlinTargetConfigurator(): AbstractKotlinTargetConfigurator<KotlinBrsIrTarget> =
        KotlinBrsIrTargetConfigurator()

    override fun createCompilationFactory(forTarget: KotlinBrsIrTarget): KotlinCompilationFactory<KotlinBrsIrCompilation> =
        KotlinBrsIrCompilationFactory(forTarget)

    override val name: String = BRS_PRESET_NAME

    companion object {
        const val BRS_PRESET_NAME = "brs"
    }
}
