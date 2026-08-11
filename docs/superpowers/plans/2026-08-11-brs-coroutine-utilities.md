# BRS Coroutine Utilities Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Working `awaitAll` & friends on the BRS backend: a real completion protocol on `JobImpl`, `join`/`await`/`awaitAll`/`joinAll`, structured concurrency (`coroutineScope`, hierarchy, supervisor component root), `withTimeout`/`withTimeoutOrNull` — preceded by fixes for two live codegen defects (`&&`/`||` short-circuit, expression-position try/catch).

**Architecture:** Instance-held completion callbacks on `JobImpl` (two kinds: terminal + cancel-request, both disposable); every stdlib suspend utility is tail-delegating (fast paths, then ONE `suspendCoroutineUninterceptedOrReturn` whose last expression is `parked.finish()`); resumption goes through `ContinuationInterceptor` so both pumping regimes (component PumpScheduler, main-thread `runBlocking`/`runPumping`) are serviced by the same queue. Spec: `docs/superpowers/plans/2026-08-11-brs-coroutine-utilities-design.md` (read it first).

**Tech Stack:** Kotlin compiler fork (BRS backend, K2/FIR + IR lowerings), BRS stdlib klib, physical Roku device tests.

## Global Constraints

- **Build:** `./rebuild.sh` is THE build command. Never run individual gradlew tasks (exceptions listed in CLAUDE.md: golden tests via `./run-compiler-tests.sh`, FIR regen).
- **Goldens:** `./run-compiler-tests.sh` (all pass = 57 + new); after intentional output changes: `./run-compiler-tests.sh --update` then REVIEW the diffs before committing.
- **Device:** `export ROKU_DEVICE_IP=192.168.1.125 ROKU_PASSWORD=pass`. Console preflight before any device run: `echo | nc -w 3 192.168.1.125 8085` — if it prints `Console connection is already in use`, STOP and ask Mike to disconnect the IDE's Roku console.
- **Stdlib device suite:** `./run-stdlib-tests.sh` — 411 tests/40 suites green baseline; must not drop.
- **E2E suite:** `cd ../roku-test-app && ./run-device-tests.sh` — 33 tests/6 suites (+3 xtests) green baseline; must not drop.
- **rebuild.sh step 7** (kotlin-test-brs compile check) must stay green every phase.
- **Tail-delegation rule (stdlib code):** stdlib suspend functions get NO state machine. Every suspend utility: synchronous fast-paths first, then ONE `suspendCoroutineUninterceptedOrReturn` as the LAST statement; inside its block, the LAST expression must be the value / `parked.finish()`. NO code after the intrinsic call. After implementing any stdlib suspend function, eyeball its generated `.brs` in `libraries/stdlib/brs/test/build/brs/source/` (or the klib regen output) for accidental non-tail suspension.
- **Until Task 2 lands:** do not write `x != null && x.method()`-shaped code in Kotlin targeting BRS (the miscompile Phase 0 fixes). After Task 2, `&&`/`||` are safe everywhere.
- **BRS has no short-circuit `and`/`or`** (device truth per repo; Task 3 probes and documents).
- **Single-threaded per component:** no atomics/locks anywhere; plain booleans/lists are correct.
- **No cross-component Job sharing** (per-GetGlobalAA singletons); all tests keep jobs within one component/driver context.
- **Commit per task** on `feature/brightscript-backend-2.2.20`, matching existing style (lowercase prefix: `brs:`, `stdlib:`, `docs:`, `roku-test-app:` in that repo). End commit messages with the Co-Authored-By line for Claude.
- **Two repos:** Kotlin fork = `/Users/Mike.Fougere/Documents/newt/git/Kotlin`; test app = `/Users/Mike.Fougere/Documents/newt/git/roku-test-app` (separate git repo — commit separately).

## File Map (who owns what)

| File | Role |
|---|---|
| `compiler/.../transformers/irToBrs/IrExpressionToBrsTransformer.kt` | ANDAND/OROR emission fix (Task 1) |
| `compiler/.../transformers/irToBrs/brsTransformerUtils.kt` | `isEffectFreeShortCircuitOperand` (Task 1) |
| `compiler/.../lower/BrsTryExpressionLowering.kt` (new) | expression-position try/catch lowering (Task 3) |
| `compiler/.../lower/BrsLoweringPhases.kt` | register new lowering (Task 3) |
| `compiler/testData/codegen/brs/expressions/shortCircuit.kt(.brs.txt)` | golden (Task 1) |
| `compiler/testData/codegen/brs/controlFlow/tryCatchExpression.kt(.brs.txt)` | golden (Task 3) |
| `compiler/ir/backend.brightscript/test/.../BrsGoldenFileTests.kt` | golden registrations |
| `libraries/stdlib/brs/src/kotlin/coroutines/cancellation/CancellationException.kt` (new) | CE type (Task 5) |
| `libraries/stdlib/brs/src/kotlin/coroutines/Job.kt` | Job/JobImpl/Deferred rework + jobImplOf (Tasks 5, 6) |
| `libraries/stdlib/brs/src/kotlin/coroutines/ParkedContinuation.kt` (new) | park helper + ensureActive/isActive + registerCallerCancel (Task 6) |
| `libraries/stdlib/brs/src/kotlin/coroutines/Await.kt` (new) | awaitAll/joinAll (Task 8) |
| `libraries/stdlib/brs/src/kotlin/coroutines/Delay.kt` | delay/yield retrofit (Task 7) |
| `libraries/stdlib/brs/src/kotlin/coroutines/builders/Builders.kt` | launch/async hierarchy attach (Task 10) |
| `libraries/stdlib/brs/src/kotlin/coroutines/Scopes.kt` (new) | coroutineScope + scope-block engine (Task 11) |
| `libraries/stdlib/brs/src/kotlin/coroutines/CoroutineScope.kt` | delete fake inline coroutineScope (Task 11) |
| `libraries/stdlib/brs/src/kotlin/coroutines/builders/WithContext.kt` | non-IO path rework (Task 11) |
| `libraries/stdlib/brs/src/kotlin/coroutines/builders/Timeout.kt` (new) | withTimeout/OrNull + TCE (Task 13) |
| `libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt` | supervisor root (Task 10) |
| `libraries/stdlib/brs/test/kotlin/standard/ShortCircuitTest.kt` (new) | device semantics tests (Task 2) |
| `libraries/stdlib/brs/test/kotlin/standard/TryExpressionTest.kt` (new) | device semantics tests (Task 4) |
| `libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt` (new) | Tasks 5, 9 |
| `libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt` (new) | Tasks 6–7 suspend-path tests |
| `libraries/stdlib/brs/test/kotlin/coroutines/AwaitAllTest.kt` (new) | Task 8 |
| `libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt` (new) | Tasks 10, 11, 13 |
| `libraries/stdlib/brs/test/kotlin/TestMain.kt` | register new suites |
| `../roku-test-app/components/fixtures/CoroutineUtilProbe.kt` (new) | E2E fixture (Task 12) |
| `../roku-test-app/src/brsTest/kotlin/tests/CoroutineUtilityTests.kt` (new) | E2E suite (Tasks 12, 14, 15) |
| `../roku-test-app/src/brsTest/kotlin/tests/TestMain.kt` | register suite (Task 12) |
| `../roku-test-app/components/ShelfView/ShelfView.kt` | concurrent-fetch demo (Task 15) |
| `CLAUDE.md`, memory dir | docs (Task 16) |

---

# Phase 0: codegen fixes

### Task 1: `&&`/`||` short-circuit emission fix + golden

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/brsTransformerUtils.kt` (add predicate)
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrExpressionToBrsTransformer.kt:3215-3231` (visitWhen ANDAND/OROR arms)
- Create: `compiler/testData/codegen/brs/expressions/shortCircuit.kt` + generated `.brs.txt`
- Modify: `compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/BrsGoldenFileTests.kt` (register)

**Interfaces:**
- Produces: `internal fun isEffectFreeShortCircuitOperand(expression: IrExpression): Boolean` in `brsTransformerUtils.kt` (package `org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs`). Task 4 does NOT depend on it; nothing else consumes it.
- Background: Kotlin IR represents `a && b` as `IrWhen(origin=ANDAND) { branch(cond=a, result=b); else -> false }` and `a || b` as `IrWhen(origin=OROR) { branch(cond=a, result=true); else -> b }`. `BrsWhenExpressionLowering` deliberately skips these origins (line ~269); the expression transformer pattern-matches them into bare `and`/`or` — which is only correct when the RHS is effect-free, because BrightScript evaluates both operands.
- The hoisting channel already exists: `genCtx.addHoistedStatement(...)` inside the expression transformer; consumers are `IrStatementToBrsTransformer.visitWhen` (line ~506, `takeHoistedStatements()` per branch condition) and `visitWhileLoop` (line ~584, with per-iteration re-evaluation machinery). The ELVIS handling (IrExpressionToBrsTransformer.kt:3699+) is the in-tree precedent.

- [ ] **Step 1: Write the golden source (the failing test)**

Create `compiler/testData/codegen/brs/expressions/shortCircuit.kt`:

```kotlin
// Short-circuit semantics: Kotlin && / || must not evaluate an effectful RHS
// when the LHS decides the result. BRS `and`/`or` evaluate both operands, so
// impure RHS operands are hoisted to a guarded temp; pure operands keep the
// compact `and`/`or` form.
class Holder(val flag: Boolean) {
    fun check(): Boolean = flag
}

fun pureOperands(a: Boolean, b: Boolean): Boolean {
    return a && b || !a
}

fun nullGuardAnd(h: Holder?): Boolean {
    return h != null && h.check()
}

fun nullGuardOr(h: Holder?): Boolean {
    return h == null || h.check()
}

fun guardInIf(h: Holder?): String {
    if (h != null && h.check()) return "yes"
    return "no"
}

fun guardInWhile(items: ArrayList<Int>): Int {
    var n = 0
    while (items.size > 0 && items[0] > 0) {
        items.removeAt(0)
        n = n + 1
    }
    return n
}

fun mixedChain(h: Holder?, enabled: Boolean): Boolean {
    return enabled && h != null && h.check()
}
```

Register in `BrsGoldenFileTests.kt` next to the other `expressions/` entries:

```kotlin
    @Test
    fun shortCircuit() = runTest("expressions/shortCircuit.kt")
```

- [ ] **Step 2: Generate the CURRENT (broken) golden and verify it shows the bug**

Run: `./run-compiler-tests.sh --update`, then Read `compiler/testData/codegen/brs/expressions/shortCircuit.brs.txt`.
Expected: `nullGuardAnd` contains `h <> invalid and h.check…` (bare `and` with a method call RHS) — the miscompile, captured. Do NOT commit yet.

- [ ] **Step 3: Add the purity predicate**

Append to `brsTransformerUtils.kt`:

```kotlin
/**
 * True when [expression] can be evaluated unconditionally with no side effects
 * and no possibility of a runtime crash — the requirement for keeping a Kotlin
 * `&&`/`||` operand inside a bare BrightScript `and`/`or`, which evaluates BOTH
 * operands. Anything not on this whitelist (calls, member/indexed access,
 * safe-calls) gets the hoisted guarded-temp form instead.
 */
internal fun isEffectFreeShortCircuitOperand(expression: IrExpression): Boolean {
    return when (expression) {
        is IrConst -> true
        is IrGetValue -> true
        is IrCall -> {
            val pureOrigin = when (expression.origin) {
                IrStatementOrigin.EQEQ, IrStatementOrigin.EXCLEQ,
                IrStatementOrigin.LT, IrStatementOrigin.GT,
                IrStatementOrigin.LTEQ, IrStatementOrigin.GTEQ,
                IrStatementOrigin.EXCL -> true
                else -> false
            }
            if (!pureOrigin) return false
            val receiver = expression.dispatchReceiver
            if (receiver != null && !isEffectFreeShortCircuitOperand(receiver)) return false
            for (i in 0 until expression.valueArgumentsCount) {
                val arg = expression.getValueArgument(i) ?: continue
                if (!isEffectFreeShortCircuitOperand(arg)) return false
            }
            true
        }
        is IrWhen -> {
            val sc = expression.origin == IrStatementOrigin.ANDAND ||
                expression.origin == IrStatementOrigin.OROR
            sc && expression.branches.all {
                isEffectFreeShortCircuitOperand(it.condition) &&
                    isEffectFreeShortCircuitOperand(it.result)
            }
        }
        else -> false
    }
}
```

