# Kotlin BrightScript Backend Development Guide

## Project Overview

This is a fork of the Kotlin compiler that adds a BrightScript backend for Roku development. It compiles Kotlin source code to BrightScript (.brs) files that run on Roku devices.

## MANDATORY BUILD RULES

### ⚠️ CRITICAL: Stdlib Changes Require Nuclear Clean ⚠️

**When editing ANY file in `libraries/stdlib/brs/` or `libraries/stdlib/brs-actual/`:**

Gradle's incremental compilation and build cache DO NOT reliably detect stdlib source changes. Even `./rebuild.sh` will show "UP-TO-DATE" or "FROM-CACHE" when files changed. You MUST use this nuclear clean command:

```bash
# REQUIRED for ALL stdlib changes - NO EXCEPTIONS
rm -rf libraries/stdlib/build && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs && \
rm -rf libraries/stdlib/brs/test/build && \
./gradlew :kotlin-stdlib:compileKotlinBrs :kotlin-stdlib:brsBrsJar --no-build-cache --no-configuration-cache -Dorg.gradle.dependency.verification=off && \
./gradlew publishBrsModulePublicationToMavenLocal --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

**The key flag is `--no-build-cache`** - without this, Gradle pulls from its remote/local build cache even if local files are deleted.

**Why this matters:** There are FOUR separate caches that can hold stale stdlib code:
1. `libraries/stdlib/build/` - Gradle's local build output
2. Gradle build cache - Remote/local cache (bypassed only with `--no-build-cache`)
3. `~/.m2/repository/.../kotlin-stdlib-brs` - Maven Local klib cache
4. `libraries/stdlib/brs/test/build` - Test build extracts stdlib from klib

If ANY of these are stale, your changes won't be tested. This has wasted HOURS of debugging time.

### Compiler Changes

**⚠️ If Gradle shows "UP-TO-DATE" or "FROM-CACHE" for compiler tasks after you made changes, your changes are NOT being built.**

For compiler code changes (e.g., `compiler/ir/backend.brightscript/`), use this nuclear clean command:

```bash
# REQUIRED when compiler changes show UP-TO-DATE
./gradlew --stop && \
rm -rf ~/.gradle/caches/build-cache-1/* && \
rm -rf compiler/ir/backend.brightscript/build && \
rm -rf compiler/cli-brs/build && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs && \
./gradlew :compiler:backend.brightscript:compileKotlin :compiler:cli-brs:fatJar :kotlin-stdlib:compileKotlinBrs :kotlin-stdlib:brsBrsJar publishBrsModulePublicationToMavenLocal --no-build-cache --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

**The key flags are:**
- `--stop` - Kill Gradle daemons that may have cached classes in memory
- `--no-build-cache` - Bypass Gradle's remote/local build cache
- Deleting `~/.gradle/caches/build-cache-1/*` - Clear the local build cache

If changes are simple and you want to try `./rebuild.sh` first, that's fine. But if it shows UP-TO-DATE for the module you changed, **immediately** use the nuclear clean above. Don't waste time debugging.

The rebuild.sh script:
- Builds the compiler fat JAR
- Regenerates the stdlib klib (using the new compiler)
- Publishes everything to Maven Local in the correct order

### Testing with roku-test-app

```bash
# From roku-test-app directory - full rebuild including compiler
cd ../roku-test-app && ./rebuild-all.sh --all
```

## DO NOT

- **DO NOT** run individual gradlew commands like `:compiler:cli-brs:fatJar` or `:kotlin-stdlib-brs-prebuilt:regenerateKlib`
- **DO NOT** try to "save time" by skipping steps - you will waste MORE time debugging cache issues
- **DO NOT** assume your change is "simple enough" to skip rebuild.sh
- **DO NOT** run publish tasks without first running the full rebuild.sh

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
| `compiler/ir/backend.brightscript/.../IrToBrsTransformer.kt` | For-each handling, @BrsCreateObject compilation |

### For-Each Loop Compilation Strategies

The compiler uses these strategies (in order) for `for (x in iterable)`:

1. **NativeIterable check**: If type implements `NativeIterable` → native `for each`
2. **NativeArrayIterator check**: If `iterator()` returns `NativeArrayIterator` → native `for each`
3. **Stdlib collections with get_array()**: ArrayList, IntArray, etc. → `for each x in obj.get_array()`
4. **Kotlin Iterable interface**: HashSet, Sequence, etc. → while loop with `iterator_k_()`, `hasNext_k_()`, `next_k_()`
5. **Default**: Native `for each`

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

## Troubleshooting

### Symptoms of Stale Cache

- Changes not reflected in compiled output
- Old error messages appearing
- Behavior not matching source code
- "Class not found" or missing symbol errors
- Gradle shows "UP-TO-DATE" but your changes aren't in output
- Generated .brs files don't contain your code changes

### Solution: Nuclear Clean

**For stdlib changes, ALWAYS use nuclear clean first:**

```bash
rm -rf libraries/stdlib/build/classes/kotlin/brs && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs && \
rm -rf libraries/stdlib/brs/test/build && \
./rebuild.sh
```

**For compiler changes that still show stale behavior:**

```bash
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs
rm -rf ~/.m2/repository/com/example/kotlin-roku
./rebuild.sh
```

### DO NOT WASTE TIME

If you edited stdlib and `./rebuild.sh` shows "UP-TO-DATE" for stdlib tasks, your changes are NOT being built. Stop immediately and run nuclear clean. Do not:
- Try touching files to update timestamps
- Try `--no-build-cache` flags
- Try checking if klib has your changes
- Spend time debugging why cache is stale

Just run nuclear clean. It takes 30 seconds. Debugging cache issues takes hours.

### SSL Errors During Gradle Builds

If you see SSL certificate errors, handshake failures, or connection reset errors during Gradle builds, **STOP and ask the user to turn off ZScaler**. This is the cause 99% of the time. Do not try to debug SSL issues yourself.

## Quick Reference

| What Changed | Run This |
|--------------|----------|
| Compiler backend only | `./rebuild.sh` (if UP-TO-DATE, use compiler nuclear clean) |
| **Stdlib sources only** | **Stdlib nuclear clean command (see above)** |
| **Both compiler AND stdlib** | **Must do BOTH nuclear cleans - see below** |
| Everything + test app | `cd ../roku-test-app && ./rebuild-all.sh --all` |
| Plugin only (no compiler changes) | `cd ../roku-test-app && ./rebuild-all.sh --plugin --clean` |

### ⚠️ Changed BOTH Compiler AND Stdlib? (VERY COMMON)

When you edit BOTH compiler code AND stdlib code in the same session, you MUST do BOTH nuclear cleans. Running just one will leave stale code. Use this combined command:

```bash
# Step 1: Compiler nuclear clean
./gradlew --stop && \
rm -rf ~/.gradle/caches/build-cache-1/* && \
rm -rf compiler/ir/backend.brightscript/build && \
rm -rf compiler/cli-brs/build && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs

# Step 2: Stdlib nuclear clean
rm -rf libraries/stdlib/build && \
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs && \
rm -rf libraries/stdlib/brs/test/build

# Step 3: Rebuild everything with --no-build-cache
./gradlew :compiler:backend.brightscript:compileKotlin :compiler:cli-brs:fatJar :kotlin-stdlib:compileKotlinBrs :kotlin-stdlib:brsBrsJar publishBrsModulePublicationToMavenLocal --no-build-cache --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

**COMMON MISTAKE:** After changing compiler code, you run nuclear clean and rebuild. Then you change stdlib code and run `./rebuild.sh`. The stdlib shows UP-TO-DATE because Gradle's cache doesn't know the files changed. **Your stdlib changes are NOT built.** Always check if you edited stdlib files and run stdlib nuclear clean too.

### Verifying Your Changes Are Actually Built

After rebuilding, ALWAYS verify your changes are in the output before running tests:

```bash
# Check generated BrightScript for your changes
grep -n "your_pattern" libraries/stdlib/brs/test/build/stdlib-runtime/*.brs

# If the file doesn't exist or pattern not found, your changes are NOT built
# Run nuclear clean and rebuild again
```

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

**Important: Stale Device Logs**

The test script reads from the Roku's debug console (port 8085), which accumulates logs across multiple runs. If you see errors in test output that don't match the current generated code:

1. **Check the generated .brs files directly** - they are the source of truth:
   ```bash
   # View generated test files
   ls libraries/stdlib/brs/test/build/brs/source/

   # Search for specific issues
   grep -n "pattern" libraries/stdlib/brs/test/build/brs/source/*.brs
   ```

2. **Verify file timestamps** - ensure files were regenerated after your changes:
   ```bash
   ls -la libraries/stdlib/brs/test/build/brs/source/*.brs
   ```

3. **Reboot the Roku device** to clear accumulated logs, then re-run tests

4. **Check test-output.txt timestamps** - log entries have timestamps like `12-08 17:08:55`. Compare these to when you made changes to determine if logs are stale.

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
| Stdlib tests (source) | `libraries/stdlib/brs/test/kotlin/` |
| Stdlib tests (generated) | `libraries/stdlib/brs/test/build/brs/source/` |
| E2E test framework | `roku-test-app/src/brsMain/kotlin/tests/TestFramework.kt` |
| E2E test suites | `roku-test-app/src/brsMain/kotlin/tests/TestMain.kt` |

### Test Output

| Test Type | Report Location |
|-----------|-----------------|
| Compiler tests (HTML) | `compiler/ir/backend.brightscript/build/reports/tests/test/index.html` |
| Compiler tests (XML) | `compiler/ir/backend.brightscript/build/test-results/test/*.xml` |
| Stdlib tests (raw log) | `libraries/stdlib/brs/test/build/test-output.txt` |
| Stdlib tests (JSON) | `libraries/stdlib/brs/test/build/results.json` |
| E2E tests (JSON) | `roku-test-app/build/test-results/roku/results.json` |
| E2E tests (XML) | `roku-test-app/build/test-results/roku/results.xml` |
- If the logs are stale, you must send the Home keypress to the roku to kill the app. Do not suggest rebooting the device, and do not give up when you see old logs.