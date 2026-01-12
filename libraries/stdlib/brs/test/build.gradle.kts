/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
}

description = "BrightScript stdlib runtime tests"

// Use dist compiler (includes all runtime dependencies in the distribution)
val distLibDir = rootProject.file("dist/kotlinc/lib")
val stdlibKlib = rootProject.file("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
val kotlinTestKlib = rootProject.file("libraries/kotlin.test/brs/build/kotlin-test-brs.klib")
val outputDir = file("build/brs")

/**
 * Compile stdlib tests to BrightScript.
 */
val compileTests by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Compile stdlib tests to BrightScript"

    classpath = fileTree(distLibDir) { include("*.jar") }
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val testSources = file("kotlin")

    doFirst {
        val compilerJar = distLibDir.resolve("kotlin-compiler.jar")
        if (!compilerJar.exists()) {
            throw GradleException(
                "Kotlin distribution not found at: ${distLibDir.absolutePath}\n" +
                "Build it with: ./gradlew dist"
            )
        }
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
        logger.lifecycle("  Compiler: dist/kotlinc/lib")
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