(Adjust imports to the file's existing style; `IrStatementOrigin`, `IrConst`, `IrGetValue`, `IrCall`, `IrWhen` come from `org.jetbrains.kotlin.ir.expressions`.)

- [ ] **Step 4: Rewrite the ANDAND/OROR arms in `visitWhen`**

Replace the two arms at `IrExpressionToBrsTransformer.kt:3217-3229` with:

```kotlin
        when (expression.origin) {
            IrStatementOrigin.ANDAND -> {
                // a && b is represented as: if (a) b else false
                val rhsExpr = expression.branches[0].result
                if (isEffectFreeShortCircuitOperand(rhsExpr)) {
                    val left = expression.branches[0].condition.accept(this, data)
                    val right = rhsExpr.accept(this, data)
                    return BrsBinaryOp(left, BrsBinaryOperator.AND, right)
                }
                // BrightScript `and` evaluates both operands; an effectful RHS
                // must only run when the LHS is true. Hoist:
                //   __sc_tmpN = false : if <lhs> then __sc_tmpN = <rhs>
                return hoistShortCircuit(expression.branches[0].condition, rhsExpr, false, data)
            }
            IrStatementOrigin.OROR -> {
                // a || b is represented as: if (a) true else b
                val rhsExpr = expression.branches[1].result
                if (isEffectFreeShortCircuitOperand(rhsExpr)) {
                    val left = expression.branches[0].condition.accept(this, data)
                    val right = rhsExpr.accept(this, data)
                    return BrsBinaryOp(left, BrsBinaryOperator.OR, right)
                }
                //   __sc_tmpN = true : if not (<lhs>) then __sc_tmpN = <rhs>
                return hoistShortCircuit(expression.branches[0].condition, rhsExpr, true, data)
            }
            else -> { /* fall through to default handling */ }
        }
```

Add the private helper in the same class:

```kotlin
    /**
     * Emits the guarded-temp form of a short-circuit operator whose RHS is not
     * effect-free. The temp declaration and guard `if` go through the hoisted-
     * statement channel (the ELVIS precedent): statement positions flush them
     * before the current statement; if/while condition positions consume them
     * via takeHoistedStatements(), while-loops re-evaluating per iteration.
     * RHS-internal hoists (nested impure short-circuits) must run only when
     * the guard passes, so they are captured and moved INSIDE the guard body.
     */
    private fun hoistShortCircuit(
        lhsExpr: IrExpression,
        rhsExpr: IrExpression,
        isOr: Boolean,
        data: Unit,
    ): BrsExpression {
        val left = lhsExpr.accept(this, data)
        // Statements hoisted while transforming the LHS run unconditionally —
        // keep them pending, but separate them from the RHS's.
        val lhsPending = genCtx.takeHoistedStatements()
        val right = rhsExpr.accept(this, data)
        val rhsHoisted = genCtx.takeHoistedStatements()
        lhsPending.forEach { genCtx.addHoistedStatement(it) }

        val tmpName = "__sc_tmp${genCtx.nextTempId()}"
        genCtx.addHoistedStatement(BrsVariable(tmpName, null, BrsBooleanLiteral(isOr)))
        val guardBody = mutableListOf<BrsStatement>()
        guardBody.addAll(rhsHoisted)
        guardBody.add(
            BrsExpressionStatement(
                BrsBinaryOp(BrsIdentifier(tmpName), BrsBinaryOperator.EQ, right)
            )
        )
        val condition = if (isOr) BrsUnaryOp(BrsUnaryOperator.NOT, left) else left
        genCtx.addHoistedStatement(
            BrsIf(condition = condition, thenBranch = BrsBlock(guardBody))
        )
        return BrsIdentifier(tmpName)
    }
```

(If `genCtx.nextTempId()` doesn't exist on the expression transformer's genCtx, it does exist for break flags — check `BrsGenerationContext.kt` and use whatever counter API is there; a private `scTempCounter` field is an acceptable fallback.)

- [ ] **Step 5: Regenerate the golden and review**

Run: `./run-compiler-tests.sh --update`, then Read `shortCircuit.brs.txt` and verify ALL of:
- `pureOperands` still uses bare `a and b` / `or` (compact form kept for pure operands).
- `nullGuardAnd` has `__sc_tmp` + `if h <> invalid then` guard around the `h.check…` call; NO bare `and` joining them.
- `nullGuardOr` guards with `if not (…)`.
- `guardInIf` — the hoisted temp+guard appear BEFORE the `if __sc_tmpN then` line.
- `guardInWhile` — the guard statements are RE-EVALUATED each iteration (inside the loop body / the while-condition re-evaluation block), not only once before the loop. **If they are emitted only once before `while`, the while-condition hoist channel does not re-evaluate — STOP and extend `visitWhileLoop`'s existing `conditionHoisted` machinery (IrStatementToBrsTransformer.kt:581+) to include them; it already has the re-evaluation scaffold for elvis conditions.**
- `mixedChain` — nested chain correct (inner pure `h <> invalid` may stay compact inside the outer guard).

- [ ] **Step 6: Full golden suite + diff review**

Run: `./run-compiler-tests.sh` → all pass (57 + 1). If other goldens changed in step 5's `--update`, review each diff: pure-boolean cases must be UNCHANGED; only impure-RHS sites may differ (expect few to none).

- [ ] **Step 7: Commit**

```bash
git add compiler/testData/codegen/brs/expressions/shortCircuit.* \
  compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/BrsGoldenFileTests.kt \
  compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/
git commit -m "brs: emit short-circuit-safe code for && / || with effectful RHS

Kotlin && / || compiled to bare BrightScript and/or, which evaluates BOTH
operands — x != null && x.foo() crashed on invalid. Impure RHS operands now
hoist to a guarded temp through the existing hoisted-statement channel; pure
operands keep the compact form.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 2: device semantics tests for short-circuit (+ platform probe)

**Files:**
- Create: `libraries/stdlib/brs/test/kotlin/standard/ShortCircuitTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt` (import + call)
- Modify: `CLAUDE.md` (BrightScript Language Notes — record probe result)

**Interfaces:**
- Consumes: Task 1's codegen fix.
- Stdlib test DSL: `fun TestRunner.xxxTests() { suite("Name") { test("case") { …asserts… } } }`, imports `kotlin.test.*`; registered by adding an import line and a call inside `runTests { }` in `TestMain.kt` (see existing coroutine imports there as the pattern).

- [ ] **Step 1: Write the test suite**

Create `libraries/stdlib/brs/test/kotlin/standard/ShortCircuitTest.kt`:

```kotlin
package test.standard

import kotlin.test.*

private class SideEffectCounter {
    var calls = 0
    fun hitTrue(): Boolean {
        calls = calls + 1
        return true
    }
    fun hitFalse(): Boolean {
        calls = calls + 1
        return false
    }
}

/**
 * Device-pinned Kotlin semantics for && / || on the BRS backend. BrightScript's
 * own and/or evaluate both operands (probe test at the bottom records the
 * platform truth); the compiler must nonetheless deliver Kotlin short-circuit
 * semantics.
 */
fun TestRunner.shortCircuitTests() {
    suite("Short-circuit semantics") {

        test("&& skips effectful RHS when LHS is false") {
            val c = SideEffectCounter()
            var lhs = false
            var entered = false
            if (lhs && c.hitTrue()) {
                entered = true
            }
            assertFalse(entered)
            assertEquals(0, c.calls)
        }

        test("&& evaluates RHS exactly once when LHS is true") {
            val c = SideEffectCounter()
            var lhs = true
            val result = lhs && c.hitTrue()
            assertTrue(result)
            assertEquals(1, c.calls)
        }

        test("|| skips effectful RHS when LHS is true") {
            val c = SideEffectCounter()
            var lhs = true
            val result = lhs || c.hitTrue()
            assertTrue(result)
            assertEquals(0, c.calls)
        }

        test("|| evaluates RHS when LHS is false") {
            val c = SideEffectCounter()
            var lhs = false
            val result = lhs || c.hitFalse()
            assertFalse(result)
            assertEquals(1, c.calls)
        }

        test("null-guard && protects member access") {
            val list: ArrayList<Int>? = null
            var entered = false
            if (list != null && list.size > 0) {
                entered = true
            }
            assertFalse(entered)
        }

        test("null-guard || protects member access") {
            val list: ArrayList<Int>? = null
            var empty = false
            if (list == null || list.size == 0) {
                empty = true
            }
            assertTrue(empty)
        }

        test("chain evaluates left to right and stops at first decider") {
            val c = SideEffectCounter()
            val list: ArrayList<Int>? = null
            var entered = false
            if (c.hitFalse() && list != null && list.size > 0) {
                entered = true
            }
            assertFalse(entered)
            assertEquals(1, c.calls)
        }

        test("&& in while condition re-evaluates guard each iteration") {
            val items = ArrayList<Int>()
            items.add(2)
            items.add(1)
            items.add(-5)
            var n = 0
            while (items.size > 0 && items[0] > 0) {
                items.removeAt(0)
                n = n + 1
            }
            assertEquals(2, n)
            assertEquals(1, items.size)
        }

        // PLATFORM PROBE (0a): records whether bare BRS `and` short-circuits on
        // this device. Asserts nothing about the platform — prints the truth
        // for CLAUDE.md. Kotlin locals share the generated function's scope
        // with brs() splice lines (ShelfView precedent).
        test("PROBE bare BRS and with invalid-receiver RHS") {
            var probeArr: ArrayList<Int>? = null
            var probeOutcome = "unset"
            try {
                brs("if probeArr <> invalid and probeArr.count() > 0 then")
                brs("  probeOutcome = \"rhs-ran\"")
                brs("end if")
                if (probeOutcome == "unset") {
                    probeOutcome = "short-circuited-or-skipped"
                }
            } catch (e: Throwable) {
                probeOutcome = "crashed-no-short-circuit"
            }
            println("[PROBE] bare BRS and, invalid receiver on RHS: " + probeOutcome)
            assertTrue(probeOutcome != "unset")
        }
    }
}
```

Note: `brs()` is the raw-splice intrinsic (`kotlin.brs`) — verify import needs by checking how `ShelfView.kt` gets it (default-imported). If local-variable names get mangled in the generated BRS so the splice can't see `probeArr`, read the generated test `.brs` under `libraries/stdlib/brs/test/build/brs/source/` and adjust the splice to the mangled names, or drop the probe test and probe manually via a temp edit to a runtime `.brs` file — the deliverable is the CLAUDE.md line, not the test.

- [ ] **Step 2: Register the suite**

In `libraries/stdlib/brs/test/kotlin/TestMain.kt` add with the other standard imports and calls:

```kotlin
import test.standard.shortCircuitTests
```
and inside `runTests { }` after `standardFunctionsTests()`:
```kotlin
        shortCircuitTests()
```

- [ ] **Step 3: Rebuild and run on device**

Run: `./rebuild.sh` (expect green incl. step 7), then `echo | nc -w 3 192.168.1.125 8085` (preflight), then `ROKU_DEVICE_IP=192.168.1.125 ROKU_PASSWORD=pass ./run-stdlib-tests.sh`.
Expected: 411 + 9 tests green. Find the `[PROBE]` line in `libraries/stdlib/brs/test/build/test-output.txt` and note the outcome.

- [ ] **Step 4: Record the probe result in CLAUDE.md**

In the "BrightScript Language Notes" section of `CLAUDE.md`, add one line stating the device-observed truth, e.g.:
`- **`and`/`or` do not short-circuit** (device-verified 2026-08-11, OS <version from run>): both operands always evaluate. The compiler emits guarded temps for Kotlin && / || with effectful RHS.`
(If the probe shows it DOES short-circuit on this OS, record that instead and note the codegen fix stays for older-OS floors.)

- [ ] **Step 5: Commit**

```bash
git add libraries/stdlib/brs/test/kotlin/standard/ShortCircuitTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt CLAUDE.md
git commit -m "stdlib: device tests pinning Kotlin short-circuit semantics + platform probe

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 3: expression-position try/catch lowering + golden

**Files:**
- Create: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsTryExpressionLowering.kt`
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsLoweringPhases.kt` (register after `BrsWhenExpressionLowering(context)` at ~line 254)
- Create: `compiler/testData/codegen/brs/controlFlow/tryCatchExpression.kt` + generated `.brs.txt`
- Modify: `BrsGoldenFileTests.kt` (register)

**Interfaces:**
- Produces: `class BrsTryExpressionLowering(private val context: BrsIrBackendContext) : FileLoweringPass`.
- Approach: transform EVERY non-Unit-typed `IrTry` (regardless of position) into `IrBlock { tmp; IrTry(unit-typed, arms assign tmp); IrGetValue(tmp) }`. Position-independence avoids duplicating the when-lowering's expression-context tracking; a non-Unit try in statement position just gains a harmless temp. The resulting block shape is exactly what `BrsWhenExpressionLowering.transformWhenToBlock` produces, which every downstream consumer already handles.
- Defect being fixed: `IrExpressionToBrsTransformer.visitTry` (line 113) wraps the statement in `BrsStatementAsExpression`, rendering garbage like `x = return try…`. That fallback stays (dead after this lowering).

- [ ] **Step 1: Write the golden source**

Create `compiler/testData/codegen/brs/controlFlow/tryCatchExpression.kt`:

```kotlin
// try/catch in expression position: initializer, return value, call argument.
fun throwingLength(s: String?): Int {
    if (s == null) throw IllegalStateException("null input")
    return s.length
}

fun tryInInitializer(s: String?): Int {
    val n = try {
        throwingLength(s)
    } catch (e: Throwable) {
        -1
    }
    return n
}

fun tryInReturn(s: String?): String {
    return try {
        "ok:" + throwingLength(s)
    } catch (e: Throwable) {
        "fallback"
    }
}

fun wrap(n: Int): String {
    return "v" + n
}

fun tryInArgument(s: String?): String {
    return wrap(try { throwingLength(s) } catch (e: Throwable) { 0 })
}
```

Register: `@Test fun tryCatchExpression() = runTest("controlFlow/tryCatchExpression.kt")` in `BrsGoldenFileTests.kt` next to the other `controlFlow/` entries.

- [ ] **Step 2: Capture the broken output**

Run `./run-compiler-tests.sh --update`; Read the generated `.brs.txt`. Expected: garbage shapes (literal `try` in expression position / `BrsStatementAsExpression` leakage). Evidence only — don't commit.

- [ ] **Step 3: Implement the lowering**

Create `BrsTryExpressionLowering.kt`:

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Rewrites non-Unit `try` expressions into statement-level try/catch with a
 * temp result variable — BrightScript's try/catch is statement-only, and the
 * expression transformer's fallback (BrsStatementAsExpression) rendered
 * invalid code like `x = return try` (Task 18 finisher ledger).
 *
 * ```kotlin
 * val x = try { a() } catch (e: Throwable) { b() }
 * ```
 * becomes
 * ```kotlin
 * val x = run {
 *     var tmp: T? = null
 *     try { tmp = a() } catch (e: Throwable) { tmp = b() }
 *     tmp
 * }
 * ```
 *
 * Runs on every non-Unit IrTry regardless of position: a statement-position
 * non-Unit try just gains a harmless temp, and the block shape matches what
 * BrsWhenExpressionLowering already produces, so all downstream handling
 * (block-as-expression hoisting, condition consumption) applies unchanged.
 */
class BrsTryExpressionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private var tempVarCounter = 0

    override fun lower(irFile: IrFile) {
        tempVarCounter = 0
        irFile.transformChildrenVoid(TryExpressionTransformer())
    }

    private inner class TryExpressionTransformer : IrElementTransformerVoid() {

        private var currentDeclarationParent: IrDeclarationParent? = null

        override fun visitFunction(
            declaration: org.jetbrains.kotlin.ir.declarations.IrFunction
        ): IrStatement {
            val previous = currentDeclarationParent
            currentDeclarationParent = declaration
            val result = super.visitFunction(declaration)
            currentDeclarationParent = previous
            return result
        }

        override fun visitClass(
            declaration: org.jetbrains.kotlin.ir.declarations.IrClass
        ): IrStatement {
            val previous = currentDeclarationParent
            currentDeclarationParent = declaration
            val result = super.visitClass(declaration)
            currentDeclarationParent = previous
            return result
        }

        override fun visitTry(aTry: IrTry): IrExpression {
            // Children first: nested tries inside arms get their own temps.
            val transformed = super.visitTry(aTry) as IrTry
            if (transformed.type.isUnit() || transformed.type.isNothing()) {
                return transformed
            }
            return transformToBlock(transformed)
        }

        private fun transformToBlock(aTry: IrTry): IrExpression {
            val startOffset = aTry.startOffset
            val endOffset = aTry.endOffset
            val resultType = aTry.type

            val tempVar = buildVariable(
                parent = currentDeclarationParent
                    ?: error("No declaration parent for try-expression temp"),
                startOffset = startOffset,
                endOffset = endOffset,
                origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                name = Name.identifier("__try_tmp${tempVarCounter++}"),
                type = resultType,
                isVar = true,
                isConst = false,
                isLateinit = false
            ).apply {
                initializer = IrConstImpl.constNull(startOffset, endOffset, resultType.makeNullable())
            }

            val newTry = IrTryImpl(
                startOffset, endOffset, context.irBuiltIns.unitType
            ).apply {
                tryResult = assignArmTo(tempVar, aTry.tryResult)
                for (catch in aTry.catches) {
                    catches.add(
                        IrCatchImpl(
                            catch.startOffset, catch.endOffset,
                            catch.catchParameter,
                            assignArmTo(tempVar, catch.result)
                        )
                    )
                }
                finallyExpression = aTry.finallyExpression
            }

            return IrBlockImpl(
                startOffset, endOffset, resultType, null,
                listOf(tempVar, newTry, IrGetValueImpl(startOffset, endOffset, resultType, tempVar.symbol))
            )
        }

        /** Rewrites an arm so its value lands in [tempVar]; control-flow tails stay. */
        private fun assignArmTo(tempVar: IrVariable, arm: IrExpression): IrExpression {
            fun assign(value: IrExpression): IrExpression = IrSetValueImpl(
                value.startOffset, value.endOffset,
                context.irBuiltIns.unitType, tempVar.symbol, value, null
            )

            return when (arm) {
                is IrBlock -> {
                    val statements = arm.statements.toMutableList()
                    val last = statements.lastOrNull()
                    if (last is IrExpression && last !is IrReturn && last !is IrThrow &&
                        last !is IrBreak && last !is IrContinue && !last.type.isNothing()
                    ) {
                        statements[statements.lastIndex] = assign(last)
                    }
                    IrBlockImpl(arm.startOffset, arm.endOffset, context.irBuiltIns.unitType, arm.origin, statements)
                }
                is IrReturn, is IrThrow, is IrBreak, is IrContinue -> arm
                else -> if (arm.type.isNothing()) arm else assign(arm)
            }
        }
    }
}
```

(API names like `IrTryImpl`/`IrCatchImpl` constructor shapes: mirror whatever `FinallyBlocksLowering` / existing code in `lower/coroutines/` uses if these differ in this Kotlin version — check compile errors and adapt; the SHAPE is the contract.)

- [ ] **Step 4: Register the lowering**

In `BrsLoweringPhases.kt` directly after the `BrsWhenExpressionLowering(context),` entry (~line 254):

```kotlin
            // Phase 12.1: try/catch used as an expression → statement try with
            // a temp result variable (BrightScript try/catch is statement-only)
            BrsTryExpressionLowering(context),
