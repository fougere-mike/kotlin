/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

description = "Kotlin Standard Library for BrightScript"

kotlin {
    // Use JS IR backend to produce klib format that BrightScript backend can consume
    // This is a bootstrapping approach until the main stdlib supports BRS as a first-class target
    js(IR) {
        nodejs()
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xallow-kotlin-package",
                    "-opt-in=kotlin.ExperimentalMultiplatform",
                    "-opt-in=kotlin.contracts.ExperimentalContracts",
                    "-Xexpect-actual-classes",
                    "-Xir-module-name=kotlin-brs"
                )
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            // BRS-specific sources (bootstrap approach)
            kotlin.srcDir("src")

            // Exclude files that have conflicts or issues
            // Phase 2 Batch 1: Enabled ExceptionHelpers.kt, io/**, exceptionsBrs.kt
            // Phase 2 Batch 6: Enabled Roku SDK bindings (kotlin/brs/roku/**)
            // Note: Collections, Math, Random excluded - JS stdlib already provides these
            // The bootstrap approach depends on JS stdlib which has all these implementations
            kotlin.exclude(
                // Collections - JS stdlib provides these
                "kotlin/collections/**",
                "kotlin/brs/collections/**",
                "kotlin/AutoCloseableBrs.kt",
                // Math - JS stdlib provides these already
                "kotlin/math.kt",
                "kotlin/mathRuntime.kt",
                "kotlin/random/**",
                // Text encoding needs more work
                "kotlin/text/utf8Encoding.kt",
                "kotlin/text/CharacterCodingExceptionBrs.kt",
                // Other files with conflicts
                "kotlin/sequences/**",
                "kotlin/concurrent.kt",
                "kotlin/enums/**",
                "kotlin/coroutines/**",
                // Time module conflicts
                "kotlin/time/**"
            )
            dependencies {
                // Depend on JS stdlib for bootstrapping - provides basic types
                implementation(project(":kotlin-stdlib"))
            }
        }
    }
}

// Expose the klib path for use by other build scripts
val brsStdlibKlib by tasks.registering {
    dependsOn("compileKotlinJs")
    val klibDir = layout.buildDirectory.dir("classes/kotlin/js/main")
    outputs.dir(klibDir)
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.jetbrains.kotlin"
            artifactId = "kotlin-stdlib-brs"
            version = project.version.toString()

            // Include the klib directory as an artifact
            artifact(tasks.named("jsJar"))
        }
    }
}

// Convenience task
tasks.register("install") {
    dependsOn("publishToMavenLocal")
}
