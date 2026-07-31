#!/usr/bin/env bash
# release.sh — Publish com.nuvyyo:* artifacts to GitHub Packages and tag the release.
#
# Prerequisites:
#   ~/.gradle/gradle.properties must contain:
#     nuvyyoGitHubUser=<github-username>
#     nuvyyoGitHubToken=<classic-pat-with-write:packages+repo>
#
# Usage:
#   ./release.sh
#
# The version is read from gradle.properties (defaultSnapshotVersion).
# Run bump-version.sh first to set the desired version before releasing.

set -euo pipefail

KOTLIN_VERSION=$(grep "^defaultSnapshotVersion=" gradle.properties | cut -d'=' -f2)
TAG="v${KOTLIN_VERSION}"

# ============================================================================
# Pre-flight checks
# ============================================================================

echo "=== BrightScript Studio Release ==="
echo "Version: $KOTLIN_VERSION"
echo "Tag:     $TAG"
echo ""

# Verify working tree is clean
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "ERROR: Working tree is not clean. Commit or stash changes before releasing."
    exit 1
fi

# Verify credentials are configured
if ! grep -q "nuvyyoGitHubUser" ~/.gradle/gradle.properties 2>/dev/null; then
    echo "ERROR: nuvyyoGitHubUser not found in ~/.gradle/gradle.properties"
    echo "  Add: nuvyyoGitHubUser=<your-github-username>"
    echo "       nuvyyoGitHubToken=<classic-pat-with-write:packages+repo>"
    exit 1
fi

echo "Pre-flight checks passed."
echo ""

# ============================================================================
# Step 1: Build everything cleanly
# ============================================================================

echo "Step 1: Building all artifacts..."
./rebuild.sh

# ============================================================================
# Step 2: Publish to GitHub Packages
# ============================================================================

FLAGS="-Pkotlin.build.useBootstrapStdlib=true --no-build-cache --no-daemon --no-configuration-cache"

echo ""
echo "Step 2: Publishing compiler to GitHub Packages..."
./gradlew :compiler:cli-brs:publishAllPublicationsToNuvyyoRepository $FLAGS

echo ""
echo "Step 3: Publishing stdlib klib to GitHub Packages..."
./gradlew :kotlin-stdlib-brs-prebuilt:publishAllPublicationsToNuvyyoRepository $FLAGS

echo ""
echo "Step 4: Publishing stdlib runtime JAR to GitHub Packages..."
./gradlew :kotlin-stdlib:publishBrsRuntimePublicationToNuvyyoRepository $FLAGS

echo ""
echo "Step 5: Publishing kotlin-test-brs to GitHub Packages..."
./gradlew :kotlin-test-brs:publishAllPublicationsToNuvyyoRepository $FLAGS

echo ""
echo "Step 6: Publishing KGP and BOM to GitHub Packages..."
./gradlew \
  :kotlin-gradle-plugin:publishPluginMavenPublicationToNuvyyoRepository \
  :kotlin-gradle-plugin-api:publishAllPublicationsToNuvyyoRepository \
  :kotlin-gradle-plugin-idea:publishAllPublicationsToNuvyyoRepository \
  :kotlin-tooling-core:publishAllPublicationsToNuvyyoRepository \
  :kotlin-gradle-plugins-bom:publishAllPublicationsToNuvyyoRepository \
  $FLAGS

# ============================================================================
# Step 3: Tag and push
# ============================================================================

echo ""
echo "Step 7: Tagging release $TAG..."
git tag "$TAG"
echo "  Tagged $TAG (not pushed — run 'git push origin $TAG' to publish the tag)"

echo ""
echo "=== Release $KOTLIN_VERSION complete ==="
echo ""
echo "Next steps:"
echo "  1. git push origin $TAG"
echo "  2. Build BrightScript Studio archives from brightscript-studio/ (run installers.cmd)"
echo "     Artifacts: brightscript-studio/out/brightscript-studio/artifacts/brightscript-studio-*.mac.{aarch64,x64}.zip"
echo "  3. gh release create $TAG \\"
echo "       brightscript-studio/out/brightscript-studio/artifacts/brightscript-studio-*.mac.aarch64.zip \\"
echo "       brightscript-studio/out/brightscript-studio/artifacts/brightscript-studio-*.mac.x64.zip \\"
echo "       --repo fougere-mike/intellij-community --title \"$TAG\" --notes-file release-notes.md"
echo ""
echo "     release-notes.md should include the install snippet:"
echo "       1. Download the .mac.aarch64.zip (Apple Silicon) or .mac.x64.zip (Intel)"
echo "       2. Double-click to extract and drag 'BrightScript Studio.app' to /Applications/ (or ~/Applications/)"
echo "       3. Open Terminal and run:"
echo "            xattr -cr /Applications/'BrightScript Studio.app'"
echo "            codesign --force --deep --sign - /Applications/'BrightScript Studio.app'"
echo "          (this strips the quarantine flag and ad-hoc re-signs the bundle so macOS launches it cleanly)"
echo "       4. Open BrightScript Studio normally."
echo "  4. Update kotlin-roku/ and kotlin-roku-intellij/ versions and publish them:"
echo "     cd ../kotlin-roku && ./gradlew publishAllPublicationsToNuvyyoRepository"
