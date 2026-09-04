# Single BRS compilation + Android-shaped Gradle scripts — design

Date: 2026-09-04. Status: approved (Mike, 2026-09-04) — decisions 1–4 below.

## Problem

kotlin-roku compiles a Roku project as TWO Kotlin compilations: `brsMain`
(`src/brsMain/kotlin`) and a plugin-created `components` compilation whose
source set is the project's `components/` directory. Three costs, all traced
on 2026-09-04:

1. **brsMain declarations are unresolvable from `components/`** (and from
   `brsTest`). The plugin shares main with components via
   `componentsCompilation.associateWith(main)` and
   `libraries.from(build/brs/brs/main/source)`; both hand the compiler
   DIRECTORIES of `.brs` output, and `-libraries` loads only real klibs
   (`K2BrsCompiler.loadLibraries` → `KlibLoader`, which silently records a
   non-klib path as problematic; the CLI never inspects that list). Verified
   by direct kotlinc-brs runs: "resolved 2 library/libraries from 4 path(s)"
   plus `unresolved reference`; a `-Xproduce=library` klib of brsMain resolves
   the same import and its manifest carries `brs_file_dependencies`.
2. **The `components/` dir is marked TEST in the IDE** (green). The IDE
   tooling model's non-legacy rule is literally
   `isTestCompilation = associateCompilations.isNotEmpty()`
   (`KotlinCompilationBuilder.kt:54-58`); a source set whose only compilation
   is test becomes test (`KotlinMPPGradleModelBuilder.kt:144`).
3. **roku-test-app carries a staging workaround** because plain `.kt` files
   under `components/` compile into `build/brs/brs/main/components/source/`,
   which packaging never ships.

There is no compiler reason for the split: `BrsCompiler.writeOutput` routes
files per DETECTED component class (`components/<Name>/` vs `source/`) with no
module-name gate, and include-closure resolution is per module — the split IS
the reason main symbols are invisible. The split was introduced in kotlin-roku
commit `7e42830` ("WIP", 2026-01-13) with no recorded rationale.

Separately, the app's `build.gradle.kts` carries two dependency-substitution
blocks (buildscript + project) because the fork publishes its Gradle-plugin
artifacts with transitive dependencies mis-grouped as
`com.nuvyyo:<module>:2.2.20-brs.1` for modules the fork never publishes; the
kotlin-roku plugin's own build repeats the hack.

## Goals

- One compilation per source set: components are ordinary brsMain classes.
- The app's module script is Android-shaped: `plugins {}` + `roku {}` only.
- No dependency-substitution hacks anywhere (fixed at the publishing source).
- Gates unchanged: goldens 100, FIR 232, stdlib 610/62, E2E 104/10,
  `validateComponentIncludes` + `validateTestComponentIncludes` strict, 0
  findings.

## Non-goals (recorded backlog)

- A `dependencies { implementation(...) }` block (decision 4b — DEFERRED).
  KMP's `brsMainImplementation` already resolves klibs at compile time, but
  nothing stages a third-party klib's runtime `.brs` into the package; a real
  block needs runtime staging via Gradle variants + IDE surfacing. Own program.
- Root/module (`:app`) split — single-module today; the module-script shape
  already matches Android's, an `:app` split later needs no plugin change.
- Output file-name collision guard: all plain files now share one `source/`
  and `<FileName>Kt.brs` naming is by FILE name, so two `.kt` files with the
  same name in different packages would collide silently (pre-existing within
  a compilation; now spans main + components). Nothing collides today.
- `K2BrsCompiler` warning on problematic `-libraries` entries (DX; would have
  surfaced cost 1 immediately).

## Decisions (approved)

1. **Package scheme (app):** flatten to `com.nuvyyo.roku.components` for the
   real components and `com.nuvyyo.roku.components.fixtures` for E2E
   fixtures; drop the one-package-per-file scheme; fix the two stray files
   declared `package ShelfView` / `package TestLayout`.
2. **`roku.componentsDir`:** KEEP, redefined as the OPTIONAL hand-written
   SceneGraph XML directory (same `components/` default); tolerate absence.
3. **kotlin-roku's uncommitted `RokuPlugin.kt` diff** (flow-klib IDE
   resolver, +72/−52): commit FIRST as its own commit.
4. **Android-shaped scripts:** fix coordinates at publish time in the fork;
   nest `test {}` / `validation {}` under `roku {}`; pin the plugin version in
   `settings.gradle.kts` `pluginManagement.plugins`; defer `dependencies {}`.

