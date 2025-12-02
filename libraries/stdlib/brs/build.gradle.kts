/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

description = "Kotlin Standard Library for BrightScript (Bootstrap/Fallback Build)"

/**
 * This build script provides two modes:
 *
 * 1. BOOTSTRAP MODE (default): Uses JS IR backend to produce klib format.
 *    This is used when the bootstrap Kotlin compiler doesn't have BRS support.
 *    The generated klib is consumed by the BrightScript backend.
 *
 * 2. FIRST-CLASS MODE: Uses native BRS target from main stdlib.
 *    When the bootstrap compiler has BRS support, the main stdlib's brs {}
 *    target should be used instead (configured in ../build.gradle.kts).
 *
 * To switch to first-class mode:
 * - Ensure bootstrap compiler has BRS target support
 * - Use main stdlib's brs {} target instead of this build
 * - This build can then be removed or kept as legacy fallback
 */

kotlin {
    // Use JS IR backend to produce klib format that BrightScript backend can consume
    // This is the bootstrap approach until the main stdlib supports BRS as a first-class target
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
            // Include all BRS-specific sources
            kotlin.srcDir("builtins")
            kotlin.srcDir("runtime")
            kotlin.srcDir("src")
            kotlin.srcDir("../brs-actual/src")  // Include BRS-specific actual implementations

            // Exclude files that conflict with JS stdlib or need more work
            // These exclusions are needed because bootstrap approach depends on JS stdlib
            kotlin.exclude(
                // Collections - JS stdlib provides these already
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
                // NOTE: sequences/ is now included - removed from exclusion list
                "kotlin/concurrent.kt",
                "kotlin/enums/**",
                "kotlin/coroutines/**",
                // Time module conflicts
                "kotlin/time/**",
                // Builtins - use JS stdlib's implementations
                "kotlin/Throwable.kt"
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
