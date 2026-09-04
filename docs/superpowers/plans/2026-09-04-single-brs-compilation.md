# Single BRS Compilation + Android-Shaped Scripts — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove kotlin-roku's separate `components` compilation so SceneGraph components are ordinary `brsMain` classes, then reduce a Roku app's Gradle scripts to `plugins {}` + `roku {}` by fixing the fork's published coordinates at the source.

**Architecture:** One KMP compilation per source set (`brsMain`, `brsTest`); the BRS compiler already routes component output to `components/<Name>/` per detected class, so the plugin only retargets its stub-generation, klib-serialization, XML-processing, validation, and packaging tasks to the flat `build/brs/brs/main/{source,components}` layout. Stage 2 rewrites mis-grouped dependency coordinates in the fork's published POM/`.module` files (one shared publishing convention) and nests the secondary Gradle extensions under `roku {}`.

**Tech Stack:** Gradle 8.14 Kotlin DSL plugin (kotlin-roku), the Kotlin fork's KGP BRS target (`KotlinBrsCompile`), kotlinc-brs fat jar, JUnit 4 + `ProjectBuilder` task tests, Roku device E2E via `run-device-tests.sh`.

**Spec:** `docs/superpowers/specs/2026-09-04-single-brs-compilation-design.md` (Kotlin repo).

## Global Constraints

- Three repos, sibling directories: `~/Documents/newt/git/Kotlin` (fork; branch `feature/brightscript-backend-2.2.20`), `~/Documents/newt/git/kotlin-roku` (plugin), `~/Documents/newt/git/roku-test-app` (app). Commit on the current branch of each; run `git branch --show-current` before the first commit in each repo and record it.
- Gates must not drop: goldens 100, FIR diagnostics 232, stdlib device suite 610 tests / 62 suites, rokuTest E2E 104 active tests / 10 suites, `validateComponentIncludes` + `validateTestComponentIncludes` strict mode with 0 findings.
- Kotlin repo build-convention or compiler changes are published ONLY via `./rebuild.sh` (Task 8). Never run individual `./gradlew publish*` tasks there.
- The plugin is rebuilt/published ONLY via `cd roku-test-app && ./rebuild-all.sh --plugin` (runs `kotlin-roku ./gradlew clean publishToMavenLocal --no-daemon`). Plugin unit tests: `cd kotlin-roku && ./gradlew test --no-daemon`.
- Device tests: ALWAYS `cd roku-test-app && ./run-device-tests.sh`, never `./gradlew rokuTest` directly. If the runner aborts with "Console connection is already in use", the IDE holds the telnet console — stop and ask Mike; do not kill the IDE.
- Long builds (`./rebuild.sh` is 20–40 min): run in the background and poll the log in bounded foreground loops; never conclude the build hung from a missing notification.
- Subagents: `model: "fable"` only.
- Every commit message ends with `Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>`. Prefixes: kotlin-roku `plugin:`, roku-test-app `app:`, Kotlin `build:` / `docs:`.
- Do not touch the compiler. If a task appears to need a compiler change, stop and report.

---

## Stage 0 — clear the decks

### Task 1: Commit the pending kotlin-roku IDE-resolver diff

**Files:**
- Modify (already modified in the working tree): `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt`

**Interfaces:**
- Produces: a clean kotlin-roku working tree so the Stage 1 diff is reviewable on its own.

- [ ] **Step 1: Confirm the diff is exactly the flow-klib IDE resolver**

Run: `cd ~/Documents/newt/git/kotlin-roku && git status --short && git diff --stat`
Expected: only `RokuPlugin.kt` modified (+72/−52); `git diff` shows `registerIdeImport` moved after the flow configuration and `BrsStdlibIdeDependencyResolver` → `BrsKlibIdeDependencyResolver` with stdlib + flow entries.

- [ ] **Step 2: Verify it compiles and tests pass**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
cd ~/Documents/newt/git/kotlin-roku
git add src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt
git commit -m "plugin: surface the flow klib to the IDE import alongside the stdlib

registerIdeImport now runs after the kotlinBrsFlow configuration exists and
resolves both default klibs (BrsKlibIdeDependencyResolver), matching the set
the compile tasks receive via libraries.from(...).

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

## Stage 1 — remove the split

### Task 2: Generalize the components klib task into `CompileKlibTask`

**Files:**
- Rename: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/CompileComponentsKlibTask.kt` → `CompileKlibTask.kt`
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt` (import at line 4; registration at ~line 322)

**Interfaces:**
- Produces: `abstract class CompileKlibTask : DefaultTask()` with the SAME properties as before — `sourceFiles: ConfigurableFileCollection`, `compilerClasspath: ConfigurableFileCollection`, `libraries: ConfigurableFileCollection`, `moduleName: Property<String>`, `outputKlib: RegularFileProperty` — and `@TaskAction fun compile()`. Task 4 registers `compileMainKlibBrs` with this class.

- [ ] **Step 1: Rename the file and class**

```bash
cd ~/Documents/newt/git/kotlin-roku
git mv src/main/kotlin/com/example/roku/gradle/tasks/CompileComponentsKlibTask.kt \
       src/main/kotlin/com/example/roku/gradle/tasks/CompileKlibTask.kt
sed -i '' 's/CompileComponentsKlibTask/CompileKlibTask/g' \
    src/main/kotlin/com/example/roku/gradle/tasks/CompileKlibTask.kt \
    src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt
```

- [ ] **Step 2: Replace the class KDoc**

In `CompileKlibTask.kt`, replace the KDoc block that starts `/** * Serializes the components compilation's Kotlin sources into a klib` (ends just above `@CacheableTask`) with:

```kotlin
/**
 * Serializes a set of Kotlin source roots into a klib so OTHER compilations can
 * reference their declarations with static types. The plugin uses it to give the
 * brsTest driver the whole of brsMain — components included — so tests can hold
 * typed component handles (createComponent<T>() plus @SG*Field property access)
 * and call main-source classes.
 *
 * compileKotlinBrs produces .brs + component XML for packaging but no compile-time
 * metadata, and the BRS compiler's -libraries flag only loads real klibs
 * (KlibLoader). This task runs the same compiler over the same sources with
 * -Xproduce=library to emit that metadata. The klib never ships in a package: it
 * is a compile-time artifact only.
 */
```

Also update the `sourceFiles` KDoc from `/** Source roots (the components dir + generated layout stubs). */` to `/** Source roots to serialize (checked-in source dirs + generated layout stubs). */`.

- [ ] **Step 3: Compile**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew compileKotlin --no-daemon -q`
Expected: BUILD SUCCESSFUL (the registration in RokuPlugin still uses the old task NAME `compileComponentsKlibBrs`; Task 4 rewires it).

- [ ] **Step 4: Commit**

```bash
cd ~/Documents/newt/git/kotlin-roku
git add -A src/main/kotlin/com/example/roku/gradle/tasks/CompileKlibTask.kt src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt
git commit -m "plugin: generalize CompileComponentsKlibTask into CompileKlibTask

Same task, no longer tied to the components dir: it serializes any source
roots to a klib. Prepares the single-compilation layout where brsTest reads
one main.klib.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 3: Teach packaging and validation the flat compiled layout