```

- [ ] **Step 5: Regenerate golden, review, run suite**

Run `./run-compiler-tests.sh --update`; verify in `tryCatchExpression.brs.txt`:
- Each function shows `__try_tmpN = invalid`-style declaration, a statement-level `try … catch e … end try` whose arms END with `__try_tmpN = <value>`, and the temp used afterward (`n = __try_tmpN` / `return wrap(__try_tmpN)`).
- No literal `return try` / no `BrsStatementAsExpression` leakage anywhere.
Then `./run-compiler-tests.sh` → all green (57 + 2 now). Review any other golden diffs (statement-position non-Unit tries may gain temps — verify semantics preserved).

- [ ] **Step 6: Commit**

```bash
git add compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsTryExpressionLowering.kt \
  compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsLoweringPhases.kt \
  compiler/testData/codegen/brs/controlFlow/tryCatchExpression.* \
  compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/BrsGoldenFileTests.kt
git commit -m "brs: lower expression-position try/catch to statement try with temp result

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 4: device semantics tests for try-expression

**Files:**
- Create: `libraries/stdlib/brs/test/kotlin/standard/TryExpressionTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

- [ ] **Step 1: Write the suite**

```kotlin
package test.standard

import kotlin.test.*

private fun boom(): Int {
    throw IllegalStateException("boom")
}

fun TestRunner.tryExpressionTests() {
    suite("try-expression semantics") {

        test("try expression yields try-arm value") {
            val n = try { "abc".length } catch (e: Throwable) { -1 }
            assertEquals(3, n)
        }

        test("try expression yields catch-arm value on throw") {
            val n = try { boom() } catch (e: Throwable) { -1 }
            assertEquals(-1, n)
        }

        test("try expression in return position") {
            fun f(fail: Boolean): String {
                return try {
                    if (fail) boom()
                    "ok"
                } catch (e: Throwable) {
                    "caught:" + (e.message ?: "?")
                }
            }
            assertEquals("ok", f(false))
            assertEquals("caught:boom", f(true))
        }

        test("try expression as call argument") {
            fun wrap(n: Int): String = "v" + n
            assertEquals("v7", wrap(try { 7 } catch (e: Throwable) { 0 }))
            assertEquals("v0", wrap(try { boom() } catch (e: Throwable) { 0 }))
        }

        test("nested try expressions") {
            val n = try {
                try { boom() } catch (e: Throwable) { boom() }
            } catch (e: Throwable) {
                42
            }
            assertEquals(42, n)
        }
    }
}
```

(Local functions inside a `test {}` lambda: if the BRS backend rejects local `fun` declarations there, hoist `f`/`wrap` to private top-level functions in the file — same assertions.)

- [ ] **Step 2: Register in TestMain.kt** (`import test.standard.tryExpressionTests` + `tryExpressionTests()` after `shortCircuitTests()`)

- [ ] **Step 3: Rebuild + device run**

`./rebuild.sh` then preflight then `./run-stdlib-tests.sh`. Expected: previous count + 5, zero failures.

- [ ] **Step 4: Full Phase-0 gate sweep**

Run: `./run-compiler-tests.sh` (59 goldens green) and confirm rebuild step 7 was green. E2E not needed this task (no stdlib/runtime behavior change beyond codegen — but run it in Task 12 baseline anyway).

- [ ] **Step 5: Commit**

```bash
git add libraries/stdlib/brs/test/kotlin/standard/TryExpressionTest.kt libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: device tests pinning try-expression semantics

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

# Phase 1: Job completion protocol + await family

### Task 5: CancellationException + JobImpl completion protocol

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/coroutines/cancellation/CancellationException.kt`
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/Job.kt` (full rewrite below)
- Create: `libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces (later tasks rely on these exactly):**
- `public interface DisposableHandle { public fun dispose() }` (in Job.kt)
- `Job.invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle` — fires exactly once at TERMINAL state with the terminal cause (null = success; `CancellationException` = cancelled; other = failed). Fires the handler synchronously at registration when already terminal.
- `JobImpl` internal API: constructor `JobImpl(parent: Job? = null, hasBody: Boolean = false, isSupervisor: Boolean = false, upcallsFailure: Boolean = true, reportsUnhandled: Boolean = false)`; `invokeOnCancelRequest(handler: (Throwable?) -> Unit): DisposableHandle`; `completionCauseInternal: Throwable?`; `attachChild/detachChild/childFailed` (used by Task 9's tests and Task 10-11's builders).
- `CompletableDeferredImpl<T>(parent: Job? = null, hasBody: Boolean = false)` with `internal val innerJob: JobImpl`.
- `Deferred<out T>` gains `public fun getCompletionExceptionOrNull(): Throwable?` (throws `IllegalStateException` when not complete).
- `internal fun jobImplOf(job: Job?): JobImpl?` lives in Job.kt.
- `public open class CancellationException` in `kotlin.coroutines.cancellation`, extends `IllegalStateException`.
- JOIN/AWAIT ARE NOT REWRITTEN IN THIS TASK: keep `join()`'s existing busy-wait body and `await()`'s existing body temporarily (Task 6 replaces them). Everything else in Job.kt is rewritten now.

- [ ] **Step 1: Write the failing tests (flag-level, no suspension)**

Create `libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt`:

```kotlin
package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.jobProtocolTests() {
    suite("Job completion protocol") {

        test("invokeOnCompletion fires on complete with null cause") {
            val job = Job()
            var fired = 0
            var seenCause: Throwable? = IllegalStateException("sentinel")
            job.invokeOnCompletion { cause ->
                fired = fired + 1
                seenCause = cause
            }
            assertTrue(job.complete())
            assertEquals(1, fired)
            assertNull(seenCause)
        }

        test("invokeOnCompletion fires with exception on completeExceptionally") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            val boom = IllegalStateException("boom")
            assertTrue(job.completeExceptionally(boom))
            assertEquals(boom, seenCause)
        }

        test("invokeOnCompletion fires with CancellationException on cancel") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            job.cancel()
            assertTrue(seenCause is CancellationException)
        }

        test("invokeOnCompletion on already-terminal job fires immediately") {
            val job = Job()
            job.complete()
            var fired = false
            job.invokeOnCompletion { fired = true }
            assertTrue(fired)
        }

        test("handler fires exactly once and dispose prevents firing") {
            val job = Job()
            var fired = 0
            val handle = job.invokeOnCompletion { fired = fired + 1 }
            handle.dispose()
            job.complete()
            assertEquals(0, fired)
        }

        test("throwing handler does not break other handlers") {
            val job = Job()
            var secondFired = false
            job.invokeOnCompletion { throw IllegalStateException("handler boom") }
            job.invokeOnCompletion { secondFired = true }
            job.complete()
            assertTrue(secondFired)
        }

        test("state flags across lifecycle") {
            val job = Job()
            assertTrue(job.isActive)
            assertFalse(job.isCompleted)
            assertFalse(job.isCancelled)
            job.complete()
            assertFalse(job.isActive)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }

        test("cancelled job is terminal for a plain Job") {
            val job = Job()
            job.cancel()
            assertTrue(job.isCancelled)
            assertTrue(job.isCompleted)
            assertFalse(job.isActive)
        }

        test("complete after cancel returns false and does not overwrite outcome") {
            val job = Job()
            job.cancel()
            assertFalse(job.complete())
            assertTrue(job.isCancelled)
        }

        test("CancellationException in completeExceptionally is quiet cancellation") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            job.completeExceptionally(CancellationException("stop"))
            assertTrue(job.isCancelled)
            assertTrue(seenCause is CancellationException)
        }

        test("deferred completion protocol delivers value then handlers") {
            val d = CompletableDeferred<String>()
            var seen: Throwable? = IllegalStateException("sentinel")
            d.invokeOnCompletion { cause -> seen = cause }
            assertTrue(d.complete("v"))
            assertNull(seen)
            assertEquals("v", d.getCompleted())
            assertNull(d.getCompletionExceptionOrNull())
        }

        test("deferred getCompletionExceptionOrNull surfaces failure") {
            val d = CompletableDeferred<String>()
            val boom = IllegalStateException("boom")
            d.completeExceptionally(boom)
            assertEquals(boom, d.getCompletionExceptionOrNull())
        }

        test("cancelled deferred getCompleted throws CancellationException") {
            val d = CompletableDeferred<String>()
            d.cancel()
            var thrown: Throwable? = null
            try {
                d.getCompleted()
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is CancellationException)
        }
    }
}
```

Register in `TestMain.kt` (import `test.coroutines.jobProtocolTests`, call `jobProtocolTests()` in the Coroutines block).

- [ ] **Step 2: Create CancellationException**

`libraries/stdlib/brs/src/kotlin/coroutines/cancellation/CancellationException.kt`:

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.cancellation

/**
 * Thrown by cancellable suspending functions when the coroutine is cancelled
 * while suspended, and used as the quiet completion cause of cancelled jobs.
 * An uncaught CancellationException cancels the coroutine — it is never
 * treated as a failure (no parent propagation, no unhandled reporting).
 */
public open class CancellationException : IllegalStateException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
}
```

- [ ] **Step 3: Rewrite Job.kt**

Replace the CONTENTS of `libraries/stdlib/brs/src/kotlin/coroutines/Job.kt`, keeping the existing `join()` busy-wait body and the existing `await()` body verbatim for this task (Task 6 replaces them). New content:

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.cancellation.CancellationException

/**
 * A handle to a registration (completion / cancel-request handler) that can be
 * disposed to deregister it. Disposal after firing is a no-op.
 */
public interface DisposableHandle {
    public fun dispose()
}

internal object NoopHandle : DisposableHandle {
    override fun dispose() {}
}

public interface Job : CoroutineContext.Element {

    @Suppress("BRS_NAME_CASE_CLASH")
    public companion object Key : CoroutineContext.Key<Job>

    /** Started and neither terminal nor cancel-requested. */
    public val isActive: Boolean

    /** TERMINAL: body finished AND all children finished (any outcome). */
    public val isCompleted: Boolean

    /** True from the moment cancellation (or failure) is requested. */
    public val isCancelled: Boolean

    public fun cancel(cause: Throwable? = null)

    /**
     * Suspends until this job reaches its terminal state. Returns normally
     * even when the target was cancelled or failed — only the CALLER's own
     * cancellation makes join throw ([CancellationException]).
     */
    public suspend fun join()

    public fun start(): Boolean

    /**
     * Registers [handler] to run exactly once when this job reaches its
     * terminal state, with the terminal cause: null = success,
     * [CancellationException] = cancelled, anything else = failure.
     * Runs the handler synchronously at registration when already terminal.
     * The returned handle deregisters it (no-op after firing).
     */
    public fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle
}

public interface CompletableJob : Job {
    /** Marks the body finished successfully. False if already finished/cancelled. */
    public fun complete(): Boolean

    /** Marks the body failed. A [CancellationException] cancels quietly instead. */
    public fun completeExceptionally(exception: Throwable): Boolean
}

/** Creates a plain job (no coroutine body): cancel() finishes it immediately. */
public fun Job(parent: Job? = null): CompletableJob = JobImpl(parent)

/**
 * A job whose CHILDREN fail independently: a failed child never cancels this
 * job or its other children (the child reports its own failure instead).
 * Used as the root of [kotlin.brs.componentScope].
 */
public fun SupervisorJob(parent: Job? = null): CompletableJob =
    JobImpl(parent, isSupervisor = true)

/** Resolves the concrete JobImpl participating in the hierarchy, if any. */
internal fun jobImplOf(job: Job?): JobImpl? {
    if (job is JobImpl) return job
    if (job is CompletableDeferredImpl<*>) return job.innerJob
    return null
}

