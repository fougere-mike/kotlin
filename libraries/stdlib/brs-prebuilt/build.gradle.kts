/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import java.security.MessageDigest

plugins {
    base
    `maven-publish`
}

description = "Pre-compiled BrightScript stdlib klib for bootstrapping"

val brsStdlibDir = file("../brs")
val brsActualDir = file("../brs-actual")
val outputKlib = file("kotlin-stdlib-brs.klib")
val sourceHashFile = file(".klib-source-hash")

// The BRS compiler uses the Kotlin distribution built by ./gradlew dist
val distCompilerJar = rootProject.file("dist/kotlinc/lib/kotlin-compiler.jar")

// Source directories that contribute to the prebuilt klib.
// Must stay in sync with regenerateKlib.args() below; any file under these dirs
// affects the compiled output and therefore the source hash.
val klibSourceDirs: List<File> = listOf(
    file("${brsStdlibDir}/builtins"),
    file("${brsStdlibDir}/runtime"),
    file("${brsStdlibDir}/src"),
    file("${brsActualDir}/src"),
).filter { it.exists() }

// Snapshot `projectDir` so hashing can run inside task actions without touching
// the Project object (configuration-cache hygiene).
val klibProjectDir: File = projectDir

/**
 * Compute a deterministic SHA-256 over all `.kt`/`.kts` files under [sourceDirs],
 * with paths expressed relative to [baseDir].
 *
 * Properties:
 * - Deterministic across platforms (sorted by POSIX relative path, LF line endings).
 * - Sensitive to added, modified, and deleted files.
 * - Stable across whitespace-insensitive editing (content is hashed verbatim,
 *   but `\r\n` is normalized to `\n` so CRLF checkouts don't produce false drift).
 */
fun computeSourceHash(baseDir: File, sourceDirs: List<File>): String {
    val aggregate = MessageDigest.getInstance("SHA-256")
    val entries = mutableListOf<Pair<String, String>>()
    for (dir in sourceDirs) {
        dir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
            .forEach { file ->
                val relPath = file.relativeTo(baseDir).invariantSeparatorsPath
                val normalized = file.readText().replace("\r\n", "\n")
                val fileHash = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.toByteArray(Charsets.UTF_8))
                    .joinToString("") { b -> "%02x".format(b) }
                entries.add(relPath to fileHash)
            }
    }
    entries.sortBy { it.first }
    for ((path, fileHash) in entries) {
        aggregate.update(path.toByteArray(Charsets.UTF_8))
        aggregate.update(0)
        aggregate.update(fileHash.toByteArray(Charsets.UTF_8))
        aggregate.update(0)
    }
    return aggregate.digest().joinToString("") { b -> "%02x".format(b) }
}

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

    // BRS-specific source directories
    // brs/builtins contains actual implementations for core types (Any, Unit, Nothing, Double.*, Float.*, etc.)
    // brs/src contains BRS-specific implementations of stdlib functions
    // brs-actual/src contains additional platform-specific actual implementations
    //
    // Note: We do NOT include:
    // - common sources (src/, unsigned/src, common/src) because BRS has its own implementations
    // - brs-actual/builtins because it duplicates brs/builtins (same Double.*, Float.* functions)
    val sourceDirs = klibSourceDirs

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
    // The source hash file is rewritten on every successful regeneration; declare
    // it as an output so Gradle's up-to-date checks stay correct.
    outputs.file(outputKlib)
    outputs.file(sourceHashFile)

    // Capture paths for config-cache friendliness: doLast must not touch Project.
    val rootDir = rootProject.projectDir
    val outputKlibLocal = outputKlib
    val sourceHashFileLocal = sourceHashFile
    val baseDirLocal = klibProjectDir
    val sourceDirsLocal = klibSourceDirs.toList()

    doLast {
        val hash = computeSourceHash(baseDirLocal, sourceDirsLocal)
        sourceHashFileLocal.writeText("$hash\n")
        logger.lifecycle("  Source hash: $hash")
        logger.lifecycle("  Wrote: $sourceHashFileLocal")
        logger.lifecycle("  Remember to commit both files together:")
        logger.lifecycle("    git add ${outputKlibLocal.relativeTo(rootDir)} ${sourceHashFileLocal.relativeTo(rootDir)}")
    }
}

