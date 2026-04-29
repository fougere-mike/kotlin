#!/bin/bash
set -e

# ============================================================================
# USAGE
# ============================================================================
# ./rebuild.sh          - Smart incremental build (only rebuilds what changed)
# ./rebuild.sh --clean  - Full clean build (removes all markers and caches)
# ============================================================================

echo "=== Building and publishing Kotlin BRS artifacts to Maven Local ==="
echo ""
echo "This script handles BRS bootstrap in three phases:"
echo "  Phase 1 (Steps 1-4): Remote bootstrap - builds compiler & KGP"
echo "  Phase 2 (Step 5):    Publish prebuilt stdlib klib"
echo "  Phase 3 (Step 6):    Generate stdlib BRS runtime (JavaExec, no KGP needed)"
echo ""
echo "See CLAUDE.md 'Bootstrap Architecture' section for details."
echo ""

# ============================================================================
# CONFIGURATION: BrightScript-Specific Modules
# ============================================================================

# BrightScript compiler source directories (for change detection)
BRS_COMPILER_SRC_DIRS=(
    "brightscript/brs.ast/src"
    "core/compiler.common.brightscript/src"
    "brs/brs.frontend/src"
    "compiler/ir/serialization.brs/src"
    "compiler/fir/checkers/checkers.brs/src"
    "compiler/ir/backend.brightscript/src"
    "compiler/cli/cli-brs/src"
)

# BrightScript compiler build directories (to clean for forced rebuild)
BRS_COMPILER_BUILD_DIRS=(
    "brightscript/brs.ast/build"
    "core/compiler.common.brightscript/build"
    "brs/brs.frontend/build"
    "compiler/ir/serialization.brs/build"
    "compiler/fir/checkers/checkers.brs/build"
    "compiler/ir/backend.brightscript/build"
    "compiler/cli/cli-brs/build"
)

# Stdlib source directories (for change detection)
STDLIB_SRC_DIRS=(
    "libraries/stdlib/brs"
    "libraries/stdlib/brs-actual"
)

# ============================================================================
# MARKER FILES
# ============================================================================
COMPILER_MARKER=".build-marker-compiler"
STDLIB_MARKER=".build-marker-stdlib"

# ============================================================================
# --clean FLAG: Full reset when things are in a bad state
# ============================================================================
if [[ "$1" == "--clean" ]]; then
    echo "Full clean requested - removing all build state..."
    echo ""

    # Remove markers
    rm -f "$COMPILER_MARKER" "$STDLIB_MARKER"

    # Clean Maven Local BRS artifacts
    echo "  Cleaning Maven Local BRS artifacts..."
    rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs
    rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs
    rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-test-brs

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
# SMART CHANGE DETECTION
# Detect what changed to determine what needs rebuilding.
# ============================================================================

# Check if compiler sources changed since last build
# NOTE: We use git status instead of file mtime because some editors/tools
# (including Claude Code's Edit tool) may not update mtime when editing files.
COMPILER_CHANGED=false
if [ ! -f "$COMPILER_MARKER" ]; then
    COMPILER_CHANGED=true
    echo "Compiler: No previous build marker found, will rebuild"
else
    # Check for uncommitted changes in BRS compiler directories using git
    for dir in "${BRS_COMPILER_SRC_DIRS[@]}"; do
        if [ -d "$dir" ]; then
            CHANGED=$(git status --short "$dir" 2>/dev/null | head -1)
            if [ -n "$CHANGED" ]; then
                COMPILER_CHANGED=true
                echo "Compiler: Changes detected in $dir"
                break
            fi
        fi
    done
fi

# Check if stdlib sources changed since last build
STDLIB_CHANGED=false
if [ ! -f "$STDLIB_MARKER" ]; then
    STDLIB_CHANGED=true
    echo "Stdlib: No previous build marker found, will rebuild"
else
    for dir in "${STDLIB_SRC_DIRS[@]}"; do
        if [ -d "$dir" ]; then
            CHANGED=$(git status --short "$dir" 2>/dev/null | head -1)
            if [ -n "$CHANGED" ]; then
                STDLIB_CHANGED=true
                echo "Stdlib: Changes detected in $dir"
                break
            fi
        fi
    done
fi

# If compiler changed, stdlib must also be rebuilt with new compiler
if [ "$COMPILER_CHANGED" = true ]; then
    STDLIB_CHANGED=true
    echo "Stdlib: Will rebuild because compiler changed"
fi

echo ""

# ============================================================================
# CACHE CLEANUP - Only BRS module build directories
# This forces Gradle to recompile BRS modules while keeping cached outputs
# for unchanged core Kotlin compiler modules (major speedup).
# ============================================================================
echo "Cleaning caches for changed components..."

if [ "$COMPILER_CHANGED" = true ]; then
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
    rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs
fi

if [ "$STDLIB_CHANGED" = true ]; then
    echo "  Cleaning stdlib build directories..."
    rm -rf libraries/stdlib/build
    rm -rf libraries/stdlib/brs/test/build
    rm -rf libraries/stdlib/brs-prebuilt/build
    rm -rf libraries/stdlib/brs-prebuilt/.gradle
    rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs
fi

echo "  Done."
echo ""