/**
 * Job state machine. Single-threaded per component (GetGlobalAA scoping), so
 * plain booleans and lists are sound — no atomics, no locks.
 *
 * Lifecycle: Active -> Completing (body finished OR cancel requested, children
 * still winding down) -> terminal Completed/Cancelled. The terminal transition
 * fires ONLY when the body is done and the children list is empty; that is
 * what makes coroutineScope's "no child still running when it returns"
 * guarantee real.
 *
 * @param hasBody a coroutine writes the body outcome later (cancel must wait
 *   for it); plain Job()/latches have none (cancel finishes immediately).
 * @param isSupervisor children's failures are ignored (they self-report).
 * @param upcallsFailure scope jobs (coroutineScope/withContext/withTimeout)
 *   set false: their failure is DELIVERED to the parked caller (rethrow at
 *   the call site), never upcalled into the caller's job.
 * @param reportsUnhandled launch sets true: a failure that has no
 *   non-supervisor parent to propagate to is printed to the console.
 */
internal open class JobImpl(
    parent: Job? = null,
    internal val hasBody: Boolean = false,
    internal val isSupervisor: Boolean = false,
    internal val upcallsFailure: Boolean = true,
    internal val reportsUnhandled: Boolean = false,
) : CompletableJob {

    internal val parentImpl: JobImpl? = jobImplOf(parent)

    private var bodyCompleted: Boolean = false
    private var cancelRequested: Boolean = false
    private var terminal: Boolean = false

    /** Null iff completed successfully; CancellationException iff cancelled. */
    internal var completionCauseInternal: Throwable? = null
        private set

    private val children = mutableListOf<JobImpl>()
    private val completionHandlers = mutableListOf<HandlerEntry>()
    private val cancelHandlers = mutableListOf<HandlerEntry>()

    init {
        parentImpl?.attachChild(this)
    }

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = !terminal && !cancelRequested
    override val isCompleted: Boolean get() = terminal
    override val isCancelled: Boolean get() = cancelRequested

    override fun start(): Boolean = false // jobs are active from creation

    private class HandlerEntry(
        private val owner: MutableList<HandlerEntry>,
        val handler: (Throwable?) -> Unit,
    ) : DisposableHandle {
        override fun dispose() {
            owner.remove(this)
        }
    }

    override fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle {
        if (terminal) {
            // CAUTION for stdlib suspend utilities: fires synchronously.
            // Fast-path the terminal case BEFORE registering (or rely on
            // ParkedContinuation's unarmed latch).
            invokeHandlerSafely(handler, completionCauseInternal)
            return NoopHandle
        }
        val entry = HandlerEntry(completionHandlers, handler)
        completionHandlers.add(entry)
        return entry
    }

    /**
     * Fires the moment cancellation is REQUESTED (before children unwind) —
     * the mid-park wakeup hook. Fires synchronously at registration when
     * already cancel-requested; never fires for a normally-completed job.
     */
    internal fun invokeOnCancelRequest(handler: (Throwable?) -> Unit): DisposableHandle {
        if (cancelRequested) {
            invokeHandlerSafely(handler, completionCauseInternal)
            return NoopHandle
        }
        if (terminal) return NoopHandle
        val entry = HandlerEntry(cancelHandlers, handler)
        cancelHandlers.add(entry)
        return entry
    }

    override fun complete(): Boolean {
        if (bodyCompleted || terminal) return false
        bodyCompleted = true
        val accepted = !cancelRequested
        tryFinish()
        return accepted
    }

    override fun completeExceptionally(exception: Throwable): Boolean {
        if (bodyCompleted || terminal) return false
        bodyCompleted = true
        if (cancelRequested) {
            // Body unwound after an earlier cancel — outcome already decided.
            tryFinish()
            return false
        }
        // Both failure and uncaught CancellationException move to cancelling;
        // the cause class decides failure-vs-quiet at the terminal transition.
        cancelRequested = true
        completionCauseInternal = exception
        fireCancelHandlers()
        cancelChildrenInternal(exception)
        tryFinish()
        return true
    }

    override fun cancel(cause: Throwable?) {
        if (terminal || cancelRequested) return
        cancelRequested = true
        completionCauseInternal = cause ?: CancellationException("Job was cancelled")
        fireCancelHandlers()
        cancelChildrenInternal(completionCauseInternal)
        tryFinish()
    }

    internal fun attachChild(child: JobImpl) {
        children.add(child)
    }

    internal fun detachChild(child: JobImpl) {
        children.remove(child)
        tryFinish()
    }

    /**
     * A child finished with a FAILURE (non-CancellationException cause).
     * First failure wins: cancels this job (and thereby the failed child's
     * siblings) with the child's exception as the cause.
     */
    internal fun childFailed(child: JobImpl, cause: Throwable) {
        if (!terminal && !cancelRequested) {
            cancelRequested = true
            completionCauseInternal = cause
            fireCancelHandlers()
            cancelChildrenInternal(cause)
        }
        detachChild(child)
    }

    private fun cancelChildrenInternal(cause: Throwable?) {
        if (children.isEmpty()) return
        val snapshot = children.toMutableList()
        val childCause: Throwable =
            if (cause is CancellationException) cause
            else CancellationException("Parent job was cancelled", cause)
        for (child in snapshot) {
            child.cancel(childCause)
        }
    }

    private fun tryFinish() {
        if (terminal) return
        if (children.isNotEmpty()) return
        if (!bodyCompleted) {
            // A coroutine-backed job must wait for its body to unwind; a
            // plain job (latch) finishes at the cancel request itself.
            if (!cancelRequested) return
            if (hasBody) return
        }
        terminal = true
        val cause = completionCauseInternal
        val isFailure = cause != null && cause !is CancellationException
        val parent = parentImpl
        if (parent != null) {
            if (isFailure && upcallsFailure && !parent.isSupervisor) {
                parent.childFailed(this, cause!!)
            } else {
                if (isFailure && reportsUnhandled) reportUnhandled(cause!!)
                parent.detachChild(this)
            }
        } else {
            if (isFailure && reportsUnhandled) reportUnhandled(cause!!)
        }
        fireCompletionHandlers(cause)
    }

    private fun fireCancelHandlers() {
        if (cancelHandlers.isEmpty()) return
        val snapshot = cancelHandlers.toMutableList()
        cancelHandlers.clear()
        for (entry in snapshot) {
            invokeHandlerSafely(entry.handler, completionCauseInternal)
        }
    }

    private fun fireCompletionHandlers(cause: Throwable?) {
        if (completionHandlers.isEmpty()) return
        val snapshot = completionHandlers.toMutableList()
        completionHandlers.clear()
        for (entry in snapshot) {
            invokeHandlerSafely(entry.handler, cause)
        }
    }

    private fun invokeHandlerSafely(handler: (Throwable?) -> Unit, cause: Throwable?) {
        try {
            handler(cause)
        } catch (e: Throwable) {
            println("[kotlin.coroutines] Completion handler threw: $e")
        }
    }

    private fun reportUnhandled(cause: Throwable) {
        println("[kotlin.coroutines] Unhandled exception in coroutine: $cause")
    }

    override suspend fun join() {
        // TEMPORARY (replaced in the next task by the parked-continuation
        // implementation): pre-existing busy-wait retained so this task's
        // flag-level protocol change lands green in isolation.
        while (!isCompleted) {
        }
    }
}

/**
 * A deferred value: a [Job] with a result.
 */
public interface Deferred<out T> : Job {
    /**
     * Suspends until complete; returns the value or throws the completion
     * exception (a [CancellationException] when the deferred was cancelled).
     */
    public suspend fun await(): T

    /** The result now, or [IllegalStateException] if not complete. */
    public fun getCompleted(): T

    /**
     * The terminal exception (failure or [CancellationException]), null on
     * success; [IllegalStateException] if not complete.
     */
    public fun getCompletionExceptionOrNull(): Throwable?
}

public interface CompletableDeferred<T> : Deferred<T>, CompletableJob {
    public fun complete(value: T): Boolean
}

public fun <T> CompletableDeferred(parent: Job? = null): CompletableDeferred<T> =
    CompletableDeferredImpl(parent)

/**
 * Deferred over an inner [JobImpl] (composition): the inner job carries ALL
 * lifecycle/handler/hierarchy state; this wrapper adds only the value slot.
 */
