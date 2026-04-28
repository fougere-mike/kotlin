# BRS Component Extractor: Supertype Walk for Inherited Annotations

## Context

`BrsComponentExtractor` is the IR-phase component that turns Kotlin `@BrsComponent` classes into the SceneGraph component XML and the BrightScript field/export/observer wiring that Roku consumes. Today it iterates `irClass.declarations` directly, never walking supertypes. This is silently wrong in three places:

1. `findOnChangeHandler` (`BrsComponentExtractor.kt:355`) — when a `@BrsOnChange("foo")` annotation references a handler `foo` that is declared on a base class, IR fails the local lookup and falls back to returning the raw, un-mangled name (line 369). The generated XML observes a function name that does not exist in the emitted `.brs`. At runtime, the SceneGraph engine fires the field-change observer pointing at the bogus name and silently does nothing. The KDoc at line 369-370 says "this will be caught by FIR checker" — but FIR's `FirBrsOnChangeHandlerChecker` (line 65) explicitly **accepts** inherited handlers via `unsubstitutedScope`. So the FIR/IR gap is a known divergence: FIR says yes, IR fails to wire it up.

2. `extractFields` (line 168) — iterates `irClass.declarations.filterIsInstance<IrProperty>()` and `IrField`. Inherited properties carrying `@SGStringField`/`@BrsField`/etc. on a base class are never seen. The component XML omits inherited fields.

3. `extractExports` (line 385) — iterates `irClass.declarations.filterIsInstance<IrSimpleFunction>()`. Inherited `@BrsExport` functions are never seen. The component does not register them.

The result: a user can write a base class encapsulating component fields, handlers, and exports; subclass it with `@BrsComponent`; and ship code that compiles, passes type checks, and fails silently on the device. The bug is wrong-output codegen, not a missing diagnostic.

This spec closes the FIR/IR divergence by giving the IR extractor a single supertype-walking helper used by all three call sites.

## Goal

`BrsComponentExtractor` collects fields, exports, and on-change handlers from the entire type hierarchy of an `@BrsComponent` class, with subclass-overrides-base semantics. Existing component output for non-inheriting classes is unchanged byte-for-byte.

## Design

### One private helper

A single private inline-reified generic on `BrsComponentExtractor`:

```kotlin
private inline fun <reified T : IrDeclaration> collectInheritedDeclarations(
    irClass: IrClass
): List<T> {
    val visited = mutableSetOf<IrClass>()
    val result = mutableListOf<T>()
    val queue = ArrayDeque<IrClass>()
    for (s in irClass.superTypes) s.classOrNull?.owner?.let(queue::add)
    while (queue.isNotEmpty()) {
        val cur = queue.removeFirst()
        if (!visited.add(cur)) continue
        for (decl in cur.declarations) if (decl is T) result.add(decl)
        for (s in cur.superTypes) s.classOrNull?.owner?.let(queue::add)
    }
    return result
}
```

- BFS over the type hierarchy excluding the class itself, identical traversal shape to the existing `hasSceneGraphComponentInHierarchy` (lines 98–123) so the precedent is obvious to readers.
- `visited` set keyed on `IrClass` identity handles diamond inheritance.
- Returns BFS-ordered list (closer ancestors first) so that "first match wins" deduplication is deterministic and stable across compilations.
- Cross-module annotation read-back already works in production: `hasSceneGraphComponentInHierarchy` calls `findAnnotation` on supertype `IrClass` instances today and is used by every `@BrsSceneGraphComponent` extension in `roku-test-app`.
- The BFS is a flatter walk than FIR's `unsubstitutedScope` (which honors Kotlin visibility and override rules). For the three concerns here — finding a named handler function, finding annotated properties/fields, finding annotated functions — the divergence is benign: FIR errors out earlier on visibility violations, so IR only ever runs on classes where FIR's stricter resolution accepted the inheritance. The BFS is a superset of what FIR allowed in.

### Three call-site updates (override-wins)

**`findOnChangeHandler` (line 355):** when the local `parentClass.declarations` lookup misses, fall back to `collectInheritedDeclarations<IrSimpleFunction>(parentClass).find { it.name.asString() == handlerName }`. The BFS order means a closer ancestor's handler shadows a more distant one. Same for the auto-convention path. The line-369 fallback that returns the raw `handlerName` is removed — every reachable handler is now mangled correctly.

**`extractFields` (line 168):** after the existing two loops over `irClass.declarations`, run two more passes over `collectInheritedDeclarations<IrProperty>(irClass)` and `collectInheritedDeclarations<IrField>(irClass)` using the same per-decl logic. The existing dedup guard (`fields.none { it.name == ... }`) generalizes naturally: a subclass override that re-states the annotation produces a local-pass entry and the inherited pass skips by name; an override that drops the annotation produces no local-pass entry and the inherited pass adds the base's. Override-wins falls out of execution order.

**`extractExports` (line 385):** symmetric. After the local pass, run `collectInheritedDeclarations<IrSimpleFunction>(irClass)`, add `@BrsExport`-annotated functions whose `name` is not already in `exports`. Override-wins by execution order.

### Override-wins case analysis

| Case | Local pass produces? | Inherited pass adds? | Result |
|---|---|---|---|
| Subclass `override @SGStringField val title` (re-annotated) | YES | NO (name match) | override wins |
| Subclass `override val title` (no annotation; base has `@SGStringField`) | NO | YES | base annotation used |
| Subclass `override @SGIntegerField val title` (changes type) | YES (integer) | NO (name match) | override wins, integer |
| No override; base declares `@SGStringField val title` | NO | YES | base annotation used |

