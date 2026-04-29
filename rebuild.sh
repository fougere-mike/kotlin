#!/bin/bash
set -e

# ============================================================================
# USAGE
# ============================================================================
# ./rebuild.sh          - Rebuild all BRS artifacts (Gradle caching handles incremental)
# ./rebuild.sh --clean  - Full clean build (removes all build state and caches)
# ============================================================================

echo "=== Building and publishing Kotlin BRS artifacts to Maven Local ==="
echo ""
echo "This script builds BRS artifacts and publishes them to Maven Local."
echo "All steps run every invocation; Gradle up-to-date checks handle incremental builds."
echo "BRS compiler modules are cleaned before each run to force BRS recompilation."
echo ""
echo "Steps:"
echo "  1. cli-brs:fatJar    - BRS compiler (self-contained, no full dist needed)"
echo "  2. regenerateKlib    - Prebuilt stdlib klib (depends on fatJar)"
echo "  3. publishToMavenLocal (compiler)"
echo "  4. publishToMavenLocal (KGP + BOM)"
echo "  5. publishToMavenLocal (stdlib klib)"
echo "  6. generateStdlibBrs + publish runtime JAR"
echo "  7. kotlin-test-brs build + publish"
echo ""
echo "See CLAUDE.md 'Bootstrap Architecture' for details."
echo ""

# ============================================================================
# CONFIGURATION: BrightScript-Specific Modules
# ============================================================================

# BrightScript compiler build directories (cleaned each run to force BRS recompilation
# while allowing core Kotlin compiler tasks to remain UP-TO-DATE from Gradle's cache)
BRS_COMPILER_BUILD_DIRS=(
    "brightscript/brs.ast/build"
    "core/compiler.common.brightscript/build"
    "brs/brs.frontend/build"
    "compiler/ir/serialization.brs/build"
    "compiler/fir/checkers/checkers.brs/build"
    "compiler/ir/backend.brightscript/build"
    "compiler/cli/cli-brs/build"
)

# ============================================================================
# --clean FLAG: Full reset when things are in a bad state
# ============================================================================
if [[ "$1" == "--clean" ]]; then
    echo "Full clean requested - removing all build state..."
    echo ""

    # Clean Maven Local BRS artifacts
    echo "  Cleaning Maven Local BRS artifacts..."
    rm -rf ~/.m2/repository/com/nuvyyo/kotlin-compiler-brs
    rm -rf ~/.m2/repository/com/nuvyyo/kotlin-stdlib-brs
    rm -rf ~/.m2/repository/com/nuvyyo/kotlin-stdlib-brs-runtime
    rm -rf ~/.m2/repository/com/nuvyyo/kotlin-test-brs

    # Clean BRS compiler build directories
    echo "  Cleaning BRS compiler build directories..."
    for dir in "${BRS_COMPILER_BUILD_DIRS[@]}"; do
        rm -rf "$dir"
    done

    # Clean stdlib build directories
    echo "  Cleaning stdlib build directories..."
    rm -rf libraries/stdlib/build
    rm -rf libraries/stdlib/brs/test/build
    rm -rf libraries/stdlib/brs-prebuilt/build
    rm -rf libraries/stdlib/brs-prebuilt/.gradle

    echo ""
    echo "Clean complete. Proceeding with full rebuild..."
    echo ""
fi

# ============================================================================
# CACHE CLEANUP - BRS module build directories
# Cleaned every run to force Gradle to recompile BRS modules.
# Core Kotlin compiler tasks (~660) remain UP-TO-DATE from Gradle's cache,
# so only the ~42 BRS-specific tasks re-execute (major speedup vs full rebuild).
# ============================================================================
echo "Cleaning BRS build directories..."

echo "  Cleaning BRS compiler build directories..."
for dir in "${BRS_COMPILER_BUILD_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        rm -rf "$dir"
    fi
done
# Also clean .gradle dirs for BRS modules
for dir in "${BRS_COMPILER_BUILD_DIRS[@]}"; do
    gradle_dir="${dir%/build}/.gradle"
    if [ -d "$gradle_dir" ]; then
        rm -rf "$gradle_dir"
    fi
done
# Clean Maven local cache for BRS compiler
rm -rf ~/.m2/repository/com/nuvyyo/kotlin-compiler-brs

