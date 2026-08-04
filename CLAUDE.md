# Kotlin BrightScript Backend Development Guide

## Project Overview

This is a fork of the Kotlin compiler that adds a BrightScript backend for Roku development. It compiles Kotlin source code to BrightScript (.brs) files that run on Roku devices.

## BrightScript Language Notes

**BrightScript is case-insensitive.** This means:
- `myFunction` and `MYFUNCTION` and `MyFunction` are all the same identifier
- `result` and `RESULT` and `Result` are the same variable name
- Method names like `resumeWith_Result_k_` and `resumeWith_RESULT_k_` are identical to BrightScript

This affects code generation - there's no need to use uppercase for "disambiguation" since case doesn't disambiguate anything in BrightScript.

## MANDATORY BUILD RULES

### The ONE Command: `./rebuild.sh`

**For ANY change to compiler or stdlib code, run:**

```bash
./rebuild.sh
```

That's it. This script:
1. Cleans BRS module build directories (forces ~42 BRS-specific tasks to re-execute)
2. Runs all 7 build steps every invocation — Gradle's up-to-date checks skip unchanged modules
3. Uses `-Pkotlin.build.useBootstrapStdlib=true` (prevents metadata version mismatch with bootstrap compiler)
4. Builds `cli-brs:fatJar` (BRS compiler, ~702 tasks — not the full `dist`)
5. Runs `regenerateKlib` and `generateStdlibBrs` using the fat JAR
6. Publishes all artifacts to Maven Local

**DO NOT** run manual cache-clearing commands. **DO NOT** run individual gradlew tasks.
If `./rebuild.sh` doesn't work correctly, that's an **infrastructure failure** - fix the script.

### Testing with roku-test-app

```bash
# From roku-test-app directory - full rebuild including compiler
cd ../roku-test-app && ./rebuild-all.sh --all
```

## INFRASTRUCTURE FAILURE PROTOCOL

When you see ANY of these, it is an **INFRASTRUCTURE FAILURE**:

| Symptom | What It Means |
|---------|---------------|
| UP-TO-DATE for modules you changed | Build cache served stale output |
| Stale timestamps in test output | Device logs from previous run |
| Changes not in generated .brs files | Build didn't include your changes |
| Test failures that don't match code | You're debugging the wrong version |

**MANDATORY RESPONSE:**

1. **STOP** - Do not run another command. Do not try to work around it.
2. **FIX** - Update rebuild.sh or run-tests.sh to prevent this
3. **DOCUMENT** - Add the fix to this file
4. **NEVER** work around infrastructure problems with manual commands

The tooling must work correctly. If it doesn't, fix the tooling.

### How rebuild.sh optimizes incremental builds

On every run, `rebuild.sh` deletes only the BRS compiler module `build/` directories
before invoking Gradle. This forces the ~42 BRS-specific tasks to re-execute while
the ~660 core Kotlin compiler tasks remain `UP-TO-DATE` from Gradle's build cache.

`regenerateKlib` and `generateStdlibBrs` track `cli-brs:fatJar` as a Gradle input.
If the fat JAR hasn't changed, those tasks are also `UP-TO-DATE`.

There are no marker files. There is no `--rerun-tasks`. Gradle's own incremental
build machinery is the source of truth for what needs rebuilding.

**BrightScript modules cleaned on each run:**
- `brightscript/brs.ast/build`
- `core/compiler.common.brightscript/build`
- `brs/brs.frontend/build`
- `compiler/ir/serialization.brs/build`
- `compiler/fir/checkers/checkers.brs/build`
- `compiler/ir/backend.brightscript/build`
- `compiler/cli/cli-brs/build`

### Forcing a full rebuild

Use `--clean` to force a full rebuild from scratch:

```bash
./rebuild.sh --clean
```

This removes all BRS build directories and Maven Local BRS artifacts before rebuilding.

## DO NOT

- **DO NOT** run manual cache-clearing commands - rebuild.sh handles this
- **DO NOT** run individual gradlew commands - use rebuild.sh
- **DO NOT** debug test failures without verifying log timestamps first
- **DO NOT** assume stale logs are showing your current code

## Compiler Architecture Context

