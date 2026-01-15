/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs.extensions

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.brs.SceneGraphLayoutGenerator

/**
 * FIR extension registrar for BrightScript-specific extensions.
 *
 * Registers the SceneGraph layout generator which creates synthetic
 * nested classes and properties for @SGLayout annotated properties.
 *
 * This registrar is in cli-brs (not checkers.brs) because FirExtensionRegistrar
 * is in fir:entrypoint, which would create a circular dependency if referenced
 * from checkers.brs.
 */
class BrsFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::SceneGraphLayoutGenerator
    }
}
