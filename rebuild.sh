#!/bin/bash
set -e

echo "=== Building and publishing Kotlin BRS artifacts to Maven Local ==="

# Common flags
FLAGS="--no-configuration-cache --dependency-verification=off"

echo ""
echo "1. Publishing BRS compiler..."
./gradlew :compiler:cli-brs:publishToMavenLocal $FLAGS

echo ""
echo "2. Publishing Kotlin Gradle Plugin..."
./gradlew :kotlin-gradle-plugin:publishToMavenLocal $FLAGS

echo ""
echo "3. Publishing stdlib (including BRS runtime)..."
./gradlew :kotlin-stdlib:publishBrsModulePublicationToMavenLocal $FLAGS

echo ""
echo "=== All artifacts published to Maven Local ==="
echo ""
echo "Published artifacts:"
echo "  - org.jetbrains.kotlin:kotlin-compiler-brs"
echo "  - org.jetbrains.kotlin:kotlin-gradle-plugin"
echo "  - org.jetbrains.kotlin:kotlin-stdlib-brs (klib + brs-runtime)"
