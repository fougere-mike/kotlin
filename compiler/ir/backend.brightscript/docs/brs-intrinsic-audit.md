# `brs()` Intrinsic — Audit (Workstream A4)

**Status:** Read-only audit. Nothing is fixed here. The purpose is to establish a shared understanding of what the intrinsic does today so B4 (hardening) can be scoped accurately.

**Audit date:** 2026-04-21, against branch `feature/brightscript-backend-2.2.20`.

---

## What it is

`brs(code: String): Dynamic` is an external Kotlin function declared in `libraries/stdlib/brs/src/kotlin/brs/core.kt:38`:

```kotlin
public external fun brs(code: String): Dynamic
```

It lets Kotlin source embed raw BrightScript code as an expression. Concept is analogous to Kotlin/JS's `js("...")` intrinsic.

Example from the golden test `compiler/testData/codegen/brs/inline/brsFunction.kt`:

| Kotlin | Compiled BRS |
|---|---|
| `return brs("1 + 2")` | `return 1 + 2` |
| `val x = brs("3 * 4")` | `x = 3 * 4` |
| `brs("m.top.visible = true")` | `m.top.visible = true` |
| `return brs("return 42")` | `return 42` |

## Where it's handled

### Declaration
- `libraries/stdlib/brs/src/kotlin/brs/core.kt:38` — the `external fun` declaration and its KDoc.

### Lowering / codegen
Everything downstream is in `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrToBrsTransformer.kt`:

- **Lines 7591–7627** — the `"brs" ->` branch of the intrinsic dispatcher (method `transformStdlibIntrinsic`).
- **Lines 7634–7652** — the `foldBrsCodeString(...)` helper that reduces an `IrExpression?` argument to a compile-time `String?`. Handles `IrConst(String)` directly and `IrStringConcatenation` recursively. Returns `null` for anything else.

### Parser
- `brightscript/brs.ast/src/org/jetbrains/kotlin/brs/backend/ast/parser/BrsParser.kt:896` — `parseBrightScriptStatements(source: String): BrsParseResult<List<BrsStatement>>`. This is what the lowering invokes on the folded string. The parser is 915 LOC; the lexer (`BrsLexer.kt`) is 479 LOC.
- `brightscript/brs.ast/src/org/jetbrains/kotlin/brs/backend/ast/parser/BrsParser.kt:874` — `data class BrsParseResult<T>(result: T, errors: List<String>) { val hasErrors: Boolean ... }`. The intrinsic reads both fields.

### Symbol resolution
- `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsIntrinsics.kt` and `BrsSymbols.kt` — register `kotlin.brs.brs` so the IR backend can match calls. Dispatch is by unqualified name `"brs"` in `IrToBrsTransformer.kt:7591`.

## What it actually does at compile time

Walk the lowering branch at `IrToBrsTransformer.kt:7591–7627`:

1. **Get the argument.** `expression.getValueArgument(0)` — the single `code: String` parameter.
2. **Fold it to a literal.** `foldBrsCodeString(codeArg)` accepts:
   - `IrConst(kind=String)` — returns `.value as String`.
   - `IrStringConcatenation` — recursively folds each `arg`; if any is non-foldable, returns `null`.
   - Anything else — returns `null`.
3. **If `null`** → emits to **`System.err.println`** the message `"brs() argument must be a compile-time constant string"` and returns `BrsInvalidLiteral()`. **This is the first `System.err` exit** (`IrToBrsTransformer.kt:7596`).
4. **Parse the folded string.** Calls `parseBrightScriptStatements(codeString)`.
5. **If parse errors** → iterates `parseResult.errors`, emits each via **`System.err.println`** prefixed with `"Error in brs() code: "`. Returns `BrsInvalidLiteral()`. **Second `System.err` exit** (`IrToBrsTransformer.kt:7604`).
6. **If parse success**, examines the resulting `List<BrsStatement>`:
   - Empty → `BrsInvalidLiteral()` (silent "error").
   - One statement, a `BrsReturn` with non-null value → returns the value.
   - One statement, a `BrsExpressionStatement` → returns its expression.
   - Multiple statements → falls through to "try the last statement": if it's `BrsExpressionStatement`, returns its expression; if `BrsReturn`, its value; else `BrsInvalidLiteral()`. **No diagnostic is emitted for this case** — the intermediate statements are silently dropped from the output.

Note on #6: `brs("m.top.visible = true")` in `brsFunction.kt` is a *statement* (`BrsExpressionStatement` wrapping an assignment), and the test's golden output confirms it's emitted as a standalone BRS statement in context. For this to work, the intrinsic's emit site must be able to produce a statement-level BRS node, not just an expression — which means the "return type `Dynamic`" in Kotlin signatures is a fiction for statement-style uses. (The lowering output is an expression node that happens to be used as a statement because the enclosing context discards the value.)

## Confirmed call sites

- `compiler/testData/codegen/brs/inline/brsFunction.kt:10, 14, 18, 22` — 4 uses in a golden test.
- `libraries/stdlib/brs/src/kotlin/brs/core.kt:31, 32` — in KDoc examples, not real calls.

**Zero production call sites** in `libraries/stdlib/brs/**` or `libraries/stdlib/brs-actual/**`. The stdlib does not use `brs()` internally. This is important: we have freedom to tighten the contract without breaking stdlib.

