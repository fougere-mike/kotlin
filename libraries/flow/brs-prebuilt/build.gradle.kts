/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import java.security.MessageDigest

plugins {
    base
}

description = "Pre-compiled BrightScript flow klib for test harnesses and bootstrapping"

val brsFlowDir = file("../brs")
val stdlibKlib = rootProject.file("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
val outputKlib = file("kotlin-flow-brs.klib")
val sourceHashFile = file(".klib-source-hash")

// The BRS compiler fat JAR from cli-brs:fatJar (self-contained, no dist needed).
// Path derived from the build directory rather than tasks.named() on a cross-project
// reference, which would force early configuration (same idiom as stdlib brs-prebuilt).
val cliBrsProject = rootProject.project(":compiler:cli-brs")
val fatJarFile: Provider<RegularFile> = cliBrsProject.layout.buildDirectory.file("libs/kotlinc-brs.jar")

// Source directories that contribute to the prebuilt klib.
// Must stay in sync with regenerateKlib.args() below; any file under these dirs
// affects the compiled output and therefore the source hash.
val klibSourceDirs: List<File> = listOf(
    file("${brsFlowDir}/src"),
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
 * Task to regenerate the pre-compiled flow klib from flow library sources.
 *
 * NOTE: unlike the stdlib prebuilt, this compiles WITHOUT -Xstdlib-compilation —
 * that is the whole point of the flow klib (flow-program spec decision 10): stdlib
 * compilation generates NO suspend state machines, so flow operator internals must
 * compile as USER-mode code. Flags must stay in sync with buildKlib in
 * libraries/flow/brs/build.gradle.kts (same sources, same module name, same flags).
 *
 * Usage:
 * - ./gradlew :kotlin-flow-brs-prebuilt:regenerateKlib
 */
val regenerateKlib by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Regenerate BRS flow klib using the development compiler"

    dependsOn(":compiler:cli-brs:fatJar")
    dependsOn(":kotlin-stdlib-brs-prebuilt:regenerateKlib")

    // Fat JAR is self-contained (all compiler deps included via fatJar)
    classpath = files(fatJarFile)
    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")

    val sourceDirs = klibSourceDirs

    doFirst {
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Generate it with: ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }
        val fatJar = fatJarFile.get().asFile
        logger.lifecycle("Regenerating BRS flow klib...")
        logger.lifecycle("  Compiler: $fatJar")
        logger.lifecycle("  Sources: ${sourceDirs.map { it.name }}")
        logger.lifecycle("  Output: $outputKlib")
    }

    args(
        "-Xproduce=library",
        "-Xallow-kotlin-package",
        "-module-name", "kotlin.flow",
        "-libraries", stdlibKlib.absolutePath,
        "-output", outputKlib.absolutePath,
        *sourceDirs.map { it.absolutePath }.toTypedArray()
    )

    inputs.dir(brsFlowDir.resolve("src"))
    inputs.file(stdlibKlib)
    inputs.files(fatJarFile)
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
 * Same three-layer check as the stdlib brs-prebuilt verifyKlib: existence,
 * validity (>= 1 KB), and source-hash freshness. Without layer 3, a contributor
 * can edit flow sources, forget to regenerate the klib, and silently produce a
 * branch where the test harnesses link against stale bytecode.
 */
val verifyKlib by tasks.registering {
    group = "verification"
    description = "Verify the pre-compiled flow klib exists and matches current sources"

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
                "Pre-compiled flow klib not found at $outputKlibLocal.\n" +
                "To fix:\n" +
                "  ./gradlew :compiler:cli-brs:fatJar\n" +
                "  ./gradlew :kotlin-flow-brs-prebuilt:regenerateKlib"
            )
        }

        val size = outputKlibLocal.length()
        if (size < 1024) {
            throw GradleException(
                "Pre-compiled flow klib at $outputKlibLocal appears invalid ($size bytes).\n" +
                "To fix:\n" +
                "  ./gradlew :compiler:cli-brs:fatJar\n" +
                "  ./gradlew :kotlin-flow-brs-prebuilt:regenerateKlib"
            )
        }

        val currentHash = computeSourceHash(baseDirLocal, sourceDirsLocal)

        if (!sourceHashFileLocal.exists()) {
            throw GradleException(
                "Source hash file not found at $sourceHashFileLocal.\n" +
                "The prebuilt klib exists, but there is no recorded source hash to verify it against.\n" +
                "To fix (this bootstraps the hash from current sources):\n" +
                "  ./gradlew :compiler:cli-brs:fatJar\n" +
                "  ./gradlew :kotlin-flow-brs-prebuilt:regenerateKlib\n" +
                "  git add $outputKlibRel $sourceHashFileRel"
            )
        }

        val committedHash = sourceHashFileLocal.readText().trim()

        if (currentHash != committedHash) {
            throw GradleException(
                buildString {
                    appendLine("Prebuilt flow klib is stale: flow sources changed since the klib was last regenerated.")
                    appendLine()
                    appendLine("  Committed source hash: $committedHash")
                    appendLine("  Current source hash:   $currentHash")
                    appendLine()
                    appendLine("Whoever edited flow sources must regenerate the klib and commit both")
                    appendLine("artifacts together; otherwise the test harnesses link against stale bytecode.")
                    appendLine()
                    appendLine("To fix:")
                    appendLine("  ./gradlew :compiler:cli-brs:fatJar")
                    appendLine("  ./gradlew :kotlin-flow-brs-prebuilt:regenerateKlib")
                    appendLine("  git add $outputKlibRel $sourceHashFileRel")
                    append("  git commit")
                }
            )
        }

        logger.lifecycle("Pre-compiled flow klib verified: $outputKlibLocal (${size / 1024} KB)")
        logger.lifecycle("Source hash matches:            $currentHash")
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