## Stage 1 — remove the split

### kotlin-roku (`RokuPlugin.kt` + tasks)

- Delete: `brsTarget.compilations.create("components")`, the `brsComponents`
  source set wiring, `associateWith(main)`, the `compileComponentsKotlinBrs`
  configure block (incl. `libraries.from(.../main/source)` and the
  `outputDirectory` override), every `dependsOn("compileComponentsKotlinBrs")`,
  and the test task's `libraries.from(.../main/source)` no-op.
- **Layout stubs:** `generateLayoutStubs.sourceFiles` = brsMain's source dirs
  MINUS anything under the build dir (lazy provider; the anti-cycle technique
  the current code already uses); output `build/generated/layout-stubs` is
  added as a brsMain `srcDir` at apply time; `compileKotlinBrs` dependsOn
  `generateLayoutStubs`.
- **Test typing:** `CompileComponentsKlibTask` → generic `CompileKlibTask`
  (serialize a set of source roots to a klib); register `compileMainKlibBrs`
  over brsMain's `kotlin.sourceDirectories` (stubs included; dependsOn
  `generateLayoutStubs`) → `build/brs/klib/main.klib`; `compileTestKotlinBrs`
  gets `main.klib` (replaces `components.klib`). This also closes the
  brsMain-invisible-to-tests hole.
- **Flat output layout.** One compilation, `outputDirectory =
  build/brs/brs/main` (KGP default), the compiler writes `source/` and
  `components/<Name>/{<Name>.xml,<Name>.deps.json,<Name>Kt.brs,
  <Name>_LayoutKt.brs}`. Retarget: `compiledComponentsDirProvider` and
  `compilerGeneratedXmlDir` both = `build/brs/brs/main/components`;
  `PackageRokuTask`: `compilerXmlDir = File(componentsDir, "components")` →
  `componentsDir`; `ValidateComponentIncludesTask`:
  `compiledDir.resolve("components")` → `compiledDir`. `MergeBrsOutputTask`,
  `CopyKotlinToBsTask`, `StageRokuTestSourceTask` need no code change
  (`stageRokuTestSource` now naturally stages component helper files — the
  app workaround becomes redundant by construction).
- **`ProcessComponentXmlTask.sourceXmlDir`:** `@InputDirectory` →
  `@InputFiles` on the DirectoryProperty (missing dir = empty, the
  ValidateComponentIncludesTask precedent) + `isDirectory` guard in the
  action. `RokuExtension.componentsDir` KDoc: optional hand-written XML dir.
- **Plugin tests:** add flat-layout cases to `PackageRokuTaskTest` (compiler
  XML at `<compiledComponents>/<Name>/<Name>.xml`, brs alongside → zip
  `components/<Name>/`) and `ValidateComponentIncludesTaskTest`
  (compiler-generated XML discovered at the flat path).

### roku-test-app

- `git mv components/**/*.kt` → `src/brsMain/kotlin/com/nuvyyo/roku/components/`
  (real components) and `.../components/fixtures/` (fixtures); rewrite
  `package` lines per decision 1; rewrite the ~45 affected imports in
  components + `src/brsTest` (enumerated 2026-09-04: all
  `com.nuvyyo.roku.components.<sub>.<X>` → `com.nuvyyo.roku.components.X` or
  `.fixtures.X`; `import ShelfView.UrlTransferTask` dropped — same package).
- Delete `components/`; delete the `componentsSharedSource` block and its two
  `tasks.withType` consumers in `build.gradle.kts`.

### Kotlin repo docs

- `CLAUDE.md`: every `../roku-test-app/components/...` path → the new
  `src/brsMain/kotlin/com/nuvyyo/roku/components/...` path; the "PLAIN .kt
  under components/" gotcha paragraph and `compileComponentsKotlinBrs`
  mentions go; the E2E "what actually runs" + test-locations rows update.
  Historical plan/research docs are left as written.

## Stage 2 — Android-shaped scripts

### Kotlin fork: publish-time coordinate fix

`repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/nuvyyo-publishing.gradle.kts`
is applied by exactly the seven published BRS modules (KGP, KGP-api, KGP-idea,
tooling-core, plugins-BOM, cli-brs, stdlib brs-prebuilt). Add there:

- `publications.withType<MavenPublication>().configureEach { pom.withXml { } }`
  rewriting every `<dependency>` / `<dependencyManagement>` entry with
  `groupId == com.nuvyyo` whose `artifactId` ends in neither `-brs` nor
  `-brs-runtime` to `groupId = org.jetbrains.kotlin`, `version = <upstream>`.
- `tasks.withType<GenerateModuleMetadata>().configureEach { doLast { } }`
  rewriting the same entries in the generated `.module` JSON
  (`variants[].dependencies[]` `group` / `version.requires`, plus
  `dependencyConstraints[]` where present).
- `<upstream>` = `project.version.toString().substringBefore("-brs")`
  (`2.2.20`) — one derivation, no second constant.

Then delete the substitution block from `kotlin-roku/build.gradle.kts` and
both blocks from `roku-test-app/build.gradle.kts`.

Verification: the 2026-09-04 POM scan (python over `~/.m2/.../com/nuvyyo/**/
*2.2.20-brs.1.pom` and `.module`) reports ZERO mis-grouped entries; kotlin-roku
`publishToMavenLocal` and the app build both succeed with the substitution
blocks gone. `.module` rewrite must keep the WithFixedAttribute → gradle813
redirect (`redirectFixedAttributeVariantsToGradle813`) intact — verify with the
memory-note python check.

### kotlin-roku: extension nesting + version pin

- `RokuExtension` gains nested `test: RokuTestExtension` and
  `validation: RokuValidationExtension` (`extensions.create` on the
  `roku` extension object — Gradle nested-extension idiom, so
  `roku { test { } validation { } }` works in Kotlin DSL). The top-level
  `rokuTest` / `rokuValidation` extensions are REMOVED (single consumer).
- roku-test-app `settings.gradle.kts`: `pluginManagement { plugins {
  id("com.nuvyyo.brightscript.kotlin-roku") version "2.2.20-brs.1" } }`;
  module `plugins { id("com.nuvyyo.brightscript.kotlin-roku") }`.

### Target app script

```kotlin
plugins {
    id("com.nuvyyo.brightscript.kotlin-roku")
}

roku {
    appName.set("RokuTestApp")
    appVersion.set("1.0.0")
    minRokuOS.set("10.0")
    test { timeout.set(300_000L) }
    validation { includeMode.set("strict") }
}
```

## Sequencing and commits

kotlin-roku: (1) commit the pending IDE-resolver diff; (2) Stage 1 merge;
(3) Stage 2 nesting. roku-test-app: (1) move + flatten + workaround removal
(with plugin (2)); (2) script cleanup (with fork fix + plugin (3)).
Kotlin: (1) `build:` publishing coordinate fix (+ `./rebuild.sh`); (2)
`docs:` CLAUDE.md paths. The fork fix can run in the background while Stage 1
plugin work proceeds (poll the build log in bounded loops — long-build rule).

## Verification (whole program)

1. kotlin-roku unit tests green (`./gradlew test`).
2. `cd roku-test-app && ./rebuild-all.sh --plugin`, then `./gradlew build`
   (packageRoku + validateComponentIncludes strict = 0 findings) and
   `./gradlew packageRokuTests` (validateTestComponentIncludes = 0).
3. Original repro: a components file importing `com.nuvyyo.roku.MessageHandler`
   compiles (temporary probe, not kept).
4. E2E: `./run-device-tests.sh` — 104 active / 10 suites green (device on LAN).
5. IDE resync: `components` no longer exists; brsMain not green (Mike).
6. Stage 2: POM/.module scan zero; both builds green without substitution;
   `./gradlew rokuTest` still honors `roku.test.timeout`.

## Risks

- **File-name collisions in one `source/`** (backlog above). Pre-move audit:
  brsMain `{Main, MessageHandler, Util}.kt` vs the 33 component files — no
  duplicates.
- **Gradle implicit-dependency validation** on the new stub/klib wiring:
  every consumer of `build/generated/layout-stubs` declares `dependsOn
  generateLayoutStubs`; the stub task never reads the build dir.
- **`.module` JSON rewrite** is a post-processing step over a Gradle-owned
  file; keep it a pure group/version substitution and re-run the
  fixed-attribute check after `./rebuild.sh`.
- **Hand-written XML**: no consumer has any today (roku-test-app: none;
  tablo-fast-roku-kotlin uses a different, older plugin id); decision 2 keeps
  the path alive but untested on a real XML — the existing
  `ProcessComponentXmlTask` code path is unchanged.
