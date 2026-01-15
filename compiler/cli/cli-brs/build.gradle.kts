/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("jvm")
    id("jps-compatible")
    `maven-publish`
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
    implementation(project(":compiler:ir.serialization.brs"))

    // FIR frontend dependencies
    implementation(project(":compiler:fir:entrypoint"))
    implementation(project(":compiler:fir:fir2ir"))
    implementation(project(":compiler:fir:fir-serialization"))
    implementation(project(":compiler:fir:cones"))
    implementation(project(":compiler:fir:tree"))
    implementation(project(":compiler:fir:resolve"))
    implementation(project(":compiler:fir:checkers:checkers.brs"))  // For BRS FIR extensions

    // These must be implementation (not compileOnly) for the fat JAR to include them
    implementation(intellijCore())
    implementation(libs.intellij.fastutil)

    // Runtime dependencies needed for CLI
    runtimeOnly(kotlinStdlib())
    runtimeOnly(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    // Required for IntelliJ plugin loading in Kotlin 2.2.x
    runtimeOnly(commonDependency("org.codehaus.woodstox:stax2-api"))
    runtimeOnly(commonDependency("com.fasterxml:aalto-xml"))
}

sourceSets {
    "main" { projectDefault() }
}

optInToK1Deprecation()

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI",
            "-opt-in=org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI"
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

// Publishing configuration for the BRS compiler fat JAR
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.jetbrains.kotlin"
            artifactId = "kotlin-compiler-brs"
            version = project.version.toString()

            artifact(tasks.named("fatJar"))
        }
    }
}

// Convenience task matching other Kotlin modules
tasks.register("install") {
    dependsOn("publishToMavenLocal")
}
