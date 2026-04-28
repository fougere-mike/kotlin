# BRS_STATIC_OVERLOAD Cross-File Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend `FirBrsStaticOverloadFileChecker` to catch `@BrsStatic` overload collisions across files in the same package — not just within a single file.

**Architecture:** The existing `FirBrsStaticOverloadFileChecker` (68 LOC, FirFileChecker) is extended in place to walk `FirPackageMemberScope` after collecting local @BrsStatic functions. Each file reports only on its own declarations; peer files report on theirs — no double-reporting. Reuses `BRS_STATIC_OVERLOAD` diagnostic id, same message format, same suppression name. Mirrors `FirBrsNameClashFileTopLevelDeclarationsChecker` exactly.

**Tech Stack:** Kotlin/FIR compiler internals — `FirPackageMemberScope`, `FirSimpleFunction`, `getAnnotationByClassId`, `AbstractDiagnosticCollector`, JUnit 5 `@Test`.

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/brs/checkers/declaration/FirBrsStaticOverloadFileChecker.kt` | **Modify** | Add cross-file package-scope walk to `check()` |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTopLevelOverloadRejected.kt` | **Create** | Positive: two-file same-package collision |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTripleOverloadRejected.kt` | **Create** | Positive: three-file same-package collision |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileMixedSameFileAndCrossFileRejected.kt` | **Create** | Positive: 2 local + 1 cross-file = count 3 |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileDifferentPackageOk.kt` | **Create** | Negative: different packages, no collision |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileOneAnnotatedOk.kt` | **Create** | Negative: peer not @BrsStatic, no collision |
| `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileSuppressedOverload.kt` | **Create** | Suppression: per-site suppress across files |
| `compiler/fir/checkers/checkers.brs/test/org/jetbrains/kotlin/fir/analysis/brs/BrsDiagnosticTests.kt` | **Modify** | Add 6 `@Test` methods for new fixtures |
| `memory/project_brs_fir_diagnostics_state.md` | **Modify** | Mark cross-file gap as resolved; remove stale CONFLICT entry |

---

## Task 1: Write the 6 failing fixture files

**Reference:** existing single-file fixtures are in `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/`. The `<!BRS_STATIC_OVERLOAD!>functionName<!>` syntax marks the diagnostic position. Multi-file fixtures use `// FILE: filename.kt` dividers (parsed by `AbstractBrsDiagnosticTest.splitMultiFile`). The `import kotlin.brs.BrsStatic` statement is needed in every file that uses `@BrsStatic`.

**Files:**
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTopLevelOverloadRejected.kt`
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTripleOverloadRejected.kt`
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileMixedSameFileAndCrossFileRejected.kt`
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileDifferentPackageOk.kt`
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileOneAnnotatedOk.kt`
- Create: `compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileSuppressedOverload.kt`

- [ ] **Step 1: Create `crossFileTopLevelOverloadRejected.kt`**

Two files, same package, each with `@BrsStatic fun foo()`. Each file reports one diagnostic with count=2.

```kotlin
// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>foo<!>(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>foo<!>(): Int = 1
```

- [ ] **Step 2: Create `crossFileTripleOverloadRejected.kt`**

Three files, same package, each with `@BrsStatic fun greet()`. Each reports count=3.

```kotlin
// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): Int = 1

// FILE: c.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): Boolean = true
```

- [ ] **Step 3: Create `crossFileMixedSameFileAndCrossFileRejected.kt`**

File A has two local `@BrsStatic fun process()` overloads; file B has one. Total count = 3. A reports 2 diagnostics, B reports 1.

```kotlin
// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: String): String = x

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: Int): Int = x

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: Boolean): Boolean = x
```

- [ ] **Step 4: Create `crossFileDifferentPackageOk.kt`**

Two files in different packages with the same function name. No diagnostics expected.

```kotlin
// FILE: a.kt
package my.app.alpha

import kotlin.brs.BrsStatic

@BrsStatic
fun foo(): String = "a"

// FILE: b.kt
package my.app.beta

import kotlin.brs.BrsStatic