- Kotlin uses a multi-stage compilation pipeline: Frontend (FIR) → IR → Backend
- BrightScript is a new backend alongside JVM, JS, Native, and Wasm
- The K2 (FIR) frontend is used; K1 is not supported
- Entry point: `K2BrsCompiler` in `compiler/cli/cli-brs/`

## Native BrightScript Type Interfaces

Native BrightScript objects (roArray, roAssociativeArray, etc.) are modeled as **external interfaces** rather than wrapper classes. This allows user code to pass native BrightScript objects directly to Kotlin functions without wrapping overhead.

### Key Concepts

**1. External Interfaces (not wrapper classes)**

Native types are declared as `external interface`, which tells the compiler the methods exist on the native object:

```kotlin
// In libraries/stdlib/brs/src/kotlin/brs/roku/NativeTypes.kt
public external interface RoArray : IArray, IArrayJoin, IArraySort, IEnumNative {
    companion object {
        @BrsCreateObject("roArray")
        fun create(size: Int = 0, resize: Boolean = true): RoArray = definedExternally
    }
}
```

**2. @BrsCreateObject Annotation**

Marks companion object factory functions. The compiler transforms calls to `CreateObject()`:

```kotlin
// Kotlin code:
val arr = RoArray.create(10, true)

// Compiles to BrightScript:
arr = CreateObject("roArray", 10, true)
```

**3. NativeArrayIterator Marker Type**

When a type's `iterator()` returns `NativeArrayIterator`, the compiler emits native BrightScript `for each` instead of the Kotlin iterator protocol:

```kotlin
public external interface NativeArrayIterator<out T>  // Marker interface

public interface NativeIterable {
    fun iterator(): NativeArrayIterator<Dynamic>
}

// Types implementing NativeIterable get native for-each:
for (item in roArray) { ... }  // → for each item in roArray ... end for
```

**4. Roku Interface Mappings**

BrightScript interfaces map to Kotlin interfaces:
- `ifArray` → `IArray`
- `ifArrayGet` → `IArrayGet`
- `ifArraySet` → `IArraySet`
- `ifEnum` → `IEnumNative`
- `ifAssociativeArray` → `IAssociativeArray`

### Key Files

| File | Purpose |
|------|---------|
| `libraries/stdlib/brs/src/kotlin/brs/annotations.kt` | `@BrsCreateObject` annotation definition |
| `libraries/stdlib/brs/src/kotlin/brs/roku/NativeTypes.kt` | Native type interfaces (RoArray, RoAssociativeArray, etc.) |
| `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` | Class IDs for compiler recognition |
| `compiler/ir/backend.brightscript/.../BrsIntrinsics.kt` | `isNativeIterable()`, `returnsNativeArrayIterator()` |
| `compiler/ir/backend.brightscript/.../irToBrs/IrToBrsTransformer.kt` | Declaration/for-each handling (irToBrs package, post-B1 split) |
| `compiler/ir/backend.brightscript/.../irToBrs/IrExpressionToBrsTransformer.kt` | Expression codegen, @BrsCreateObject compilation |

### For-Each Loop Compilation Strategies

The compiler uses these strategies (in order) for `for (x in iterable)`:

1. **NativeIterable check**: If type implements `NativeIterable` → native `for each`
2. **NativeArrayIterator check**: If `iterator()` returns `NativeArrayIterator` → native `for each`
3. **Stdlib collections with get_array()**: ArrayList, IntArray, etc. → `for each x in obj.get_array()`
4. **Kotlin Iterable interface**: HashSet, Sequence, etc. → while loop with `iterator_k_()`, `hasNext_k_()`, `next_k_()`
5. **Default**: Native `for each`

**Reality check (backlogged):** strategies 1–2 are currently unreachable from user
code. `for (x in roArray)` does NOT compile: `NativeIterable.iterator()` lacks the
`operator` convention and `NativeArrayIterator` declares no `hasNext()`/`next()`,
so the frontend rejects the loop before the backend's strategy choice matters.
Indexing is also broken for native arrays: `RoArray` operator `get` emits a
`.get(i)` method call that native roArray does not have. Until both are fixed,
drain native arrays with `count()`/`shift()` or render them via `join()` —
see `roku-test-app/components/ShelfView/ShelfView.kt` (onShelfItemsChanged)
for the working pattern.

