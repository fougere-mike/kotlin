/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
    `maven-publish`
}

description = "Kotlin Standard Library for BrightScript"

project.configureJvmToolchain(JdkMajorVersion.JDK_1_8)

dependencies {
    // Compile-only dependency on builtins for type definitions
    compileOnly(project(":core:builtins"))
}

kotlin {
    explicitApi()
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_1
        apiVersion = KotlinVersion.KOTLIN_2_1
        freeCompilerArgs.addAll(
            "-Xallow-kotlin-package",
            "-Xstdlib-compilation",
            "-Xdont-warn-on-error-suppression",
            "-opt-in=kotlin.ExperimentalMultiplatform",
        )
    }
}

sourceSets["main"].kotlin {
    srcDir("src")
}

// Task to generate source jar for BrightScript stdlib
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

// Package sources for use by BrightScript compiler
val brsStdlibSources by tasks.registering(Sync::class) {
    from(sourceSets["main"].kotlin.srcDirs)
    into(layout.buildDirectory.dir("brs-stdlib-sources"))
}

artifacts {
    archives(sourcesJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "kotlin-stdlib-brs"
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}