internal class CompletableDeferredImpl<T>(
    parent: Job? = null,
    hasBody: Boolean = false,
) : CompletableDeferred<T> {

    internal val innerJob = JobImpl(parent, hasBody = hasBody, reportsUnhandled = false)
    private var _value: T? = null

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = innerJob.isActive
    override val isCompleted: Boolean get() = innerJob.isCompleted
    override val isCancelled: Boolean get() = innerJob.isCancelled

    override fun cancel(cause: Throwable?) = innerJob.cancel(cause)

    override fun start(): Boolean = innerJob.start()

    override fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle =
        innerJob.invokeOnCompletion(handler)

    override fun complete(): Boolean = innerJob.complete()

    override fun completeExceptionally(exception: Throwable): Boolean =
        innerJob.completeExceptionally(exception)

    override fun complete(value: T): Boolean {
        if (innerJob.isCompleted || innerJob.isCancelled) return false
        // Value stored BEFORE complete() so handlers observe it.
        _value = value
        return innerJob.complete()
    }

    override fun getCompleted(): T {
        if (!innerJob.isCompleted) throw IllegalStateException("Deferred has not completed yet")
        val cause = innerJob.completionCauseInternal
        if (cause != null) throw cause
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun getCompletionExceptionOrNull(): Throwable? {
        if (!innerJob.isCompleted) throw IllegalStateException("Deferred has not completed yet")
        return innerJob.completionCauseInternal
    }

    override suspend fun await(): T {
        // TEMPORARY (replaced in the next task): join-then-read retained so
        // this task lands green in isolation.
        innerJob.join()
        return getCompleted()
    }
}
```

- [ ] **Step 4: Rebuild and run the stdlib device suite**

`./rebuild.sh` (green incl. step 7 — kotlin-test-brs compiles against the new Job surface), preflight, `./run-stdlib-tests.sh`.
Expected: previous count + 13 green, zero failures. Existing CoroutineTest Job/Deferred tests must still pass — if any pinned old semantics that changed intentionally (e.g. `isCompleted` now true after cancel; `getCompleted()` on cancelled deferred now throws CancellationException instead of returning null), UPDATE those tests to the new kotlinx-parity semantics and note it in the commit message.

- [ ] **Step 5: Run golden + E2E regression canaries**

`./run-compiler-tests.sh` (59 green). `cd ../roku-test-app && ./run-device-tests.sh` (33/6 green — Suites 2/4 exercise launch/runTask against the new JobImpl via `LaunchContinuation.resumeWith -> complete()/completeExceptionally()`, unchanged call sites).

- [ ] **Step 6: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/cancellation/CancellationException.kt \
  libraries/stdlib/brs/src/kotlin/coroutines/Job.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: JobImpl completion protocol (invokeOnCompletion, cancel-request handlers, hierarchy state)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 6: ParkedContinuation + real join()/await() + ensureActive/isActive

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/coroutines/ParkedContinuation.kt`
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/Job.kt` (replace the two TEMPORARY bodies)
- Create: `libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces:**
- Produces `internal class ParkedContinuation(continuation: Continuation<Any?>)` with `val handles: MutableList<DisposableHandle>`, `fun tryResume(value: Any?)`, `fun tryResumeException(exception: Throwable)`, `fun finish(): Any?` — used by Tasks 7, 8, 11, 13.
- Produces `public fun Job.ensureActive()`, `public fun CoroutineContext.ensureActive()`, `public val CoroutineScope.isActive: Boolean`, `internal fun registerCallerCancel(parked: ParkedContinuation, context: CoroutineContext)` — all in ParkedContinuation.kt.
- THE IDIOM every suspend utility uses from now on (tail-delegation + inline-resume safety):

```kotlin
public suspend fun example(): T = suspendCoroutineUninterceptedOrReturn { continuation ->
    continuation.context.ensureActive()            // entry check (throws CE)
    if (<already satisfied>) {
        <the value>                                 // sync resume: return the value
    } else {
        @Suppress("UNCHECKED_CAST")
        val parked = ParkedContinuation(continuation as Continuation<Any?>)
        parked.handles.add(<source>.invokeOnCompletion { cause -> ... parked.tryResume/-Exception ... })
        registerCallerCancel(parked, continuation.context)
        parked.finish()                             // MUST be the last expression
    }
}
```

- [ ] **Step 1: Write the failing tests**

Create `libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt` (runBlocking = main-thread pumping regime; `launch` bodies dispatch through the queue that runBlocking's loop drains):

```kotlin
package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.awaitTests() {
    suite("join/await suspension") {

        test("join suspends until launched job completes") {
            var order = ""
            runBlocking {
                val job = launch {
                    delay(30)
                    order = order + "A"
                }
                order = order + "B"
                job.join()
                order = order + "C"
            }
            assertEquals("BAC", order)
        }

        test("join on already-completed job returns immediately") {
            runBlocking {
                val job = launch { }
                job.join()
                job.join()
                assertTrue(job.isCompleted)
            }
        }

        test("join on failed job returns normally") {
            var joined = false
            runBlocking {
                val job = launch(EmptyCoroutineContext) {
                    throw IllegalStateException("boom")
                }
                job.join()
                joined = true
            }
            assertTrue(joined)
        }

        test("join on cancelled job returns normally") {
            runBlocking {
                val job = Job()
                job.cancel()
                job.join()
                assertTrue(job.isCancelled)
            }
        }

        test("await returns async result") {
            runBlocking {
                val d = async {
                    delay(20)
                    21 * 2
                }
                assertEquals(42, d.await())
            }
        }

        test("await on completed deferred is immediate") {
            runBlocking {
                val d = CompletableDeferred<String>()
                d.complete("v")
                assertEquals("v", d.await())
            }
        }

        test("await rethrows async failure") {
            runBlocking {
                val d = async<Int> {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("boom", thrown?.message)
            }
        }

        test("await on cancelled deferred throws CancellationException") {
            runBlocking {
                val d = CompletableDeferred<String>()
                d.cancel()
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is CancellationException)
            }
        }

        test("two coroutines can await the same deferred") {
            runBlocking {
                val d = CompletableDeferred<Int>()
                var sum = 0
                val j1 = launch { sum = sum + d.await() }
                val j2 = launch { sum = sum + d.await() }
                delay(10)
                d.complete(5)
                j1.join()
                j2.join()
                assertEquals(10, sum)
            }
        }

        test("caller cancellation wakes a parked join") {
            runBlocking {
                val gate = Job()               // never completed
                var observed: Throwable? = null
                val waiter = launch {
                    try {
                        gate.join()
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(10)                       // let the waiter park
                waiter.cancel()
                delay(10)                       // let the wakeup dispatch
                assertTrue(observed is CancellationException)
                assertTrue(waiter.isCompleted)
            }
        }

        test("ensureActive and isActive") {
            runBlocking {
                val job = Job()
                job.ensureActive()             // active: no throw
                job.cancel()
                var thrown: Throwable? = null
                try {
                    job.ensureActive()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is CancellationException)
            }
        }
    }
}
```

Register in TestMain.kt (`awaitTests()`).

- [ ] **Step 2: Verify the red state honestly**

These tests CANNOT pass against the busy-wait (`join` on an incomplete job would hang the suite). Do NOT run them on device yet — proceed to implementation, then run. (A hang, not a failure, is this task's red form; note it and move on.)

- [ ] **Step 3: Implement ParkedContinuation.kt**

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

/**
 * One parked suspension. Owns the once-guard that resolves every
 * completion-vs-cancellation race, the disposal of the handles the utility
 * registered, and resumption through the ContinuationInterceptor (dispatcher)
 * when one is present.
 *
 * INLINE-RESUME SAFETY: in the no-interceptor regime (runBlocking/runPumping
 * contexts carry no dispatcher) a handler can fire while the enclosing
 * suspendCoroutineUninterceptedOrReturn block is STILL RUNNING (e.g.
 * coroutineScope starting a block that completes synchronously). Resuming the
 * continuation then would double-execute the caller. So a ParkedContinuation
 * starts UNARMED: settlements before [finish] are recorded, and [finish] —
 * which MUST be the block's last expression — either reports them as a
 * synchronous return/throw or arms the park and reports COROUTINE_SUSPENDED.
 */
internal class ParkedContinuation(
    private val continuation: Continuation<Any?>,
) {
    internal val handles = mutableListOf<DisposableHandle>()

    private var resumed = false
    private var armed = false
    private var syncSettled = false
    private var syncValue: Any? = null
    private var syncException: Throwable? = null

    fun tryResume(value: Any?) {
        if (resumed) return
        resumed = true
        disposeHandles()
        if (!armed) {
            syncSettled = true
            syncValue = value
            return
        }
        resumeTarget().resume(value)
    }

    fun tryResumeException(exception: Throwable) {
        if (resumed) return
        resumed = true
        disposeHandles()
        if (!armed) {
            syncSettled = true
            syncException = exception
            return
        }
        resumeTarget().resumeWithException(exception)
    }

    /** MUST be the last expression of the enclosing intrinsic block. */
    fun finish(): Any? {
        armed = true
        if (syncSettled) {
            val e = syncException
            if (e != null) throw e
            return syncValue
        }
        return COROUTINE_SUSPENDED
    }

    private fun disposeHandles() {
        for (handle in handles) {
            handle.dispose()
        }
        handles.clear()
    }

    private fun resumeTarget(): Continuation<Any?> {
        val interceptor = continuation.context[ContinuationInterceptor]
        if (interceptor != null) {
            return interceptor.interceptContinuation(continuation)
        }
        return continuation
    }
}

/**
 * Throws this job's cancellation cause if it was cancelled. The
 * cooperative-cancellation check: every stdlib suspend utility calls the
 * context form on entry; user loops can call it (or check [isActive]).
 */
public fun Job.ensureActive() {
    if (!isCancelled) return
    val impl = jobImplOf(this)
    val cause = impl?.completionCauseInternal
    if (cause is CancellationException) throw cause
    throw CancellationException("Job was cancelled", cause)
}

/** No-op when the context has no [Job] (main-thread driver coroutines). */
public fun CoroutineContext.ensureActive() {
    this[Job]?.ensureActive()
}

/** True while the scope's job is active; true for job-less scopes. */
public val CoroutineScope.isActive: Boolean
    get() {
        val job = coroutineContext[Job] ?: return true
        return job.isActive
    }

/**
 * Mid-park wakeup registration: cancelling the CALLER's job resumes this park
 * exceptionally with the cancellation cause. No-op for foreign Job types.
 */
internal fun registerCallerCancel(parked: ParkedContinuation, context: CoroutineContext) {
    val impl = jobImplOf(context[Job]) ?: return
    parked.handles.add(impl.invokeOnCancelRequest { cause ->
        val ce = if (cause is CancellationException) cause
        else CancellationException("Job was cancelled", cause)
        parked.tryResumeException(ce)
    })
}
```

- [ ] **Step 4: Replace the two TEMPORARY bodies in Job.kt**

`JobImpl.join()` becomes:

```kotlin
    override suspend fun join() {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            continuation.context.ensureActive()
            if (terminal) {
                Unit
            } else {
                @Suppress("UNCHECKED_CAST")
                val parked = ParkedContinuation(continuation as Continuation<Any?>)
                // join() resumes normally whatever the target's outcome.
                parked.handles.add(invokeOnCompletion { parked.tryResume(Unit) })
                registerCallerCancel(parked, continuation.context)
                parked.finish()
            }
        }
    }
```

`CompletableDeferredImpl.await()` becomes:

```kotlin
    override suspend fun await(): T {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            continuation.context.ensureActive()
            if (innerJob.isCompleted) {
                val cause = innerJob.completionCauseInternal
                if (cause != null) throw cause
                @Suppress("UNCHECKED_CAST")
                (_value as T)
            } else {
                @Suppress("UNCHECKED_CAST")
                val parked = ParkedContinuation(continuation as Continuation<Any?>)
                parked.handles.add(innerJob.invokeOnCompletion { cause ->
                    if (cause != null) {
                        parked.tryResumeException(cause)
                    } else {
                        parked.tryResume(_value)
                    }
                })
                registerCallerCancel(parked, continuation.context)
                parked.finish()
            }
        }
    }
```

Add to Job.kt's imports: `kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn`. Remove the TEMPORARY comments.

- [ ] **Step 5: Rebuild, inspect generated BRS, run device suite**

`./rebuild.sh`. Then find `join`/`await` in the regenerated stdlib BRS (search `libraries/stdlib/brs-prebuilt` regen output or `libraries/stdlib/brs/test/build/brs/source/` after the test build) and verify the tail-delegation shape survived: ONE intrinsic block, no statements after it, `parked.finish()` value returned. Then preflight + `./run-stdlib-tests.sh`.
Expected: previous + 12 green. Watch specifically for a HANG (would mean a resumption never dispatched — check the interceptor path) with the 10s/whole-run timeouts.

- [ ] **Step 6: E2E canary + commit**

`cd ../roku-test-app && ./run-device-tests.sh` → 33/6 green.

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/ParkedContinuation.kt \
  libraries/stdlib/brs/src/kotlin/coroutines/Job.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: real join()/await() over the completion protocol (ParkedContinuation)

join() was a busy-wait that hung the single-threaded pump once dispatch became
real; await() additionally had post-suspension code, which stdlib suspend
functions (no state machine) miscompile. Both are now tail-delegating parks
resumed through the interceptor, with entry cancellation checks and mid-park
caller-cancel wakeup.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 7: delay()/yield() cancellation retrofit

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/Delay.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt` (add suite)
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces:** Consumes ParkedContinuation/ensureActive/registerCallerCancel (Task 6). Produces no new API — behavior only: delay/yield now (a) throw `CancellationException` on entry if the caller is cancelled, (b) delay wakes mid-park on caller cancellation.

- [ ] **Step 1: Write the failing tests** (append to AwaitTest.kt as a second suite `suite("delay/yield cancellation")` inside a new `fun TestRunner.delayCancellationTests()`):

```kotlin
fun TestRunner.delayCancellationTests() {
    suite("delay/yield cancellation") {

        test("cancel wakes a coroutine parked in delay") {
            runBlocking {
                var observed: Throwable? = null
                var after = false
                val sleeper = launch {
                    try {
                        delay(10000)
                        after = true
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(20)                    // let it park
                sleeper.cancel()
                delay(20)                    // let the wakeup dispatch
                assertTrue(observed is CancellationException)
                assertFalse(after)
                assertTrue(sleeper.isCompleted)
            }
        }

        test("delay entry check throws when already cancelled") {
            runBlocking {
                var thrown: Throwable? = null
                val job = launch {
                    val self = coroutineContext[Job]
                    self?.cancel()
                    try {
                        delay(10)
                    } catch (e: Throwable) {
                        thrown = e
                    }
                }
                job.join()
                assertTrue(thrown is CancellationException)
            }
        }

        test("stale DelayTracker callback after cancel is a no-op") {
            runBlocking {
                val sleeper = launch {
                    delay(50)
                }
                delay(10)
                sleeper.cancel()
                // Ride past the original deadline: the tracker still fires the
                // registered callback; the once-guard must swallow it.
                delay(100)
                assertTrue(sleeper.isCompleted)
            }
        }

        test("yield entry check throws when cancelled") {
            runBlocking {
                var thrown: Throwable? = null
                val job = launch {
                    coroutineContext[Job]?.cancel()
                    try {
                        yield()
                    } catch (e: Throwable) {
                        thrown = e
                    }
                }
                job.join()
                assertTrue(thrown is CancellationException)
            }
        }
    }
}
```

Note: the launch block's receiver is `CoroutineScope`, so `coroutineContext[Job]` resolves via the scope property. If name resolution is ambiguous on this backend, capture it as `val self = launch-returned job` from outside instead (declare `var selfRef: Job? = null` before, assign after launch, and spin one `delay(1)` before cancelling). Register the new suite in TestMain.kt (`import test.coroutines.delayCancellationTests` + `delayCancellationTests()` after `awaitTests()`).

- [ ] **Step 2: Retrofit `delay()`** — replace its body in Delay.kt:

```kotlin
public suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return

    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        @Suppress("UNCHECKED_CAST")
        val parked = ParkedContinuation(continuation as Continuation<Any?>)
        // DelayTracker has no deregistration: after a mid-park cancel wakeup
        // the deadline callback still fires and lands in the once-guard.
        DelayTracker.current.register(timeMillis) { parked.tryResume(Unit) }
        registerCallerCancel(parked, continuation.context)
        parked.finish()
    }
}
```

Keep the existing KDoc; drop the now-unused `ContinuationInterceptor` import if nothing else in the file uses it.

- [ ] **Step 3: Retrofit `yield()`** — add the entry check as the block's first statement:

```kotlin
public suspend fun yield(): Unit = suspendCoroutineUninterceptedOrReturn { continuation ->
    continuation.context.ensureActive()
    val interceptor = continuation.context[ContinuationInterceptor]
    if (interceptor != null) {
        val intercepted = interceptor.interceptContinuation(continuation)
        intercepted.resume(Unit)
    } else {
        continuation.resume(Unit)
    }
    COROUTINE_SUSPENDED
}
```

- [ ] **Step 4: Rebuild + device suite**

`./rebuild.sh`, preflight, `./run-stdlib-tests.sh`. Expected: previous + 4 green; existing delay/yield suites (delayFunctionTests, yieldFunctionTests) still green.

- [ ] **Step 5: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/Delay.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/AwaitTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: delay()/yield() cancellation — entry checks + mid-park wakeup

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 8: awaitAll / joinAll

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/coroutines/Await.kt`
- Create: `libraries/stdlib/brs/test/kotlin/coroutines/AwaitAllTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces (produced, final):**
- `public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T>`
- `public suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T>`
- `public suspend fun joinAll(vararg jobs: Job)`
- `public suspend fun Collection<Job>.joinAll()`
- Semantics: results in INPUT order; empty input → `emptyList()`/return; first failure resumes `awaitAll` exceptionally immediately (siblings NOT cancelled); `joinAll` never throws for target outcomes; both throw `CancellationException` if the CALLER is cancelled (entry or mid-park).

- [ ] **Step 1: Write the failing tests**

Create `libraries/stdlib/brs/test/kotlin/coroutines/AwaitAllTest.kt`:

```kotlin
package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.awaitAllTests() {
    suite("awaitAll/joinAll") {

        test("awaitAll returns results in input order") {
            runBlocking {
                val slow = async {
                    delay(60)
                    "slow"
                }
                val fast = async {
                    delay(10)
                    "fast"
                }
                val results = awaitAll(slow, fast)
                assertEquals(2, results.size)
                assertEquals("slow", results[0])
                assertEquals("fast", results[1])
            }
        }

        test("awaitAll runs deferreds concurrently, not sequentially") {
            runBlocking {
                val clock = kotlin.brs.roku.RoTimespan.create()
                clock.mark()
                val a = async { delay(80) }
                val b = async { delay(80) }
                awaitAll(a, b)
                val elapsed = clock.totalMilliseconds()
                assertTrue(elapsed < 150, "expected concurrent (<150ms), got $elapsed")
            }
        }

        test("awaitAll of Collection extension") {
            runBlocking {
                val list = ArrayList<Deferred<Int>>()
                list.add(async { 1 })
                list.add(async { 2 })
                list.add(async { 3 })
                val results = list.awaitAll()
                assertEquals(3, results.size)
                assertEquals(6, results[0] + results[1] + results[2])
            }
        }

        test("awaitAll on empty vararg returns empty list") {
            runBlocking {
                val results = awaitAll<Int>()
                assertEquals(0, results.size)
            }
        }

        test("awaitAll with already-completed deferreds is immediate") {
            runBlocking {
                val a = CompletableDeferred<Int>()
                val b = CompletableDeferred<Int>()
                a.complete(1)
                b.complete(2)
                val results = awaitAll(a, b)
                assertEquals(1, results[0])
                assertEquals(2, results[1])
            }
        }

        test("awaitAll rethrows first failure without waiting for the rest") {
            runBlocking {
                val slow = async {
                    delay(5000)
                    "never"
                }
                val failing = async<String> {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                var thrown: Throwable? = null
                val clock = kotlin.brs.roku.RoTimespan.create()
                clock.mark()
                try {
                    awaitAll(slow, failing)
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("boom", thrown?.message)
                assertTrue(clock.totalMilliseconds() < 1000)
                // kotlinx parity: siblings are NOT cancelled by awaitAll
                assertFalse(slow.isCancelled)
                slow.cancel() // clean up so runBlocking can exit
            }
        }

        test("awaitAll with an already-failed deferred throws on entry") {
            runBlocking {
                val failed = CompletableDeferred<Int>()
                failed.completeExceptionally(IllegalStateException("early"))
                val fine = CompletableDeferred<Int>()
                fine.complete(1)
                var thrown: Throwable? = null
                try {
                    awaitAll(fine, failed)
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("early", thrown?.message)
            }
        }

        test("awaitAll duplicate deferred in input") {
            runBlocking {
                val d = async {
                    delay(10)
                    7
                }
                val results = awaitAll(d, d)
                assertEquals(7, results[0])
                assertEquals(7, results[1])
            }
        }

        test("awaitAll wakes on caller cancellation") {
            runBlocking {
                val never = CompletableDeferred<Int>()
                var observed: Throwable? = null
                val waiter = launch {
                    try {
                        awaitAll(never)
                    } catch (e: Throwable) {
                        observed = e
                    }
                }
                delay(20)
                waiter.cancel()
                delay(20)
                assertTrue(observed is CancellationException)
            }
        }

        test("joinAll waits for all jobs including failed ones") {
            runBlocking {
                var count = 0
                val ok = launch {
                    delay(10)
                    count = count + 1
                }
                val bad = launch(EmptyCoroutineContext) {
                    delay(20)
                    throw IllegalStateException("boom")
                }
                joinAll(ok, bad)
                assertEquals(1, count)
                assertTrue(ok.isCompleted)
                assertTrue(bad.isCompleted)
            }
        }

        test("joinAll on empty and completed inputs is immediate") {
            runBlocking {
                joinAll()
                val done = launch { }
                done.join()
                joinAll(done, done)
                assertTrue(done.isCompleted)
            }
        }
    }
}
```

Register in TestMain.kt (`awaitAllTests()`).

- [ ] **Step 2: Implement Await.kt**

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Awaits all [deferreds], returning their results in INPUT order.
 *
 * Resumes exceptionally with the FIRST failure as soon as it happens —
 * without waiting for, or cancelling, the remaining deferreds (kotlinx
 * parity). Throws [kotlin.coroutines.cancellation.CancellationException]
 * if the calling coroutine is cancelled (on entry or mid-park).
 */
public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T> =
    deferreds.toList().awaitAll()

/** See [awaitAll]. */
public suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T> {
    if (isEmpty()) return emptyList()
    val list = this.toList()
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        var pendingCount = 0
        var priorFailure: Throwable? = null
        for (d in list) {
            if (d.isCompleted) {
                if (priorFailure == null) {
                    priorFailure = d.getCompletionExceptionOrNull()
                }
            } else {
                pendingCount = pendingCount + 1
            }
        }
        val alreadyFailed = priorFailure
        if (alreadyFailed != null) throw alreadyFailed
        if (pendingCount == 0) {
            collectResults(list)
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            // Mutable state lives in a class instance, NOT captured mutable
            // locals — stdlib closures must not need shared-box codegen.
            val state = CountdownState(pendingCount)
            for (d in list) {
                if (!d.isCompleted) {
                    parked.handles.add(d.invokeOnCompletion { cause ->
                        if (cause != null) {
                            parked.tryResumeException(cause)
                        } else {
                            state.remaining = state.remaining - 1
                            if (state.remaining == 0) {
                                parked.tryResume(collectResults(list))
                            }
                        }
                    })
                }
            }
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

/**
 * Suspends until all [jobs] are terminal, whatever their outcomes — target
 * failures/cancellations never throw here. Caller cancellation does.
 */
public suspend fun joinAll(vararg jobs: Job) {
    return jobs.toList().joinAll()
}

/** See [joinAll]. */
public suspend fun Collection<Job>.joinAll() {
    if (isEmpty()) return
    val list = this.toList()
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        var pendingCount = 0
        for (job in list) {
            if (!job.isCompleted) pendingCount = pendingCount + 1
        }
        if (pendingCount == 0) {
            Unit
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            val state = CountdownState(pendingCount)
            for (job in list) {
                if (!job.isCompleted) {
                    parked.handles.add(job.invokeOnCompletion {
                        state.remaining = state.remaining - 1
                        if (state.remaining == 0) {
                            parked.tryResume(Unit)
                        }
                    })
                }
            }
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

private class CountdownState(var remaining: Int)

private fun <T> collectResults(list: List<Deferred<T>>): List<T> {
    val results = ArrayList<T>(list.size)
    for (d in list) {
        results.add(d.getCompleted())
    }
    return results
}
```

- [ ] **Step 3: Vararg smoke check**

`./rebuild.sh`. If `deferreds.toList()` on a vararg array fails to compile/regen for the BRS klib (vararg-of-generic risk flagged in the spec), fall back: iterate the vararg into an `ArrayList<Deferred<T>>` manually (`val list = ArrayList<Deferred<T>>(); for (d in deferreds) list.add(d)`) — same for joinAll. Record which form worked in the commit message.

- [ ] **Step 4: Device suite**

Preflight + `./run-stdlib-tests.sh`. Expected: previous + 12 green.

- [ ] **Step 5: Full Phase-1 gate sweep**

`./run-compiler-tests.sh` (59), `cd ../roku-test-app && ./run-device-tests.sh` (33/6). Both green.

- [ ] **Step 6: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/Await.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/AwaitAllTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: awaitAll/joinAll — countdown parks over the completion protocol

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

# Phase 2: hierarchy + builders + scope functions

### Task 9: hierarchy semantics (public-surface tests over Task 5's internals)

**Files:**
- Modify: `libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt` (add suite `fun TestRunner.jobHierarchyTests()`)
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces:** Consumes only public API (`Job(parent)`, `SupervisorJob(parent)`, handlers). The hierarchy CODE landed in Task 5; this task pins its semantics before the builders start relying on them. If any test exposes a JobImpl bug, fix it in Job.kt within this task.

- [ ] **Step 1: Write the tests**

```kotlin
fun TestRunner.jobHierarchyTests() {
    suite("Job hierarchy") {

        test("parent completes only after children finish (Completing state)") {
            val parent = Job()
            val child = Job(parent)
            var parentDone = false
            parent.invokeOnCompletion { parentDone = true }
            parent.complete()
            assertFalse(parentDone)
            assertFalse(parent.isCompleted)
            assertTrue(parent.isActive)      // Completing counts as active (kotlinx parity)
            child.complete()
            assertTrue(parentDone)
            assertTrue(parent.isCompleted)
        }

        test("cancel cascades to children recursively") {
            val root = Job()
            val mid = Job(root)
            val leaf = Job(mid)
            root.cancel()
            assertTrue(mid.isCancelled)
            assertTrue(leaf.isCancelled)
            assertTrue(root.isCompleted)     // plain jobs: terminal once children drain
        }

        test("child failure cancels parent and sibling with original cause") {
            val parent = Job()
            val failing = Job(parent)
            val sibling = Job(parent)
            var parentCause: Throwable? = null
            parent.invokeOnCompletion { cause -> parentCause = cause }
            val boom = IllegalStateException("boom")
            failing.completeExceptionally(boom)
            assertTrue(parent.isCancelled)
            assertTrue(sibling.isCancelled)
            assertEquals(boom, parentCause)  // original exception, not a wrapper
        }

        test("supervisor parent ignores child failure") {
            val parent = SupervisorJob()
            val failing = Job(parent)
            val sibling = Job(parent)
            failing.completeExceptionally(IllegalStateException("boom"))
            assertTrue(parent.isActive)
            assertTrue(sibling.isActive)
            sibling.complete()
            parent.complete()
            assertTrue(parent.isCompleted)
        }

        test("cancelled child detaches quietly (CancellationException is not failure)") {
            val parent = Job()
            val child = Job(parent)
            child.cancel()
            assertTrue(parent.isActive)
            parent.complete()
            assertTrue(parent.isCompleted)
        }

        test("first failure wins over later sibling failures") {
            val parent = Job()
            val a = Job(parent)
            val b = Job(parent)
            var parentCause: Throwable? = null
            parent.invokeOnCompletion { cause -> parentCause = cause }
            a.completeExceptionally(IllegalStateException("first"))
            b.completeExceptionally(IllegalStateException("second"))
            assertEquals("first", parentCause?.message)
        }
    }
}
```

- [ ] **Step 2: Register** (`jobHierarchyTests()` in TestMain.kt after `jobProtocolTests()`).

- [ ] **Step 3: Rebuild + device run**

`./rebuild.sh`, preflight, `./run-stdlib-tests.sh`. Expected: previous + 6 green. Fix JobImpl in place if any semantics test fails (likely candidates: detach-before-cause-recorded ordering in `childFailed`; `isActive` during Completing).

- [ ] **Step 4: Commit**

```bash
git add libraries/stdlib/brs/test/kotlin/coroutines/JobProtocolTest.kt libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: device tests pinning job hierarchy semantics (cascade, child failure, supervisor)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 10: builders join the hierarchy; supervisor roots

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/builders/Builders.kt`
- Modify: `libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt`
- Create: `libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces:**
- `launch`/`async` signatures unchanged; they now attach `parent = mergedContext[Job]`.
- **DESIGN DECISION (documents a kotlinx divergence):** `runBlocking`'s root job becomes `SupervisorJob()`, and so does `componentScope()`'s. In kotlinx a failed child cancels the runBlocking job; here a failed top-level `launch` reports to the console and dies alone. This preserves the existing test corpus and the kotlin.test driver, and matches the componentScope philosophy. Failures INSIDE `coroutineScope {}` (Task 11) get full kotlinx propagation.
- `LaunchContinuation`/`AsyncContinuation`/`RunBlockingContinuation` classes are unchanged — completion flows through `complete()/completeExceptionally()` which now do the right thing.

- [ ] **Step 1: Write the failing tests**

Create `libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt`:

```kotlin
package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.builders.async
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.builderHierarchyTests() {
    suite("Builder hierarchy") {

        test("launch attaches to scope job: cancel scope cancels launch") {
            runBlocking {
                val scopeJob = Job()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var woken: Throwable? = null
                scope.launch {
                    try {
                        delay(10000)
                    } catch (e: Throwable) {
                        woken = e
                    }
                }
                delay(20)
                scopeJob.cancel()
                delay(20)
                assertTrue(woken is CancellationException)
            }
        }

        test("launch on cancelled scope never runs the block") {
            runBlocking {
                val scopeJob = Job()
                scopeJob.cancel()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var ran = false
                val job = scope.launch { ran = true }
                assertTrue(job.isCancelled)
                assertTrue(job.isCompleted)
                delay(20)
                assertFalse(ran)
            }
        }

        test("child failure cancels sibling in same non-supervisor scope") {
            runBlocking {
                val scopeJob = Job()
                val scope = CoroutineScope(coroutineContext + scopeJob)
                var siblingWoken = false
                scope.launch {
                    try {
                        delay(10000)
                    } catch (e: CancellationException) {
                        siblingWoken = true
                    }
                }
                scope.launch {
                    delay(10)
                    throw IllegalStateException("boom")
                }
                delay(50)
                assertTrue(scopeJob.isCancelled)
                assertTrue(siblingWoken)
            }
        }

        test("runBlocking root is supervisor: failed launch does not poison siblings") {
            var after = false
            runBlocking {
                launch {
                    delay(10)
                    throw IllegalStateException("reported-not-propagated")
                }
                delay(50)
                // A cancelled root would make this delay throw CancellationException.
                after = true
            }
            assertTrue(after)
        }

        test("async attaches and failure surfaces only at await") {
            runBlocking {
                val d = async<Int> {
                    delay(10)
                    throw IllegalStateException("async-boom")
                }
                delay(50)
                var thrown: Throwable? = null
                try {
                    d.await()
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("async-boom", thrown?.message)
            }
        }

        test("launch job completes only after its own launched children") {
            runBlocking {
                var childDone = false
                val outer = launch {
                    launch {
                        delay(40)
                        childDone = true
                    }
                    // outer body ends now; job must hold in Completing
                }
                outer.join()
                assertTrue(childDone)
            }
        }
    }
}
```

Register in TestMain.kt (`builderHierarchyTests()`).

- [ ] **Step 2: Implement — Builders.kt**

Replace `launch` and `async` bodies (imports add `kotlin.coroutines.cancellation.CancellationException`):

```kotlin
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val newContext = coroutineContext + context
    val parent = newContext[Job]
    if (parent != null && parent.isCancelled) {
        // kotlinx parity: launching on a cancelled scope yields a dead job
        // and never runs the block.
        val dead = JobImpl(null)
        dead.cancel(CancellationException("Parent job was cancelled"))
        return dead
    }
    val job = JobImpl(parent, hasBody = true, reportsUnhandled = true)
    val newScope = CoroutineScope(newContext + job)
    val continuation = LaunchContinuation(newScope, block, job)
    continuation.start()
    return job
}

public fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val newContext = coroutineContext + context
    val parent = newContext[Job]
    if (parent != null && parent.isCancelled) {
        val dead = CompletableDeferredImpl<T>(null)
        dead.cancel(CancellationException("Parent job was cancelled"))
        return dead
    }
    val deferred = CompletableDeferredImpl<T>(parent, hasBody = true)
    val newScope = CoroutineScope(newContext + deferred)
    val continuation = AsyncContinuation(newScope, block, deferred)
    continuation.start()
    return deferred
}
```

In `runBlocking`, change `val job = Job()` to:

```kotlin
    // Supervisor root (kotlinx divergence, deliberate): a failed top-level
    // launch reports to the console instead of cancelling the whole
    // runBlocking scope — same philosophy as componentScope's root.
    val job = SupervisorJob()
```

- [ ] **Step 3: Implement — ComponentCoroutines.kt**

In `componentScope()`, change `CoroutineScope(Dispatchers.Main + Job())` to `CoroutineScope(Dispatchers.Main + SupervisorJob())` and extend the KDoc: failed top-level `launch` reports `[kotlin.coroutines] Unhandled exception in coroutine: …` to the console and does not poison the component's scope.

- [ ] **Step 4: Rebuild + device suite + canaries**

`./rebuild.sh`, preflight, `./run-stdlib-tests.sh` (previous + 6 green; earlier suites must survive the hierarchy change — the supervisor runBlocking root is what keeps Task 6-8's failing-launch tests green). Then `cd ../roku-test-app && ./run-device-tests.sh` (33/6 green).

- [ ] **Step 5: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/builders/Builders.kt \
  libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: launch/async attach to the job hierarchy; supervisor roots for runBlocking/componentScope

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 11: real coroutineScope {} + withContext rework

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/coroutines/Scopes.kt`
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/CoroutineScope.kt` (DELETE the fake inline `coroutineScope`)
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/builders/WithContext.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt` (add suite)
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces (produced; Task 13 reuses the engine):**
- `public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R` — package `kotlin.coroutines` (unchanged for callers; no longer `inline`).
- `internal fun <R> parkScopedBlock(continuation: Continuation<Any?>, scopeContext: CoroutineContext, scopeJob: JobImpl, block: suspend CoroutineScope.() -> R, onTerminal: (cause: Throwable?, value: Any?, parked: ParkedContinuation) -> Unit): Any?` in Scopes.kt.
- Scope jobs: `JobImpl(callerJob, hasBody = true, upcallsFailure = false)` — attached child (downward cancellation flows in) whose failure is DELIVERED to the parked caller, not upcalled.

- [ ] **Step 1: Write the failing tests** (append to ScopeFunctionsTest.kt):

```kotlin
fun TestRunner.scopeFunctionTests() {
    suite("coroutineScope/withContext") {

        test("coroutineScope returns block value and preserves dispatcher") {
            runBlocking {
                val v = coroutineScope {
                    delay(10)
                    "value"
                }
                assertEquals("value", v)
            }
        }

        test("coroutineScope waits for launched children") {
            runBlocking {
                var childDone = false
                coroutineScope {
                    launch {
                        delay(40)
                        childDone = true
                    }
                }
                assertTrue(childDone)
            }
        }

        test("coroutineScope rethrows child failure after cancelling siblings") {
            runBlocking {
                var siblingWoken = false
                var thrown: Throwable? = null
                try {
                    coroutineScope {
                        launch {
                            try {
                                delay(10000)
                            } catch (e: CancellationException) {
                                siblingWoken = true
                                throw e
                            }
                        }
                        launch {
                            delay(10)
                            throw IllegalStateException("scope-boom")
                        }
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("scope-boom", thrown?.message)
                assertTrue(siblingWoken)
            }
        }

        test("coroutineScope failure does not cancel the caller's job") {
            runBlocking {
                try {
                    coroutineScope {
                        throw IllegalStateException("contained")
                    }
                } catch (e: Throwable) {
                    // expected
                }
                // Caller continues: a cancelled caller job would make this throw.
                delay(10)
                assertTrue(true)
            }
        }

        test("withContext returns value with merged context") {
            runBlocking {
                val v = withContext(kotlin.coroutines.dispatchers.Dispatchers.Main) {
                    delay(10)
                    21 * 2
                }
                assertEquals(42, v)
            }
        }

        test("withContext propagates block failure") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withContext(kotlin.coroutines.dispatchers.Dispatchers.Main) {
                        throw IllegalStateException("wc-boom")
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("wc-boom", thrown?.message)
            }
        }
    }
}
```

Add import `kotlin.coroutines.builders.withContext` to the file. Register `scopeFunctionTests()`.

- [ ] **Step 2: Implement Scopes.kt**

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.builders.startCoroutine
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Runs [block] in a child scope and suspends until the block AND every
 * coroutine launched in it complete. A child's failure cancels the scope's
 * other children (mid-park wakeup) and, once all have finished, rethrows the
 * ORIGINAL exception here — it is delivered to the caller, never upcalled
 * into the caller's job (kotlinx ScopeCoroutine parity). The caller's own
 * cancellation reaches the scope through the child cascade.
 */
public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
        @Suppress("UNCHECKED_CAST")
        parkScopedBlock(
            continuation as Continuation<Any?>,
            outerContext + scopeJob,
            scopeJob,
            block
        ) { cause, value, parked ->
            if (cause != null) {
                parked.tryResumeException(cause)
            } else {
                parked.tryResume(value)
            }
        }
    }
}

internal class ScopeResultHolder {
    var value: Any? = null
}

/** Completion continuation for a scope's block: parks the value, settles the job. */
internal class ScopeBlockCompletion<R>(
    override val context: CoroutineContext,
    private val job: JobImpl,
    private val holder: ScopeResultHolder,
) : Continuation<R> {
    override fun resumeWith(result: Result<R>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            job.completeExceptionally(exception)
        } else {
            holder.value = result.getOrNull()
            job.complete()
        }
    }
}

/**
 * Shared engine for coroutineScope/withContext/withTimeout: starts [block] as
 * a coroutine completing [scopeJob], and resumes [continuation] from the
 * scope job's TERMINAL handler via [onTerminal]. Returns parked.finish() —
 * the caller must return this from its intrinsic block, LAST.
 */
internal fun <R> parkScopedBlock(
    continuation: Continuation<Any?>,
    scopeContext: CoroutineContext,
    scopeJob: JobImpl,
    block: suspend CoroutineScope.() -> R,
    onTerminal: (cause: Throwable?, value: Any?, parked: ParkedContinuation) -> Unit,
): Any? {
    val parked = ParkedContinuation(continuation)
    val holder = ScopeResultHolder()
    parked.handles.add(scopeJob.invokeOnCompletion { cause ->
        onTerminal(cause, holder.value, parked)
    })
    val scope = CoroutineScope(scopeContext)
    block.startCoroutine(scope, ScopeBlockCompletion(scopeContext, scopeJob, holder))
    return parked.finish()
}
```

- [ ] **Step 3: Delete the fake** — remove the `coroutineScope` function (and its `@Suppress`) from `CoroutineScope.kt`, leaving the interface/factory/impl. Grep for callers of `coroutineScope` across `libraries/stdlib/brs` and `libraries/kotlin.test/brs` — package is unchanged so imports keep working; fix any caller that relied on the inline signature.

- [ ] **Step 4: Rework withContext (non-IO)** — in WithContext.kt replace everything from the `return suspendCoroutineUninterceptedOrReturn` in `withContext` down (keep the IO identity check and `withContextIOSuspend` as-is):

```kotlin
@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED") // stdlib-internal identity check on the quarantined IO pipeline
public suspend fun <T> withContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    val dispatcher = context[ContinuationInterceptor] as? CoroutineDispatcher
    if (dispatcher === Dispatchers.IO || dispatcher === IODispatcher) {
        return withContextIOSuspend(context, block)
    }
    // Scope-engine path: same machinery as coroutineScope with the requested
    // context merged in. Replaces the old nested-runBlocking implementation,
    // which BLOCKED the calling thread (inside a pump drain: froze frames).
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
        @Suppress("UNCHECKED_CAST")
        parkScopedBlock(
            continuation as Continuation<Any?>,
            outerContext + context + scopeJob,
            scopeJob,
            block
        ) { cause, value, parked ->
            if (cause != null) {
                parked.tryResumeException(cause)
            } else {
                parked.tryResume(value)
            }
        }
    }
}
```

Update imports (add `kotlin.coroutines.parkScopedBlock` etc. via `kotlin.coroutines.*`; drop `Runnable`/`runBlocking` if now unused).

- [ ] **Step 5: Rebuild + device suite + canaries + commit**

`./rebuild.sh` (step 7 matters: kotlin.test compiles against the non-inline `coroutineScope`), preflight, `./run-stdlib-tests.sh` (previous + 6), `cd ../roku-test-app && ./run-device-tests.sh` (33/6). Then:

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/Scopes.kt \
  libraries/stdlib/brs/src/kotlin/coroutines/CoroutineScope.kt \
  libraries/stdlib/brs/src/kotlin/coroutines/builders/WithContext.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: real coroutineScope + withContext over the scope-job engine

The old coroutineScope dropped the caller's context (no dispatcher, no
waiting); withContext non-IO ran the block via a nested runBlocking that
blocked the render thread. Both now ride parkScopedBlock: child scope job,
sibling cancellation on failure, rethrow-at-call-site delivery.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 12: E2E — component-regime coverage (fixture + suite)

**Files:**
- Create: `../roku-test-app/components/fixtures/CoroutineUtilProbe.kt`
- Create: `../roku-test-app/src/brsTest/kotlin/tests/CoroutineUtilityTests.kt`
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/TestMain.kt`

**Interfaces:** Fixture follows the PumpBackendProbe shape exactly (mode → start → result last). `GroupComponent`, `@SG*Field`, `@BrsOnChange`, `launch` are default-imported in fixtures. Suites register manually in TestMain.kt's `runTests {}`. New suite name: `coroutineUtilitySuite(scene)`, suite string `"CoroutineUtilities"`. Timeout modes/tests come in Task 14, runTask-acceptance in Task 15 — this task lands 4 tests.

- [ ] **Step 1: Write the fixture**

`../roku-test-app/components/fixtures/CoroutineUtilProbe.kt`:

```kotlin
package com.nuvyyo.roku.components.fixtures.coroutineutilprobe

import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.Deferred
import kotlin.coroutines.awaitAll
import kotlin.coroutines.builders.async
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineScope
import kotlin.coroutines.delay

// Fixture for the CoroutineUtilities suite: exercises awaitAll, coroutineScope
// sibling cancellation, and the supervisor component root in the COMPONENT
// pumping regime (PumpScheduler wakeups — no pump wiring, that's the point).
// Scenario protocol: set `mode`, flip `start`; `result` is written LAST.
class CoroutineUtilProbe : GroupComponent() {

    @SGStringField
    var mode: String = ""

    @SGBooleanField
    @BrsOnChange("onStartChanged")
    var start: Boolean = false

    @SGStringField(alwaysNotify = true)
    var result: String = ""

    // Secondary observation written BEFORE result (sibling wakeup evidence).
    @SGStringField
    var detail: String = ""

    private fun onStartChanged(msg: RoSGNodeEvent) {
        if ("${msg.getData()}" != "true") return
        val runMode = "${top.getField("mode")}"
        if (runMode == "awaitAll") {
            launch {
                val slow = async {
                    delay(60)
                    "A"
                }
                val fast = async {
                    delay(20)
                    "B"
                }
                val results = awaitAll(slow, fast)
                result = "done:" + results[0] + results[1]
            }
        } else if (runMode == "awaitAllFail") {
            launch {
                var slow: Deferred<String>? = null
                try {
                    val s = async {
                        delay(5000)
                        "slow"
                    }
                    slow = s
                    val bad = async<String> {
                        delay(20)
                        throw IllegalStateException("boom")
                    }
                    awaitAll(s, bad)
                    result = "unexpected"
                } catch (e: Throwable) {
                    slow?.cancel()
                    result = "caught:" + (e.message ?: "?")
                }
            }
        } else if (runMode == "siblingCancel") {
            launch {
                var siblingWoken = false
                try {
                    coroutineScope {
                        launch {
                            try {
                                delay(5000)
                            } catch (e: CancellationException) {
                                siblingWoken = true
                                throw e
                            }
                        }
                        launch {
                            delay(30)
                            throw IllegalStateException("child-boom")
                        }
                    }
                    result = "unexpected"
                } catch (e: Throwable) {
                    detail = if (siblingWoken) "sibling-woken" else "sibling-not-woken"
                    result = "caught:" + (e.message ?: "?")
                }
            }
        } else if (runMode == "supervisor") {
            launch {
                delay(10)
                throw IllegalStateException("first-fails-reported")
            }
            launch {
                delay(60)
                result = "alive"
            }
        }
    }
}
```

- [ ] **Step 2: Write the suite**

`../roku-test-app/src/brsTest/kotlin/tests/CoroutineUtilityTests.kt`:

```kotlin
package com.nuvyyo.roku.tests

import kotlin.brs.roku.RoSGNode
import kotlin.test.TestRunner
import kotlin.test.assertEquals
import kotlin.test.device.roundTrip

// CoroutineUtilities suite: awaitAll & friends in the COMPONENT pumping
// regime (the stdlib suite covers the runBlocking regime). Fixture:
// components/fixtures/CoroutineUtilProbe.kt.
fun TestRunner.coroutineUtilitySuite(scene: RoSGNode) {
    suite("CoroutineUtilities") {

        testAsync("awaitAllJoinsConcurrentAsyncs") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "awaitAll")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("done:AB", "$result")
        }

        testAsync("awaitAllRethrowsFirstFailureFast") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "awaitAllFail")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("caught:boom", "$result")
        }

        testAsync("coroutineScopeCancelsSiblingMidPark") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "siblingCancel")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("caught:child-boom", "$result")
            assertEquals("sibling-woken", "${probe.getField("detail")}")
        }

        testAsync("supervisorRootSurvivesFailedLaunch") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "supervisor")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("alive", "$result")
        }
    }
}
```

- [ ] **Step 3: Register** — in `../roku-test-app/src/brsTest/kotlin/tests/TestMain.kt` add `coroutineUtilitySuite(scene)` after `fieldSemanticsSuite(scene)`.

- [ ] **Step 4: Build + run E2E**

`cd ../roku-test-app && ./rebuild-all.sh --all` (picks up the new stdlib from Maven Local + builds the app), preflight, `./run-device-tests.sh`.
Expected: 37 tests / 7 suites green (33 + 4), `validateComponentIncludes` strict 0 findings.

- [ ] **Step 5: Commit (roku-test-app repo)**

```bash
cd ../roku-test-app && git add components/fixtures/CoroutineUtilProbe.kt \
  src/brsTest/kotlin/tests/CoroutineUtilityTests.kt src/brsTest/kotlin/tests/TestMain.kt
