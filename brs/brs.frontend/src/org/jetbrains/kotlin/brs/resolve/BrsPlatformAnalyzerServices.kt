/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.resolve

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.ImportPath
import org.jetbrains.kotlin.resolve.PlatformConfigurator
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.storage.StorageManager

/**
 * Platform analyzer services for BrightScript (Roku).
 *
 * This provides BrightScript-specific default imports and platform configuration
 * for the Kotlin compiler frontend.
 */
object BrsPlatformAnalyzerServices : PlatformDependentAnalyzerServices() {
    override fun computePlatformSpecificDefaultImports(storageManager: StorageManager, result: MutableList<ImportPath>) {
        result.add(ImportPath.fromString("kotlin.brs.*"))
    }

    override val platformConfigurator: PlatformConfigurator = BrsPlatformConfigurator

    // BrightScript has no excluded imports currently
    override val excludedImports: List<FqName> = emptyList()
}