**Files:**
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/PackageRokuTask.kt` (the `compilerXmlDir` line inside `packageApp`, ~line 143)
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/ValidateComponentIncludesTask.kt` (~line 106)
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/ProcessComponentXmlTask.kt` (`sourceXmlDir` annotations, ~line 95)
- Test: `kotlin-roku/src/test/kotlin/com/example/roku/gradle/tasks/PackageRokuTaskTest.kt`
- Test: `kotlin-roku/src/test/kotlin/com/example/roku/gradle/tasks/ValidateComponentIncludesTaskTest.kt`

**Interfaces:**
- Consumes: nothing new.
- Produces: with one compilation the compiler writes `<compiledComponents>/<Name>/<Name>.xml`, `<Name>.deps.json`, `<Name>Kt.brs` (no extra `components/` level). Packaging zips them to `components/<Name>/…`; validation discovers compiler-generated XML at that flat path. Task 4 points both tasks at `build/brs/brs/main/components`.

- [ ] **Step 1: Write the failing packaging test**

Append inside `class PackageRokuTaskTest` (after the last existing test):

```kotlin
    @Test
    fun `flat compiled layout packages compiler xml and brs alongside under components`() {
        // One compilation: the compiler writes <compiledComponents>/<Name>/<Name>.xml + <Name>Kt.brs
        write("compiledComponents/Widget/Widget.xml", "<component name=\"Widget\" extends=\"Group\"/>\n")
        write("compiledComponents/Widget/WidgetKt.brs", "sub Widget_init()\nend sub\n")
        write("compiledComponents/Widget/Widget_LayoutKt.brs", "function Widget_Layout()\nend function\n")
        val task = makeTask()
        task.compiledComponents.set(File(tmp.root, "compiledComponents"))
        task.processedXmlDir.set(File(tmp.root, "processed-empty").also { it.mkdirs() })

        task.packageApp()

        ZipFile(File(tmp.root, "out/app.zip")).use { zip ->
            assertNotNull("compiler-generated XML must ship", zip.getEntry("components/Widget/Widget.xml"))
            assertNotNull("component brs sits beside its XML", zip.getEntry("components/Widget/WidgetKt.brs"))
            assertNotNull("layout brs sits beside its XML", zip.getEntry("components/Widget/Widget_LayoutKt.brs"))
            assertNull("no stray root-level copy", zip.getEntry("components/WidgetKt.brs"))
        }
    }
```

Add `import org.junit.Assert.assertNull` to the test file's imports.

- [ ] **Step 2: Run it to verify it fails**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q --tests '*PackageRokuTaskTest*'`
Expected: FAIL — `compiler-generated XML must ship` (the task looks for XML under `compiledComponents/components/`).

- [ ] **Step 3: Fix PackageRokuTask**

In `PackageRokuTask.packageApp()`, replace:

```kotlin
                // Add compiler-generated XMLs (from components/components/ subdirectory)
                // These are for @Component classes that don't have user-authored XML files
                val compilerXmlDir = File(componentsDir, "components")
```

with:

```kotlin
                // Add compiler-generated XMLs. One compilation writes them directly as
                // <compiledComponents>/<Name>/<Name>.xml (flat layout). These are for
                // component classes that don't have user-authored XML files.
                val compilerXmlDir = componentsDir
```

- [ ] **Step 4: Run the packaging tests**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q --tests '*PackageRokuTaskTest*'`
Expected: PASS (all cases, including the pre-existing duplicate-entry guards).

- [ ] **Step 5: Write the failing validation test**

Append inside `class ValidateComponentIncludesTaskTest`:

```kotlin
    @Test
    fun `compiler-generated xml at the flat compiled path is discovered`() {
        // One compilation: compiler XML lives at <compiled>/<Name>/<Name>.xml, no extra
        // components/ level. A clean flat package must pass strict mode (not "zero components").
        write("staged/FillerKt.brs", fillerDefinitions())
        write("compiled/Widget/WidgetKt.brs", "sub Widget_init()\nend sub\n")
        write(
            "compiled/Widget/Widget.xml",
            "<component name=\"Widget\" extends=\"Group\">\n" +
                "  <script type=\"text/brightscript\" uri=\"pkg:/components/Widget/WidgetKt.brs\" />\n" +
                "</component>\n"
        )
        write("processed/.keep", "")
        val project = ProjectBuilder.builder().withProjectDir(tmp.root).build()
        val task = project.tasks
            .register("validateComponentIncludesFlat", ValidateComponentIncludesTask::class.java)
            .get()
        task.processedXmlDir.set(File(tmp.root, "processed"))
        task.compiledComponentsDir.set(File(tmp.root, "compiled"))
        task.stagedSourceDir.set(File(tmp.root, "staged"))
        task.mode.set("strict")
        task.extraBuiltins.set(emptySet())
        task.reportFile.set(File(tmp.root, "report.txt"))

        task.validate()

        assertTrue(File(tmp.root, "report.txt").readText().contains("0 finding(s)"))
    }
```

- [ ] **Step 6: Run it to verify it fails**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q --tests '*ValidateComponentIncludesTaskTest*'`
Expected: FAIL — GradleException from the zero-components strict guard (the task only looks under `compiled/components/`).

- [ ] **Step 7: Fix ValidateComponentIncludesTask**

Replace:

```kotlin
        // Packaged component XMLs: processed user XMLs win; compiler-generated XMLs
        // (components/<Name>/<Name>.xml) fill in the rest — mirrors PackageRokuTask.
```
…
```kotlin
        compiledDir?.resolve("components")?.takeIf { it.isDirectory }?.walkTopDown()
```

with:

```kotlin
        // Packaged component XMLs: processed user XMLs win; compiler-generated XMLs
        // (<compiled>/<Name>/<Name>.xml — the flat single-compilation layout) fill in
        // the rest — mirrors PackageRokuTask.
```
…
```kotlin
        compiledDir?.walkTopDown()
```

