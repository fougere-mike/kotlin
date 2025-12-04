# Kotlin BrightScript Backend Development Guide

## Project Overview

This is a fork of the Kotlin compiler that adds a BrightScript backend for Roku development. It compiles Kotlin source code to BrightScript (.brs) files that run on Roku devices.

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

## Build Commands

### Full Rebuild (Recommended)

```bash
./rebuild.sh
```

This script runs all steps in the correct order with proper flags.

### Individual Tasks (Order Matters!)

```bash
# 1. Always build fat JAR first for compiler changes
./gradlew :compiler:cli-brs:fatJar --no-configuration-cache

# 2. Regenerate stdlib if stdlib sources changed (requires step 1)
./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib --no-configuration-cache

# 3. Publish compiler to Maven Local
./gradlew :compiler:cli-brs:publishToMavenLocal --no-configuration-cache

# 4. Publish Gradle plugin
./gradlew :kotlin-gradle-plugin:publishToMavenLocal --no-configuration-cache

# 5. Publish stdlib (klib + brs-runtime)
./gradlew :kotlin-stdlib:publishBrsModulePublicationToMavenLocal --no-configuration-cache
```

## CRITICAL: Cache Invalidation

### Build Dependency Chain

```
Compiler Sources → Fat JAR → Stdlib klib → Maven Local → kotlin-roku plugin → roku-test-app
```

### When Making Compiler Changes

Changes in `compiler/ir/backend.brightscript/` or `compiler/cli/cli-brs/`:

1. Rebuild fat JAR: `./gradlew :compiler:cli-brs:fatJar --no-configuration-cache`
2. Republish: `./gradlew :compiler:cli-brs:publishToMavenLocal --no-configuration-cache`
3. If the change affects how stdlib is compiled, also regenerate klib

### When Making Stdlib Changes

Changes in `libraries/stdlib/brs/` or `libraries/stdlib/brs-actual/`:

1. Regenerate klib: `./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib --no-configuration-cache`
2. Republish: `./gradlew :kotlin-stdlib:publishBrsModulePublicationToMavenLocal --no-configuration-cache`

### Testing with roku-test-app

From the `roku-test-app` directory:

```bash
# After compiler changes:
./rebuild-all.sh --compiler --clean

# After plugin changes only:
./rebuild-all.sh --plugin --clean

# Full rebuild of everything:
./rebuild-all.sh --all

# Test the app on Roku device:
./gradlew installRoku
```

## Troubleshooting

### Symptoms of Stale Cache

- Changes not reflected in compiled output
- Old error messages appearing
- Behavior not matching source code
- "Class not found" or missing symbol errors for recently added code

### Manual Cache Cleaning

```bash
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-compiler-brs
rm -rf ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs
rm -rf ~/.m2/repository/com/example/kotlin-roku
```

### Force Dependency Refresh

```bash
./gradlew build --refresh-dependencies
```

### Gradle Configuration Cache Issues

Always use `--no-configuration-cache` for publishing tasks. The rebuild.sh script includes this flag.

## Common Pitfalls

1. **Forgetting to rebuild fat JAR**: The stdlib klib is built using the compiler fat JAR. If you change the compiler but don't rebuild the JAR, the klib regeneration uses the old compiler.

2. **Not regenerating klib after stdlib changes**: The klib in `brs-prebuilt/` is checked into VCS and won't auto-regenerate. You must explicitly run `regenerateKlib`.

3. **SNAPSHOT caching**: Maven caches SNAPSHOT versions aggressively. Always clean Maven local when switching between builds or use `--refresh-dependencies`.

4. **Order of operations**: The order is `fatJar → regenerateKlib → publish`. Never skip steps or do them out of order.

5. **Configuration cache**: Gradle's configuration cache can hold stale state. Use `--no-configuration-cache` for all publish operations.

6. **Forgetting to publish**: Building alone doesn't update Maven local. You must run the publish tasks for downstream projects to see changes.

## Quick Reference

| What Changed | Run This |
|--------------|----------|
| Compiler backend | `./gradlew :compiler:cli-brs:fatJar :compiler:cli-brs:publishToMavenLocal --no-configuration-cache` |
| Stdlib sources | `./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib :kotlin-stdlib:publishBrsModulePublicationToMavenLocal --no-configuration-cache` |
| Both | `./rebuild.sh` |
| Everything + test | `cd ../roku-test-app && ./rebuild-all.sh --all` |
