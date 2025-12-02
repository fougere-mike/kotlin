/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
}

description = "Kotlin test framework for BrightScript"

val kotlincBrsJar = rootProject.file("compiler/cli/cli-brs/build/libs/kotlinc-brs-2.1.255-SNAPSHOT.jar")
val stdlibKlib = rootProject.file("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
val outputKlib = file("build/kotlin-test-brs.klib")

val buildKlib by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Build kotlin.test klib for BRS target"

    classpath = files(kotlincBrsJar)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val brsSrc = file("src/main/kotlin")

    doFirst {
        if (!kotlincBrsJar.exists()) {
            throw GradleException(
                "BRS compiler not found: ${kotlincBrsJar.absolutePath}\n" +
                "Build it with: ./gradlew :compiler:cli-brs:fatJar"
            )
        }
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Generate it with: ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }
        logger.lifecycle("Building kotlin.test klib for BRS...")
        logger.lifecycle("  Compiler: ${kotlincBrsJar.name}")
        logger.lifecycle("  Stdlib: ${stdlibKlib.name}")
        logger.lifecycle("  Output: ${outputKlib.name}")
    }

    args(
        "-Xproduce=library",
        "-Xallow-kotlin-package",
        "-Xir-module-name=kotlin-test",
        "-libraries", stdlibKlib.absolutePath,
        "-output", outputKlib.absolutePath,
        brsSrc.absolutePath
    )

    dependsOn(":kotlin-stdlib-brs-prebuilt:regenerateKlib")

    inputs.dir(brsSrc)
    outputs.file(outputKlib)
}

tasks.named("build") {
    dependsOn(buildKlib)
}

tasks.named<Delete>("clean") {
    delete("build")
}