- [ ] **Step 8: Run the validation tests**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q --tests '*ValidateComponentIncludesTaskTest*'`
Expected: PASS (the pre-existing cases write XML under `compiled/components/Widget/`, which the recursive walk still finds).

- [ ] **Step 9: Make the hand-written XML dir optional**

In `ProcessComponentXmlTask.kt`, replace:

```kotlin
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceXmlDir: DirectoryProperty
```

with:

```kotlin
    /**
     * Hand-written SceneGraph component XML (roku.componentsDir). OPTIONAL: most
     * projects have none — components are Kotlin classes whose XML the compiler
     * generates. Declared as @InputFiles so a missing directory is an empty input,
     * not a validation failure (ValidateComponentIncludesTask precedent).
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceXmlDir: DirectoryProperty
```

Add `import org.gradle.api.tasks.InputFiles` if not already imported (it is — `stdlibBrsFiles` uses it; verify with `grep -n 'import org.gradle.api.tasks.InputFiles' ProcessComponentXmlTask.kt`).

Verification of this change is Task 5 Step 6 (the app builds with no `components/` directory); a unit test cannot exercise Gradle's input validation.

- [ ] **Step 10: Full plugin test run, then commit**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q`
Expected: BUILD SUCCESSFUL.

```bash
cd ~/Documents/newt/git/kotlin-roku
git add src/main/kotlin/com/example/roku/gradle/tasks/PackageRokuTask.kt \
        src/main/kotlin/com/example/roku/gradle/tasks/ValidateComponentIncludesTask.kt \
        src/main/kotlin/com/example/roku/gradle/tasks/ProcessComponentXmlTask.kt \
        src/test/kotlin/com/example/roku/gradle/tasks/PackageRokuTaskTest.kt \
        src/test/kotlin/com/example/roku/gradle/tasks/ValidateComponentIncludesTaskTest.kt
git commit -m "plugin: package and validate the flat single-compilation components layout

Compiler XML now lives at <compiledComponents>/<Name>/<Name>.xml with no
extra components/ level; PackageRokuTask and ValidateComponentIncludesTask
read it there. The hand-written XML dir becomes an optional input.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 4: Remove the components compilation from RokuPlugin

**Files:**
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt` (apply(): lines ~70–83 and ~185–211; registerTasks(): lines ~296–341, ~343–360, and every `dependsOn("compileComponentsKotlinBrs")`; the hybrid KDoc graph ~line 628)
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuExtension.kt` (`componentsDir` KDoc)

**Interfaces:**
- Consumes: `CompileKlibTask` (Task 2); flat-layout tasks (Task 3).
- Produces: task graph `generateLayoutStubs → compileKotlinBrs → {processComponentXml, compileMainKlibBrs → compileTestKotlinBrs → stageRokuTestSource}`; `build/brs/klib/main.klib`; compiled components at `build/brs/brs/main/components/<Name>/`.

- [ ] **Step 1: Replace the components compilation with a brsMain stub source dir**

In `apply()`, replace:

```kotlin
        // Create the components compilation for SceneGraph component Kotlin files
        val brsTarget = kotlinExt.targets.getByName("brs") as KotlinBrsIrTarget
        val componentsCompilation = brsTarget.compilations.create("components")

        // Configure brsComponents source set to use components/ directory
        val brsComponentsSourceSet = componentsCompilation.defaultSourceSet
        brsComponentsSourceSet.kotlin.srcDir(rokuExtension.componentsDir)

        // Share symbols (including internal) between brsComponents and brsMain via the
        // canonical KMP associateWith mechanism. dependsOn into the main compilation's default
        // source set triggers KotlinSourceSetDependsOnDefaultCompilationSourceSet and
        // KotlinDefaultHierarchyFallbackDependsOnUsageDetected warnings.
        componentsCompilation.associateWith(brsTarget.compilations.getByName("main"))
```

with:

```kotlin
        // ONE compilation per source set. SceneGraph components are ordinary brsMain
        // classes: the compiler routes each detected component's output to
        // components/<Name>/ and everything else to source/, so no second compilation
        // is needed — and a second one would make brsMain symbols unresolvable from
        // component code (-libraries loads klibs, not .brs output dirs) and mark its
        // source set as TEST in the IDE (associateWith => isTestCompilation).
        //
        // Generated @SGLayout accessor stubs are part of brsMain; the generating task is
        // registered in registerTasks and compileKotlinBrs depends on it there.
        kotlinExt.sourceSets.named("brsMain") {
            kotlin.srcDir(project.layout.buildDirectory.dir("generated/layout-stubs"))
        }
```

Delete the now-unused import `import org.jetbrains.kotlin.gradle.targets.brs.KotlinBrsIrTarget` (verify with `grep -n KotlinBrsIrTarget RokuPlugin.kt` → only the import).

- [ ] **Step 2: Rewire the compile-task configuration**

Replace the whole `project.tasks.withType(KotlinBrsCompile::class.java).configureEach { … }` block with:

```kotlin
        // Configure all BRS compile tasks with the compiler JAR and the default klibs
        project.tasks.withType(KotlinBrsCompile::class.java).configureEach {
            compilerJar.fileProvider(project.provider { brsCompilerConfig.singleFile })
            libraries.from(brsStdlibConfig)
            libraries.from(brsFlowConfig)

            // The test compilation references main classes — components included — with
            // static types (createComponent<T>() + @SG*Field property access) through the
            // main klib. The .brs output dirs carry no compile-time metadata (-libraries
            // only loads real klibs), so main.klib is the only route. compileKotlinBrs is
            // an explicit dependency because stageRokuTestSource consumes its output too.
            if (name == "compileTestKotlinBrs") {
                dependsOn("compileKotlinBrs")
                dependsOn("compileMainKlibBrs")
                libraries.from(project.layout.buildDirectory.file("brs/klib/main.klib"))
            }
        }
```

- [ ] **Step 3: Retarget stub generation and the klib task in registerTasks**

Replace the region from the comment `// Generate Layout stubs for IDE support` through the `project.afterEvaluate { … brsComponentsSourceSet?.kotlin?.srcDir(…) }` block (inclusive) with:

```kotlin
        // brsMain's CHECKED-IN source roots: every srcDir outside the build dir. The stub
        // generator and the klib serializer read these rather than the source set's full
        // sourceDirectories, which include the generated stub dir itself — a task must not
        // read its own output.
        val kotlinExt = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        val buildDir = project.layout.buildDirectory.get().asFile.canonicalFile
        val brsMainCheckedInSources = project.provider {
            kotlinExt.sourceSets.getByName("brsMain").kotlin.srcDirs
                .filter { !it.canonicalFile.startsWith(buildDir) }
        }
        val layoutStubsDir = project.layout.buildDirectory.dir("generated/layout-stubs")

        // Generate @SGLayout accessor stubs (<ClassName>_Layout) for IDE support and
        // compilation. Output is a brsMain srcDir (added in apply()).
        val generateLayoutStubsTask = project.tasks.register("generateLayoutStubs", GenerateLayoutStubsTask::class.java).apply {
            configure {
                sourceFiles.from(brsMainCheckedInSources)
                stubOutputDir.set(layoutStubsDir)
            }
        }
        // brsMain sources include the stub dir, so compilation must wait for generation.
        project.tasks.named("compileKotlinBrs") { dependsOn(generateLayoutStubsTask) }

        // Serialize brsMain (components included) to a klib so the brsTest driver can
        // reference main classes with static types. Compile-time artifact only.
        project.tasks.register("compileMainKlibBrs", CompileKlibTask::class.java).configure {
            group = "brightscript"
            description = "Serializes brsMain to a klib for typed test-driver references"
            dependsOn(generateLayoutStubsTask)
            sourceFiles.from(brsMainCheckedInSources)
            sourceFiles.from(layoutStubsDir)
            compilerClasspath.from(brsCompilerConfig)
            libraries.from(brsStdlibConfig)
            libraries.from(brsFlowConfig)
            moduleName.set("main")
            outputKlib.set(project.layout.buildDirectory.file("brs/klib/main.klib"))
        }
```

- [ ] **Step 4: Point the XML/validation/package tasks at the flat layout**

Replace:

```kotlin
        // Compiled components directory
        val compiledComponentsDirProvider = project.layout.buildDirectory.dir("brs/brs/main/components")
```

with:

```kotlin
        // Compiled components directory: the single compilation writes
        // components/<Name>/{<Name>.xml, <Name>.deps.json, <Name>Kt.brs} here (flat layout)
        val compiledComponentsDirProvider = project.layout.buildDirectory.dir("brs/brs/main/components")
```

In the `processComponentXml` configure block replace:

```kotlin
                // Compiler-generated XML with interface sections (for SceneGraph fields)
                compilerGeneratedXmlDir.set(project.layout.buildDirectory.dir("brs/brs/main/components/components"))
```

with:

```kotlin
                // Compiler-generated XML with interface sections (for SceneGraph fields)
                compilerGeneratedXmlDir.set(compiledComponentsDirProvider)
```

- [ ] **Step 5: Delete every `dependsOn("compileComponentsKotlinBrs")`**

Run: `cd ~/Documents/newt/git/kotlin-roku && grep -n 'compileComponentsKotlinBrs\|compileComponentsKlibBrs\|brsComponents\|componentsCompilation' src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt`

Delete each remaining `dependsOn("compileComponentsKotlinBrs")` line (processComponentXml, validateComponentIncludes, packageRoku, validateTestComponentIncludes, packageRokuTests, copyKotlinToBrighterScript) and fix the two comments that mention "both compile tasks" / "all compile tasks" to say `compileKotlinBrs`. In the hybrid KDoc graph, delete the two lines `*           ↓` / `*   compileComponentsKotlinBrs` so it reads `compileKotlinBrs → copyKotlinToBrighterScript → …`. Re-run the grep: expected no matches.

- [ ] **Step 6: Redefine `componentsDir`**

In `RokuExtension.kt`, replace `    abstract val componentsDir: DirectoryProperty` with:

```kotlin
    /**
     * OPTIONAL directory of hand-written SceneGraph component XML files (default
     * `components/`). Kotlin components live in `src/brsMain/kotlin` like every other
     * class — the compiler generates their XML. Most projects never create this dir.
     */
    abstract val componentsDir: DirectoryProperty