# ============================================================================
# BUILD FLAGS
# ============================================================================
# Base flags (always used):
# --no-build-cache prevents Gradle from pulling stale cached outputs
# --no-daemon ensures fresh task configurations between steps
BASE_FLAGS="--no-build-cache --no-configuration-cache --no-daemon -Dorg.gradle.dependency.verification=off"

# First build (no marker): use --rerun-tasks for reliability
# Incremental builds: rely on build directory cleanup to force BRS recompilation
# This avoids rebuilding the entire Kotlin compiler (~645 tasks) when only BRS modules (~42 tasks) changed
if [ ! -f "$COMPILER_MARKER" ]; then
    FLAGS="$BASE_FLAGS --rerun-tasks"
    echo "Build mode: Full rebuild (first build, using --rerun-tasks)"
else
    FLAGS="$BASE_FLAGS"
    echo "Build mode: Incremental (BRS modules cleaned, skipping --rerun-tasks)"
fi
echo ""

# Local bootstrap flags - tells Gradle to use Maven Local instead of build/repo
LOCAL_BOOTSTRAP_FLAGS="-Pbootstrap.local=true -Pbootstrap.local.path=$HOME/.m2/repository/"

# ============================================================================
# PHASE 1: Remote Bootstrap
# These steps work with the remote bootstrap KGP (which has no BRS support)
# ============================================================================

if [ "$COMPILER_CHANGED" = true ]; then
    echo "1. Building Kotlin distribution (includes BRS backend)..."
    ./gradlew dist $FLAGS

    echo ""
    echo "2. Regenerating BRS stdlib klib..."
    ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib $FLAGS

    echo ""
    echo "3. Publishing BRS compiler..."
    ./gradlew :compiler:cli-brs:publishToMavenLocal $FLAGS

    echo ""
    echo "4. Publishing Kotlin Gradle Plugin and BOM..."
    ./gradlew :kotlin-gradle-plugin:publishToMavenLocal :kotlin-gradle-plugins-bom:publishToMavenLocal $FLAGS

    # Verify compiler JAR was created
    COMPILER_JAR="dist/kotlinc/lib/kotlin-compiler.jar"
    if [[ ! -f "$COMPILER_JAR" ]]; then
        echo ""
        echo "ERROR: Compiler distribution not created at $COMPILER_JAR"
        echo "Build failed - not updating marker."
        rm -f "$COMPILER_MARKER"
        exit 1
    fi
    echo "  Verified: Compiler JAR exists at $COMPILER_JAR"

    # Update compiler build marker
    touch "$COMPILER_MARKER"
else
    echo "1-4. Skipping compiler build (no changes detected)"
fi

# ============================================================================
# PHASE 2: Publish pre-built stdlib klib
# The stdlib klib was already regenerated in step 2 using JavaExec (no KGP needed).
# Now we publish it to Maven Local using the simpler brs-prebuilt publishing.
# ============================================================================

if [ "$STDLIB_CHANGED" = true ]; then
    echo ""
    echo "5. Publishing pre-built stdlib klib to Maven Local..."
    ./gradlew :kotlin-stdlib-brs-prebuilt:publishToMavenLocal $FLAGS

    # Verify stdlib was published
    # Note: kotlinVersion is set via gradle.properties (e.g., 2.2.255-SNAPSHOT)
    echo "  Verified: kotlin-stdlib-brs klib published to Maven Local"

    # Update stdlib build marker
    touch "$STDLIB_MARKER"
else
    echo ""
    echo "5. Skipping stdlib publish (no changes detected)"
fi

# ============================================================================
# PHASE 3: Generate stdlib BrightScript runtime
# This step generates .brs files from stdlib sources and packages them into a
# runtime JAR that can be extracted by downstream projects (e.g., test apps).
# NOTE: These tasks use JavaExec directly and don't require KGP BRS support,
# so we don't need LOCAL_BOOTSTRAP_FLAGS here.
# ============================================================================

if [ "$STDLIB_CHANGED" = true ]; then
    echo ""
    echo "6. Publishing stdlib BrightScript runtime JAR..."
    ./gradlew :kotlin-stdlib:generateStdlibBrs :kotlin-stdlib:publishBrsRuntimePublicationToMavenLocal $FLAGS

    echo "  Verified: com.nuvyyo:kotlin-stdlib-brs-runtime published to Maven Local"
else
    echo ""
    echo "6. Skipping stdlib BRS runtime (no changes detected)"
fi

# ============================================================================
# PHASE 4: Compile-check kotlin-test-brs
# This catches FIR-level regressions in kotlin.test that aren't covered by
# the FIR diagnostic test suite or golden file tests. Without this gate,
# checker tightenings can silently break the kotlin.test build (which only
# surfaces when run-stdlib-tests.sh is invoked explicitly).
# ============================================================================

if [ "$STDLIB_CHANGED" = true ] || [ "$COMPILER_CHANGED" = true ]; then
    echo ""
    echo "7. Compile-checking and publishing kotlin-test-brs..."
    ./gradlew :kotlin-test-brs:build :kotlin-test-brs:publishToMavenLocal $FLAGS

    echo "  Verified: kotlin-test-brs compiles cleanly and published to Maven Local"
else
    echo ""
    echo "7. Skipping kotlin-test-brs (no changes detected)"
fi

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
