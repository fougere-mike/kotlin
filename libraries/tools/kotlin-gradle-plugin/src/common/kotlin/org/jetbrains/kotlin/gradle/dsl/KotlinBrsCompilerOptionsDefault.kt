/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal abstract class KotlinBrsCompilerOptionsDefault @Inject constructor(
    objectFactory: ObjectFactory
) : KotlinCommonCompilerOptionsDefault(objectFactory), KotlinBrsCompilerOptions {

    override val moduleName: org.gradle.api.provider.Property<String> =
        objectFactory.property(String::class.java)

    override val minRokuOS: org.gradle.api.provider.Property<String> =
        objectFactory.property(String::class.java).convention("9.4")

    override val debugMode: org.gradle.api.provider.Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    override val generateXml: org.gradle.api.provider.Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(true)

    override val minify: org.gradle.api.provider.Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    override val strictMode: org.gradle.api.provider.Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)
}
