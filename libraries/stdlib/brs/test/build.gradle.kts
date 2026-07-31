/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
}

description = "BrightScript stdlib runtime tests"

// The BRS compiler fat JAR from cli-brs:fatJar (self-contained, no dist needed).
// Path derived from the build directory rather than tasks.named() on a cross-project
// reference, which would force early configuration (same idiom as brs-prebuilt).
val cliBrsProject = rootProject.project(":compiler:cli-brs")
val fatJarFile: Provider<RegularFile> = cliBrsProject.layout.buildDirectory.file("libs/kotlinc-brs.jar")
val stdlibKlib = rootProject.file("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
val kotlinTestKlib = rootProject.file("libraries/kotlin.test/brs/build/kotlin-test-brs.klib")
val outputDir = file("build/brs")

/**
 * Compile stdlib tests to BrightScript.
 */
val compileTests by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Compile stdlib tests to BrightScript"

    dependsOn(":compiler:cli-brs:fatJar")

    classpath = files(fatJarFile)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val testSources = file("kotlin")

    doFirst {
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Build it with: ./rebuild.sh"
            )
        }
        if (!kotlinTestKlib.exists()) {
            throw GradleException(
                "kotlin.test klib not found: ${kotlinTestKlib.absolutePath}\n" +
                "Build it with: ./gradlew :kotlin-test-brs:build"
            )
        }

        outputDir.mkdirs()
        logger.lifecycle("Compiling stdlib tests...")
        logger.lifecycle("  Compiler: ${fatJarFile.get().asFile}")
        logger.lifecycle("  Stdlib: ${stdlibKlib.name}")
        logger.lifecycle("  kotlin.test: ${kotlinTestKlib.name}")
        logger.lifecycle("  Output: ${outputDir.absolutePath}")
    }

    args(
        "-Xproduce=executable",
        "-Xallow-kotlin-package",
        "-libraries", "${stdlibKlib.absolutePath}${File.pathSeparator}${kotlinTestKlib.absolutePath}",
        "-output-dir", outputDir.absolutePath,
        testSources.absolutePath
    )

    inputs.dir(testSources)
    inputs.file(fatJarFile)
    inputs.file(stdlibKlib)
    inputs.file(kotlinTestKlib)
    outputs.dir(outputDir)
}

compileTests {
    dependsOn(":kotlin-test-brs:build")
}

tasks.named("build") {
    dependsOn(compileTests)
}

tasks.named<Delete>("clean") {
    delete("build")
}
