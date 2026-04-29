# BrightScript Studio Distribution — Design Spec

**Date:** 2026-04-29  
**Status:** Approved  
**Scope:** v1 — Maven namespace rename + remote publish + BrightScript Studio rebrand DMG

---

## Problem

The Kotlin/BRS fork cannot be shared with the team today. Every developer must clone the full Kotlin monorepo and run `rebuild.sh` to produce local Maven artifacts. All published artifacts impersonate JetBrains (`org.jetbrains.kotlin:*`), creating Maven Central collision risk. The IDE fork (`brightscript-studio/`) is unbranded and has no installer pipeline configured for our product.

## Solution

Three deliverables, one lockstep version (`2.2.20-brs.N`):

1. **`com.nuvyyo:*` Maven artifacts** on GitHub Packages — compiler, stdlib, runtime, test, KGP fork.
2. **`com.nuvyyo.brightscript:kotlin-roku` Gradle plugin** — single coordinate user projects apply.
3. **BrightScript Studio.dmg** — rebranded IntelliJ IDEA Community 2025.2 with our Kotlin/BRS plugin bundled.

## Coordinate Rename Map

| Today | After |
|---|---|
| `org.jetbrains.kotlin:kotlin-compiler-brs:2.2.255-SNAPSHOT` | `com.nuvyyo:kotlin-compiler-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-stdlib-brs:2.2.255-SNAPSHOT` | `com.nuvyyo:kotlin-stdlib-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-stdlib-brs:...:brs-runtime` (manual `cp`) | `com.nuvyyo:kotlin-stdlib-brs-runtime:2.2.20-brs.1` (properly published) |
| `org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.255-SNAPSHOT` | `com.nuvyyo:kotlin-gradle-plugin-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-gradle-plugin-api:...` | `com.nuvyyo:kotlin-gradle-plugin-api-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-gradle-plugin-idea:...` | `com.nuvyyo:kotlin-gradle-plugin-idea-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-tooling-core:...` | `com.nuvyyo:kotlin-tooling-core-brs:2.2.20-brs.1` |
| `org.jetbrains.kotlin:kotlin-gradle-plugins-bom:...` | `com.nuvyyo:kotlin-gradle-plugins-bom-brs:2.2.20-brs.1` |
| (not published) `kotlin-test-brs.klib` | `com.nuvyyo:kotlin-test-brs:2.2.20-brs.1` |
| `com.example:kotlin-roku:1.0.0-SNAPSHOT` | `com.nuvyyo.brightscript:kotlin-roku:2.2.20-brs.1` |
| `com.example.roku:kotlin-roku-intellij:1.0.0-SNAPSHOT` | `com.nuvyyo.brightscript:kotlin-roku-intellij:2.2.20-brs.1` |

**Plugin ID stays `org.jetbrains.kotlin`** — hundreds of IntelliJ `<depends>` declarations and runtime checks require it. The plugin ID is separate from the Maven GAV.

## Architecture

```
Kotlin/ (compiler repo)
  └─ ./rebuild.sh → com.nuvyyo:* → ~/.m2 (dev loop)
  └─ ./release.sh → com.nuvyyo:* → github.com/nuvyyo/maven-brs (packages)

kotlin-roku/ (Gradle plugin)
  └─ com.nuvyyo.brightscript:kotlin-roku → github.com/nuvyyo/maven-brs

brightscript-studio/ (IDE fork)
  └─ installers.cmd → BrightScriptStudio-2.2.20-brs.1.dmg
  └─ gh release create → github.com/nuvyyo/brightscript-studio (releases)

User project build.gradle.kts:
  plugins {
    id("com.nuvyyo.brightscript.kotlin-roku") version "2.2.20-brs.1"
  }
  // No manual stdlib-brs declaration — plugin pins it transitively
```

## Implementation Steps

### Step 1 — Group rewrite in Kotlin/ (compiler repo)

- `repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/common-configuration.gradle.kts:19` — `group = "com.nuvyyo"`
- `gradle.properties:25` — `defaultSnapshotVersion=2.2.20-brs.1`
- Append `-brs` to `archivesName`/`artifactId` for: `kotlin-gradle-plugin`, `kotlin-gradle-plugin-api`, `kotlin-gradle-plugin-idea`, `kotlin-tooling-core`, `kotlin-gradle-plugins-bom`

### Step 2 — Fix two broken publishes

- **`kotlin-stdlib-brs-runtime`**: replace manual `cp` at `rebuild.sh:286` with proper `MavenPublication` on `:kotlin-stdlib:brsBrsJar`. Own artifactId `kotlin-stdlib-brs-runtime`, no classifier.
- **`kotlin-test-brs`**: add `maven-publish` block to `libraries/kotlin.test/brs/build.gradle.kts`. Add step 8 to `rebuild.sh`: `./gradlew :kotlin-test-brs:publishToMavenLocal`.

### Step 3 — kotlin-roku/ and kotlin-roku-intellij/ updates

- `kotlin-roku/build.gradle.kts`: `group = "com.nuvyyo.brightscript"`, `version = "2.2.20-brs.1"`, KGP dep coords → `com.nuvyyo:kotlin-gradle-plugin-brs:2.2.20-brs.1` (+ 3 siblings)
- `kotlin-roku/settings.gradle.kts`: add GitHub Packages repo
- `kotlin-roku-intellij/build.gradle.kts`: group/version flip

