/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.jetbrains.kotlin.gradle.plugin.KotlinOnlyTargetConfigurator

/**
 * Configurator for Kotlin/BrightScript IR target.
 *
 * This configurator sets up the compilation tasks and wires them into the build lifecycle.
 */
open class KotlinBrsIrTargetConfigurator :
    KotlinOnlyTargetConfigurator<KotlinBrsIrCompilation, KotlinBrsIrTarget>(createTestCompilation = true) {

    override fun configureTarget(target: KotlinBrsIrTarget) {
        super.configureTarget(target)
        // Additional BRS-specific configuration can be added here
    }
}
