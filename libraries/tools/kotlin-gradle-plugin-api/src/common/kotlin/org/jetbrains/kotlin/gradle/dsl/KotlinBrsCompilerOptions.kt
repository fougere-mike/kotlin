/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

/**
 * Compiler options for Kotlin/BrightScript.
 */
interface KotlinBrsCompilerOptions : KotlinCommonCompilerOptions {

    /**
     * Base name of generated module.
     *
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val moduleName: org.gradle.api.provider.Property<String>

    /**
     * Minimum Roku OS version to target.
     *
     * Possible values: "9.0", "9.4", "10.0", "11.0", "12.0", "13.0"
     *
     * Default value: "9.4"
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val minRokuOS: org.gradle.api.provider.Property<String>

    /**
     * Generate debug information.
     *
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val debugMode: org.gradle.api.provider.Property<Boolean>

    /**
     * Generate SceneGraph XML component files.
     *
     * Default value: true
     */
    @get:org.gradle.api.tasks.Input
    val generateXml: org.gradle.api.provider.Property<Boolean>

    /**
     * Minify generated BrightScript code.
     *
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val minify: org.gradle.api.provider.Property<Boolean>

    /**
     * Enable strict mode for additional compile-time checks.
     *
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val strictMode: org.gradle.api.provider.Property<Boolean>
}