### Step 4 — Remote publish: GitHub Packages

- **Repo:** `nuvyyo/maven-brs` (private)
- **Auth:** PAT (classic). `read:packages` for consumers; `write:packages` + `repo` for release dev. Stored in `~/.gradle/gradle.properties` as `nuvyyoGitHubUser` / `nuvyyoGitHubToken`.
- **New convention plugin:** `repo/gradle-build-conventions/.../nuvyyo-publishing.gradle.kts` — registers `nuvyyo` Maven repo on every `publishing {}` block.
- **New `release.sh`:** clean tree check → `rebuild.sh` → `publishAllPublicationsToNuvyyoRepository` → `git tag v2.2.20-brs.N && git push --tags`

### Step 5 — brightscript-studio/ rebrand

**Visible-rebrand edits** in `build/src/org/jetbrains/intellij/build/IdeaCommunityProperties.kt`:

| Field | New value |
|---|---|
| `baseFileName` (L38) | `"brightscript-studio"` |
| `platformPrefix` (L41) | `"BrightScriptStudio"` |
| Windows full name (L144, L146) | `"BrightScript Studio"` |
| Linux root dir (L162) | `"brightscript-studio-$buildNumber"` |
| `urlSchemes` (L174) | `listOf("brightscriptstudio")` |
| `bundleIdentifier` (L177) | `"com.nuvyyo.brightscriptstudio"` |
| Mac app name (L182-183) | `"BrightScript Studio.app"` |
| System selector (L192) | `"BrightScriptStudio${appInfo.majorVersion}.${appInfo.minorVersionMainPart}"` |
| Base artifact name (L194) | `"brightscript-studio-$buildNumber"` |
| Output dir (L196) | `"brightscript-studio"` |

Plus `ApplicationInfo.xml`: product code `IC` → `BSS`, product name → `BrightScript Studio`. Icons: owner-supplied; v1 ships with IDEA placeholder icons.

**Compiler-resolution edits** (so JPS resolves `com.nuvyyo:*`):

1. `plugins/kotlin/base/plugin/src/.../KotlinArtifacts.kt:14` — `KOTLIN_MAVEN_GROUP_ID = "com.nuvyyo"`
2. All 40 `.idea/libraries/kotlinc_*.xml` files — `sed` rewrite: `maven-id="org.jetbrains.kotlin:` → `maven-id="com.nuvyyo:` and version string → `2.2.20-brs.1`
3. `lib/BUILD.bazel:3448+` — `http_jar` URL + SHA256 rewrites to GitHub Packages
4. `plugins/kotlin/gradle/gradle/src/.../KotlinGradleConstants.kt` — `GROUP_ID` and `STDLIB_GROUP_ID` → `"com.nuvyyo"` (makes IDE recognize user `com.nuvyyo:*` deps as Kotlin)
5. `kotlinc/build.txt` → `2.2.20-brs.1`

**Do NOT touch:** `plugins/kotlin/plugin/resources/META-INF/plugin.xml` (plugin ID `org.jetbrains.kotlin` stays)

### Step 6 — DMG: GitHub Releases

```bash
gh release create v2.2.20-brs.1 \
  out/brightscript-studio/artifacts/BrightScriptStudio-2.2.20-brs.1.dmg \
  --repo nuvyyo/brightscript-studio --title "BrightScript Studio 2.2.20-brs.1"
```

README documents Gatekeeper right-click-Open workaround (no code-signing in v1).

### Step 7 — bump-version.sh helper

Single script bumping all five version locations in lockstep:
1. `Kotlin/gradle.properties`
2. `kotlin-roku/build.gradle.kts`
3. `kotlin-roku-intellij/build.gradle.kts`
4. `brightscript-studio/.../ApplicationInfo.xml`
5. `brightscript-studio/kotlinc/build.txt` + `.idea/libraries/kotlinc_*.xml`

## Verification

| Stage | What to run | Pass criterion |
|---|---|---|
| 1 | `./rebuild.sh` from `Kotlin/` | `find ~/.m2/repository/com/nuvyyo` shows all 9 artifacts at `2.2.20-brs.1`; golden-file tests green |
| 2 | `cd ../kotlin-roku && ./rebuild.sh` | POM transitive deps reference `com.nuvyyo:*` only |
| 3 | `cd ../roku-test-app && ./rebuild-all.sh --all` + on-device tests | `run-device-tests.sh` passes; stdlib tests 409/409 |
| 4 | `release.sh` against `nuvyyo/maven-brs-test`; fresh-clone build on clean machine | Builds from GitHub Packages with no `mavenLocal()` |
| 5 | `installers.cmd` from `brightscript-studio/`; mount DMG | "BrightScript Studio" branding; opens `roku-test-app/` and JPS build succeeds |
| 6 | Teammate follows README cold | No undocumented steps |

## v1 Non-Goals

- Apple code-signing / notarization (defer to v2)
- OTA updates (defer to v2)
- Windows / Linux installers
- CI/CD
- Real BrightScript Studio icons
- Internal Artifactory migration