### Adding New Native Types

To add a new native BrightScript type (e.g., `RoList`):

1. **Define the interface** in `NativeTypes.kt`:
   ```kotlin
   public external interface RoList : IEnumNative {
       fun count(): Int
       fun addTail(value: Dynamic)
       // ... other methods from Roku docs

       companion object {
           @BrsCreateObject("roList")
           fun create(): RoList = definedExternally
       }
   }
   ```

2. **Add class ID** (if needed for compiler recognition) in `BrsStandardClassIds.kt`

3. **No wrapper class needed** - the external interface describes what the native object can do

## Typed Tasks (`runTask`) - THE Task-Boundary Mechanism

Work that must leave the render thread goes through a **typed task component**.
This is the only sanctioned way to cross the render/task thread boundary.

**1. Declare the task** - subclass `TaskComponent`, annotate typed input/output
fields, put the task-thread work in `run()`:

```kotlin
class FetchShelfTask : TaskComponent() {
    @SGIntegerField
    var count: Int = 0            // input

    @SGArrayField
    var items: RoArray? = null    // output

    override fun run() {          // runs on the TASK thread
        val fetched = RoArray.create(0, true)
        for (i in 1..count) {
            fetched.push("$i")
        }
        items = fetched
    }
}
```

**2. Run it** from a render-thread coroutine. `runTask` creates a fresh node,
configures the inputs, runs it on the task thread, and suspends until the task
completes - resuming with the completed node so typed outputs read directly:

```kotlin
val node = top   // alias: a coroutine lambda's `m` is the coroutine object,
                 // not the component scope, so hand results back via the node
CoroutineScope(Dispatchers.Main).launch {
    try {
        val task = runTask<FetchShelfTask> { count = 5 }
        node.setField("shelfItems", task.items)
    } catch (e: TaskException) {
        // run() threw on the task thread; message/number/backtrace preserved
        println("fetch failed: ${e.message}")
    }
}
```

Key facts:

- A failed `run()` surfaces as `TaskException` at the suspend point (fields of
  the BrightScript error object: `message`, `number`, `backtrace`).
- v1 constraints: must be called from render-thread component context; a fresh
  unparented node per invocation (no pooling); one-shot; no cancellation or
  timeout yet (M3 backlog).
- FIR diagnostics guard the pattern: `BRS_TASK_STATE_NOT_FIELD` (task state
  must be @SG interface fields - plain properties are lost across the node
  clone) and `BRS_CREATE_COMPONENT_INVALID_TYPE` (+ an "[IR] " backstop).
- Canonical example: `../roku-test-app/components/ShelfView/ShelfView.kt` +
  `FetchShelfTask.kt` (the flagship demo). Acceptance coverage: E2E Suite 4
  (TypedTaskAcceptance).
- Stdlib implementation: `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`.

### QUARANTINED: `withContext(Dispatchers.IO)` / TaskPool / runIOWorker

`withContext(Dispatchers.IO)`, `TaskPool`, and the `IOWorkerRegistry`/ioWorker
pipeline are **QUARANTINED - do not use**. That pipeline is unverified on
device, has zero E2E coverage, and its last consumer (ShelfView) was migrated
to `runTask` in Phase 2. It is slated to be re-layered as sugar that
synthesizes a typed task per block (M3 backlog); until then any new
`withContext(Dispatchers.IO)` use is a review-blocking regression.

## SceneGraph Layouts: @SGLayout DSL + Layout Accessors

The layout story: declare the component's children ONCE in the `sceneLayout {}`
DSL; access them through the generated `<ClassName>_Layout` accessor class.

```kotlin
class MainScreen : SceneComponent() {
    private val layout = MainScreen_Layout(top)   // generated stub

    init {
        layout.shelf_view.setFocus(true)          // typed accessor
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            layoutGroup(id = "mainLayout", layoutDirection = LayoutDirection.horiz) {
                component("ShelfView", id = "shelf_view", focusable = true)
            }
        }
    }
}
```

How it works:

- The compiler extracts the DSL into the component XML `<children>` section.
  Attribute values must be compile-time constants - a non-constant value is
  dropped from the XML with a compiler warning naming the attribute and
  component (set those at runtime in `init {}` instead).
- The kotlin-roku Gradle plugin (`GenerateLayoutStubsTask`) generates the
  `<ClassName>_Layout` stub class (lazily cached `findNode()` getters) for
  every node id in the DSL - including embedded custom components declared
  via `component("Type", id = "...")`.
- The stub compiles as normal Kotlin and IS the runtime implementation; the
  component stores it in its `layout` property (`m.layout` in generated BRS).

**@SGNodeField is NOT for layout-child access.** It declares a node-typed
*interface field* on the component (`<field type="node" nodeType="..."/>`) - a
slot that external code can set/observe. Use Layout accessors for nodes the
component declares itself; use `@SGNodeField` only for node-valued
inputs/outputs on the component's public interface.

## Key Directories

### Compiler Modules
- `compiler/ir/backend.brightscript/` - IR to BrightScript transformer (IrToBrsTransformer)
- `compiler/cli/cli-brs/` - CLI entry point (K2BrsCompiler)
- `core/compiler.common.brightscript/` - Shared BRS compiler types
- `compiler/fir/checkers/checkers.brs/` - BRS-specific FIR checkers
- `compiler/ir/serialization.brs/` - Klib serialization for BRS
- `brightscript/brs.ast/` - BrightScript AST definitions

### Stdlib
- `libraries/stdlib/brs/` - Stdlib source (builtins/, runtime/, src/)
- `libraries/stdlib/brs-actual/` - Platform-specific implementations
- `libraries/stdlib/brs-prebuilt/` - Pre-compiled klib (checked into VCS)

### Related External Repositories
- `../kotlin-roku/` - Gradle plugin for Roku projects
- `../roku-test-app/` - Test application for validation

## Build Dependency Chain

```
Compiler Sources → Fat JAR → Stdlib klib → Maven Local → kotlin-roku plugin → roku-test-app
```

The rebuild.sh script handles this entire chain correctly. Manual commands break the chain.

## Bootstrap Architecture

### Why BRS Requires Special Handling

This is a fork of the Kotlin compiler. The remote bootstrap compiler from JetBrains
(v2.2.20-Beta2-71, see `gradle.properties:42`) speaks metadata version 2.0.x. If
`:kotlin-stdlib:jvmJar` is forced to run (e.g. by `--rerun-tasks`), the project's
own 2.2.x stdlib lands on `:kotlin-util-klib`'s compile classpath — the 2.0.x
bootstrap compiler cannot read 2.2.x metadata → BUILD FAILED:

```
binary version of its metadata is 2.2.0, expected version is 2.0.0
```

### The Fix: `-Pkotlin.build.useBootstrapStdlib=true`

`rebuild.sh` passes `-Pkotlin.build.useBootstrapStdlib=true` to every Gradle
invocation. This flag (defined in
`repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/BuildPropertiesExt.kt`)
makes `kotlinStdlib()` (`repoDependencies.kt:58-63`) return the external Maven
bootstrap stdlib instead of `project(":kotlin-stdlib")`. The project's own stdlib
never appears on the bootstrap compiler's classpath.

**DO NOT remove this flag from rebuild.sh.** Without it, any build that re-executes
`:kotlin-stdlib:jvmJar` will fail with the metadata version error above.

### Why `cli-brs:fatJar` Instead of `dist`

The `dist` task builds the full Kotlin distribution: JVM + JS + Native + BRS
(~1381 tasks). BRS only needs the BRS backend. `compiler:cli-brs:fatJar` produces
a self-contained fat JAR with all BRS compiler dependencies bundled (~702 tasks
with `useBootstrapStdlib=true`).

`regenerateKlib` (`brs-prebuilt/build.gradle.kts`) and `generateStdlibBrs`
(`stdlib/build.gradle.kts`) both depend on `:compiler:cli-brs:fatJar`. The fat JAR
is also tracked as a Gradle input — so changes to the compiler automatically trigger
klib and runtime regeneration.

### How `rebuild.sh` Works (Current Architecture)

