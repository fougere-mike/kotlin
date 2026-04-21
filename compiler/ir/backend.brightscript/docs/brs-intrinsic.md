# `brs()` Intrinsic — Contract

**Status:** Normative. This document is the source of truth for what `brs("…")` accepts and produces. The implementation at `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrToBrsTransformer.kt:7591` must conform. If implementation and contract disagree, one of them is a bug.

**Companion docs:**
- [brs-intrinsic-audit.md](./brs-intrinsic-audit.md) — the read-only A4 audit that preceded this contract. Captures historical state and lists the B4 hardening scope.

---

## Purpose

`brs(code: String): Dynamic` embeds raw BrightScript into the compiler's output. It is the Kotlin-to-BrightScript analog of Kotlin/JS's `js("…")` intrinsic. Declared at `libraries/stdlib/brs/src/kotlin/brs/core.kt:38`:

```kotlin
public external fun brs(code: String): Dynamic
```

Use it when Kotlin-level code cannot express a target-native operation that BrightScript can — e.g. touching a SceneGraph node field, calling a Roku SDK function the stdlib does not yet wrap, or emitting BrightScript syntax the compiler does not have a code path for.

Prefer a proper stdlib wrapper or a `@BrsCreateObject` external interface if one exists or could exist. `brs()` is the escape hatch, not the first choice.

## Input: the `code` argument

### Must be a compile-time constant

The argument is constant-folded at IR time. The folder (`foldBrsCodeString` in `IrToBrsTransformer.kt:7634`) walks:

| IR node | Handling |
|---|---|
| `IrConst` with `IrConstKind.String` | value used directly |
| `IrStringConcatenation` | each fragment folded recursively; all must fold |
| anything else (including `IrGetValue` for `const val`) | fold fails → compile error |

**Known gap** (audit §Open-questions-2): `const val` references are not walked today. `brs("print ${SOME_CONST}")` where `SOME_CONST` is a `const val` of a `String` will not fold. A future enhancement could walk `IrGetValue` to resolve compile-time-constant property references. Track with a new issue if you hit this in practice.

If the fold fails, the compiler emits:

```
error: brs() argument must be a compile-time constant string
```

and returns a placeholder (`BrsInvalidLiteral`) from the intrinsic so the rest of the IR can continue lowering.

### Must be non-empty

A `brs("")` call is a contract violation. Empty input is rejected with:

```
error: brs() argument parsed to no statements; expected a single expression or statement
```

## Output: the parsed BrightScript

After folding, the string is passed to `parseBrightScriptStatements(…)` at `brightscript/brs.ast/src/org/jetbrains/kotlin/brs/backend/ast/parser/BrsParser.kt:896`. The parser returns a `List<BrsStatement>` plus any errors encountered.

### If the parser reports errors

Each parser error is forwarded through `MessageCollector` as a separate `ERROR`-severity diagnostic prefixed with `Error in brs() code:`. The intrinsic returns `BrsInvalidLiteral`.

### If the parser succeeds

The resulting statement list **must have exactly one element.** What that element may be:

| Parsed shape | Example | Emitted output | Call-site value |
|---|---|---|---|
| `BrsReturn(expr)` | `brs("return 42")` | `return 42` | value of `expr` (when appearing in expression position — see below) |
| `BrsExpressionStatement(expr)` | `brs("1 + 2")` | `1 + 2` | value of `expr` |
| any other `BrsStatement` (assignment, method call, …) | `brs("m.top.visible = true")` | `m.top.visible = true` | `invalid` (statement has no value) |

Zero or more-than-one statements are rejected:

```
error: brs() must contain exactly one expression or statement; got N. Split into multiple brs() calls instead.
```

If you need multiple BrightScript statements, write multiple `brs()` calls. The intrinsic is not a block-of-statements facility.

## Examples

### Valid

```kotlin
// Expression returned from a function:
return brs("1 + 2")                    // → return 1 + 2

// Expression bound to a local:
val x = brs("3 * 4")                   // → x = 3 * 4

// Statement used for its side effect:
brs("m.top.visible = true")            // → m.top.visible = true

// Explicit return wrapping:
return brs("return 42")                // → return 42

// Template where every piece folds to a literal:
val msg = "hello"
brs("print \"${msg}\"")                // → print "hello"
```

### Invalid — caller error

```kotlin
// Runtime variable — not a compile-time constant:
val name: String = getName()
brs(name)                              // ERROR: brs() argument must be a compile-time constant string

// const val references are a known gap:
const val GREETING = "hi"
brs("print \"${GREETING}\"")           // ERROR (current implementation)

// Empty string:
brs("")                                // ERROR: parsed to no statements

// Multiple statements in one call:
brs("x = 1 : y = 2 : z = x + y")       // ERROR: must contain exactly one expression or statement; got 3
```

### Invalid — parser error

```kotlin
// BrightScript syntax error inside the string:
brs("return return")                   // ERROR: Error in brs() code: <parser diagnostic>
```

## Error surface

| Property | Today | Future (B4(b)) |
|---|---|---|
| Severity | `CompilerMessageSeverity.ERROR` | unchanged |
| Routing | `MessageCollector` via `BrsIrBackendContext.reportError` | `DiagnosticReporter` via FIR checker |
| Source location | **Declaration-scoped only** — points at the enclosing function/property, not the `brs()` call itself | Expression-scoped (file + line + column of the `brs(…)` token) |
| Phase | IR backend (after lowering; ~10s wait for the user) | FIR frontend (~100ms, same trip as other type errors) |

The declaration-scoped location is a known limitation of `BrsIrBackendContext.reportError` at `BrsIrBackendContext.kt:759` — `IrCall` is not an `IrDeclaration`, so the message collector receives `location = null` and falls back to whatever surrounds the call. A FIR-phase checker `FirBrsIntrinsicArgChecker` (workstream B4(b)) will supersede the backend-phase check and thread the call's source range into the diagnostic.

## Non-goals

- **No runtime evaluation.** The string is parsed and embedded at compile time. There is no BrightScript interpreter inside the Kotlin compiler.
- **No symbol resolution back into Kotlin scope.** Identifiers in the BrightScript string are passed through verbatim to the BrightScript runtime. They do not resolve against Kotlin `val`/`fun` names, imports, or packages.
- **No interpolation of Kotlin values beyond compile-time folding.** `"${someVar}"` where `someVar` is not a compile-time constant fails to fold; there is no runtime `String.format`-style substitution.
- **No support for multi-statement blocks.** If you want three BrightScript statements, write three `brs()` calls.
- **No feature flag.** `brs()` is unconditionally available when the BRS backend is in use. Whether that remains true in an eventual upstream submission is an open question (audit §Open-questions-3).

## Conformance

An implementation conforms to this contract iff for every well-formed Kotlin program:

1. Every rejected input listed under **Invalid** above produces an `ERROR`-severity diagnostic through the message collector, and the compilation does not succeed with silent fallback output.
2. Every accepted input listed under **Valid** emits the corresponding BrightScript as shown, unchanged.
3. No `System.err.println`, `println`, or `throw` is used to surface errors to the user. All diagnostics route through `MessageCollector`.

The test at `compiler/testData/codegen/brs/inline/brsFunction.kt` covers (2). Coverage for (1) is manual today; a formal diagnostics harness is part of B4(b).
