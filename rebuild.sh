#!/bin/bash
set -e

echo "=== Building and publishing Kotlin BRS artifacts to Maven Local ==="
echo ""
echo "This script handles the two-phase bootstrap process:"
echo "  Phase 1 (Steps 1-4): Uses remote bootstrap from JetBrains Space"
echo "  Phase 2 (Steps 5-6): Uses local bootstrap with BRS support"
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
# SMART CHANGE DETECTION
# Detect what changed to determine what needs rebuilding.
# ============================================================================

# Get the last build timestamps (stored in marker files)
COMPILER_MARKER=".build-marker-compiler"
STDLIB_MARKER=".build-marker-stdlib"

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
    echo "1. Building BRS compiler fat JAR..."
    ./gradlew :compiler:cli-brs:fatJar $FLAGS

    echo ""
    echo "2. Regenerating BRS stdlib klib..."
    ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib $FLAGS

    echo ""
    echo "3. Publishing BRS compiler..."
    ./gradlew :compiler:cli-brs:publishToMavenLocal $FLAGS

    echo ""
    echo "4. Publishing Kotlin Gradle Plugin..."
    ./gradlew :kotlin-gradle-plugin:publishToMavenLocal $FLAGS

    # Update compiler build marker
    touch "$COMPILER_MARKER"
else
    echo "1-4. Skipping compiler build (no changes detected)"
fi

# ============================================================================
# PHASE 2: Local Bootstrap
# After step 4, KGP with BRS support is in Maven Local.
# ============================================================================

if [ "$STDLIB_CHANGED" = true ]; then
    echo ""
    echo "5. Publishing stdlib (including BRS runtime)..."
    echo "   (Using local bootstrap from Maven Local)"
    ./gradlew :kotlin-stdlib:publishBrsModulePublicationToMavenLocal $LOCAL_BOOTSTRAP_FLAGS $FLAGS

    echo ""
    echo "6. Publishing kotlin.test-brs..."
    echo "   (Using local bootstrap from Maven Local)"
    ./gradlew :kotlin-test:publishBrsModulePublicationToMavenLocal $LOCAL_BOOTSTRAP_FLAGS $FLAGS

    # Update stdlib build marker
    touch "$STDLIB_MARKER"
else
    echo ""
    echo "5-6. Skipping stdlib build (no changes detected)"
fi

echo ""
echo "=== All artifacts published to Maven Local ==="
echo ""
echo "Published artifacts:"
echo "  - org.jetbrains.kotlin:kotlin-compiler-brs"
echo "  - org.jetbrains.kotlin:kotlin-gradle-plugin"
echo "  - org.jetbrains.kotlin:kotlin-stdlib-brs (klib + brs-runtime)"
echo "  - org.jetbrains.kotlin:kotlin-test-brs"