All 7 steps run on every invocation. Incremental speed comes from two mechanisms:

1. **Build directory cleanup**: BRS compiler module `build/` dirs are deleted before
   each run, forcing only the ~42 BRS-specific Gradle tasks to re-execute. The ~660
   core Kotlin compiler tasks see their outputs unchanged and are `UP-TO-DATE`.
2. **Gradle up-to-date checks**: `regenerateKlib` and `generateStdlibBrs` track the
   fat JAR as an input. If the JAR hasn't changed, those tasks are UP-TO-DATE.

There are no marker files. There is no `--rerun-tasks`. Gradle's own incremental
build machinery is the source of truth.

### Bootstrap Build Sequence

```
:compiler:cli-brs:fatJar  (~702 tasks, useBootstrapStdlib=true)
         ↓
:kotlin-stdlib-brs-prebuilt:regenerateKlib  (uses fatJar as classpath)
         ↓
publishToMavenLocal  (compiler, KGP, stdlib klib, stdlib runtime, kotlin-test-brs)
```

### Fresh Clone Workflow

From a fresh clone, just run:

```bash
./rebuild.sh
```

This handles everything automatically. After completion, all artifacts are in Maven
Local under `com.nuvyyo:*` at version `2.2.20-brs.1`.

## Troubleshooting

### SSL Errors During Gradle Builds

If you see SSL certificate errors, handshake failures, or connection reset errors during Gradle builds, **STOP and ask the user to turn off ZScaler**. This is the cause 99% of the time. Do not try to debug SSL issues yourself.

### Roku Documentation Access

Roku's official documentation (developer.roku.com) blocks bot/programmatic access. If you need to reference Roku SDK documentation:

1. **Provide the URL** to the user (e.g., `https://developer.roku.com/docs/references/scenegraph/widget-nodes/button.md`)
2. **Stop and wait** - the user will manually download the page
3. **Read from `../RokuDocs/`** - downloaded documentation is stored there

**DO NOT** repeatedly try to fetch from developer.roku.com - it will always fail with 403.

## Regenerating FIR diagnostic containers

When you edit `compiler/fir/checkers/checkers-component-generator/src/.../diagnostics/FirBrsDiagnosticsList.kt` (add/rename/remove a BRS FIR diagnostic), regenerate the generated containers:

```bash
./gradlew :compiler:fir:checkers:checkers.brs:generateCheckersComponents --no-configuration-cache
./gradlew :compiler:fir:checkers:generateCheckersComponents --no-configuration-cache
```

The first regenerates `compiler/fir/checkers/checkers.brs/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrors.kt`. The second regenerates `compiler/fir/checkers/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/FirNonSuppressibleErrorNames.kt` (the cross-platform aggregator). **Commit both generated files.** Then add the new diagnostic's message to `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrorsDefaultMessages.kt`.

This is a manual step — `./rebuild.sh` does not invoke the generator. Follow the upstream JS/Wasm pattern: generated output is committed, regen happens on demand.

## Quick Reference

**rebuild.sh now compile-checks `kotlin-test-brs` whenever the stdlib or compiler
changes** (step 7). This catches FIR-level regressions in `kotlin.test` that golden
file tests and the diagnostic suite don't exercise. If step 7 fails, the new diagnostic
or checker change is firing on real `kotlin.test` source — fix at the declaration site
with `@Suppress("BRS_<NAME>")` (mirroring Job.Key, ContinuationInterceptor.Key, and
the kotlin.test Test/test() suppression sites) or rework the checker.

| What Changed | Run This |
|--------------|----------|
| Compiler or stdlib code | `./rebuild.sh` |
| Full clean rebuild (nuclear option) | `./rebuild.sh --clean` |
| Everything + test app | `cd ../roku-test-app && ./rebuild-all.sh --all` |
| kotlin-roku plugin only | `cd ../roku-test-app && ./rebuild-all.sh --plugin` |
| Run stdlib tests | `./run-stdlib-tests.sh` |
| Run E2E device tests | `cd ../roku-test-app && ./run-device-tests.sh` |

