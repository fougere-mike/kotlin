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
                    "-Xir-module-name=kotlin-brs"
                )
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            kotlin.srcDir("src")
            // Exclude files that conflict with JS stdlib or have unresolved issues
            // These will be fixed incrementally
            kotlin.exclude(
                // Files with actual/expect mismatch - entire collections directory
                "kotlin/collections/**",
                "kotlin/AutoCloseableBrs.kt",
                "kotlin/ExceptionHelpers.kt",
                // All roku-specific files - these need core.kt fixed first
                "kotlin/brs/roku/**",
                // Math files with expect/actual in same module issue
                "kotlin/math.kt",
                "kotlin/mathRuntime.kt",
                // Text encoding
                "kotlin/text/CharacterCodingExceptionBrs.kt",
                "kotlin/text/utf8Encoding.kt",
                // Files with other conflicts - will fix incrementally
                "kotlin/random/**",
                "kotlin/sequences/**",
                "kotlin/exceptionsBrs.kt",
                "kotlin/concurrent.kt",
                "kotlin/enums/**",
                "kotlin/coroutines/**",
                "kotlin/io/**"
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
