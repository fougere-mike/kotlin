/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
    `maven-publish`
}

description = "Pre-compiled BrightScript stdlib klib for bootstrapping"

val brsStdlibDir = file("../brs")
val brsActualDir = file("../brs-actual")
val outputKlib = file("kotlin-stdlib-brs.klib")

// The BRS compiler uses the Kotlin distribution built by ./gradlew dist
val distCompilerJar = rootProject.file("dist/kotlinc/lib/kotlin-compiler.jar")

/**
 * Task to regenerate the pre-compiled klib from BRS stdlib sources.
 *
 * Prerequisites:
 * - A working Kotlin/BRS compiler must be built first
 * - Run `./gradlew :compiler:cli-brs:fatJar` to build the BRS compiler
 *
 * Usage:
 * - ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib
 */
val regenerateKlib by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Regenerate BRS stdlib klib using the development compiler"

    // Use dist compiler (includes all runtime dependencies in the distribution)
    val distLibDir = rootProject.file("dist/kotlinc/lib")
    classpath = fileTree(distLibDir) { include("*.jar") }
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    // Collect all source directories from brs/ and brs-actual/
    // Both are needed - brs/ has base implementations, brs-actual/ has platform-specific ones
    // Note: brs-actual/builtins is excluded (duplicates brs/builtins)
    val sourceDirs = listOf(
        file("${brsStdlibDir}/builtins"),
        file("${brsStdlibDir}/runtime"),
        file("${brsStdlibDir}/src"),
        file("${brsActualDir}/src"),
    ).filter { it.exists() }

    doFirst {
        if (!distCompilerJar.exists()) {
            throw GradleException(
                "BRS Compiler not found. Please build the distribution first:\n" +
                "  ./gradlew dist\n" +
                "Expected location:\n" +
                "  - ${distCompilerJar}"
            )
        }

        logger.lifecycle("Regenerating BRS stdlib klib...")
        logger.lifecycle("  Compiler: ${distCompilerJar}")
        logger.lifecycle("  Sources: ${sourceDirs.map { it.name }}")
        logger.lifecycle("  Output: $outputKlib")
    }

    args(
        "-Xproduce=library",
        "-Xallow-kotlin-package",
        "-Xexpect-actual-classes",
        "-Xstdlib-compilation",  // Enable stdlib compilation mode for proper klib metadata
        "-module-name", "stdlib",  // Use "stdlib" to match Native convention
        "-output", outputKlib.absolutePath,
        *sourceDirs.map { it.absolutePath }.toTypedArray()
    )

    inputs.dir(brsStdlibDir)
    inputs.dir(brsActualDir)
    // Track the compiler JAR as an input so changes to the compiler trigger a rebuild
    if (distCompilerJar.exists()) {
        inputs.file(distCompilerJar)
    }
    // If dist doesn't exist, the doFirst block will fail with a clear error message
    outputs.file(outputKlib)
}

/**
 * Task to verify the pre-compiled klib exists and is valid.
 */
val verifyKlib by tasks.registering {
    group = "verification"
    description = "Verify the pre-compiled klib exists"

    doLast {
        if (!outputKlib.exists()) {
            throw GradleException(
                "Pre-compiled klib not found at $outputKlib. " +
                "Run 'regenerateKlib' task to generate it."
            )
        }

        val size = outputKlib.length()
        if (size < 1024) {
            throw GradleException(
                "Pre-compiled klib appears invalid (${size} bytes). " +
                "Run 'regenerateKlib' task to regenerate it."
            )
        }

        logger.lifecycle("Pre-compiled klib verified: $outputKlib (${size / 1024} KB)")
    }
}

tasks.named("check") {
    dependsOn(verifyKlib)
}

/**
 * Clean task to remove generated files.
 */
tasks.named<Delete>("clean") {
    // Don't delete the klib on clean - it's a checked-in artifact
    // Only delete build directory
}

/**
 * Publishing configuration for the pre-built stdlib klib.
 *
 * This publishes the pre-compiled klib to Maven Local so it can be used by
 * consumer projects without requiring the full local bootstrap.
 */
val kotlinVersion: String by rootProject.extra

publishing {
    publications {
        create<MavenPublication>("brsStdlib") {
            groupId = "org.jetbrains.kotlin"
            artifactId = "kotlin-stdlib-brs"
            version = kotlinVersion

            artifact(outputKlib) {
                extension = "klib"
            }

            pom {
                name.set("Kotlin Standard Library for BrightScript")
                description.set("Kotlin Standard Library compiled for the BrightScript (Roku) platform")
            }
        }
    }
}
