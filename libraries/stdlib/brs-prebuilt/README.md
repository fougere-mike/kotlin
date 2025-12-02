# BrightScript Stdlib Pre-compiled Klib

This directory contains a pre-compiled klib artifact for the BrightScript stdlib.
This is used to bootstrap the Kotlin/BrightScript compiler when the bootstrap
Kotlin Gradle Plugin doesn't yet support the BRS target.

## Purpose

The Kotlin compiler requires a stdlib to compile user code, but the stdlib itself
needs a compiler to compile. For new targets like BrightScript, we solve this
chicken-and-egg problem by:

1. Compiling the BRS stdlib using the development compiler
2. Storing the resulting klib as a binary artifact
3. Using this pre-compiled klib during bootstrap until the upstream Kotlin
   Gradle Plugin supports BRS natively

## Files

- `kotlin-stdlib-brs.klib` - Pre-compiled stdlib klib (binary, checked into VCS)
- `build.gradle.kts` - Script to regenerate the klib
- `.gitattributes` - Marks .klib files as binary for proper VCS handling

## Regenerating the Klib

When the stdlib source changes, regenerate the klib:

```bash
# From the Kotlin root directory
./gradlew :libraries:stdlib:brs-prebuilt:regenerateKlib
```

This requires a working Kotlin/BRS compiler. The typical workflow is:

1. Build the compiler with the current pre-compiled klib
2. Make stdlib changes
3. Regenerate the klib using the newly built compiler
4. Commit the updated klib

## Integration

The main stdlib build uses this pre-compiled klib when:
- Building with the bootstrap Kotlin Gradle Plugin (which lacks BRS support)
- The `use.prebuilt.brs.stdlib` property is set to `true`

Once JetBrains adds native BRS support to the Kotlin Gradle Plugin (or we
maintain our own bootstrap), this mechanism becomes optional.