**Careful with `--plugin --clean`:** `--clean` wipes ALL `com.nuvyyo` artifacts
from Maven Local but `--plugin` republishes only the kotlin-roku plugin - the
compiler/stdlib artifacts stay missing until the next `./rebuild.sh`. Use
`--clean` only together with `--all` (or right after a fresh `./rebuild.sh`).

`./rebuild.sh` handles all cache cleaning automatically. Use `--clean` when things are in a bad state.

## Running Tests

### Compiler Tests (Golden File Tests)

Golden file tests verify that Kotlin code compiles to the expected BrightScript output. No Roku device required.

```bash
# Run all compiler tests
./run-compiler-tests.sh

# Or with Gradle directly
./gradlew :compiler:backend.brightscript:test --tests "*GoldenFile*" --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

### Updating Golden Files

After making intentional changes to the compiler's output:

```bash
# Update golden files with current compiler output
./run-compiler-tests.sh --update

# Or with Gradle
./gradlew :compiler:backend.brightscript:test --tests "*GoldenFile*" -PupdateGoldenFiles=true --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

### Stdlib Tests (Device Tests)

Run stdlib unit tests on a physical Roku device. Requires device IP and password.

```bash
# Set device credentials
export ROKU_DEVICE_IP=192.168.1.xxx
export ROKU_PASSWORD=your_password

# Run stdlib tests
./run-stdlib-tests.sh

# Build only (no device required) - useful for checking compilation
./run-stdlib-tests.sh --build-only
```

**Stale Log Handling (Sentinel-Based Filtering)**

The Roku debug console (telnet port 8085) has an internal buffer that retains logs from previous runs. The test infrastructure handles this using a **flood + sentinel** approach:

1. **At app startup**: The test adapter prints a unique sentinel marker (`===KOTLINTEST_SENTINEL_<timestamp>===`)
2. **Buffer flood**: 100 lines are printed to push stale logs through the buffer
3. **Filtering**: The test runner finds the sentinel and discards all output before it

This guarantees you only see logs from the current run, regardless of what's in the device buffer.

**The completion monitor is sentinel-scoped too.** The wait loop only accepts a sentinel that
(a) arrived in the capture AFTER deployment finished and (b) has a fresh embedded timestamp
(within 300s, tolerating device clock skew), and it greps for `[KOTLINTEST_END]` / crash
markers ONLY in the lines after that sentinel. Before this fix, a previous run's
`[KOTLINTEST_END]` sitting in the console backlog stopped the capture mid-run and silently
truncated the tail of the suite while still reporting "All tests passed" — if a run ever
reports a suspiciously low total with zero failures, check for exactly this class of bug.

**Why telnet instead of nc (netcat)?**

The test runner uses `telnet` to connect to the Roku debug console, NOT `nc`. This is because `nc` exits immediately after receiving the initial buffer dump from the Roku (about 65 lines), while `telnet` stays connected waiting for more data. When run from a script (vs interactive terminal), `nc` doesn't keep the connection open for incoming data.

**What you'll see:**
```
Filtering output by sentinel...
  Sentinel found at line 847 (3s ago - FRESH)
  Filtered: 156 lines (discarded 846 stale lines from buffer)
```

**If sentinel is stale** (> 120s old):
- The current app didn't start correctly
- Try: Re-run tests, or reboot the Roku device

**If no sentinel found:**
- App crashed before `startRun()` was called
- Check the unfiltered output for crash details

The test output file is saved to: `libraries/stdlib/brs/test/build/test-output.txt`

### E2E Device Tests (roku-test-app)

Run E2E tests from the roku-test-app project on a physical Roku device.

```bash
# Set device credentials
export ROKU_DEVICE_IP=192.168.1.xxx
export ROKU_PASSWORD=your_password

# Run E2E tests (from roku-test-app directory)
cd ../roku-test-app && ./run-device-tests.sh

# Or with Gradle
cd ../roku-test-app && ./gradlew rokuTest
```

**What actually runs:** `rokuTest` (KGP task) packages the test app from
`roku-test-app/src/brsTest/kotlin/tests/` + the fixture components in
`roku-test-app/components/fixtures/`, sideloads it, and parses structured
`[KOTLINTEST_EVENT]` JSON events off the telnet console (sentinel-armed like
the stdlib runner: replayed events from a previous run are discarded).
Results land in `build/test-results/roku/` as JSON + JUnit XML.