@BrsStatic
fun foo(): Int = 1
```

- [ ] **Step 5: Create `crossFileOneAnnotatedOk.kt`**

File A has `@BrsStatic fun foo()`, file B has plain `fun foo()` (no annotation). No diagnostics — unannotated peers don't count.

```kotlin
// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun foo(): String = "a"

// FILE: b.kt
package my.app

fun foo(): Int = 1
```

- [ ] **Step 6: Create `crossFileSuppressedOverload.kt`**

Two files, both `@BrsStatic fun bar()`. File A suppresses, file B does not. Only B reports a diagnostic (count=2); A is silent.

```kotlin
// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@Suppress("BRS_STATIC_OVERLOAD")
@BrsStatic
fun bar(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>bar<!>(): Int = 1
```

---

## Task 2: Register the 6 failing @Test methods

**Files:**
- Modify: `compiler/fir/checkers/checkers.brs/test/org/jetbrains/kotlin/fir/analysis/brs/BrsDiagnosticTests.kt`

Add 6 new `@Test` methods after the last existing `testBrsStatic*` method (after `testBrsStaticSuppressedOneOfOverload`, line 607, before the closing `}`).

- [ ] **Step 1: Add @Test methods**

In `BrsDiagnosticTests.kt`, insert before the closing `}` of the class (after line 607):

```kotlin
    @Test
    fun testBrsStaticCrossFileTopLevelOverloadRejected() {
        runTest("brsStaticValidation/crossFileTopLevelOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileTripleOverloadRejected() {
        runTest("brsStaticValidation/crossFileTripleOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileMixedSameFileAndCrossFileRejected() {
        runTest("brsStaticValidation/crossFileMixedSameFileAndCrossFileRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileDifferentPackageOk() {
        runTest("brsStaticValidation/crossFileDifferentPackageOk.kt")
    }

    @Test
    fun testBrsStaticCrossFileOneAnnotatedOk() {
        runTest("brsStaticValidation/crossFileOneAnnotatedOk.kt")
    }

    @Test
    fun testBrsStaticCrossFileSuppressedOverload() {
        runTest("brsStaticValidation/crossFileSuppressedOverload.kt")
    }
```

- [ ] **Step 2: Run the new tests to confirm they FAIL**

```bash
./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests.testBrsStaticCrossFile*" --no-configuration-cache -Dorg.gradle.dependency.verification=off 2>&1 | tail -40
```

Expected: 6 test failures. The positive fixtures will fail because the checker doesn't yet report cross-file diagnostics; the negative/suppression fixtures will fail because the harness detects unexpected diagnostics or missing expected ones.

---

## Task 3: Implement cross-file detection in FirBrsStaticOverloadFileChecker

**Files:**
- Modify: `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/brs/checkers/declaration/FirBrsStaticOverloadFileChecker.kt`

Reference: `FirBrsNameClashFileTopLevelDeclarationsChecker.kt` lines 58–61 for scope acquisition; lines 65 and 105–125 for the local-dedup and `!origin.fromSource` filter pattern.

The key imports to add:
- `org.jetbrains.kotlin.fir.packageFqName`
- `org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope`
- `org.jetbrains.kotlin.fir.scopes.impl.PACKAGE_MEMBER`
- `org.jetbrains.kotlin.fir.symbols.SymbolInternals`
- `org.jetbrains.kotlin.name.Name`

- [ ] **Step 1: Replace the checker implementation**

Replace the entire content of `FirBrsStaticOverloadFileChecker.kt` with:

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope
import org.jetbrains.kotlin.fir.scopes.impl.PACKAGE_MEMBER
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Reports `BRS_STATIC_OVERLOAD` when two or more top-level `@BrsStatic` functions
 * in the same package share a Kotlin name — whether they are in the same file or
 * different files. `@BrsStatic` compiles to a flat, unmangled BrightScript function
 * name, so overloads collide at runtime (BrightScript is case-insensitive and has
 * no module/file scope for top-level functions).
 *
 * Walks `FirPackageMemberScope` so cross-file same-package collisions are caught
 * (same pattern as `FirBrsNameClashFileTopLevelDeclarationsChecker`). Each file's
 * checker invocation reports only on its OWN declarations; peer files report on
 * theirs. This avoids double-reporting.
 *
 * Skips:
 *  - Peer functions without `@BrsStatic` (only annotated-vs-annotated collisions matter).
 *  - Library-origin peers (`!origin.fromSource`).
 *  - Fake-source declarations (compiler-synthesized).
 *
 * The class-scope counterpart lives in `FirBrsStaticClassChecker`; this checker
 * handles only file-level top-level functions.
 *
 * Defense-in-depth: the IR-phase walker in `BrsCompiler.validateBrsStaticAnnotations`
 * still runs for callers that bypass FIR (klib-deserialized dependencies).
 */
object FirBrsStaticOverloadFileChecker : FirFileChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session

        // --- Phase 1: collect this file's @BrsStatic functions ---
        val localByName = mutableMapOf<String, MutableList<FirSimpleFunction>>()
        val localDeclarations = mutableSetOf<FirDeclaration>()

        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            if (member !is FirSimpleFunction) continue
            if (member.source?.kind is KtFakeSourceElementKind) continue
            if (member.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null) continue
            localByName.getOrPut(member.name.asString()) { mutableListOf() }.add(member)
            localDeclarations += member
        }

        if (localByName.isEmpty()) return

        // --- Phase 2: probe package scope for cross-file @BrsStatic peers ---
        val packageScope: FirPackageMemberScope =
            context.sessionHolder.scopeSession.getOrBuild(declaration.packageFqName to session, PACKAGE_MEMBER) {
                FirPackageMemberScope(declaration.packageFqName, context.sessionHolder.session)
            }

        val peerCount = mutableMapOf<String, Int>()
        for (name in localByName.keys) {
            packageScope.processFunctionsByName(Name.identifier(name)) { peerSymbol ->
                @OptIn(SymbolInternals::class)
                val peerFir = peerSymbol.fir
                if (peerFir in localDeclarations) return@processFunctionsByName  // own decl, already counted
                if (!peerSymbol.origin.fromSource) return@processFunctionsByName  // skip library peers
                if (peerFir.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null)
                    return@processFunctionsByName  // unannotated peer doesn't count
                peerCount[name] = (peerCount[name] ?: 0) + 1
            }
        }

        // --- Phase 3: report on local functions whose total count >= 2 ---
        for ((name, localList) in localByName) {
            val total = localList.size + (peerCount[name] ?: 0)
            if (total < 2) continue
            val countRendered = total.toString()
            for (func in localList) {
                if (func.isSuppressedByAnnotation("BRS_STATIC_OVERLOAD")) continue
                reporter.reportOn(func.source, FirBrsErrors.BRS_STATIC_OVERLOAD, name, countRendered)
            }
        }
    }
}

private fun FirDeclaration.isSuppressedByAnnotation(diagnosticName: String): Boolean {
    val suppressed = AbstractDiagnosticCollector.getDiagnosticsSuppressedForContainer(this) ?: return false
    return diagnosticName in suppressed ||
        AbstractDiagnosticCollector.SUPPRESS_ALL_ERRORS in suppressed
}
```

- [ ] **Step 2: Run the new tests to confirm they now PASS**

```bash
./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests.testBrsStaticCrossFile*" --no-configuration-cache -Dorg.gradle.dependency.verification=off 2>&1 | tail -40
```

Expected: 6 tests PASS.

- [ ] **Step 3: Run the full BrsDiagnosticTests suite to confirm no regressions**

```bash
./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests*" --no-configuration-cache -Dorg.gradle.dependency.verification=off 2>&1 | tail -40
```

Expected: all tests PASS (was 114, now 120).

- [ ] **Step 4: Commit the checker change and all fixtures together**

```bash
git add \
  compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/brs/checkers/declaration/FirBrsStaticOverloadFileChecker.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTopLevelOverloadRejected.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileTripleOverloadRejected.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileMixedSameFileAndCrossFileRejected.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileDifferentPackageOk.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileOneAnnotatedOk.kt \
  compiler/testData/diagnostics/testsWithBrsStdLib/brsStaticValidation/crossFileSuppressedOverload.kt \
  compiler/fir/checkers/checkers.brs/test/org/jetbrains/kotlin/fir/analysis/brs/BrsDiagnosticTests.kt
git commit -m "BRS_STATIC_OVERLOAD: cross-file same-package detection via FirPackageMemberScope (B2)"
```

---

## Task 4: Full build and golden-file regression check

- [ ] **Step 1: Run full compiler test suite**

```bash
./run-compiler-tests.sh 2>&1 | tail -20
```

Expected: all tests pass (FIR diagnostics + golden-file tests). If golden-file tests fail, the checker has accidentally changed code-generation — investigate before proceeding.

- [ ] **Step 2: Rebuild stdlib to confirm no new BRS_STATIC_OVERLOAD noise**

```bash
./rebuild.sh 2>&1 | grep -i "brs_static_overload\|error\|warning" | head -20
```

Expected: zero `BRS_STATIC_OVERLOAD` mentions. Stdlib has zero `@BrsStatic` usages, so the package-scope walk is a no-op for all stdlib packages.

---

## Task 5: Memory housekeeping

**Files:**
- Modify: `memory/project_brs_fir_diagnostics_state.md` (under `/Users/Mike.Fougere/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/`)

- [ ] **Step 1: Update BRS_STATIC_OVERLOAD section in memory**

In the `### 11. BRS_STATIC_OVERLOAD` section, make these changes:

1. In `**Deferred follow-ups:**`, change:
   ```
   - Cross-file overload detection: same package, two files, both with `@BrsStatic fun foo()` would collide at runtime but isn't caught (file-local scope). Symmetric with the now-resolved BRS_NAME_CASE_CLASH cross-file gap.
   ```
   to:
   ```
   _(none)_
   ```

2. Add after the KDoc-update sentence (after "KDoc updated to document FIR as primary path and IR as fallback."):
   ```
   **Cross-file scope (resolved):** file checker walks `FirPackageMemberScope`; each file reports only on its own decls; library-origin peers and unannotated peers excluded. Same `FirPackageMemberScope` pattern as `FirBrsNameClashFileTopLevelDeclarationsChecker`.
   ```

- [ ] **Step 2: Remove stale BRS_SCENEGRAPH_FIELD_CONFLICT deferred entry**

In the `## Deferred follow-ups` section at the bottom of the file, remove this entry:
```
- `BRS_SCENEGRAPH_FIELD_CONFLICT` — two `@SG*Field` annotations on one property (currently pins two `BRS_SCENEGRAPH_FIELD_TYPE` errors; fixture `conflictingAnnotationsBothMismatch.kt` documents this). Trivial ~20 LOC addition to existing `FirBrsSceneGraphFieldTypeChecker`.
```

Reason: the existing checker already short-circuits on `sgFieldAnnotations.size >= 2`, reporting one `BRS_SCENEGRAPH_FIELD_CONFLICT` and returning early. The fixture confirms this. The deferred entry was stale as of the landing of diagnostic #4.

- [ ] **Step 3: Save the memory file**

`memory/` lives under `/Users/Mike.Fougere/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/` — NOT in the git repo. No git commit needed. The Write tool saves it directly.

---

## Verification Summary

| Check | Command | Expected |
|-------|---------|----------|
| 6 new cross-file tests | `./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*testBrsStaticCrossFile*" ...` | 6 PASS |
| Full FIR diagnostic suite | `./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests*" ...` | 120 PASS (was 114) |
| Full compiler test suite | `./run-compiler-tests.sh` | All pass, 0 regressions |
| Stdlib noise check | `./rebuild.sh` + grep | 0 new BRS_STATIC_OVERLOAD diagnostics |

## Out of Scope

- Class-scope cross-file detection — impossible (a class is single-file)
- Inherited-member name-case clashes — separate diagnostic, separate plan
- IR extractor parity for `@BrsOnChange` — codegen fix, not FIR
- Legacy `@BrsField(type = "xyz")` literal validation — zero users, deferred
