/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.lower.InnerClassesSupport
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedPlus
import java.util.*

/**
 * Support for inner classes in BrightScript code generation.
 *
 * In BrightScript, inner classes are implemented by capturing the outer
 * instance in a field and passing it through the constructor.
 */
class BrsInnerClassesSupport(
    private val irFactory: IrFactory
) : InnerClassesSupport {

    private val outerThisFields = WeakHashMap<IrClass, IrField>()
    private val innerClassConstructors = WeakHashMap<IrConstructor, IrConstructor>()
    private val originalInnerClassPrimaryConstructorByClass = WeakHashMap<IrClass, IrConstructor>()

    override fun getOuterThisField(innerClass: IrClass): IrField {
        require(innerClass.isInner) { "Class is not inner: $innerClass" }
        return outerThisFields.getOrPut(innerClass) {
            createOuterThisField(innerClass)
        }
    }

    private fun createOuterThisField(innerClass: IrClass): IrField {
        val outerClass = innerClass.parentAsClass

        return irFactory.createField(
            startOffset = innerClass.startOffset,
            endOffset = innerClass.endOffset,
            origin = BrsDeclarationOrigin.OUTER_THIS_FIELD,
            name = Name.identifier("\$outer"),
            visibility = DescriptorVisibilities.PRIVATE,
            symbol = org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl(),
            type = outerClass.defaultType,
            isFinal = true,
            isStatic = false,
            isExternal = false
        ).apply {
            parent = innerClass
        }
    }

    fun getOuterThisFieldSymbol(innerClass: IrClass): IrFieldSymbol {
        return getOuterThisField(innerClass).symbol
    }

    override fun getInnerClassConstructorWithOuterThisParameter(innerClassConstructor: IrConstructor): IrConstructor {
        val innerClass = innerClassConstructor.parent as IrClass
        require(innerClass.isInner) { "Class is not inner: $innerClass" }

        return innerClassConstructors.getOrPut(innerClassConstructor) {
            createInnerClassConstructorWithOuterThisParameter(innerClassConstructor)
        }.also {
            if (innerClassConstructor.isPrimary) {
                originalInnerClassPrimaryConstructorByClass[innerClass] = innerClassConstructor
            }
        }
    }

    override fun getInnerClassOriginalPrimaryConstructorOrNull(innerClass: IrClass): IrConstructor? {
        require(innerClass.isInner) { "Class is not inner: $innerClass" }
        return originalInnerClassPrimaryConstructorByClass[innerClass]
    }

    private fun createInnerClassConstructorWithOuterThisParameter(oldConstructor: IrConstructor): IrConstructor {
        val irClass = oldConstructor.parent as IrClass
        val outerThisType = (irClass.parent as IrClass).defaultType

        val newConstructor = irFactory.buildConstructor {
            updateFrom(oldConstructor)
            returnType = oldConstructor.returnType
        }.also {
            it.parent = oldConstructor.parent
            it.annotations = oldConstructor.annotations
        }

        newConstructor.copyTypeParametersFrom(oldConstructor)

        val newValueParameters = mutableListOf(buildValueParameter(newConstructor) {
            origin = BrsDeclarationOrigin.OUTER_THIS_FIELD
            name = Name.identifier("\$outer")
            type = outerThisType
        })

        for (p in oldConstructor.valueParameters) {
            newValueParameters += p.copyTo(newConstructor)
        }

        newConstructor.valueParameters = newConstructor.valueParameters memoryOptimizedPlus newValueParameters

        return newConstructor
    }
}

/**
 * Declaration origins specific to BrightScript code generation.
 */
object BrsDeclarationOrigin : IrDeclarationOrigin {
    override val name: String = "BRS_GENERATED"
    override val isSynthetic: Boolean = true

    /**
     * Origin for outer this reference fields in inner classes.
     */
    object OUTER_THIS_FIELD : IrDeclarationOrigin {
        override val name: String = "BRS_OUTER_THIS_FIELD"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for closure capture fields.
     */
    object CLOSURE_CAPTURE_FIELD : IrDeclarationOrigin {
        override val name: String = "BRS_CLOSURE_CAPTURE_FIELD"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for generated default parameter functions.
     */
    object DEFAULT_PARAMETER_HANDLER : IrDeclarationOrigin {
        override val name: String = "BRS_DEFAULT_PARAMETER_HANDLER"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for generated property accessors.
     */
    object GENERATED_ACCESSOR : IrDeclarationOrigin {
        override val name: String = "BRS_GENERATED_ACCESSOR"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for helper functions generated during lowering.
     */
    object LOWERED_HELPER : IrDeclarationOrigin {
        override val name: String = "BRS_LOWERED_HELPER"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for hoisted lambda functions.
     */
    object HOISTED_LAMBDA : IrDeclarationOrigin {
        override val name: String = "BRS_HOISTED_LAMBDA"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for shared variable wrappers.
     * Variables with this origin are boxed in {value: ...} at transform time.
     */
    object SHARED_VARIABLE_WRAPPER : IrDeclarationOrigin {
        override val name: String = "BRS_SHARED_VARIABLE_WRAPPER"
        override val isSynthetic: Boolean = true
    }

    /**
     * Origin for IO worker functions extracted from withContext(Dispatchers.IO) blocks.
     */
    object IO_WORKER : IrDeclarationOrigin {
        override val name: String = "BRS_IO_WORKER"
        override val isSynthetic: Boolean = true
    }
}
