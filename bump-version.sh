#!/usr/bin/env bash
# bump-version.sh — Update the BrightScript Studio version across all repos.
#
# Usage:
#   ./bump-version.sh 2.2.20-brs.2
#
# Updates:
#   1. Kotlin/gradle.properties            (defaultSnapshotVersion)
#   2. kotlin-roku/build.gradle.kts        (version + dep versions)
#   3. kotlin-roku-intellij/build.gradle.kts (version)
#   4. brightscript-studio/ApplicationInfo.xml  (version fields)
#   5. brightscript-studio/kotlinc/build.txt    (bundled compiler version)
#   6. brightscript-studio/.idea/libraries/kotlinc_*.xml (maven-id versions)

set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <new-version>"
    echo "Example: $0 2.2.20-brs.2"
    exit 1
fi

NEW_VERSION="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KOTLIN_DIR="$SCRIPT_DIR"
ROKU_DIR="$(dirname "$SCRIPT_DIR")/kotlin-roku"
INTELLIJ_DIR="$(dirname "$SCRIPT_DIR")/kotlin-roku-intellij"
BSS_DIR="$(dirname "$SCRIPT_DIR")/brightscript-studio"

# Validate version format
if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+-brs\.[0-9]+$ ]]; then
    echo "ERROR: Version must match pattern X.Y.Z-brs.N (e.g. 2.2.20-brs.2)"
    exit 1
fi

OLD_VERSION=$(grep "^defaultSnapshotVersion=" "$KOTLIN_DIR/gradle.properties" | cut -d'=' -f2)
echo "Bumping $OLD_VERSION → $NEW_VERSION"
echo ""

# 1. Kotlin/gradle.properties
echo "1. Updating Kotlin/gradle.properties..."
sed -i '' "s/^defaultSnapshotVersion=.*/defaultSnapshotVersion=${NEW_VERSION}/" \
    "$KOTLIN_DIR/gradle.properties"

# 2. kotlin-roku/build.gradle.kts
if [[ -f "$ROKU_DIR/build.gradle.kts" ]]; then
    echo "2. Updating kotlin-roku/build.gradle.kts..."
    sed -i '' "s/^version = \"${OLD_VERSION}\"/version = \"${NEW_VERSION}\"/" \
        "$ROKU_DIR/build.gradle.kts"
    sed -i '' "s/:${OLD_VERSION}\"/:${NEW_VERSION}\"/g" \
        "$ROKU_DIR/build.gradle.kts"
else
    echo "2. WARNING: $ROKU_DIR/build.gradle.kts not found — skipping"
fi

# 3. kotlin-roku-intellij/build.gradle.kts
if [[ -f "$INTELLIJ_DIR/build.gradle.kts" ]]; then
    echo "3. Updating kotlin-roku-intellij/build.gradle.kts..."
    sed -i '' "s/^version = \"${OLD_VERSION}\"/version = \"${NEW_VERSION}\"/" \
        "$INTELLIJ_DIR/build.gradle.kts"
else
    echo "3. WARNING: $INTELLIJ_DIR/build.gradle.kts not found — skipping"
fi

# 4. brightscript-studio ApplicationInfo.xml
BSS_APP_INFO=$(find "$BSS_DIR" -name "ApplicationInfo.xml" -path "*/idea/*" 2>/dev/null | head -1)
if [[ -n "$BSS_APP_INFO" ]]; then
    echo "4. Updating $BSS_APP_INFO..."
    # Version format in ApplicationInfo.xml: major="2" minor="2.20" patch="brs.1"
    MAJOR=$(echo "$NEW_VERSION" | cut -d. -f1)
    MINOR=$(echo "$NEW_VERSION" | cut -d. -f2-3 | cut -d- -f1)
    PATCH=$(echo "$NEW_VERSION" | cut -d- -f2-)
    sed -i '' "s/major=\"[0-9]*\" minor=\"[0-9.]*\"/major=\"${MAJOR}\" minor=\"${MINOR}\"/" \
        "$BSS_APP_INFO" 2>/dev/null || true
else
    echo "4. WARNING: ApplicationInfo.xml not found in $BSS_DIR — skipping"
fi

# 5. brightscript-studio/kotlinc/build.txt
BSS_BUILD_TXT="$BSS_DIR/kotlinc/build.txt"
if [[ -f "$BSS_BUILD_TXT" ]]; then
    echo "5. Updating $BSS_BUILD_TXT..."
    echo "$NEW_VERSION" > "$BSS_BUILD_TXT"
else
    echo "5. WARNING: $BSS_BUILD_TXT not found — skipping"
fi

# 6. brightscript-studio/.idea/libraries/kotlinc_*.xml
BSS_LIBS="$BSS_DIR/.idea/libraries"
if [[ -d "$BSS_LIBS" ]]; then
    echo "6. Updating kotlinc_*.xml maven-id version strings ($BSS_LIBS)..."
    find "$BSS_LIBS" -name "kotlinc_*.xml" -exec \
        sed -i '' "s/maven-id=\"com\.nuvyyo:\([^:]*\):${OLD_VERSION}\"/maven-id=\"com.nuvyyo:\1:${NEW_VERSION}\"/g" {} \;
    COUNT=$(find "$BSS_LIBS" -name "kotlinc_*.xml" | wc -l | tr -d ' ')
    echo "   Updated $COUNT files"
else
    echo "6. WARNING: $BSS_LIBS not found — skipping"
fi

echo ""
echo "=== Version bumped to $NEW_VERSION ==="
echo ""
echo "Review changes with: git diff"
echo "Then run: ./release.sh"
