/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    base
    `maven-publish`
}

description = "Kotlin test framework for BrightScript"

// The BRS compiler fat JAR from cli-brs:fatJar (self-contained, no dist needed).
// Path derived from the build directory rather than tasks.named() on a cross-project
// reference, which would force early configuration (same idiom as brs-prebuilt).
val cliBrsProject = rootProject.project(":compiler:cli-brs")
val fatJarFile: Provider<RegularFile> = cliBrsProject.layout.buildDirectory.file("libs/kotlinc-brs.jar")
val stdlibKlib = rootProject.file("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
val outputKlib = file("build/kotlin-test-brs.klib")

val buildKlib by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Build kotlin.test klib for BRS target"

    dependsOn(":compiler:cli-brs:fatJar")

    classpath = files(fatJarFile)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val brsSrc = file("src/main/kotlin")

    doFirst {
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Generate it with: ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }
        logger.lifecycle("Building kotlin.test klib for BRS...")
        logger.lifecycle("  Compiler: ${fatJarFile.get().asFile}")
        logger.lifecycle("  Stdlib: ${stdlibKlib.name}")
        logger.lifecycle("  Output: ${outputKlib.name}")
    }

    args(
        "-Xproduce=library",
        "-Xallow-kotlin-package",
        "-module-name", "kotlin.test",
        "-libraries", stdlibKlib.absolutePath,
        "-output", outputKlib.absolutePath,
        brsSrc.absolutePath
    )

    dependsOn(":kotlin-stdlib-brs-prebuilt:regenerateKlib")

    inputs.dir(brsSrc)
    inputs.file(fatJarFile)
    inputs.file(stdlibKlib)
    outputs.file(outputKlib)
}

// Generate .brs sources from the kotlin.test Kotlin sources. Mirrors the
// compiler invocation in libraries/stdlib/brs/test/run-tests.sh (kotlin.test
// step): source-to-.brs, not -Xproduce=library.
val brsRuntimeDir = layout.buildDirectory.dir("brs-runtime")

val generateTestBrs by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generate BrightScript source files from kotlin.test Kotlin sources"

    dependsOn(":compiler:cli-brs:fatJar")
    dependsOn(":kotlin-stdlib-brs-prebuilt:regenerateKlib")

    classpath = files(fatJarFile)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val brsSrc = file("src/main/kotlin")

    doFirst {
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Generate it with: ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }
        // Start from a clean output dir: the compiler only writes files for current
        // sources, so a renamed/deleted .kt would otherwise leave its stale .brs
        // behind and get packed into the runtime JAR.
        brsRuntimeDir.get().asFile.deleteRecursively()
    }

    args(
        "-Xallow-kotlin-package",
        "-libraries", stdlibKlib.absolutePath,
        "-output-dir", brsRuntimeDir.get().asFile.absolutePath,
        brsSrc.absolutePath
    )

    inputs.dir(brsSrc)
    inputs.file(fatJarFile)
    inputs.file(stdlibKlib)
    outputs.dir(brsRuntimeDir)
}

// JAR of the compiled .brs files, packed flat for extraction into Roku apps
// (mirrors the kotlin-stdlib-brs-runtime jar in libraries/stdlib/build.gradle.kts)
val brsRuntimeJar = tasks.register<Jar>("brsRuntimeJar") {
    archiveBaseName.set("kotlin-test-brs-runtime")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    from(layout.buildDirectory.dir("brs-runtime/source"))
    dependsOn(generateTestBrs)
}

tasks.named("build") {
    dependsOn(buildKlib)
    dependsOn(brsRuntimeJar)
}

tasks.named<Delete>("clean") {
    delete("build")
}

apply(plugin = "nuvyyo-publishing")

publishing {
    publications {
        create<MavenPublication>("brsKotlinTest") {
            groupId = "com.nuvyyo"
            artifactId = "kotlin-test-brs"
            version = project.version.toString()

            artifact(outputKlib) {
                extension = "klib"
                builtBy(buildKlib)
            }

            pom {
                name.set("Kotlin Test for BrightScript")
                description.set("Kotlin test framework compiled for the BrightScript (Roku) platform")
            }
        }

        create<MavenPublication>("brsKotlinTestRuntime") {
            groupId = "com.nuvyyo"
            artifactId = "kotlin-test-brs-runtime"
            version = project.version.toString()

            artifact(brsRuntimeJar)

            pom {
                name.set("Kotlin Test BrightScript Runtime")
                description.set("Compiled .brs files of the Kotlin test framework for packaging in Roku apps")
            }
        }
    }
}
