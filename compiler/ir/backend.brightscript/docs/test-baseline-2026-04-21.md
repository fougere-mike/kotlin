# BRS Test Suite Baseline — 2026-04-21

Pre-A1 baseline captured on branch `feature/brightscript-backend-2.2.20` at `HEAD=987c03340f05` with the uncommitted IO Worker working tree in place. Purpose: establish a reference point so regressions introduced by A1 (or subsequent work) are detectable against a known state.

## Compiler golden tests

**Command:** `./run-compiler-tests.sh` (runs `./gradlew :compiler:backend.brightscript:test --tests "*GoldenFile*"`)

**Runtime:** 48s (incremental, everything except the BRS backend was UP-TO-DATE).

**Result:** `28 tests completed, 4 failed.`

**Failing tests:**

| Class | Test | Nature |
|---|---|---|
| `BrsControlFlowGoldenFileTests` | `conditionals` | golden mismatch |
| `BrsExpressionGoldenFileTests` | `nullability` | golden mismatch |
| `BrsExpressionGoldenFileTests` | `whenExpression` | golden mismatch |
| `BrsExpressionGoldenFileTests` | `safeCast` | golden mismatch |

**Root cause:** all 4 failures are the **same drift**. An uncommitted change to `brightscript/brs.ast/src/.../BrsRenderer.kt` adds an `isLiteralTrue()` helper and special-cases `else if true then X` → `else X` in the rendered if/else-if chain. The compiler output is strictly better BrightScript (same semantics, fewer redundant literal-true branches) — but the committed `.brs.txt` golden files were captured under the old renderer and therefore no longer match.

Concretely, every failure is of the form:
- Committed golden: `else if true then\n    __when_tmp0 = "..."\n    end if`
- Current output:   `else\n    __when_tmp0 = "..."\n    end if`

**Disposition:** the uncommitted renderer change is the correct direction, but the goldens were not regenerated at the same time. When A1 lands (or when renderer improvements are intentionally committed), run `./run-compiler-tests.sh --update` to refresh the 4 affected files, diff them for a sanity check, and commit the goldens in the same change as the renderer.

**Health check:** 24/28 (86%) passing on committed-goldens terms; 28/28 would be passing if goldens reflected the current renderer. No tests fail for reasons unrelated to this single renderer improvement.

## Stdlib build (no device)

**Command:** `bash libraries/stdlib/brs/test/run-tests.sh --build-only`.

**Runtime:** 27s.

**Result:** `BUILD SUCCESSFUL`. 24 BrightScript output files emitted. The compiler's full FIR→IR→BRS pipeline completes against the current working tree (including the uncommitted IO Worker stdlib additions — `IOWorkerRegistry.kt`, `IODispatcher.kt`, `CoroutineTask.kt`, `TaskPool.kt`, `WithContext.kt` — and their tests).

**Compile-time warnings (2, non-blocking):**
- `kotlin/coroutines/CoroutineTest.kt:146:41` and `:158:41` — `override fun resumeWith(r: ...)` differs from supertype `Continuation.resumeWith(result: ...)` parameter name. Would cause named-argument callers to break; current test doesn't use named arguments so not triggering. Cosmetic; fix by renaming `r` → `result`.

**Target configuration reported by compiler:** `minRokuOS=9.4, strictMode=false, debugMode=false`.

## Stdlib on-device tests

**Not run in this baseline pass.** Reason: executing `./run-stdlib-tests.sh` (without `--build-only`) would deploy the freshly-compiled tests to the LAN Roku device. Test deployment itself is fine and device is reachable — the concern is that this baseline is meant to capture pre-A1 state, and the uncommitted IO Worker stdlib additions would be implicitly exercised by any on-device test run. First device test run should happen as part of A1 where the feature's correctness is being validated, not as part of a baseline snapshot.

**Known preconditions:** LAN Roku device is reachable; `../roku-test-app/local.properties` has `roku.deviceIp` and `roku.devicePassword`.

## E2E (roku-test-app)

**Not run.** Not part of this baseline's scope; will be exercised during A1 device validation.

## Environment

- Host: Darwin 25.4.0, JDK per `gradle-daemon-jvm.properties`.
- Branch: `feature/brightscript-backend-2.2.20`.
- HEAD: `987c03340f05 Fix local class extraction and add constructor dependency tracking`.
- Working tree: modified/untracked per `git status --short` (IO Worker feature + 4 renderer/transformer files — see the plan's Audit finding #10 for the full list).
- Merge base with master: `f8465a28810666a505380d899f08b6aff27771c3` (2025-07-07).

## Regression detection procedure

To check whether A1 introduces a regression:

1. After A1 lands the IO Worker feature and re-runs `./run-compiler-tests.sh`, expect either:
   - 28/28 passing (if A1 included the regolden step), or
   - Only the same 4 tests failing with the same `else if true then` → `else` drift (if A1 did not include the regolden step).
2. Any **other** failing test after A1 is a regression from A1 itself and must be investigated before A1 is considered landed.
3. On-device stdlib tests: A1's first successful on-device run establishes the post-A1 baseline; pre-A1 on-device behavior is not known in this document.