git commit -m "tests: CoroutineUtilities E2E suite — awaitAll, sibling cancel, supervisor root (component pumping regime)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

# Phase 3: withTimeout

### Task 13: withTimeout / withTimeoutOrNull + stdlib tests

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/coroutines/builders/Timeout.kt`
- Modify: `libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt` (add suite)
- Modify: `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces (produced, final):**
- `public class TimeoutCancellationException internal constructor(message: String) : CancellationException(message)` — package `kotlin.coroutines.builders`.
- `public suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T` — throws its own TCE on expiry; `timeMillis <= 0` throws immediately.
- `public suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend CoroutineScope.() -> T): T?` — `null` on ITS OWN expiry (identity-matched TCE, so a nested timeout's TCE still propagates); `timeMillis <= 0` returns null.
- Timeout identity: the TCE instance is created up front; the DelayTracker callback cancels the scope job with exactly that instance; `withTimeoutOrNull`'s terminal handler compares `cause === timeout`.

- [ ] **Step 1: Write the failing tests** (append to ScopeFunctionsTest.kt):

```kotlin
fun TestRunner.withTimeoutTests() {
    suite("withTimeout") {

        test("block wins: value returned, no exception") {
            runBlocking {
                val v = withTimeout(5000) {
                    delay(20)
                    "fast"
                }
                assertEquals("fast", v)
            }
        }

        test("timeout expires: TimeoutCancellationException thrown, child woken") {
            runBlocking {
                var childWoken = false
                var thrown: Throwable? = null
                try {
                    withTimeout(60) {
                        try {
                            delay(10000)
                        } catch (e: CancellationException) {
                            childWoken = true
                            throw e
                        }
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is TimeoutCancellationException)
                assertTrue(childWoken)
            }
        }

        test("timeout expiry waits for children to unwind before rethrowing") {
            runBlocking {
                var siblingUnwound = false
                try {
                    withTimeout(40) {
                        launch {
                            try {
                                delay(10000)
                            } catch (e: CancellationException) {
                                siblingUnwound = true
                                throw e
                            }
                        }
                        delay(10000)
                    }
                } catch (e: Throwable) {
                    // by the time withTimeout rethrows, no child is running
                }
                assertTrue(siblingUnwound)
            }
        }

        test("block failure beats timeout and propagates as-is") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeout(5000) {
                        delay(10)
                        throw IllegalStateException("block-boom")
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertEquals("block-boom", thrown?.message)
                assertFalse(thrown is TimeoutCancellationException)
            }
        }

        test("stale timeout callback after a win is a no-op") {
            runBlocking {
                val v = withTimeout(50) {
                    delay(10)
                    "won"
                }
                // Ride past the original deadline; the tracker fires the stale
                // callback into a terminal scope job — nothing may explode.
                delay(100)
                assertEquals("won", v)
            }
        }

        test("withTimeout(<=0) throws immediately") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeout(0) {
                        "never"
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                assertTrue(thrown is TimeoutCancellationException)
            }
        }

        test("withTimeoutOrNull returns null on its own expiry") {
            runBlocking {
                val a = withTimeoutOrNull(40) {
                    delay(10000)
                    "a"
                }
                assertNull(a)
                val b = withTimeoutOrNull(5000) {
                    delay(10)
                    "b"
                }
                assertEquals("b", b)
            }
        }

        test("nested: inner timeout TCE propagates through outer withTimeoutOrNull") {
            runBlocking {
                var thrown: Throwable? = null
                try {
                    withTimeoutOrNull(5000) {
                        withTimeout(30) {
                            delay(10000)
                        }
                        "never"
                    }
                } catch (e: Throwable) {
                    thrown = e
                }
                // The INNER timeout's TCE is not the outer's instance: it must
                // propagate out as an exception, not become the outer's null.
                assertTrue(thrown is TimeoutCancellationException)
            }
        }
    }
}
```

Add import `kotlin.coroutines.builders.withTimeout`, `kotlin.coroutines.builders.withTimeoutOrNull`, `kotlin.coroutines.builders.TimeoutCancellationException`. Register `withTimeoutTests()`.

- [ ] **Step 2: Implement Timeout.kt**

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.builders

import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Thrown by [withTimeout] on expiry. Extends [CancellationException], so an
 * uncaught one CANCELS the coroutine instead of counting as a failure.
 */
public class TimeoutCancellationException internal constructor(
    message: String,
) : CancellationException(message)

/**
 * Runs [block] in a child scope with a deadline. On expiry the scope job is
 * cancelled with a [TimeoutCancellationException] (children get mid-park
 * wakeup); once they unwind, that TCE is rethrown here.
 *
 * Render-side only: work already handed to a task thread (runTask) is NOT
 * stopped — the waiting stops, and the task's late completion resumes nothing.
 *
 * The PumpScheduler arms a one-shot wakeup for DelayTracker deadlines, so
 * component timeouts fire promptly with zero wiring.
 */
public suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        if (timeMillis <= 0) {
            throw TimeoutCancellationException("Timed out immediately (timeMillis=$timeMillis)")
        }
        val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
        val timeout = TimeoutCancellationException("Timed out after ${timeMillis}ms")
        DelayTracker.current.register(timeMillis) {
            if (!scopeJob.isCompleted) {
                scopeJob.cancel(timeout)
            }
        }
        @Suppress("UNCHECKED_CAST")
        parkScopedBlock(
            continuation as Continuation<Any?>,
            outerContext + scopeJob,
            scopeJob,
            block
        ) { cause, value, parked ->
            if (cause != null) {
                parked.tryResumeException(cause)
            } else {
                parked.tryResume(value)
            }
        }
    }
}