```

- [ ] **Step 7: Compile, test, publish the plugin**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q`
Expected: BUILD SUCCESSFUL.

Run: `cd ~/Documents/newt/git/roku-test-app && ./rebuild-all.sh --plugin 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL` for the plugin publish. (The app build that this script may run afterwards is EXPECTED to fail at this point — the app still has the `components/` layout and the workaround block referencing `compileComponentsKotlinBrs`. Task 5 fixes the app.)

- [ ] **Step 8: Commit**

```bash
cd ~/Documents/newt/git/kotlin-roku
git add src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt src/main/kotlin/com/example/roku/gradle/RokuExtension.kt
git commit -m "plugin: one compilation per source set — drop the components compilation

SceneGraph components are ordinary brsMain classes; the compiler already
routes their output to components/<Name>/ per detected class. The separate
compilation made brsMain symbols unresolvable from component code (its
associateWith/libraries wiring handed the compiler .brs dirs, which
-libraries silently ignores) and marked components/ as a TEST source set in
the IDE (associateWith => isTestCompilation).

Layout stubs generate from brsMain's checked-in roots into a brsMain srcDir;
compileMainKlibBrs serializes brsMain for the brsTest driver (replacing
components.klib, and giving tests brsMain types too). roku.componentsDir is
now the optional hand-written XML dir.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 5: Move roku-test-app components into brsMain and flatten packages

**Files:**
- Move: `roku-test-app/components/{MainScreen,ShelfView,TestLayout,TestScreen}/*.kt` → `roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/*.kt`
- Move: `roku-test-app/components/fixtures/*.kt` → `roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/*.kt`
- Modify: `roku-test-app/build.gradle.kts` (delete lines 59–80: the `componentsSharedSource` block and its two `tasks.withType` consumers)
- Modify: imports in the moved files and in `roku-test-app/src/brsTest/kotlin/tests/*.kt`
- Delete: `roku-test-app/components/`

**Interfaces:**
- Consumes: the plugin published in Task 4 Step 7.
- Produces: packages `com.nuvyyo.roku.components` (MainScreen, ShelfView, FetchShelfTask, UrlTransferTask, TestLayout, TestScreen, TestScreenVM, TestScreenChildLabel, plus top-level `renderTitle`) and `com.nuvyyo.roku.components.fixtures` (all 27 fixture files). Task 7 documents these paths.

- [ ] **Step 1: Record the branch and confirm a clean tree apart from the known edits**

Run: `cd ~/Documents/newt/git/roku-test-app && git branch --show-current && git status --short`
Expected: only `components/ShelfView/ShelfView.kt` (an added `spawnTask` import + blank line) and `components/TestScreen/TestScreenVM.kt` (Mike's looping-emit demo tweak) modified. These are Mike's in-progress experiments: they move WITH the files verbatim and are included in this task's commit (called out in the message and the final report). Keep a safety copy first:

```bash
git diff components/ > /private/tmp/claude-502/-Users-Mike-Fougere-Documents-newt-git-Kotlin/efa6744b-a19d-4988-82c3-924321242937/scratchpad/mike-wip-components.patch
```

- [ ] **Step 2: Move the files**

```bash
cd ~/Documents/newt/git/roku-test-app
DEST=src/brsMain/kotlin/com/nuvyyo/roku/components
mkdir -p "$DEST/fixtures"
for f in components/MainScreen/*.kt components/ShelfView/*.kt components/TestLayout/*.kt components/TestScreen/*.kt; do
  git mv "$f" "$DEST/$(basename "$f")"
done
for f in components/fixtures/*.kt; do
  git mv "$f" "$DEST/fixtures/$(basename "$f")"
done
find components -type f          # expect only .DS_Store files
rm -rf components
git status --short | head -40    # renames (R) for 35 files
```

- [ ] **Step 3: Rewrite package declarations**

```bash
cd ~/Documents/newt/git/roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components
sed -i '' -E 's/^package (com\.nuvyyo\.roku\.components\.[a-z0-9.]+|ShelfView|TestLayout)$/package com.nuvyyo.roku.components/' *.kt
sed -i '' -E 's/^package com\.nuvyyo\.roku\.components\.fixtures\.[a-z0-9]+$/package com.nuvyyo.roku.components.fixtures/' fixtures/*.kt
grep -h '^package ' *.kt fixtures/*.kt | sort | uniq -c
```
Expected: exactly two lines — `8 package com.nuvyyo.roku.components` and `27 package com.nuvyyo.roku.components.fixtures`.

- [ ] **Step 4: Rewrite imports (components + tests)**

```bash
cd ~/Documents/newt/git/roku-test-app
FILES=$(grep -rl --include='*.kt' -E '^import (com\.nuvyyo\.roku\.components\.|ShelfView\.)' src)
sed -i '' -E \
  -e 's/^import com\.nuvyyo\.roku\.components\.fixtures\.[a-z0-9]+\./import com.nuvyyo.roku.components.fixtures./' \
  -e 's/^import com\.nuvyyo\.roku\.components\.(mainscreen|shelfview\.fetchshelftask|shelfview|testlayout|testscreen\.testscreenchildlabel|testscreen)\./import com.nuvyyo.roku.components./' \
  -e '/^import ShelfView\.UrlTransferTask$/d' \
  $FILES
grep -rhn --include='*.kt' -E '^import (com\.nuvyyo\.roku\.components\.fixtures\.[a-z0-9]+\.[A-Za-z]|com\.nuvyyo\.roku\.components\.(mainscreen|shelfview|testlayout|testscreen)\.|ShelfView\.|TestLayout\.)' src || echo "no stale imports"
```
Expected: `no stale imports`. Same-package imports that survive (e.g. `TestScreen.kt` importing `com.nuvyyo.roku.components.TestScreenVM`) are legal Kotlin; leave them.

- [ ] **Step 5: Audit for simple-name collisions introduced by flattening**

```bash
cd ~/Documents/newt/git/roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components
for d in . fixtures; do
  grep -hoE '^(abstract |open |data |sealed |private |internal )*(class|object|interface|fun|val|var) +[A-Za-z_][A-Za-z0-9_]*' $d/*.kt \
    | awk '{print $NF}' | sort | uniq -d
done
```
Expected: no output, or only names whose every occurrence is `private` (file-scoped, legal). Nested declarations are indented and excluded; a genuine top-level duplicate would also be a Kotlin redeclaration error in Step 6 — this is the early warning.

Also confirm no file-name clash with the existing brsMain files: `ls src/brsMain/kotlin/com/nuvyyo/roku/ src/brsMain/kotlin/com/nuvyyo/common/` vs the moved names — expected disjoint (`Main.kt`, `MessageHandler.kt`, `Util.kt` vs the 35 moved files).

- [ ] **Step 6: Delete the staging workaround and build**

In `roku-test-app/build.gradle.kts`, delete from the comment line `// PLAIN (non-component) .kt files under components/ — shared fixture` through the closing `}` of `tasks.withType<com.example.roku.gradle.tasks.ValidateComponentIncludesTask>().configureEach { … }` (inclusive). The file then ends after the `rokuValidation { … }` block.

Run: `cd ~/Documents/newt/git/roku-test-app && ./gradlew clean build --no-daemon 2>&1 | tail -30`
Expected: BUILD SUCCESSFUL; `validateComponentIncludes` reports 0 findings in strict mode; no `components/` directory needed (proves Task 3 Step 9).

Then: `ls build/brs/brs/main/components/ build/brs/brs/main/source/ | head -40`
Expected: `components/` holds one dir per component (`MainScreen`, `ShelfView`, `FetchShelfTask`, `UrlTransferTask`, `TestLayout`, `TestScreen`, `TestScreenChildLabel`, every fixture component…), each with `<Name>.xml` + `<Name>.deps.json` + `<Name>Kt.brs`; `source/` holds `MainKt.brs`, `MessageHandlerKt.brs`, `UtilKt.brs`, `TestScreenVMKt.brs`, `ScopeVmFixtureKt.brs`, `FlowFixturesKt.brs`, etc. No `components/source/` and no `components/components/`.

- [ ] **Step 7: Build the test package**

Run: `cd ~/Documents/newt/git/roku-test-app && ./gradlew packageRokuTests --no-daemon 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL; `compileMainKlibBrs` ran; `compileTestKotlinBrs` resolved the fixture imports; `validateTestComponentIncludes` 0 findings.

- [ ] **Step 8: Prove the original repro compiles (temporary probe, NOT kept)**

```bash
cd ~/Documents/newt/git/roku-test-app
cat > src/brsMain/kotlin/com/nuvyyo/roku/components/MainVisibilityProbe.kt <<'EOF'
package com.nuvyyo.roku.components

import com.nuvyyo.roku.MessageHandler
import kotlin.brs.roku.RoMessagePort

// Temporary probe: brsMain declarations must resolve from component-side code.
object MainVisibilityProbe {
    fun make(): MessageHandler = MessageHandler(RoMessagePort.create())
}
EOF
./gradlew compileKotlinBrs --no-daemon -q && echo "PROBE COMPILES"
rm src/brsMain/kotlin/com/nuvyyo/roku/components/MainVisibilityProbe.kt
```
Expected: `PROBE COMPILES`.

- [ ] **Step 9: Commit**

```bash
cd ~/Documents/newt/git/roku-test-app
git add -A
git commit -m "app: components live in brsMain — flatten into com.nuvyyo.roku.components

components/ is gone: real components in com.nuvyyo.roku.components, E2E
fixtures in com.nuvyyo.roku.components.fixtures (one package each, dropping
the per-file packages and the stray 'package ShelfView'/'package TestLayout').
The components/source staging workaround in build.gradle.kts is deleted — one
compilation puts helper files in source/ by construction.

Includes Mike's in-progress edits to ShelfView.kt (spawnTask import) and
TestScreenVM.kt (looping-emit demo), moved verbatim.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 6: Run the E2E device suite

**Files:** none modified.

**Interfaces:**
- Consumes: the app as committed in Task 5.
- Produces: the E2E gate re-established on the new layout (104 active tests / 10 suites, 3 red-guarded xtests).

- [ ] **Step 1: Run the suite**

Run: `cd ~/Documents/newt/git/roku-test-app && ./run-device-tests.sh 2>&1 | tail -40`
Expected: all suites green, `104` passed, `0` failed. If it aborts with "Console connection is already in use", stop and ask Mike to disconnect the IDE's Roku console; do not kill anything.

- [ ] **Step 2: Cross-check with the report script**

Run: `cd ~/Documents/newt/git/Kotlin && ./test-report.sh 2>&1 | tail -20`
Expected: the E2E summary shows 104 passed / 0 failed across 10 suites.

No commit for this task; report the numbers.

### Task 7: Update the Kotlin repo docs and the session memory

**Files:**
- Modify: `Kotlin/CLAUDE.md` (every `../roku-test-app/components/…` and `roku-test-app/components/…` path; the E2E "what actually runs" paragraph; the test-locations table row)
- Modify: memory `~/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/project_brsmain_invisible_to_components.md` + its `MEMORY.md` line

**Interfaces:** documentation only.

- [ ] **Step 1: Apply the path rewrites**

```bash
cd ~/Documents/newt/git/Kotlin
NEW='src/brsMain/kotlin/com/nuvyyo/roku/components'
sed -i '' -E \
  -e "s#roku-test-app/components/fixtures/#roku-test-app/$NEW/fixtures/#g" \
  -e "s#roku-test-app/components/(MainScreen|ShelfView|TestLayout|TestScreen)/#roku-test-app/$NEW/#g" \
  -e "s#roku-test-app/components/#roku-test-app/$NEW/#g" \
  CLAUDE.md
grep -n 'roku-test-app/components\|compileComponentsKotlinBrs\|components/fixtures/' CLAUDE.md
```
Expected: the grep shows only the new `src/brsMain/...components/fixtures/` forms; any leftover bare `components/fixtures/` (e.g. "Probe nodes are created via `components/fixtures/` components") is edited by hand to `src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/`.

- [ ] **Step 2: Fix the two prose passages by hand**

In the "E2E Device Tests (roku-test-app)" section, the sentence beginning `**What actually runs:** \`rokuTest\` (KGP task) packages the test app from` must read `… from \`roku-test-app/src/brsTest/kotlin/tests/\` + the fixture components in \`roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/\` (ordinary brsMain classes — there is no separate components compilation), sideloads it, …`.

In the "Test Locations" table, the row `| E2E fixture components | \`roku-test-app/components/fixtures/\` |` must read `| E2E fixture components | \`roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/\` |`.

Add one paragraph at the end of the "Component Include Closure (deps.json)" section:

```markdown
**One compilation per source set (2026-09-04).** kotlin-roku no longer creates a
separate `components` compilation: SceneGraph components are ordinary `brsMain`
classes (the compiler routes each detected component's output to
`components/<Name>/`, everything else to `source/`). The old split made brsMain
declarations unresolvable from component code — its `associateWith`/`libraries`
wiring handed the compiler `.brs` output DIRECTORIES, which `-libraries` silently
ignores (only klibs load) — and marked `components/` as a TEST source set in the
IDE (`associateWith` ⇒ `isTestCompilation`). brsTest sees brsMain (components
included) through `build/brs/klib/main.klib` (`compileMainKlibBrs`).
`roku.componentsDir` is now only the OPTIONAL hand-written-XML directory.
```

- [ ] **Step 3: Update the memory note**

Replace the body of `project_brsmain_invisible_to_components.md` after its frontmatter with:

```markdown
RESOLVED 2026-09-04 (single-BRS-compilation program, spec
`docs/superpowers/specs/2026-09-04-single-brs-compilation-design.md`): kotlin-roku
no longer has a `components` compilation; components are brsMain classes
(`roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components{,/fixtures}`), brsTest
reads `build/brs/klib/main.klib`, `roku.componentsDir` = optional hand-written XML.

**Why it mattered:** the split made brsMain unresolvable from components (plugin
passed .brs dirs on -libraries — silently dropped) and marked components/ TEST in
the IDE (associateWith ⇒ isTestCompilation).

**How to apply:** if "unresolved reference" to a brsMain symbol ever reappears from
component code, check that `compileKotlinBrs` is the only main-side compile task and
that no `associateWith` was reintroduced. Related: [[project_include_correctness]].
```

Rename the file to `project_single_brs_compilation.md` (update `name:` to `project_single_brs_compilation`, `description:` to `RESOLVED 2026-09-04 — components are brsMain classes; no components compilation; brsTest uses main.klib`) and replace its `MEMORY.md` line with:
`- [Single BRS compilation](project_single_brs_compilation.md) — RESOLVED 2026-09-04: kotlin-roku components compilation removed; components are brsMain classes (com.nuvyyo.roku.components{,.fixtures}); brsTest reads main.klib; roku.componentsDir = optional hand-written XML`

- [ ] **Step 4: Commit the docs**

```bash
cd ~/Documents/newt/git/Kotlin
git add CLAUDE.md
git commit -m "docs: components are brsMain classes — paths + include-closure note

roku-test-app components moved to src/brsMain/kotlin/com/nuvyyo/roku/components
(fixtures in .../components/fixtures); kotlin-roku has no separate components
compilation any more.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

## Stage 2 — Android-shaped scripts

### Task 8: Fix published coordinates at the source (Kotlin fork)

**Files:**
- Modify: `Kotlin/repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/nuvyyo-publishing.gradle.kts`

**Interfaces:**
- Produces: every POM and `.module` published under `com.nuvyyo` references non-BRS Kotlin modules as `org.jetbrains.kotlin:<module>:<upstream>` where `<upstream> = project.version.substringBefore("-brs")` (= `2.2.20`). Tasks 9–10 delete the substitution hacks that compensated.

- [ ] **Step 1: Write the pre-fix scan and record the baseline**

Save as `/private/tmp/claude-502/-Users-Mike-Fougere-Documents-newt-git-Kotlin/efa6744b-a19d-4988-82c3-924321242937/scratchpad/scan_coords.py` (scratchpad; not committed):

```python
import glob, json, os, re, sys
root = os.path.expanduser('~/.m2/repository/com/nuvyyo')
bad = 0
def nuvyyo_ok(m): return m.endswith('-brs') or m.endswith('-brs-runtime')
for p in glob.glob(root + '/**/*2.2.20-brs.1.pom', recursive=True):
    t = open(p).read()
    for d in re.findall(r'<dependency>(.*?)</dependency>', t, re.S):
        g = re.search(r'<groupId>(.*?)</groupId>', d); a = re.search(r'<artifactId>(.*?)</artifactId>', d)
        if g and a and g.group(1) == 'com.nuvyyo' and not nuvyyo_ok(a.group(1)):
            bad += 1; print('POM', os.path.relpath(p, root), '->', a.group(1))
