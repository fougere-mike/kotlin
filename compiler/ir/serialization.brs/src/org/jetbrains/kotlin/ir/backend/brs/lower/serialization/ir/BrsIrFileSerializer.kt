/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir

import org.jetbrains.kotlin.backend.common.serialization.DeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.library.impl.IrArrayReader
import org.jetbrains.kotlin.library.impl.IrStringWriter
import org.jetbrains.kotlin.library.impl.toArray

/**
 * Metadata for BrightScript IR file serialization.
 *
 * Implements [IrFileSerializer.FileBackendSpecificMetadata] to support klib serialization.
 */
class BrsIrFileMetadata(val exportedNames: List<String>) : IrFileSerializer.FileBackendSpecificMetadata {
    override fun toByteArray(): ByteArray {
        return IrStringWriter(exportedNames).writeIntoMemory()
    }

    companion object {
        fun fromByteArray(data: ByteArray): BrsIrFileMetadata {
            return BrsIrFileMetadata(
                exportedNames = IrArrayReader(data).toArray().map(WobblyTF8::decode)
            )
        }
    }
}

/**
 * Factory for creating BrightScript IR file metadata.
 */
fun interface BrsIrFileMetadataFactory {
    fun createBrsIrFileMetadata(irFile: IrFile): BrsIrFileMetadata
}

/**
 * Default factory that creates empty metadata.
 */
object BrsIrFileEmptyMetadataFactory : BrsIrFileMetadataFactory {
    override fun createBrsIrFileMetadata(irFile: IrFile) = BrsIrFileMetadata(emptyList())
}

/**
 * Serializer for BrightScript IR files.
 *
 * Handles serialization of individual IR files for klib creation.
 */
class BrsIrFileSerializer(
    settings: IrSerializationSettings,
    declarationTable: DeclarationTable.Default,
    private val brsIrFileMetadataFactory: BrsIrFileMetadataFactory
) : IrFileSerializer(settings, declarationTable) {

    override fun backendSpecificExplicitRoot(node: IrAnnotationContainer): Boolean = false

    override fun backendSpecificExplicitRootExclusion(node: IrAnnotationContainer): Boolean = false

    override fun backendSpecificMetadata(irFile: IrFile) = brsIrFileMetadataFactory.createBrsIrFileMetadata(irFile)
}