/**
 * [withTimeout] that maps ITS OWN expiry to `null` instead of throwing.
 * A nested timeout's exception (different instance) still propagates.
 */
public suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend CoroutineScope.() -> T): T? {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        if (timeMillis <= 0) {
            null
        } else {
            val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
            val timeout = TimeoutCancellationException("Timed out after ${timeMillis}ms")
            DelayTracker.current.register(timeMillis) {
                if (!scopeJob.isCompleted) {
                    scopeJob.cancel(timeout)
                }
            }
            @Suppress("UNCHECKED_CAST")
            parkScopedBlock(
                continuation as Continuation<Any?>,
                outerContext + scopeJob,
                scopeJob,
                block
            ) { cause, value, parked ->
                if (cause === timeout) {
                    parked.tryResume(null)
                } else if (cause != null) {
                    parked.tryResumeException(cause)
                } else {
                    parked.tryResume(value)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Rebuild + device suite**

`./rebuild.sh`, preflight, `./run-stdlib-tests.sh`. Expected: previous + 8 green.

- [ ] **Step 4: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/coroutines/builders/Timeout.kt \
  libraries/stdlib/brs/test/kotlin/coroutines/ScopeFunctionsTest.kt \
  libraries/stdlib/brs/test/kotlin/TestMain.kt
git commit -m "stdlib: withTimeout/withTimeoutOrNull — deadline race over the scope-job engine

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 14: E2E timeout coverage (component regime)

**Files:**
- Modify: `../roku-test-app/components/fixtures/CoroutineUtilProbe.kt` (add modes)
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/CoroutineUtilityTests.kt` (add tests)

- [ ] **Step 1: Add fixture modes** — imports add `kotlin.coroutines.builders.withTimeout`, `kotlin.coroutines.builders.withTimeoutOrNull`, `kotlin.coroutines.builders.TimeoutCancellationException`; new branches before the closing of `onStartChanged`:

```kotlin
        } else if (runMode == "timeoutExpires") {
            launch {
                try {
                    withTimeout(80) {
                        delay(5000)
                    }
                    result = "unexpected"
                } catch (e: TimeoutCancellationException) {
                    result = "timeout"
                }
            }
        } else if (runMode == "timeoutWins") {
            launch {
                val v = withTimeout(5000) {
                    delay(30)
                    "v"
                }
                result = "win:" + v
            }
        } else if (runMode == "timeoutOrNull") {
            launch {
                val a = withTimeoutOrNull(50) {
                    delay(5000)
                    "a"
                }
                val b = withTimeoutOrNull(5000) {
                    delay(20)
                    "b"
                }
                result = "orNull:" + (a ?: "null") + "," + (b ?: "null")
            }
        }
```

- [ ] **Step 2: Add the tests** (append inside the `suite("CoroutineUtilities")` block):

```kotlin
        testAsync("withTimeoutExpiryThrowsTceOnDevice") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "timeoutExpires")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("timeout", "$result")
        }

        testAsync("withTimeoutBlockWins") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "timeoutWins")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("win:v", "$result")
        }

        testAsync("withTimeoutOrNullMapsOwnExpiryOnly") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "timeoutOrNull")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("orNull:null,b", "$result")
        }