echo "  Cleaning stdlib build directories..."
rm -rf libraries/stdlib/build
rm -rf libraries/stdlib/brs/test/build
rm -rf libraries/stdlib/brs-prebuilt/build
rm -rf libraries/stdlib/brs-prebuilt/.gradle
rm -rf ~/.m2/repository/com/nuvyyo/kotlin-stdlib-brs
rm -rf ~/.m2/repository/com/nuvyyo/kotlin-stdlib-brs-runtime

echo "  Done."
echo ""

# ============================================================================
# BUILD FLAGS
# ============================================================================
# --no-build-cache: prevent Gradle from pulling stale cached outputs
# --no-daemon: fresh task configurations between steps
# useBootstrapStdlib=true: use external Maven stdlib instead of project(":kotlin-stdlib")
#   REQUIRED: without this, any build that re-executes :kotlin-stdlib:jvmJar puts the
#   project's 2.2.x stdlib on the classpath alongside the 2.0.21 bootstrap compiler
#   → metadata version mismatch → BUILD FAILED.
FLAGS="--no-build-cache --no-configuration-cache --no-daemon -Dorg.gradle.dependency.verification=off -Pkotlin.build.useBootstrapStdlib=true"
echo "Build mode: Gradle up-to-date checks (BRS modules cleaned to force BRS recompilation)"
echo ""

# ============================================================================
# PHASE 1: Build BRS compiler and publish compiler + KGP artifacts
# ============================================================================

echo "1. Building BRS compiler fat JAR..."
./gradlew :compiler:cli-brs:fatJar $FLAGS

echo ""
echo "2. Regenerating BRS stdlib klib..."
./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib $FLAGS

echo ""
echo "3. Publishing BRS compiler to Maven Local..."
./gradlew :compiler:cli-brs:publishToMavenLocal $FLAGS

echo ""
echo "4. Publishing Kotlin Gradle Plugin and BOM..."
./gradlew :kotlin-gradle-plugin:publishToMavenLocal :kotlin-gradle-plugins-bom:publishToMavenLocal $FLAGS

# ============================================================================
# PHASE 2: Publish pre-built stdlib klib
# The stdlib klib was already regenerated in step 2 using JavaExec (no KGP needed).
# Now we publish it to Maven Local using the simpler brs-prebuilt publishing.
# ============================================================================

echo ""
echo "5. Publishing pre-built stdlib klib to Maven Local..."
./gradlew :kotlin-stdlib-brs-prebuilt:publishToMavenLocal $FLAGS
echo "  Verified: kotlin-stdlib-brs klib published to Maven Local"

# ============================================================================
# PHASE 3: Generate stdlib BrightScript runtime
# This step generates .brs files from stdlib sources and packages them into a
# runtime JAR that can be extracted by downstream projects (e.g., test apps).
# ============================================================================

echo ""
echo "6. Publishing stdlib BrightScript runtime JAR..."
./gradlew :kotlin-stdlib:generateStdlibBrs :kotlin-stdlib:publishBrsRuntimePublicationToMavenLocal $FLAGS
echo "  Verified: com.nuvyyo:kotlin-stdlib-brs-runtime published to Maven Local"

# ============================================================================
# PHASE 4: Compile-check kotlin-test-brs
# This catches FIR-level regressions in kotlin.test that aren't covered by
# the FIR diagnostic test suite or golden file tests. Without this gate,
# checker tightenings can silently break the kotlin.test build (which only
# surfaces when run-stdlib-tests.sh is invoked explicitly).
# ============================================================================

echo ""
echo "7. Compile-checking and publishing kotlin-test-brs..."
./gradlew :kotlin-test-brs:build :kotlin-test-brs:publishToMavenLocal $FLAGS
echo "  Verified: kotlin-test-brs compiles cleanly and published to Maven Local"

echo ""
echo "=== All artifacts published to Maven Local ==="
echo ""
echo "Published artifacts:"
echo "  - com.nuvyyo:kotlin-compiler-brs"
echo "  - com.nuvyyo:kotlin-gradle-plugin-brs (with BRS target support)"
echo "  - com.nuvyyo:kotlin-gradle-plugins-bom-brs"
echo "  - com.nuvyyo:kotlin-stdlib-brs (klib)"
echo "  - com.nuvyyo:kotlin-stdlib-brs-runtime (JAR)"
echo "  - com.nuvyyo:kotlin-test-brs (klib)"