**The suites (5 suites, 21 active tests + 3 red-guarded `xtest` placeholders):**

| Suite | File | Exercises |
|-------|------|-----------|
| 0 HarnessSmoke | `tests/HarnessSmokeTests.kt` | driver plumbing, sync + async pass/fail paths |
| 1 ComponentObserver | `tests/ComponentObserverTests.kt` | @SG field writes, @BrsOnChange, rapid sets |
| 2 RenderCoroutines | `tests/RenderCoroutineTests.kt` | coroutines on the render thread, captured vars |
| 3 TaskBoundary | `tests/TaskBoundaryTests.kt` | task-thread round trips via EchoTask fixtures |
| 4 TypedTaskAcceptance | `tests/TypedTaskTests.kt` | `runTask` success/error/overlap/round-trip/derived |

**The main-thread driver:** `tests/TestMain.kt` is a `main()` that creates the
SceneGraph screen, installs the screen's message port as the shared `TestPort`,
shows the (empty) `TestScene`, and calls `runTests { ... }`. Test bodies use the
device-test API from `kotlin.test.device` (`libraries/kotlin.test/brs/src/main/kotlin/kotlin/test/device/DeviceTestLoop.kt`):

- `testAsync("name") { ... }` — suspending test body, pumped by the driver's
  port loop (`runPumping`); default 10s whole-test timeout.
- `awaitField(node, "field") { predicate }` — suspends until the field changes
  to a value the predicate accepts (scoped observers, port-drained between runs).
- `awaitFieldEquals(node, "field", expected)` / `roundTrip(node, set, value, await)` —
  conveniences over `awaitField`.

Predicates must return false rather than throw. Probe nodes are created via
`components/fixtures/` components appended to the TestScene.

### Run All Tests

```bash
# Run compiler tests, then E2E if device is configured
./run-all-tests.sh
```

### View Test Results

```bash
# Display summary of most recent test results
./test-report.sh
```

### Test Locations

| Test Type | Location |
|-----------|----------|
| Golden file tests | `compiler/ir/backend.brightscript/test/.../BrsGoldenFileTests.kt` |
| Golden file test data | `compiler/testData/codegen/brs/` |
| FIR diagnostic tests | `compiler/fir/checkers/checkers.brs/test/` + fixtures in `compiler/testData/diagnostics/testsWithBrsStdLib/` |
| Stdlib tests (source) | `libraries/stdlib/brs/test/kotlin/` |
| Stdlib tests (generated) | `libraries/stdlib/brs/test/build/brs/source/` |
| Device-test API (kotlin.test) | `libraries/kotlin.test/brs/src/main/kotlin/kotlin/test/device/DeviceTestLoop.kt` |
| E2E test suites + driver | `roku-test-app/src/brsTest/kotlin/tests/` (TestMain.kt is the main-thread driver) |
| E2E fixture components | `roku-test-app/components/fixtures/` |

### Current Gate Numbers (as of Task 14, 2026-08)

These are the whole-branch green gates; a drop in any of them is a regression.

| Gate | Count |
|------|-------|
| Golden file tests | 53 |
| FIR diagnostic suite (checkers.brs) | 214 |
| Stdlib device suite | 409 tests / 40 suites |
| rokuTest E2E | 21 active tests / 5 suites (+3 red-guarded xtests) |
| `validateComponentIncludes` | strict mode, 0 findings (no allowlist) |

### Test Output

| Test Type | Report Location |
|-----------|-----------------|
| Compiler tests (HTML) | `compiler/ir/backend.brightscript/build/reports/tests/test/index.html` |
| Compiler tests (XML) | `compiler/ir/backend.brightscript/build/test-results/test/*.xml` |
| Stdlib tests (raw log) | `libraries/stdlib/brs/test/build/test-output.txt` |
| Stdlib tests (JSON) | `libraries/stdlib/brs/test/build/results.json` |
| E2E tests (JSON) | `roku-test-app/build/test-results/roku/results.json` |
| E2E tests (XML) | `roku-test-app/build/test-results/roku/results.xml` |