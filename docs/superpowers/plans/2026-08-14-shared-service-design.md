# SharedService: Reference-Shared Classes Across Components — Design

**Date:** 2026-08-14
**Status:** Approved design, pending implementation plan
**Branch:** `feature/brightscript-backend-2.2.20`
**Implements:** decision A4 of `docs/superpowers/plans/2026-08-12-scopehandle-design.md`
(Addendum A.1/A.4: shared-VM option (ii) + toolchain demotion path, MVVM-layer floor
OS 15.0+) — with the demotion **shipped as the normal compilation mode** rather than
held as an exit (decision 1 below). Platform facts of record:
`spikes/scope-handle-spike/FINDINGS.md`.

## 1. Decisions made during brainstorming (Mike, 2026-08-14)

| # | Decision | Choice |
|---|----------|--------|
| 1 | Scope | Core sharing layer + **ship static dispatch in this program** (not verify-only): with call sites never reading fn slots and fn-typed properties FIR-banned, the dependence on Roku's officially-disclaimed fn-ref behavior disappears from every normal path. StateFlow, @AppScoped DI, spawnTask stay separate programs |
| 2 | Marker | ONE stdlib type: `abstract class SharedService` (kotlin.brs). **No stdlib ViewModel** — the toolchain has no opinion about app architecture; apps mint their own vocabulary (`abstract class ViewModel : SharedService()`, `Store`, `Presenter`, ...). All machinery keys on the SharedService supertype walk; no name allowlists, no annotations |
| 3 | Hierarchies | REAL base classes are legal — members, overridable hooks, any depth (Mike's requirement: common state-delivery/action/lifecycle code in a VM base). Enabled by data-based virtual dispatch (decision 4). The only structural rule: concrete descendants are final |
| 4 | Dispatch | Final methods → direct static calls. Open/abstract methods reached through a base static type (incl. base-internal `this.hook()`) → generated `__proto`-name dispatchers (virtual dispatch over DATA — closed-world per compilation). `super.f()` → direct static call to the base impl. Fn slots kept as thin forwarding wrappers (compatibility for residual shapes; never load-bearing) |
| 5 | Public surface | `shareOn(node, instance[, key])` / `sharedFrom<T>(node[, key])` / `sharedFromOrNull` / `SharedService.isLive()` / `canShare()`, all in kotlin.brs. Keys compiler-derived from the class name by default (both ends have the type — unlike ScopeRequest), explicit `key` for multi-instance |
| 6 | Liveness | Identity, not flags: acquisition returns GetRef references (live by construction); `isLive()` = roUtils.IsSameObject against the stash. A boolean field cannot discriminate a husk (it would be copied too) |
| 7 | Floor | `shareOn` THROWS on pre-OS-15 with a guided message naming the floor decision (no silent field-copy fallback); `canShare()` for apps that gate features |
| 8 | Republish | Same key on same node replaces; stale references from the prior generation fail `isLive()`. Pairs with the recreate-don't-reuse screen convention |
| 9 | Canary | The fn-slot-through-SetRef canary is a NORMAL red-capable E2E test labeled CANARY, with docs saying what its failure means (Roku changed the disclaimed behavior; wrapper-slot residuals dead; normal operation unaffected) |
| 10 | Adjacent (recorded, not scoped) | Component-input DX pair: `createComponent<T> { field = v }` configure-lambda (runTask precedent) + `@SGRequired` inputs with a generated `onInputsReady()` lifecycle hook. Constructor-parameter syntax for SG inputs is rejected on principle: the platform has no creation-time argument channel, and the syntax would promise init-time availability it cannot deliver. **SUPERSEDED** by 2026-09-04-component-lifecycle-design.md (constructor inputs land in plan B). |

## 2. Context and load-bearing facts

Why: ScopeHandle (shipped 2026-08-14) gives cross-component *execution*; this program
gives cross-component *objects* — the shared ViewModels and services the MVVM track
is for. Everything below is device-verified (spike FINDINGS) or explorer-verified
against the current backend (2026-08-14).

- **Dispatch today is slot dispatch.** `vm.refresh()` → `BrsMethodCall` →
  `vm.refresh_k_(...)` through a function slot the constructor attached
  (`IrExpressionToBrsTransformer.kt:1645` emission; `addMethodAttachments`
  IrToBrsTransformer.kt:2838-2938 attachment; no final/open distinction anywhere in
  emission). Cross-component, slot invocation is exactly the behavior Roku's doc
  disclaims ("do not build dependencies on it").
- **The receiver-as-argument shape already exists.** Extensions, primitive-receiver
  methods, and companion calls all compile as global calls with the receiver in the
  argument list (`:1705/:1748`, `:1214-1388`, `:1394-1416`). Static dispatch for
  shared classes reuses this proven shape — it is not a new calling convention.
  Stage 2's `<this>`-rendering fixes cleaned up this parameter class.
- **`__proto` is pure data and survives everything** — nested class-name string
  arrays (ctor emission :2588-2634), read by `__kotlin_isInstanceOf`
  (BrsCompiler.kt:922-1031) with no fn-slot involvement. Dispatching on proto names
  is dispatching on data.
- **SetRef/GetRef facts** (spike addendum): genuine shared identity, OS 15.0+,
  render-thread-only, AA fields only, incompatible with queueFields, observer-SILENT
  stash; `roUtils.IsSameObject` is the identity oracle; a full Kotlin object crosses
  alive today but its fn-slot usability rides the disclaimed behavior. **No stdlib
  bindings exist** — the spike used `@BrsInline` splices
  (`spikes/scope-handle-spike/kotlin-probe/components/shared/SpikeHelpers.kt`), which
  are the binding precedent. Doc quirk of record: SetRef's signature line says void;
  its Return Value section and the device say Boolean.
- **Closed world is the current reality**: apps are single-module (already the
  documented constraint for ScopeHandle `run{}` blocks); dispatcher generation
  enumerates the compilation's concrete shared classes, same machinery family as the
  binding tables (BrsCompiler.populateScopeBindingTables precedent).
- Publish/acquire precedent shapes: ScopeApi.kt (`exposeScope` arm-before-advertise,
  guided ISEs, per-component holder objects, `kotlinScope*` hook naming).

## 3. Public surface (package kotlin.brs, default-imported)

```kotlin
public abstract class SharedService {
    public fun isLive(): Boolean       // roUtils.IsSameObject vs the stash entry; false if never shared
    // internal: stash back-reference (node + key), set by shareOn
}

public fun shareOn(node: RoSGNode, instance: SharedService): Unit          // key = class name
public fun shareOn(node: RoSGNode, instance: SharedService, key: String): Unit
public fun canShare(): Boolean                                             // OS 15+ SetRef available?
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode): T
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode, key: String): T
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode): T?
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode, key: String): T?
```

Semantics:

- **Stash:** one runtime-added AA field `__kotlinShared` per publishing node, holding
  key→instance entries, written via `SetRef` (never ordinary setField — that would
  copy). Advertisement is implicit in `CanGetRef` succeeding; acquisition validates
  and throws guided ISEs: "nothing shared under key '<k>' on node '<id>' —
  shareOn(node, instance) in the owner first, or use sharedFromOrNull" / the pre-15
  floor message at `shareOn`: "SharedService requires Roku OS 15.0+ (SetRef) — the
  MVVM-layer floor decision (design A4); gate with canShare()".
- **Keys compiler-derived:** publish reads the instance's `__proto` head; acquire
  lowers `reified T` to the same class-name string `is`-checks use. Both ends derive
  the same name with zero ceremony; explicit `key` covers two-instances-of-one-type.
  (Unlike ScopeRequest, no hand-written names: both ends are typed.)
- **Live by construction:** `sharedFrom` returns the `GetRef` reference. There is no
  API path that returns a copy. `isLive()` is defense-in-depth/debug, not routine.
- **Type check on acquire:** `sharedFrom<T>` verifies the entry `is T` (proto walk)
  — a key collision across types is a guided ISE, not a husk-shaped surprise.
- Republish replaces (decision 8). `shareOn` is render-thread/component-context only
  (ambient checks mirroring ScopeHandle's; guided ISE otherwise).

Canonical usage (spec example of record):

```kotlin
abstract class ViewModel : SharedService() {            // APP-owned vocabulary + common code
    protected fun log(t: String, m: String) { ... }     // final member: static call
    abstract fun onAction(action: String)               // hook: dispatcher-served
}
class ApiClient(baseUrl: String) : SharedService() { ... }
class GuideVm(private val owner: ScopeHandle, private val api: ApiClient) : ViewModel() {
    var selectedDay: Int = 0
    override fun onAction(action: String) { ... }
    suspend fun refresh(): Int = owner.run { ... }      // composes with ScopeHandle
}
// Scene bootstrap:      shareOn(top, ApiClient("https://..."))         // app-wide services live on the SCENE
// Owner screen:         shareOn(top, GuideVm(scopeHandleOf(top), api))
// Any component:        val api = sharedFrom<ApiClient>(top.getScene())
// Child of the screen:  val vm  = sharedFrom<GuideVm>(screenNode)      // node via getParent()/findNode/@SGNodeField
```

Input-timing idiom (documented, interim until the adjacent DX item lands): node
handles arrive via `@SGNodeField` + `@BrsOnChange` — acquire in the onChange, not in
`init` (SG sets fields after creation).

## 4. Dispatch lowering (classes whose supertype walk reaches SharedService)

1. **Emission — extension shape.** Methods compile as global functions taking the
   receiver as an explicit parameter (existing extension convention; suspend members
   follow the existing suspend-extension parameter ordering, verified in place
   during planning). Accessor slots (`__get_X`/`__set_X`) follow the same treatment
   where accessors are non-trivial; simple `val`/`var` reads remain direct member
   access (data-class precedent, `:1585-1591`).
2. **Call sites.**
   - Receiver's static type is a **final** shared class → direct static call:
     `GuideVm_refresh(args..., recv)`.
   - Receiver's static type is an **abstract** shared base and the method is
     open/abstract → call the generated dispatcher (below). Final methods on bases →
     direct static call regardless of receiver static type (no override can exist).
   - `super.f(...)` → direct static call to the superclass implementation.
   - **Residual (documented):** receivers statically typed as `Any` or an interface
     still slot-dispatch through the wrappers. Rare by construction; the canary
     covers the day Roku breaks it.
3. **Dispatchers.** Per open/abstract method on a shared base:
   `Base_f_dispatch(args..., recv)` reads the runtime class name from `recv.__proto`
   and if-chains to the right concrete implementation (all concrete descendants of
   that base in the compilation — closed world, single-module documented). Abstract
   with no concrete descendant → dispatcher body is a guided runtime error.
4. **Wrapper slots.** Constructors still attach slots, but each is a thin forwarder
   reading `m` and delegating to the static implementation (arity- and
   continuation-parameter-correct — the wrapper is generated alongside the
   extension-shaped method, never hand-derived). Residual slot-shaped paths keep
   working today (including cross-component via the currently-working disclaimed
   behavior); nothing normal depends on them.
5. **Detection.** `SharedService` ClassId in `BrsStandardClassIds` is the SOLE
   machinery root — lowering, FIR, publish/acquire all use one shared supertype-walk
   helper (single predicate, not duplicated per checker — Stage 2 review note made
   this a requirement). No other type is known to the compiler.

## 5. FIR rule family (all @Suppress-escapable, messages name the fix)

| Diagnostic | Severity | Fires on | Message gist |
|---|---|---|---|
| `BRS_SHARED_CLASS_NOT_FINAL` | ERROR | a CONCRETE SharedService descendant declared `open` | "shared classes must be final (dispatchers enumerate concrete leaves) — remove `open`, or move shared behavior into an abstract base" |
| `BRS_SHARED_FN_PROPERTY` | ERROR | function-typed property anywhere in a SharedService hierarchy | "a stored callback is a function reference in the shared bag — the one shape static dispatch cannot rescue. Use an overridable method on the base, or fields/observers/ScopeHandle for cross-component behavior" |
| `BRS_SHARED_THROUGH_COPYING_CHANNEL` | ERROR | SharedService-typed value into a copying channel: `setField` value args, `@SG*Field` declarations on task components, `callFunc` args | "shared instances cross by reference only — pass the stash node and use sharedFrom<T>()" |

- Abstract bases with members/overrides are LEGAL (no member-free rule, no flat
  hierarchy rule, no name knowledge) — the checker is purely structural.
- ScopeHandle's paths need no new rule: the existing CAPTURE/ARG/RESULT
  marshallability checkers already classify SharedService types as non-external
  classes (unmarshallable).
- Deliberate non-rules (documented instead): no `is`/`as?` warning on shared types
  (rule 3 makes husks near-unconstructible; casts on live refs are legitimate and
  constant). Standard rollout ritual: diagnostics list → BOTH regens committed →
  default messages → firing/clean/@Suppress fixtures each → rebuild step 7 as the
  stdlib false-positive canary.

## 6. Stdlib bindings (retire the spike splices)

- `ISGNodeField` gains: `setRef(fieldName, data): Boolean` (Boolean per device/Return
  Value section, doc signature line notwithstanding), `canGetRef(fieldName): Boolean`,
  `getRef(fieldName): Dynamic`, `moveIntoField(fieldName, data): Int`,
  `moveFromField(fieldName): Dynamic` — external-interface declarations, simple-name
  emission, KDoc carrying the OS 15+/render-thread/AA-field/observer-silent
  constraints and roku.com URLs (house style, SceneGraph.kt).
- `RoUtils` external interface (`isSameObject(a, b): Boolean`, `deepCopy(data): Dynamic`)
  + `@BrsCreateObject("roUtils")` factory.

## 7. Verification

| Layer | Coverage |
|---|---|
| **E2E Suite 9 "SharedService"** (new; fixtures: owner probe, consumer probe, a base-with-hook + final-subclass hierarchy fixture) | same-instance round trip (mutation visible both ways); acquire-before-publish guided ISE; `sharedFromOrNull` null; explicit-key coexistence; wrong-type key collision ISE; replace-on-republish + stale `isLive()` false / live true; **dispatch on device**: final-method static call, base-typed call reaching the override via dispatcher, `super` call, base-internal `this.hook()` template dispatch — each also exercised CROSS-component through a SetRef-acquired instance; scene-stash idiom (`sharedFrom` via `getScene()`); **CANARY**: raw fn-slot invocation cross-component (red = Roku changed the disclaimed behavior — CLAUDE.md documents the meaning; normal operation unaffected) |
| **Goldens** | shared-class ctor emission (wrapper slots), extension-shaped methods (plain + suspend), dispatcher generation (incl. no-descendant guided error), call-site shapes (direct static / through-base dispatcher / super / residual-Any slot call), `sharedFrom` reified lowering. Existing goldens untouched (no SharedService in current testData) |
| **FIR fixtures** | 3 rules × firing/clean/@Suppress; hierarchy shapes (open concrete fires; member-bearing abstract base CLEAN; fn-prop on base fires; each copying-channel shape) |
| **Stdlib units** (runBlocking regime) | key derivation, guided message texts, `shareOn`/`sharedFrom` outside component context ISEs, `canShare` off-render behavior |

**Gates baseline (must not drop):** goldens 71 · FIR 225 · stdlib 514/51 · E2E 72/8
(+3 xtests) · validators strict 0/0. All grow; per-phase device-green; `./rebuild.sh`
only; commit-per-task with klib-regen convention; roku-test-app separate.

## 8. Documentation deliverables (CLAUDE.md)

New "SharedService" section (after ScopeHandle): API + canonical example, the
scene-stash idiom for app services, node-handoff + onChange input timing, the
recreate-don't-reuse screen convention (with close-is-terminal cross-reference), the
dispatch model in one paragraph (incl. the residual shapes and what the CANARY means),
single-module closed-world note, marshallable-interaction note (shared types are
compile-errors in copying channels), test-hook inventory, gate table update.

## 9. Phasing

- **Phase 1 — sharing on today's dispatch:** bindings (§6) + public surface (§3) +
  liveness + Suite 9's sharing tests. Slot dispatch is device-proven to work
  cross-component TODAY, so this ships immediate value with risk unchanged.
- **Phase 2 — FIR family (§5):** the final-class rule gates dispatcher enumeration;
  rules land before the lowering that depends on them.
- **Phase 3 — dispatch lowering (§4):** emission + call sites + dispatchers +
  wrappers + goldens + Suite 9's dispatch/canary tests. After this phase, no normal
  path reads a fn slot.
- **Phase 4 — acceptance + docs (§7/§8)** + backlog/memory sweep.

## 10. Risks

- **Emission-shape change is the big one**: switching shared-class methods to
  extension shape touches ctor emission, suspend lowering interplay
  (continuation-parameter ordering — verify the suspend-extension convention in
  place before coding), and the `<this>`/receiver-param rendering history (Stage 2
  fixed two bugs in exactly this area; their goldens are the regression net).
  Mitigation: zero existing goldens contain SharedService (churn is additive), and
  Phase 1 ships user value before Phase 3 takes this on.
- **Wrapper arg-shift hazard**: a wrapper that forwards wrong arity (esp. suspend
  continuations) reintroduces the exact crash class fix A killed. Wrappers are
  generated from the same signature as the static form; goldens pin both.
- **Dispatcher closed-world**: a separately-compiled module's shared subclass would
  be invisible to dispatchers — single-module today, documented; promote to a FIR
  diagnostic when multi-module becomes real (same note as ScopeHandle's run{}
  blocks).
- **Canary politics**: a Roku OS update flipping the canary makes the suite red on
  a healthy app. That is the designed behavior; CLAUDE.md's entry is the runbook
  (verify normal tests green → delete wrapper-slot residual paths → retire canary).

## 11. Out of scope (recorded)

- **StateFlow / VM→View propagation** — next program; base-class hierarchies from
  this design are its foundation.
- **@AppScoped DI** — separate program; the scene-stash idiom is its manual
  precursor; it mints `shareOn`/`sharedFrom` calls.
- **Component-input DX pair** (decision 10): `createComponent<T> { ... }`
  configure-lambda + `@SGRequired`/`onInputsReady()` — recorded adjacent, likely
  rides the lifecycle/VM program.
- **spawnTask** — unchanged from ScopeHandle's record.
- Member-bearing-base *interface* dispatch (interface-typed receivers), multi-module
  dispatchers, unpublish/retract API (republish + node death cover v1).

## 12. Public API summary (new/changed)

```kotlin
// package kotlin.brs
public abstract class SharedService { public fun isLive(): Boolean }
public fun shareOn(node: RoSGNode, instance: SharedService): Unit
public fun shareOn(node: RoSGNode, instance: SharedService, key: String): Unit
public fun canShare(): Boolean
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode): T
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode, key: String): T
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode): T?
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode, key: String): T?

// brs/roku/SceneGraph.kt — ISGNodeField additions
public fun setRef(fieldName: String, data: Dynamic): Boolean
public fun canGetRef(fieldName: String): Boolean
public fun getRef(fieldName: String): Dynamic
public fun moveIntoField(fieldName: String, data: Dynamic): Int
public fun moveFromField(fieldName: String): Dynamic
// + RoUtils external interface with @BrsCreateObject("roUtils")

// New FIR diagnostics: BRS_SHARED_CLASS_NOT_FINAL, BRS_SHARED_FN_PROPERTY,
// BRS_SHARED_THROUGH_COPYING_CHANNEL (all ERROR, suppressible)

// Changed codegen (SharedService descendants only): extension-shaped method
// emission, static call sites, __proto-name dispatchers, forwarding wrapper slots.
```
