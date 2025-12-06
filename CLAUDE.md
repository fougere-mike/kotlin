# Kotlin BrightScript Backend Development Guide

## Project Overview

This is a fork of the Kotlin compiler that adds a BrightScript backend for Roku development. It compiles Kotlin source code to BrightScript (.brs) files that run on Roku devices.

## MANDATORY BUILD RULE

**ALWAYS run `./rebuild.sh` after ANY change to compiler or stdlib code.**

There are NO exceptions. Do not run individual gradle commands. The rebuild.sh script:
- Builds the compiler fat JAR
- Regenerates the stdlib klib (using the new compiler)
- Publishes everything to Maven Local in the correct order

If you skip steps or run commands out of order, you WILL get stale cache issues.

### The One Command You Need

```bash
# From the Kotlin directory - run this after ANY change
./rebuild.sh
```

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

### Solution

Run `./rebuild.sh` again. If issues persist:

```bash
# Clean Maven Local caches
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs
rm -rf ~/.m2/repository/com/example/kotlin-roku

# Then rebuild
./rebuild.sh
```

## Quick Reference

| What Changed | Run This |
|--------------|----------|
| Compiler backend | `./rebuild.sh` |
| Stdlib sources | `./rebuild.sh` |
| Both | `./rebuild.sh` |
| Everything + test app | `cd ../roku-test-app && ./rebuild-all.sh --all` |
| Plugin only (no compiler changes) | `cd ../roku-test-app && ./rebuild-all.sh --plugin --clean` |

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

### E2E Device Tests

Run tests on a physical Roku device. Requires device IP and password.

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
| E2E test framework | `roku-test-app/src/brsMain/kotlin/tests/TestFramework.kt` |
| E2E test suites | `roku-test-app/src/brsMain/kotlin/tests/TestMain.kt` |

### Test Output

| Test Type | Report Location |
|-----------|-----------------|
| Compiler tests (HTML) | `compiler/ir/backend.brightscript/build/reports/tests/test/index.html` |
| Compiler tests (XML) | `compiler/ir/backend.brightscript/build/test-results/test/*.xml` |
| E2E tests (JSON) | `roku-test-app/build/test-results/roku/results.json` |
| E2E tests (XML) | `roku-test-app/build/test-results/roku/results.xml` |
