/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*

object BrsDeclarationCheckers : DeclarationCheckers() {
    // BrightScript-specific declaration checkers can be added here as needed
    // For initial implementation, we inherit common checkers only
}