Downstream projects (`kotlin-roku`, `roku-test-app`) are not in this repo; assume they may call `brs()` and plan for that compatibility concern.

## Known problems (for B4 to address)

These are listed as **observations** only. No fix is proposed here.

1. **`System.err.println` at `IrToBrsTransformer.kt:7596, 7604`.** Breaks the MessageCollector contract — errors don't get source locations, severity, or KT-codes. Reviewers will flag this.

2. **Backend-phase error reporting.** A non-literal argument or parse error only surfaces after the IR lowering pipeline has run. User has already waited ~10s for an error a FIR-phase check could have delivered in ~100ms with a source pointer.

3. **Silent multi-statement drop** (`IrToBrsTransformer.kt:7614–7623`). If someone writes `brs("x = 1 : y = 2 : z = x + y")`, statements `x = 1` and `y = 2` are silently dropped from the output; only `z = x + y` survives. Compare to the explicit "return value of last expression" semantics of Kotlin's own block expressions — the intrinsic is supposed to mimic an expression, but a user who writes it as a multi-statement block gets data loss instead of a diagnostic.

4. **`BrsInvalidLiteral()` return on error** is indistinguishable from a successful `brs("invalid")` call. Downstream users see nothing in the compiled output; the error exists only in stderr (which tests may be capturing, but IDE users won't see as a proper diagnostic).

5. **No restriction on what BRS is embedded.** A user can embed `CreateObject("..."), eval()` or `library "..."` statements (if the parser accepts them) and the compiler will happily pass them through. Some of these collide with compiler-managed state.

6. **No FIR-phase check that the caller is where `brs()` is valid.** If someone calls `brs("...")` from `commonMain` (without an `expect`/`actual` split), the backend happily lowers it, producing non-portable commonMain code. This is the scenario covered by the `BRS_MPP_TARGET_MISSING` diagnostic proposed in B2.

7. **Parser surface is large** (915 LOC `BrsParser.kt` + 479 LOC `BrsLexer.kt`). The intrinsic exposes the *entire* BRS parser to user-submitted strings at compile time. Any bug in the parser becomes a compile-time-code-execution risk. Not a security concern (the parser doesn't execute the code), but a stability concern: a malformed `brs()` argument that crashes the parser crashes the compiler.

## B4 scope (for reference — not in this audit)

When the team starts B4, these are the obvious moves, roughly in order of impact:

- (a) Replace `System.err.println` at lines 7596 and 7604 with `context.messageCollector.report(CompilerMessageSeverity.ERROR, message, location)`. Location comes from the `IrCall`'s `startOffset`/`endOffset` + the enclosing IrFile's path.
- (b) Add a FIR-phase checker `FirBrsIntrinsicArgChecker` (new file under `compiler/fir/checkers/checkers.brs/src/.../expression/`) that emits `BRS_INTRINSIC_LITERAL_REQUIRED` for calls whose argument doesn't fold to a compile-time string. This is the FIR-analog of what `foldBrsCodeString` does at IR time.
- (c) Reject multi-statement `brs()` calls with a diagnostic instead of silently dropping (`IrToBrsTransformer.kt:7614`). Option: offer a sibling `brsBlock { """...""" }` function for the legitimate multi-statement case, OR accept that multi-statement should be written as multiple `brs()` calls (each a proper single expression/statement). The test case `brs("m.top.visible = true")` is already a single statement and would still work.
- (d) Write a contract document at `compiler/ir/backend.brightscript/docs/brs-intrinsic.md` — the "public spec" of the intrinsic — that replaces the loose KDoc at `core.kt:23–36` with something a compiler-reviewer or a user can cite.

None of these should be done in this audit phase.

## Open questions for the team

1. Do any downstream repos (`kotlin-roku`, `roku-test-app`) call `brs()` with multi-statement strings? If yes, B4's option (c) needs a deprecation path, not a hard rejection.
2. Should `brs()` support a constant-folded `@BrsConstant` reference, or only literal string expressions? Example: `brs("print \"${SOME_CONST}\"")` where `SOME_CONST` is a `const val` — does `foldBrsCodeString` need to walk `IrGetValue` to resolve it? Today it does not.
3. Is the compile-time-code-execution concept itself acceptable for upstream, or does it need a feature flag (`-Xenable-brs-intrinsic`, off by default) to signal it's not part of the stable language surface? This ties into the C1 KEEP conversation.

## File index

| File | Role |
|---|---|
| `libraries/stdlib/brs/src/kotlin/brs/core.kt:38` | Declaration |
| `compiler/ir/backend.brightscript/src/.../IrToBrsTransformer.kt:7591–7627` | Dispatch + diagnostics |
| `compiler/ir/backend.brightscript/src/.../IrToBrsTransformer.kt:7634–7652` | `foldBrsCodeString` helper |
| `brightscript/brs.ast/src/.../parser/BrsParser.kt:896` | `parseBrightScriptStatements` entry point |
| `brightscript/brs.ast/src/.../parser/BrsParser.kt:874` | `BrsParseResult` container |
| `compiler/testData/codegen/brs/inline/brsFunction.kt` | The only test |
| `compiler/testData/codegen/brs/inline/brsFunction.brs.txt` | Its golden output |
