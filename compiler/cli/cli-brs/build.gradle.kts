/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":compiler:cli"))
    implementation(project(":compiler:cli-common"))
    implementation(project(":core:compiler.common.brightscript"))
    implementation(project(":compiler:backend.brightscript"))
    implementation(project(":brightscript:brs.ast"))

    implementation(project(":compiler:frontend"))
    implementation(project(":compiler:frontend.java"))
    implementation(project(":compiler:ir.tree"))
    implementation(project(":compiler:ir.backend.common"))
    implementation(project(":compiler:ir.serialization.common"))

    // FIR frontend dependencies
    implementation(project(":compiler:fir:entrypoint"))
    implementation(project(":compiler:fir:fir2ir"))
    implementation(project(":compiler:fir:fir-serialization"))
    implementation(project(":compiler:fir:cones"))
    implementation(project(":compiler:fir:tree"))
    implementation(project(":compiler:fir:resolve"))

    implementation(intellijCore())
    implementation(commonDependency("org.jetbrains.intellij.deps:trove4j"))

    // Runtime dependencies needed for CLI (from prepare/compiler)
    runtimeOnly(kotlinStdlib())
    runtimeOnly(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    runtimeOnly(commonDependency("org.jetbrains.intellij.deps.fastutil:intellij-deps-fastutil"))
    runtimeOnly(commonDependency("org.codehaus.woodstox:stax2-api")) { isTransitive = false }
    runtimeOnly(commonDependency("com.fasterxml:aalto-xml")) { isTransitive = false }
}

sourceSets {
    "main" { projectDefault() }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI"
        )
    }
}

// Create executable jar
val mainClass = "org.jetbrains.kotlin.cli.brs.K2BrsCompiler"

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("kotlinc-brs")
    manifest {
        attributes["Main-Class"] = mainClass
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Task to run the compiler
tasks.register<JavaExec>("run") {
    dependsOn(":compiler:cli-common:jar")
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")
    classpath = sourceSets["main"].runtimeClasspath
    // Run from project root so the hack for finding compiler.xml works
    workingDir = rootProject.projectDir
}