for p in glob.glob(root + '/**/*2.2.20-brs.1.module', recursive=True):
    d = json.load(open(p))
    for v in d.get('variants', []):
        for key in ('dependencies', 'dependencyConstraints'):
            for dep in v.get(key, []):
                if dep.get('group') == 'com.nuvyyo' and not nuvyyo_ok(dep.get('module', '')):
                    bad += 1; print('MODULE', os.path.relpath(p, root), v['name'], '->', dep['module'])
print('MISGROUPED ENTRIES:', bad)
sys.exit(1 if bad else 0)
```

Run: `python3 <scratchpad>/scan_coords.py | tail -3`
Expected: `MISGROUPED ENTRIES: <N>` with N > 0 (the failing baseline; 2026-09-04 showed hits in the BOM, KGP, KGP-api and KGP-idea POMs and the KGP `.module`).

- [ ] **Step 2: Add the rewrite to the shared publishing convention**

Replace the entire contents of `nuvyyo-publishing.gradle.kts` with:

```kotlin
/*
 * Registers the Nuvyyo GitHub Packages Maven repository on every subproject that
 * applies maven-publish, and fixes published dependency coordinates.
 *
 * Credentials are read from ~/.gradle/gradle.properties:
 *
 *   nuvyyoGitHubUser=<github-username>
 *   nuvyyoGitHubToken=<classic-pat-with-read:packages-or-write:packages>
 *
 * Publishing to the remote repo requires write:packages + repo scope on the PAT.
 * Consuming artifacts only requires read:packages.
 *
 * COORDINATE FIX: the fork sets group=com.nuvyyo globally, so generated POM and
 * Gradle-module-metadata files reference sibling modules the fork does NOT publish
 * (kotlin-native-utils, kotlin-gradle-plugin-model, kotlin-test, ...) as
 * com.nuvyyo:<module>:<brs-version>. Only *-brs / *-brs-runtime artifacts exist under
 * com.nuvyyo; everything else lives at org.jetbrains.kotlin:<module>:<upstream base
 * version>. Rewriting here, at publish time, means consumers (kotlin-roku, Roku apps)
 * need no dependency-substitution blocks.
 */
