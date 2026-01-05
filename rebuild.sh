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

# Common flags
FLAGS="--no-configuration-cache -Dorg.gradle.dependency.verification=off"

# Local bootstrap flags - tells Gradle to use Maven Local instead of build/repo
LOCAL_BOOTSTRAP_FLAGS="-Pbootstrap.local=true -Pbootstrap.local.path=$HOME/.m2/repository/"

# ============================================================================
# PHASE 1: Remote Bootstrap
# These steps work with the remote bootstrap KGP (which has no BRS support)
# ============================================================================

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

# ============================================================================
# PHASE 2: Local Bootstrap
# After step 4, KGP with BRS support is in Maven Local.
# The -Pbootstrap.local=true flag tells Gradle to use our locally-published KGP
# instead of the remote bootstrap, enabling the brs {} blocks in stdlib/kotlin.test.
# The -Pbootstrap.local.path flag points to Maven Local since that's where we published.
# ============================================================================

echo ""
echo "5. Publishing stdlib (including BRS runtime)..."
echo "   (Using local bootstrap from Maven Local)"
./gradlew :kotlin-stdlib:publishBrsModulePublicationToMavenLocal $LOCAL_BOOTSTRAP_FLAGS $FLAGS

echo ""
echo "6. Publishing kotlin.test-brs..."
echo "   (Using local bootstrap from Maven Local)"
./gradlew :kotlin-test:publishBrsModulePublicationToMavenLocal $LOCAL_BOOTSTRAP_FLAGS $FLAGS

echo ""
echo "=== All artifacts published to Maven Local ==="
echo ""
echo "Published artifacts:"
echo "  - org.jetbrains.kotlin:kotlin-compiler-brs"
echo "  - org.jetbrains.kotlin:kotlin-gradle-plugin"
echo "  - org.jetbrains.kotlin:kotlin-stdlib-brs (klib + brs-runtime)"
echo "  - org.jetbrains.kotlin:kotlin-test-brs"
