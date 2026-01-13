/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.targets.brs.dsl.KotlinBrsTargetDsl
import org.jetbrains.kotlin.gradle.utils.newInstance
import javax.inject.Inject

/**
 * Kotlin/BrightScript target for compiling Kotlin to BrightScript (Roku).
 */
abstract class KotlinBrsIrTarget @Inject constructor(
    project: Project,
    platformType: KotlinPlatformType,
) : KotlinOnlyTarget<KotlinBrsIrCompilation>(project, platformType),
    KotlinBrsTargetDsl {

    override val compilerOptions: KotlinCommonCompilerOptions =
        project.objects.newInstance<KotlinCommonCompilerOptionsDefault>()

    override val minRokuOS: Property<String> = project.objects.property(String::class.java)
        .convention("9.4")

    override val debugMode: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    override val generateXml: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(true)

    override val minify: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    override val mainFunction: Property<String> = project.objects.property(String::class.java)

    override val defaultComponentExtends: Property<String> = project.objects.property(String::class.java)

    override val strictMode: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    init {
        // Use BRS platform type for RokuStudio IDE integration.
        // RokuStudio (forked IntelliJ) includes BRS in IdePlatformKind.ALL_KINDS
        // and provides proper tooling for semantic analysis.
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.brs)
    }
}
