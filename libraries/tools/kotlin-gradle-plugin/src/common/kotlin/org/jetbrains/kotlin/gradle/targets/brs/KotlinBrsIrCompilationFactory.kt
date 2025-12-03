/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCompilationFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.DefaultKotlinCompilationFriendPathsResolver
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.DefaultKotlinCompilationDependencyConfigurationsFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinCompilationImplFactory
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory.KotlinMultiplatformCommonCompilerOptionsFactory

/**
 * Factory for creating Kotlin/BrightScript IR compilations.
 */
class KotlinBrsIrCompilationFactory internal constructor(
    override val target: KotlinBrsIrTarget,
) : KotlinCompilationFactory<KotlinBrsIrCompilation> {

    override val itemClass: Class<KotlinBrsIrCompilation>
        get() = KotlinBrsIrCompilation::class.java

    private val compilationImplFactory: KotlinCompilationImplFactory = KotlinCompilationImplFactory(
        compilerOptionsFactory = KotlinMultiplatformCommonCompilerOptionsFactory,
        compilationFriendPathsResolver = DefaultKotlinCompilationFriendPathsResolver(
            friendArtifactResolver = { _ ->
                target.project.files()
            }
        ),
        compilationDependencyConfigurationsFactory = DefaultKotlinCompilationDependencyConfigurationsFactory.WithRuntime(
            withResourcesConfigurationExtending = { runtimeDependencyConfiguration, _ ->
                runtimeDependencyConfiguration
            }
        ),
    )

    override fun create(name: String): KotlinBrsIrCompilation = target.project.objects.newInstance(
        itemClass, compilationImplFactory.create(target, name)
    )
}
