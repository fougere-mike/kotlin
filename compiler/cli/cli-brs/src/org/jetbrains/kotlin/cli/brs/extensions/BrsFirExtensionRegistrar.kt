/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs.extensions

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * FIR extension registrar for BrightScript-specific extensions.
 *
 * Note: SceneGraphLayoutGenerator has been removed.
 * Layout classes (e.g., MainScreen_Layout) are now generated as actual Kotlin source files
 * by the Gradle plugin's GenerateLayoutStubsTask. This provides:
 * 1. Full IDE support (code completion, navigation, type checking)
 * 2. No duplicate class conflicts between FIR-generated and source-generated classes
 * 3. The IR backend (IrToBrsTransformer) still generates the optimized BrightScript
 *    implementation with caching by detecting @SGLayout annotations.
 *
 * This registrar is kept for future FIR extensions (e.g., DSL validators, checkers).
 */
class BrsFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        // Currently no FIR extensions registered.
        // Layout class generation moved to Gradle plugin (GenerateLayoutStubsTask).
    }
}