`findAnnotation` (line 443-456) reads only the declaration's own annotations, never inherited — so the no-annotation override case correctly produces no local entry.

### Test harness extension

`AbstractBrsGoldenFileTest.compileKotlinToBrightScript` (line 138-149) currently filters output to `*.brs` files only. `BrsCompiler.kt:1934-1946` writes a `.xml` file per component plus an optional `.deps.json`. The harness must include the XML in its sorted-and-concatenated output so component fixtures can validate the generated XML. Concretely: change the filter from `extension == "brs"` to `extension in setOf("brs", "xml")` and keep the existing `// --- File: …` separator format so existing goldens are unaffected. Optionally include `.deps.json` if a fixture needs cross-component dependency assertions; not required for this work.

This is the only test-infrastructure change. Existing `basicComponent`, `sceneComponent`, and `componentWithMAccess` fixtures contain plain Kotlin functions (no `@BrsComponent`) and produce no XML, so they are unaffected.

### Test fixtures (5 new pairs under `compiler/testData/codegen/brs/components/`)

Each is a `.kt` input plus a `.brs.txt` golden:

- `inheritedField.kt` — open base class with `@SGStringField val title: String = ""`; subclass `@BrsComponent class Foo : Base()`. Golden contains a component XML for `Foo` with `<field id="title" type="string" />`.
- `inheritedExport.kt` — open base class with `@BrsExport fun handleTap()`; subclass `@BrsComponent`. Golden's component XML for `Foo` registers `handleTap` as exported.
- `inheritedOnChangeHandler.kt` — open base class with `@SGStringField @BrsOnChange("onTitleChanged") val title: String = ""` and `fun onTitleChanged()`; subclass `@BrsComponent`. Golden's component XML references the mangled `onTitleChanged` name from the BFS lookup.
- `overriddenFieldKeepsBaseAnnotation.kt` — base has `@SGStringField val title`; subclass `override val title` (no annotation). Golden's XML still has the field; covers the override-without-annotation fallback.
- `overriddenFieldChangesAnnotation.kt` — base has `@SGStringField val title`; subclass `override @SGIntegerField val title`. Golden's XML has `type="integer"`; covers override-wins replacement.

Test methods added to `BrsComponentGoldenFileTests` in `compiler/ir/backend.brightscript/test/.../BrsGoldenFileTests.kt`.

### What's *not* in scope

- **No FIR-side changes.** `FirBrsOnChangeHandlerChecker` already walks supertypes correctly. The work is entirely IR-side.
- **No new diagnostic.** This is not a checker — it's a codegen fix. Existing diagnostics 5 and 7 are unaffected.
- **No change to `mergeInterfaceFields` or `extractLayout`.** Layout is companion-object-driven and doesn't have the inheritance gap.
- **No change to `getExtendsComponent`.** That method already walks one level of supertype to find `@BrsSceneGraphComponent`; the fix here is orthogonal to component-base-class resolution.

## File map

| File | Action |
|---|---|
| `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsComponentExtractor.kt` | Modify — add `collectInheritedDeclarations<T>` helper; update `findOnChangeHandler`, `extractFields`, `extractExports`; remove stale fallback comment at line 369-370 |
| `compiler/ir/backend.brightscript/test/.../AbstractBrsGoldenFileTest.kt` | Modify — extend file-extension filter to include `xml` |
| `compiler/ir/backend.brightscript/test/.../BrsGoldenFileTests.kt` | Modify — add 5 `@Test` methods to `BrsComponentGoldenFileTests` |
| `compiler/testData/codegen/brs/components/inheritedField.kt` + `.brs.txt` | Create |
| `compiler/testData/codegen/brs/components/inheritedExport.kt` + `.brs.txt` | Create |
| `compiler/testData/codegen/brs/components/inheritedOnChangeHandler.kt` + `.brs.txt` | Create |
| `compiler/testData/codegen/brs/components/overriddenFieldKeepsBaseAnnotation.kt` + `.brs.txt` | Create |
| `compiler/testData/codegen/brs/components/overriddenFieldChangesAnnotation.kt` + `.brs.txt` | Create |
| `memory/project_brs_fir_diagnostics_state.md` | Modify — strike the "IR extractor parity" deferred follow-up; remove stale "cross-file `@BrsStatic` overload" entry (Diagnostic 11 already handles cross-file via `FirPackageMemberScope`) |

## Verification

End-to-end:

```bash
./rebuild.sh
./run-compiler-tests.sh
```

Expectations:
1. The 5 new `@Test` methods pass (after running once with `-PupdateGoldenFiles=true` to seed the goldens, then locking them).
2. The 3 pre-existing `BrsComponentGoldenFileTests` (`basicComponent`, `sceneComponent`, `componentWithMAccess`) pass unchanged — their inputs don't use `@BrsComponent`, so the harness change is a no-op for them.
3. All other golden-file tests pass unchanged — the harness change is additive (broader filter, never narrower).
4. The step-7 gate (`:kotlin-test-brs:build`) passes — no new FIR errors fire on `kotlin.test` sources (this work doesn't add FIR checks).

Optional cross-check on `roku-test-app`:

```bash
cd ../roku-test-app && ./rebuild-all.sh --all
./run-device-tests.sh   # if ROKU_DEVICE_IP / ROKU_PASSWORD set
```

Existing component tests there don't exercise inheritance, so they should pass unchanged. If a future test adds inheritance, it'll exercise the new path on a real device.