```

- [ ] **Step 3: Build, run, commit**

`cd ../roku-test-app && ./rebuild-all.sh --all`, preflight, `./run-device-tests.sh` → 40/7 green. Commit in roku-test-app:

```bash
git add components/fixtures/CoroutineUtilProbe.kt src/brsTest/kotlin/tests/CoroutineUtilityTests.kt
git commit -m "tests: withTimeout E2E coverage (expiry, win, orNull) in the component regime

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

# Phase 4: flagship demo + docs

### Task 15: ShelfView concurrent fetch + runTask awaitAll acceptance test

**Files:**
- Modify: `../roku-test-app/components/ShelfView/ShelfView.kt` (the `init` launch block)
- Modify: `../roku-test-app/components/fixtures/CoroutineUtilProbe.kt` (add `awaitAllTasks` mode)
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/CoroutineUtilityTests.kt` (add test)

- [ ] **Step 1: Read `../roku-test-app/components/fixtures/EchoTask.kt`** and note its exact typed input/output field names and semantics (it is the Suite 3/4 echo fixture). The code below assumes `input: String` in / `output: String` out with echo semantics — ADJUST FIELD NAMES to what the file actually declares before writing the mode.

- [ ] **Step 2: Add the acceptance mode to CoroutineUtilProbe** (imports add `kotlin.coroutines.task.TaskException`, `kotlin.coroutines.task.runTask`, and the EchoTask package import used by TaskBoundary fixtures):

```kotlin
        } else if (runMode == "awaitAllTasks") {
            launch {
                try {
                    val first = async {
                        runTask<EchoTask> { input = "one" }
                    }
                    val second = async {
                        runTask<EchoTask> { input = "two" }
                    }
                    awaitAll(first, second)
                    result = "tasks:" + first.await().output + "," + second.await().output
                } catch (e: TaskException) {
                    result = "taskfail:" + (e.message ?: "?")
                }
            }
        }
```

Add the E2E test:

```kotlin
        testAsync("awaitAllOverConcurrentRunTasks") {
            val probe = Probes.create(scene, "CoroutineUtilProbe")
            probe.setField("mode", "awaitAllTasks")
            val result = roundTrip(probe, "start", true, "result")
            assertEquals("tasks:one,two", "$result")
        }
```

(Adjust the expected string to EchoTask's real echo transform if it decorates the value.)

- [ ] **Step 3: Rewrite ShelfView's fetch block** — in `../roku-test-app/components/ShelfView/ShelfView.kt`, add imports `kotlin.coroutines.awaitAll` and `kotlin.coroutines.builders.async`, and replace the `launch { ... }` block in `init` with:

```kotlin
        // Flagship concurrent-fetch demo: both runTasks are started as async
        // children and awaitAll joins them — the IP fetch and the shelf fetch
        // run on their task threads AT THE SAME TIME (previously sequential).
        launch {
            try {
                println("ShelfView: fetching ip + shelf items concurrently")
                val ipTask = async {
                    runTask<UrlTransferTask> {
                        url = "https://api.ipify.org?format=json"
                    }
                }
                val shelfTask = async {
                    runTask<FetchShelfTask> { count = 5 }
                }
                awaitAll(ipTask, shelfTask)
                val ip = ipTask.await()
                println("ShelfView: Got ip address = ${ip.response} (${ip.code})")
                shelfItems = shelfTask.await().items
                println("ShelfView: concurrent fetch complete, items published to the shelf")
            } catch (e: TaskException) {
                println("ShelfView: fetch failed: ${e.message}")
            }
        }
```

- [ ] **Step 4: Build, run E2E, eyeball the demo**

`cd ../roku-test-app && ./rebuild-all.sh --all`, preflight, `./run-device-tests.sh` → 41/7 green. Then launch the main app once (per roku-test-app's normal deploy flow) and check the console for the two ShelfView prints and the populated shelf.

- [ ] **Step 5: Commit (roku-test-app)**

```bash
git add components/ShelfView/ShelfView.kt components/fixtures/CoroutineUtilProbe.kt \
  src/brsTest/kotlin/tests/CoroutineUtilityTests.kt
git commit -m "ShelfView: concurrent ip+shelf fetch via async/awaitAll (flagship demo) + runTask awaitAll acceptance test

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 16: docs, memory, final gate sweep

**Files:**
- Modify: `CLAUDE.md`
- Modify: `/Users/Mike.Fougere/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/project_brs_suspend_codegen_defects.md`
- Modify: `.../memory/project_component_coroutine_scaffolding.md`
- Modify: `.../memory/MEMORY.md`

- [ ] **Step 1: CLAUDE.md updates**
- New section "Coroutine Utilities (awaitAll & friends)" after "Component Coroutine Scaffolding": one-screen summary — the utility surface (join/await/awaitAll/joinAll/coroutineScope/withContext/withTimeout/withTimeoutOrNull/ensureActive/isActive/SupervisorJob), the tail-delegation + `parked.finish()` idiom for future stdlib suspend functions, cancellation promises (entry checks + mid-park wakeup; task-thread work not stopped), supervisor roots (componentScope AND runBlocking — kotlinx divergence flagged), no cross-component Job sharing, unhandled-launch console prefix `[kotlin.coroutines]`.
- Update "Component Coroutine Scaffolding": remove any join()-busy-wait caveats; mention utilities.
- Update the "Current Gate Numbers" table with the new counts observed in Step 3 (goldens 59; stdlib 411+N; E2E 41 active/7 suites +3 xtests).
- Remove/replace the stale nested-if guidance if any remains that says Kotlin `&&` is unsafe (Task 2 already recorded the probe truth).

- [ ] **Step 2: Memory updates**
- `project_brs_suspend_codegen_defects.md`: move to FIXED — JobImpl.join busy-wait (now parked continuations), `@BrsInline`-adjacent `&&`/`||` non-short-circuit emission, expression-position try/catch. Leave `@BrsInline` single-line if/else splice as the remaining open item. Update the frontmatter description line.
- `project_component_coroutine_scaffolding.md`: append the utilities landing (date, commit range, supervisor-root decision incl. runBlocking divergence, parked.finish() idiom pointer to CLAUDE.md).
- `MEMORY.md`: update both hook lines to match.

- [ ] **Step 3: Final full gate sweep (all numbers recorded)**

```bash
./rebuild.sh
./run-compiler-tests.sh
ROKU_DEVICE_IP=192.168.1.125 ROKU_PASSWORD=pass ./run-stdlib-tests.sh
cd ../roku-test-app && ./run-device-tests.sh
```
All green; record exact counts into CLAUDE.md's gate table (Step 1). Preflight the console before each device run.

- [ ] **Step 4: Commit (Kotlin repo)**

```bash
git add CLAUDE.md
git commit -m "docs: coroutine utilities — usage, cancellation promises, updated gates

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```
(Memory files live outside the repo — no git action needed.)

---

## Plan self-review notes (kept for the executor)

- **Numbering truth:** goldens go 57 → 59 (Tasks 1, 3). Stdlib device adds ~66 tests across Tasks 2-13 (exact count recorded at each task's run). E2E goes 33/6 → 41/7 (Tasks 12: +4, 14: +3, 15: +1).
- **Order matters:** Task 5 must precede 6 (temp bodies), 10 must precede 11 (supervisor runBlocking keeps scope tests honest), 13 must precede 14/15 (fixture imports withTimeout).
- **Known unknowns each executor must verify in place:** exact Ir* constructor shapes in this Kotlin version (Task 3), `genCtx.nextTempId()` availability (Task 1), vararg `.toList()` on the BRS backend (Task 8 — suspend+vararg has ZERO precedent in this stdlib, fallback included), EchoTask field names (Task 15), `brs()` splice local-name mangling (Task 2 probe).
