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
- `.klib-source-hash` - SHA-256 over the stdlib source tree at the time the klib was regenerated; used by `verifyKlib` to detect drift
- `build.gradle.kts` - Script to regenerate the klib and to verify it against current sources
- `.gitattributes` - Marks .klib files as binary for proper VCS handling

## Regenerating the Klib

When the stdlib source changes, regenerate the klib:

```bash
# From the Kotlin root directory
./gradlew dist
./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib
```

This requires a working Kotlin/BRS compiler. `regenerateKlib` rewrites both the
klib and `.klib-source-hash` atomically; the two files must be committed together:

```bash
git add libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib \
        libraries/stdlib/brs-prebuilt/.klib-source-hash
git commit
```

The typical workflow is:

1. Build the compiler with the current pre-compiled klib (`./rebuild.sh`).
2. Make stdlib changes under `libraries/stdlib/brs/` or `libraries/stdlib/brs-actual/`.
3. Regenerate the klib using the newly built compiler.
4. Commit the updated klib **and** hash file.

## Drift Detection

`./gradlew :kotlin-stdlib-brs-prebuilt:verifyKlib` (which is wired into `check`)
computes a fresh SHA-256 over the stdlib sources and compares it to
`.klib-source-hash`. If they differ, the build fails with an explicit
remediation message. This prevents the silent-corruption failure mode where a
contributor edits stdlib sources, forgets to regenerate the klib, and produces
a branch whose downstream consumers link against stale bytecode.

The hash is:

- A SHA-256 over sorted `(relativePath, SHA-256(content))` tuples for every
  `.kt` and `.kts` file under `brs/builtins`, `brs/runtime`, `brs/src`, and
  `brs-actual/src`.
- Deterministic across platforms: paths use POSIX separators, content is
  normalized to LF line endings before hashing.
- Sensitive to added, modified, and deleted files.

The hashed source set deliberately matches the set `regenerateKlib` passes to
the compiler, so the hash summarizes "what went into the klib."

## Integration

The main stdlib build uses this pre-compiled klib when:
- Building with the bootstrap Kotlin Gradle Plugin (which lacks BRS support)
- The `use.prebuilt.brs.stdlib` property is set to `true`

Once JetBrains adds native BRS support to the Kotlin Gradle Plugin (or we
maintain our own bootstrap), this mechanism becomes optional.
