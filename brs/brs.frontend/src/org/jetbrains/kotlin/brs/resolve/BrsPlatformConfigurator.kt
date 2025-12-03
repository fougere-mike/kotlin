/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.resolve

import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.resolve.PlatformConfiguratorBase

/**
 * Platform configurator for BrightScript (Roku).
 *
 * This provides BrightScript-specific configuration for the Kotlin compiler.
 * Currently minimal as BrightScript doesn't require additional call or declaration checkers.
 */
object BrsPlatformConfigurator : PlatformConfiguratorBase(
    additionalCallCheckers = emptyList(),
    additionalDeclarationCheckers = emptyList(),
) {
    override fun configureModuleComponents(container: StorageComponentContainer) {
        // BRS-specific module components can be added here as needed
    }
}
