/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs.dsl

import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * DSL extension for configuring Kotlin/BrightScript target.
 */
interface KotlinBrsTargetDsl : KotlinTarget,
    HasConfigurableKotlinCompilerOptions<KotlinCommonCompilerOptions> {
    /**
     * Minimum Roku OS version to target.
     * Affects available language features (e.g., try/catch requires 9.0+).
     *
     * Supported versions: "9.0", "9.4", "10.0", "11.0", "12.0", "13.0"
     * Default: "9.4"
     */
    val minRokuOS: Property<String>

    /**
     * Enable debug mode for generated BrightScript code.
     * When enabled, generates additional debug information like line numbers.
     *
     * Default: false
     */
    val debugMode: Property<Boolean>

    /**
     * Enable SceneGraph XML component file generation.
     * When enabled, generates .xml files alongside .brs files for SceneGraph components.
     *
     * Default: true
     */
    val generateXml: Property<Boolean>

    /**
     * Enable minification of generated BrightScript code.
     * When enabled, removes whitespace and shortens identifiers.
     *
     * Default: false
     */
    val minify: Property<Boolean>

    /**
     * Fully qualified name of the main function entry point.
     * If not set, the compiler will look for a standard `main()` function.
     */
    val mainFunction: Property<String>

    /**
     * Default base type for SceneGraph components.
     * Common values: "Group", "Task", "Node", "ContentNode"
     */
    val defaultComponentExtends: Property<String>

    /**
     * Enable strict mode for additional compile-time checks.
     * When enabled, enforces stricter type checking and BrightScript compatibility.
     *
     * Default: false
     */
    val strictMode: Property<Boolean>
}
