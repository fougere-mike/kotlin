/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir

import org.jetbrains.kotlin.backend.common.serialization.DeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrModuleSerializer
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrDiagnosticReporter
import org.jetbrains.kotlin.ir.declarations.IrFile

/**
 * Module serializer for BrightScript IR.
 *
 * Serializes an entire IR module to klib format for BrightScript compilation.
 */
class BrsIrModuleSerializer(
    settings: IrSerializationSettings,
    diagnosticReporter: IrDiagnosticReporter,
    irBuiltIns: IrBuiltIns,
    private val brsIrFileMetadataFactory: BrsIrFileMetadataFactory = BrsIrFileEmptyMetadataFactory,
) : IrModuleSerializer<BrsIrFileSerializer>(settings, diagnosticReporter) {

    override val globalDeclarationTable = BrsGlobalDeclarationTable(irBuiltIns)

    override fun createSerializerForFile(file: IrFile): BrsIrFileSerializer =
        BrsIrFileSerializer(settings, DeclarationTable.Default(globalDeclarationTable), brsIrFileMetadataFactory)
}