import org.gradle.api.publish.tasks.GenerateModuleMetadata

plugins {
    `maven-publish`
}

val upstreamKotlinVersion: String = project.version.toString().substringBefore("-brs")

fun publishedUnderNuvyyo(module: String): Boolean =
    module.endsWith("-brs") || module.endsWith("-brs-runtime")

publishing {
    repositories {
        maven {
            name = "nuvyyo"
            url = uri("https://maven.pkg.github.com/nuvyyo/maven-brs")
            credentials {
                username = providers.gradleProperty("nuvyyoGitHubUser").orNull
                password = providers.gradleProperty("nuvyyoGitHubToken").orNull
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom.withXml {
            val root = asNode()
            fun childNodes(parent: groovy.util.Node, name: String): List<groovy.util.Node> =
                (parent.get(name) as groovy.util.NodeList).filterIsInstance<groovy.util.Node>()
            fun rewrite(dependency: groovy.util.Node) {
                val group = childNodes(dependency, "groupId").firstOrNull() ?: return
                val artifact = childNodes(dependency, "artifactId").firstOrNull() ?: return
                if (group.text() != "com.nuvyyo" || publishedUnderNuvyyo(artifact.text())) return
                group.setValue("org.jetbrains.kotlin")
                childNodes(dependency, "version").firstOrNull()?.setValue(upstreamKotlinVersion)
            }
            childNodes(root, "dependencies")
                .flatMap { childNodes(it, "dependency") }
                .forEach(::rewrite)
            childNodes(root, "dependencyManagement")
                .flatMap { childNodes(it, "dependencies") }
                .flatMap { childNodes(it, "dependency") }
                .forEach(::rewrite)
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    doLast {
        val file = outputFile.get().asFile
        @Suppress("UNCHECKED_CAST")
        val json = groovy.json.JsonSlurper().parse(file) as MutableMap<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val variants = json["variants"] as? List<MutableMap<String, Any?>> ?: emptyList()
        var rewritten = 0
        for (variant in variants) {
            for (key in listOf("dependencies", "dependencyConstraints")) {
                @Suppress("UNCHECKED_CAST")
                val deps = variant[key] as? List<MutableMap<String, Any?>> ?: continue
                for (dep in deps) {
                    val module = dep["module"] as? String ?: continue
                    if (dep["group"] != "com.nuvyyo" || publishedUnderNuvyyo(module)) continue
                    dep["group"] = "org.jetbrains.kotlin"
                    @Suppress("UNCHECKED_CAST")
                    val version = dep["version"] as? MutableMap<String, Any?>
                    version?.keys?.toList()?.forEach { version[it] = upstreamKotlinVersion }
                    rewritten++
                }
            }
        }
        if (rewritten > 0) {
            file.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(json)))
            logger.lifecycle("nuvyyo-publishing: rewrote $rewritten dependency coordinate(s) in ${file.name}")
        }
    }
}
```

- [ ] **Step 3: Republish via the ONE command (background + polling)**

```bash
cd ~/Documents/newt/git/Kotlin
nohup ./rebuild.sh > /private/tmp/claude-502/-Users-Mike-Fougere-Documents-newt-git-Kotlin/efa6744b-a19d-4988-82c3-924321242937/scratchpad/rebuild.log 2>&1 &
echo $!
```
Then poll in bounded foreground loops (each ≤ 5 min), e.g. `for i in $(seq 1 10); do sleep 30; tail -2 <log>; done`, until the log ends with the script's completion banner or `BUILD FAILED`. Do NOT declare a hang from silence. While it runs, Task 9's CODE edits (not its build) may proceed.

Expected: rebuild completes; step 5 output contains `nuvyyo-publishing: rewrote N dependency coordinate(s)` lines for the KGP publications.

- [ ] **Step 4: Verify with the scan and the fixed-attribute check**

Run: `python3 <scratchpad>/scan_coords.py | tail -3`
Expected: `MISGROUPED ENTRIES: 0`, exit 0.

Run:
```bash
python3 - <<'EOF'
import json, os
p = os.path.expanduser('~/.m2/repository/com/nuvyyo/kotlin-gradle-plugin-brs/2.2.20-brs.1/kotlin-gradle-plugin-brs-2.2.20-brs.1.module')
d = json.load(open(p))
for v in d['variants']:
    if 'WithFixedAttribute' in v['name']:
        print(v['name'], '->', [f['name'] for f in v.get('files', [])])
EOF
ls ~/.m2/repository/com/nuvyyo/kotlin-gradle-plugin-brs/2.2.20-brs.1/kotlin-gradle-plugin-brs-2.2.20-brs.1.jar
```
Expected: both WithFixedAttribute variants → `['kotlin-gradle-plugin-brs-2.2.20-brs.1-gradle813.jar']`; the base jar exists (the rewrite preserved `redirectFixedAttributeVariantsToGradle813`'s work).

- [ ] **Step 5: Commit**

```bash
cd ~/Documents/newt/git/Kotlin
git add repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/nuvyyo-publishing.gradle.kts
git commit -m "build: publish non-BRS sibling dependencies at their org.jetbrains.kotlin coordinates

The fork's global group=com.nuvyyo leaked into every published POM/.module as
com.nuvyyo:<unpublished-module>:<brs-version>. nuvyyo-publishing now rewrites
those entries at publish time (POM withXml + GenerateModuleMetadata doLast) to
org.jetbrains.kotlin:<module>:<upstream base version>, so kotlin-roku and Roku
apps need no dependency-substitution blocks.

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 9: Nest the secondary extensions and drop kotlin-roku's substitution hack

**Files:**
- Modify: `kotlin-roku/build.gradle.kts` (delete the `configurations.all { resolutionStrategy.dependencySubstitution { … } }` block and its comment)
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuExtension.kt`
- Modify: `kotlin-roku/src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt` (the `rokuTest` / `rokuValidation` `extensions.create` calls and the `registerTasks(...)` call)

**Interfaces:**
- Consumes: Task 8's republished artifacts in Maven Local.
- Produces: `roku { test { timeout.set(…); ignoreFailures.set(…); filter.set(…); reportsDir.set(…) } validation { includeMode.set(…); extraBuiltins.set(…) } }`. Types unchanged: `RokuTestExtension`, `RokuValidationExtension`. Top-level `rokuTest` / `rokuValidation` extensions REMOVED.

- [ ] **Step 1: Delete the substitution block from the plugin build**

In `kotlin-roku/build.gradle.kts`, delete from the comment `// The KGP -brs POMs declare their transitive utility deps (kotlin-native-utils, etc.)` through the closing `}` of `configurations.all { … }` (inclusive).

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew clean test --no-daemon -q --refresh-dependencies`
Expected: BUILD SUCCESSFUL — resolution now works from the fixed POM/.module files alone. If it fails with `Could not find com.nuvyyo:<module>:2.2.20-brs.1`, Task 8 did not cover that module: STOP and report the module name (do not re-add the hack).

- [ ] **Step 2: Nest the extensions**

Replace the whole `RokuExtension.kt` with:

```kotlin
package com.example.roku.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * The `roku { }` block — the ONE configuration surface of a Roku app module,
 * shaped like Android's `android { }`: app metadata, compilation options,
 * project layout, device deployment, and the nested `test { }` / `validation { }`
 * option groups.
 */
abstract class RokuExtension @Inject constructor(project: Project) {
    // App metadata
    abstract val appName: Property<String>
    abstract val appId: Property<String>
    abstract val appVersion: Property<String>

    // BRS compilation options
    abstract val minRokuOS: Property<String>
    abstract val debugMode: Property<Boolean>

    // Roku project structure
    abstract val manifestFile: RegularFileProperty
    abstract val imagesDir: DirectoryProperty

    /**
     * OPTIONAL directory of hand-written SceneGraph component XML files (default
     * `components/`). Kotlin components live in `src/brsMain/kotlin` like every other
     * class — the compiler generates their XML. Most projects never create this dir.
     */
    abstract val componentsDir: DirectoryProperty
    abstract val fontsDir: DirectoryProperty
    abstract val assetsDir: DirectoryProperty

    // Device deployment
    abstract val deviceIP: Property<String>
    abstract val devicePassword: Property<String>

    // BrighterScript integration
    abstract val brighterScriptEnabled: Property<Boolean>
    abstract val brighterScriptStagingDir: DirectoryProperty
    abstract val brighterScriptCommand: Property<String>
    abstract val brighterScriptSourceDir: DirectoryProperty

    /** Device-test options: `roku { test { timeout.set(300_000L) } }`. */
    val test: RokuTestExtension = project.objects.newInstance(RokuTestExtension::class.java, project)

    /** Include-closure validation options: `roku { validation { includeMode.set("strict") } }`. */
    val validation: RokuValidationExtension = project.objects.newInstance(RokuValidationExtension::class.java)

    fun test(action: Action<in RokuTestExtension>) = action.execute(test)

    fun validation(action: Action<in RokuValidationExtension>) = action.execute(validation)

    init {
        appName.convention(project.name)
        appVersion.convention("1.0.0")
        minRokuOS.convention("9.4")
        debugMode.convention(false)
        manifestFile.convention(project.layout.projectDirectory.file("manifest"))
        imagesDir.convention(project.layout.projectDirectory.dir("images"))
        componentsDir.convention(project.layout.projectDirectory.dir("components"))
        fontsDir.convention(project.layout.projectDirectory.dir("fonts"))
        assetsDir.convention(project.layout.projectDirectory.dir("assets"))

        // BrighterScript defaults
        brighterScriptEnabled.convention(false)
        brighterScriptStagingDir.convention(project.layout.projectDirectory.dir("out"))
        brighterScriptCommand.convention("npx bsc")
        brighterScriptSourceDir.convention(project.layout.projectDirectory.dir("src"))
    }
}
```

In `RokuPlugin.apply()`, delete the two blocks:

```kotlin
        // Create Roku test extension
        val rokuTestExtension = project.extensions.create(
            "rokuTest",
            RokuTestExtension::class.java,
            project
        )

        // Create Roku validation extension
        val rokuValidationExtension = project.extensions.create(
            "rokuValidation",
            RokuValidationExtension::class.java
        )
```

and change the `registerTasks(project, rokuExtension, rokuTestExtension, rokuValidationExtension, …)` call to `registerTasks(project, rokuExtension, rokuExtension.test, rokuExtension.validation, …)`. `registerTasks`'s parameter list is unchanged.

- [ ] **Step 3: Test and publish the plugin**

Run: `cd ~/Documents/newt/git/kotlin-roku && ./gradlew test --no-daemon -q`
Expected: BUILD SUCCESSFUL.

Run: `cd ~/Documents/newt/git/roku-test-app && ./rebuild-all.sh --plugin 2>&1 | tail -5`
Expected: plugin publish BUILD SUCCESSFUL (the app build afterwards is EXPECTED to fail on the now-missing `rokuTest { }` / `rokuValidation { }` accessors until Task 10).

- [ ] **Step 4: Commit**

```bash
cd ~/Documents/newt/git/kotlin-roku
git add build.gradle.kts src/main/kotlin/com/example/roku/gradle/RokuExtension.kt src/main/kotlin/com/example/roku/gradle/RokuPlugin.kt
git commit -m "plugin: roku { test { } validation { } } — one configuration block, no substitution hack

The Kotlin fork now publishes correct coordinates, so the com.nuvyyo ->
org.jetbrains.kotlin dependency substitution is gone. rokuTest {} and
rokuValidation {} move under roku {} (Android's testOptions/lint shape).

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 10: Reduce the app scripts to `plugins {}` + `roku {}`

**Files:**
- Modify: `roku-test-app/settings.gradle.kts` (add `pluginManagement.plugins`)
- Modify: `roku-test-app/build.gradle.kts` (replace entirely)

**Interfaces:**
- Consumes: the plugin published in Task 9 Step 3; Task 8's artifacts.
- Produces: the target scripts from the spec.

- [ ] **Step 1: Pin the plugin version in settings**

In `settings.gradle.kts`, inside `pluginManagement { … }` after the `repositories { … }` block, add:

```kotlin
    plugins {
        id("com.nuvyyo.brightscript.kotlin-roku") version "2.2.20-brs.1"
    }
```

- [ ] **Step 2: Replace the module script**

Replace the entire contents of `roku-test-app/build.gradle.kts` with:

```kotlin
plugins {
    id("com.nuvyyo.brightscript.kotlin-roku")
}

roku {
    appName.set("RokuTestApp")
    appVersion.set("1.0.0")
    minRokuOS.set("10.0")

    test {
        timeout.set(300_000L)  // 5 minutes
        ignoreFailures.set(false)
    }

    validation {
        // Fail packaging on any component include hole. This project reached zero
        // findings (2026-08-13); the plugin-wide default stays "warning".
        includeMode.set("strict")
    }
}
```

- [ ] **Step 3: Build both packages without any substitution**

Run: `cd ~/Documents/newt/git/roku-test-app && ./gradlew clean build packageRokuTests --no-daemon --refresh-dependencies 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL; both validators 0 findings in strict mode. A `Could not find com.nuvyyo:<module>` failure means Task 8 missed a module — STOP and report; do not re-add substitution.

- [ ] **Step 4: Confirm the nested options reach the tasks**

Run: `cd ~/Documents/newt/git/roku-test-app && ./gradlew help --task runRokuTests --no-daemon -q | head -5 && head -5 build/roku/validation/componentIncludes.txt`
Expected: `runRokuTests` exists; the report header names strict mode with 0 finding(s) (proving `roku.validation.includeMode` is wired — the validator writes `mode` into its report).

- [ ] **Step 5: Re-run the E2E suite**

Run: `cd ~/Documents/newt/git/roku-test-app && ./run-device-tests.sh 2>&1 | tail -20`
Expected: 104 passed / 0 failed (proves `roku.test.timeout` etc. still drive `runRokuTests`).

- [ ] **Step 6: Commit**

```bash
cd ~/Documents/newt/git/roku-test-app
git add settings.gradle.kts build.gradle.kts
git commit -m "app: Android-shaped scripts — plugins {} + roku {} only

Plugin version pinned in settings pluginManagement; rokuTest/rokuValidation
nested under roku {}; both dependency-substitution blocks deleted (the fork
publishes correct coordinates now).

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

### Task 11: Close out — docs, memory, gate table

**Files:**
- Modify: `Kotlin/CLAUDE.md` (Quick Reference / Running Tests sections if they mention `rokuValidation { }`; the "Current Gate Numbers" note)
- Modify: memory `project_single_brs_compilation.md` (add the Stage 2 outcome) + `MEMORY.md` line

**Interfaces:** documentation only.

- [ ] **Step 1: Grep for stale script shapes**

Run: `cd ~/Documents/newt/git/Kotlin && grep -n 'rokuValidation\|rokuTest {\|dependencySubstitution\|com.nuvyyo:\*' CLAUDE.md docs/superpowers/plans/2026-09-04-single-brs-compilation.md`
Expected: only this plan's own text. Edit any CLAUDE.md hit to the nested `roku { test { } validation { } }` form.

- [ ] **Step 2: Add the gate-table note**

In `CLAUDE.md` under "Current Gate Numbers", change the heading line to `### Current Gate Numbers (as of the single-BRS-compilation program close, 2026-09-04)` and add after the table:

```markdown
Verified 2026-09-04 on the single-compilation layout (no `components` compilation;
app scripts are `plugins {}` + `roku {}`; no dependency substitution anywhere).
```

- [ ] **Step 3: Update the memory note**

Append to `project_single_brs_compilation.md`:

```markdown

**Stage 2 (2026-09-04):** the fork's `nuvyyo-publishing.gradle.kts` rewrites
non-BRS sibling deps to `org.jetbrains.kotlin:<module>:2.2.20` in POM + .module at
publish time; kotlin-roku and roku-test-app carry NO dependency-substitution
blocks. App script shape: `plugins { id(...) }` + `roku { ... test { } validation { } }`,
version pinned in settings `pluginManagement.plugins`. `dependencies {}` for
third-party klibs is DEFERRED (needs runtime .brs staging via Gradle variants).
```

- [ ] **Step 4: Commit**

```bash
cd ~/Documents/newt/git/Kotlin
git add CLAUDE.md
git commit -m "docs: gate table + script shape after the single-BRS-compilation program

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

- [ ] **Step 5: Final report**

Report, for a reader who saw nothing else: the three repos' new HEAD commits, the gate numbers observed in Tasks 6 and 10, the scan result from Task 8 Step 4, and the one thing Mike must check himself (IDE resync: no `components` module, brsMain not green).
