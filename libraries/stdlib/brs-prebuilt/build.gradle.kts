/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
}

description = "Pre-compiled BrightScript stdlib klib for bootstrapping"

val brsStdlibDir = file("../brs")
val outputKlib = file("kotlin-stdlib-brs.klib")

// The BRS compiler CLI can be found in multiple locations:
// 1. Fat JAR built by :compiler:cli-brs:fatJar
// 2. Dist location after ./gradlew dist
val brsCompilerFatJar = rootProject.file("compiler/cli/cli-brs/build/libs/kotlinc-brs-2.1.255-SNAPSHOT.jar")
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

    // Prefer the BRS-specific fat JAR if available, otherwise use dist compiler
    val compilerJar = if (brsCompilerFatJar.exists()) brsCompilerFatJar else distCompilerJar
    classpath = files(compilerJar)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    // Collect all source directories
    // Note: Only includes brs (base implementation), not brs-actual
    // brs-actual is an overlay that gets included during normal stdlib compilation
    val sourceDirs = listOf(
        file("${brsStdlibDir}/builtins"),
        file("${brsStdlibDir}/runtime"),
        file("${brsStdlibDir}/src"),
    ).filter { it.exists() }

    doFirst {
        val selectedJar = if (brsCompilerFatJar.exists()) brsCompilerFatJar else distCompilerJar
        if (!selectedJar.exists()) {
            throw GradleException(
                "BRS Compiler not found. Please build it first:\n" +
                "  ./gradlew :compiler:cli-brs:fatJar\n" +
                "Expected locations:\n" +
                "  - ${brsCompilerFatJar}\n" +
                "  - ${distCompilerJar}"
            )
        }

        logger.lifecycle("Regenerating BRS stdlib klib...")
        logger.lifecycle("  Compiler: ${selectedJar}")
        logger.lifecycle("  Sources: ${sourceDirs.map { it.name }}")
        logger.lifecycle("  Output: $outputKlib")
    }

    args(
        "-Xproduce=library",
        "-Xallow-kotlin-package",
        "-Xexpect-actual-classes",
        "-Xir-module-name=kotlin-stdlib-brs",
        "-output", outputKlib.absolutePath,
        *sourceDirs.map { it.absolutePath }.toTypedArray()
    )

    inputs.dir(brsStdlibDir)
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