/**
 * Task to verify the pre-compiled klib exists, is non-trivially sized, and was
 * generated from the same sources currently checked into the tree.
 *
 * The check has three layers:
 *
 * 1. Existence: the klib file must be present.
 * 2. Validity: the klib must be at least 1 KB (rules out truncated artifacts).
 * 3. Freshness: a SHA-256 over the canonical, sorted list of `(path, contentHash)`
 *    tuples for all `.kt`/`.kts` files under the stdlib sources must match the
 *    hash committed at `.klib-source-hash`. If sources changed but the klib was
 *    not regenerated, this fails with an explicit remediation message.
 *
 * Layer 3 is the important one: without it, a contributor can edit stdlib
 * sources, forget to regenerate the klib, and silently produce a branch where
 * downstream consumers link against stale bytecode. See
 * `docs/brs-intrinsic-audit.md` style drift concerns — this is the fork-only
 * equivalent safeguard for stdlib binaries.
 */
val verifyKlib by tasks.registering {
    group = "verification"
    description = "Verify the pre-compiled klib exists and matches current sources"

    // Verification always runs: it reads files (outputKlib, sourceHashFile, source dirs)
    // whose presence and contents we validate in the task action, so Gradle's own
    // up-to-date check isn't useful here — and declaring the hash file as an input
    // triggers a pre-task validation error when it's absent, which defeats our
    // custom "source hash file not found" remediation message.
    outputs.upToDateWhen { false }

    // Capture paths for config-cache friendliness: doLast must not touch Project.
    val rootDir = rootProject.projectDir
    val outputKlibLocal = outputKlib
    val sourceHashFileLocal = sourceHashFile
    val outputKlibRel = outputKlibLocal.relativeTo(rootDir)
    val sourceHashFileRel = sourceHashFileLocal.relativeTo(rootDir)
    val baseDirLocal = klibProjectDir
    val sourceDirsLocal = klibSourceDirs.toList()

    doLast {
        if (!outputKlibLocal.exists()) {
            throw GradleException(
                "Pre-compiled klib not found at $outputKlibLocal.\n" +
                "To fix:\n" +
                "  ./gradlew dist\n" +
                "  ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }

        val size = outputKlibLocal.length()
        if (size < 1024) {
            throw GradleException(
                "Pre-compiled klib at $outputKlibLocal appears invalid ($size bytes).\n" +
                "To fix:\n" +
                "  ./gradlew dist\n" +
                "  ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }

        val currentHash = computeSourceHash(baseDirLocal, sourceDirsLocal)

        if (!sourceHashFileLocal.exists()) {
            throw GradleException(
                "Source hash file not found at $sourceHashFileLocal.\n" +
                "The prebuilt klib exists, but there is no recorded source hash to verify it against.\n" +
                "To fix (this bootstraps the hash from current sources):\n" +
                "  ./gradlew dist\n" +
                "  ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib\n" +
                "  git add $outputKlibRel $sourceHashFileRel"
            )
        }

        val committedHash = sourceHashFileLocal.readText().trim()

        if (currentHash != committedHash) {
            throw GradleException(
                buildString {
                    appendLine("Prebuilt klib is stale: stdlib sources changed since the klib was last regenerated.")
                    appendLine()
                    appendLine("  Committed source hash: $committedHash")
                    appendLine("  Current source hash:   $currentHash")
                    appendLine()
                    appendLine("Whoever edited stdlib sources must regenerate the klib and commit both")
                    appendLine("artifacts together; otherwise downstream consumers link against stale bytecode.")
                    appendLine()
                    appendLine("To fix:")
                    appendLine("  ./gradlew dist")
                    appendLine("  ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib")
                    appendLine("  git add $outputKlibRel $sourceHashFileRel")
                    append("  git commit")
                }
            )
        }

        logger.lifecycle("Pre-compiled klib verified: $outputKlibLocal (${size / 1024} KB)")
        logger.lifecycle("Source hash matches:       $currentHash")
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
