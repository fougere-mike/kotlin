# Component Lifecycle: onStart/onStop, retire/revive, constructor inputs — Design

**Date:** 2026-09-04
**Status:** Approved design, pending implementation plan
**Branch:** `feature/brightscript-backend-2.2.20`
**Supersedes:** decision 10 of `docs/superpowers/plans/2026-08-14-shared-service-design.md`
(the constructor-input rejection and the `@SGRequired`/`onInputsReady()` adjacent
program) — see §1 decision 3 for the reasoning of record.
**Companion spec (spec 2, not yet written):** scoped SharedService lookup — walking
`sharedFrom`/`sharedFromOrNull`, suspend `awaitShared`, constructor-value `serviceKey`,
and the `by sharedService { key }` delegate that registers into this spec's ready gate
(§13 is the interface it consumes). Spec 2 is written after this spec is approved.

## 1. Decisions made during brainstorming (Mike, 2026-09-04)

| # | Decision | Choice |
|---|----------|--------|
| 1 | Program split | TWO specs, lifecycle first. This spec = component lifecycle (hooks, gate, retire/revive, constructor inputs, typed layout builders). Spec 2 = scoped SharedService lookup, which consumes §13 |
| 2 | Hook shape | **Approach A**: `override suspend fun onStart()` with a COMPILER-SYNTHESIZED driver (`launch { awaitReady(); onStart() }`) emitted only when the class hierarchy overrides the hook. Rejected: plain hook + stdlib callback driver (B — one `launch` line per override, hook failures outside the coroutine machinery); a `kotlin-lifecycle-brs` user-mode klib (C — a prebuilt klib for ~20 lines) |
| 3 | Constructor inputs | `class Screen(@SGStringField val airingId: String) : GroupComponent()` declares REQUIRED inputs. Decision 10's rejection is CONSCIOUSLY REVERSED: its objection was the init-time-availability PROMISE the syntax makes; the FIR init-read rule (§7) plus the ready gate (§4) turn that promise into a checked contract. Task components take no constructor inputs in v1 (`runTask<T> { }` is their input path) |
| 4 | Construction | `AiringDetailsScreen(airingId)` is a real constructor call, lowered to create + input writes + ready marker; the result is a node handle statically typed as the class (the `createComponent<T>()` contract). Static layouts declare structure; runtime-valued components are constructed in code (in a coroutine or handler) and navigated to / appended by the app |
| 5 | Typed layout builders | The Gradle plugin generates one DSL builder per component INCLUDING input-bearing ones (`airingDetailsScreen(id = "d", airingId = "123")`): hard-coded constant inputs in a static layout are allowed; required inputs are required Kotlin parameters; a non-constant required value is a backend ERROR (not the existing warning) |
| 6 | Reversibility | Retire is REVERSIBLE (recycling law: `retire → removeChild → reconfigure → appendChild → revive`). Re-activation is EXPLICIT (`revive(node)`) because the platform has no attach signal. Constructor `val` inputs are identity and survive the cycle; recyclable content is `@SG var` fields (Roku `itemContent` parity) |
| 7 | Naming | Hooks `onStart`/`onStop` (Android names, now honest because the cycle is reversible: start fires on first readiness and after every revive; stop on every retire). Parent-side calls `retire(node)`/`revive(node)` name the APP's action, so nothing reads as if a freshly constructed node needs a start call. `onReady`/`onDestroy` rejected (terminal connotation) |
| 8 | Teardown detection | EXPLICIT `retire` is the contract (ScopeHandle `host.close()` precedent). Parent-`change`-field detach detection is spike-gated (§8 Q7) and, if it works at all, a BACKSTOP the design does not depend on — Probe A4 (flow spike) gives it a strong negative prior |
| 9 | Init injection | The lifecycle attach is UNCONDITIONAL in every concrete render component's `init()` and absorbs the pump attach — closing the documented helper-file hole of the per-file coroutine scan as a side effect |
| 10 | Per-instance cost | Gated statically: a component with no hook override and no inputs pays one attach call (two ref stores); input-bearing types pay one boolean marker field; only hook-overriding types pay a coroutine per instance. Pinned by an informational E2E timing line (§9) |
| 11 | Latent defects fixed en route | Component `super.f()` self-recursion (no superQualifier handling) and leaf-wrapper shadowing of an inherited `onKeyEvent` override — both land before any hook contract invites `super.onStart()` |

## 2. Context and load-bearing facts

Why: the flagship child (`roku-test-app/.../TestScreenChildLabel.kt`) needs a
`screenRef` node field + `@BrsOnChange` + a one-shot re-fire guard just to acquire a VM
its parent published. The root gap is that components have no "my dependencies exist
now" moment: SceneGraph runs a child's `init()` before the parent's, and `init()` of a
dynamically created node runs inside `CreateObject` before any field write or
`appendChild`. Every consumer rebuilds a readiness signal by hand. Everything below is
explorer-verified against the current backend (2026-09-04) or device-pinned where
marked; the unpinned platform facts are §8's spike questions.

**Codegen (IrToBrsTransformer = T, IrExpressionToBrsTransformer = E, BrsComponentExtractor = X):**

- `sub init()` emission order (T:967-1200): task `functionName` → `__kotlinPumpAttach`
  iff the FILE uses coroutines (T:995-1010, predicate T:242-283) → scope-binding
  install → ALL property initializers (T:1050-1091; `@SG` → `m.top.f = init`) → method
  attachments for the class's OWN non-fake-override functions (T:1112-1130; inherited
  members are never attached) → accessor attachments → ALL init blocks (T:1170). Kotlin's
  source-order interleaving is not preserved.
- Component member calls are SLOT calls on `m` (`m.f_k_()`, E:1640-1698); `<this>` → `m`
  (E:258); a `ComponentBase` extension's receiver parameter is literally named `m`.
  `ComponentBase` (`SceneComponent.kt:29-104`) compiles as a REGULAR class in the stdlib:
  its default bodies exist as globals nothing calls. An un-overridden base hook invoked
  as `m.hook_k_()` is a missing slot → "Member function not found".
- `generateOnKeyEventFunction` (T:778-824) emits a per-leaf unmangled wrapper that calls
  `m.onKeyEvent_Str_Z_k_` only when the LEAF declares the override, else `return false`
  — a concrete user base's override is silently shadowed (latent defect). Abstract user
  intermediates DO get their own XML + init and sit in the SceneGraph extends chain
  (X:198-226); SceneGraph runs base `init()` before derived `init()` over a shared `m`.
- `super.f()` inside a component: no `superQualifierSymbol` handling anywhere in E;
  compiles to the override's own slot = self-recursion (latent defect; the SharedService
  program fixed this only for shared classes, `BrsSharedDispatchLowering.kt:286`).
- Stdlib compilation generates NO suspend state machines (`BrsLoweringPhases.kt`
  `fullCoroutinesAvailable = !isStdlibCompilation`): a driver that suspends around the
  hook cannot live in the stdlib — hence decision 2's compiler-synthesized driver.
- `@SG*Field` annotations are `@Target(PROPERTY)` (`annotations.kt:142-312`); on a
  constructor parameter the annotation lands on the generated property, which IS in
  `irClass.declarations`, so the extractor already emits the XML `<field>` (X:231-285)
  and nothing requires `var` or an initializer. The ONLY breakage today: init step 4
  emits `m.top.airingId = airingId` — an undefined identifier inside `sub init()`.
  No FIR diagnostic rejects a component constructor with parameters.
- `createComponent<T>()` is a codegen intrinsic (E:787-816), not a lowering; the runTask
  rewrite (`lower/BrsRunTaskCallLowering.kt`, phase 0.055) is the model for call-site
  rewrites. `visitConstructorCall` (E:2910-2995) has no component check: a component
  constructor call today emits a call to `<Class>_create_..._k_`, which is never
  emitted (dependency-recorder warning at compile time, "Function is not defined" on
  device). `SceneComponent.kt:303-313` KDoc already advertises `val item = VideoItem()`.
- XML extraction runs on PRE-lowering IR (BrsCompiler.kt:152-163); fields synthesized
  by a lowering are invisible to XML unless the extractor emits them or they are
  registered via `context.synthesizedComponents` (BrsCompiler.kt:169-173).
- The layout DSL is string-typed: `component(componentType: String, id: String, …,
  init: ComponentBuilder.() -> Unit)` (`LayoutBuilder.kt:751-795`); extraction
  (X:941-996) requires IrConst type and id (else the entry is dropped SILENTLY); the
  per-attribute constness check + `warnDroppedAttribute` (X:37-45) is a location-less
  MessageCollector WARNING. `BrsIrBackendContext.reportWarning(element, msg)` exists
  (location-bearing); there is no error twin yet.
- The FIR layout generator (`fir/extensions/brs/SceneGraphLayoutGenerator.kt`) is DEAD:
  replaced by Gradle-side source generation for IDE support, no duplicate-class
  conflicts, debuggability. `GenerateLayoutStubsTask` (kotlin-roku) regex-parses the
  DSL for ids (`:130-190`).

**Runtime:**

- `componentScope()` (`ComponentCoroutines.kt:30-63`): per-instance object singleton in
  GetGlobalAA (per-component-instance on the render thread), `Dispatchers.Main +
  SupervisorJob()`. NO whole-scope cancel API; the holder is never reset; `launch` on a
  cancelled scope returns a dead job (`Builders.kt:36-42`).
- Cancel cascade is already correct: a parked `runTask` await writes `control="STOP"`
  (`TaskRunner.kt:262-270`); a `flowOn` collector writes `flowCancel` then `STOP`
  (`TaskFlow.kt:346-359`); exposed ScopeHost request jobs settle "closed"
  (`ScopeHostImpl.kt:255-264`); a StateFlow collector deregisters its doorbell
  (`Doorbells.kt:243-274`).
- `PumpScheduler.attach` stores `hostTop`/`hostGlobal` only (idempotent); channel and
  Timer are deferred to the first wakeup; no detach exists (`PumpScheduler.kt:84-92`).
  The ambient oracle (`hostTopOrNull`) is null in a coroutine-free component file.
- `callFunc` is bound (0/1-arg, `SceneGraph.kt:546,555`); device-pinned MAIN→render
  (Suite 1 `callFuncEchoAndAdd`); render→render from Kotlin is UNPINNED (§8 Q6). The
  only toolchain-registered per-component functions are `__kotlinTaskMain` (tasks) and
  the `onKeyEvent` wrapper — bare-named functions in the component's script namespace,
  which stdlib code included in that namespace can call by name.
- `ScopeHostImpl.close()` is idempotent and reconstructible from `ScopeHostHolder.state`
  by code running in the child's context (`ScopeHostImpl.kt:70-72, 114-126`).
- Reusable triad: per-component registry object in GetGlobalAA (ComponentMailbox /
  FlowDoorbells / TaskRunner), name-registered scoped observer with the SAME-FILE law,
  `DelayTracker.register(ms) {}` watchdog (no deregistration; registry-miss no-op idiom).
- `unobserveFieldScoped(field)` strips EVERY scoped observer this component holds on
  that field (`TaskRunner.kt:216-221`) → lifecycle traffic uses dedicated `__kotlin*`
  fields. Node-typed field observers RE-FIRE on later graph mutation (CLAUDE.md, flagship
  boot) → gates keyed on node fields must be idempotent. Events can outlive nodes
  (`getRoSGNode()` invalid, `DeviceTestLoop.kt:281-289`).

**Device facts bearing on teardown (spikes/flow-spike/FINDINGS.md Probe A4):** a
removed component's scoped observer is DEAD within the SAME synchronous stack as
`removeChild` (`a.a4.ghostFires.immediate … ghosts=none`). No destroy callback exists
in any spike or plan; the platform is repeatedly recorded as unable to signal
unsignaled death (ScopeHandle watchdog rationale). The `change` field has zero prior use
in stdlib, flow, kotlin.test or roku-test-app.

**Roku docs of record (`../RokuDocs/ifSGNodeChildren.html`, `roSGNode.html`, added
2026-09-04):** "Removing or replacing a node in a SceneGraph node tree can cause that
node to be destroyed entirely if there are no more references to it" — the refcount-death
rule is documented, and no callback is mentioned anywhere on the page. `getParent()`
"returns the parent node of a node [that] has been added to a list of children;
otherwise invalid" — unattached ⇒ invalid is documented (§8 Q3 still pins the init-time
timing). `reparent(newParent, adjustTransform)` moves a node in ONE call (§8 Q7 uses it).
`createChild(nodeType)` creates AND appends. Scene `<children>` are "hidden elements used
by the SceneGraph framework" that `getChild()` on the Scene does not return — what
`getParent()` answers for a scene's direct child is therefore an open question for
spec 2's walk (§8 Q5b).

**Roku docs of record, second batch (`Node.html`, `ifSGNodeDict.html`, `ifSGNodeField.html`,
added 2026-09-04):**

- **`change` field (Node):** an AA `{Index1, Index2, Operation}` on the PARENT node,
  READ_ONLY, "recorded in this field if, and only if, this field has been observed".
  Operations: `none`, `insert`, `add`, `remove` (index1..index2), `set` (replace at
  index1), `clear`, `move`, `setall`, `modify` (ContentNode metadata only). The detach
  probe (§8 Q7) observes the parent's field and reacts to `remove`/`set`/`clear`/
  `setall`; `move` and `insert`/`add` are not removals.
- **`callFunc` (ifSGNodeDict):** "a synchronized interface … always executes in the
  component's owning ScriptEngine and thread (by rendezvous if necessary), and it will
  always use the m and m.top of the owning component"; multiple parameters of any type;
  arbitrary return. Same-thread calls execute directly, so render→render and a component
  calling its OWN node are covered by the docs (§8 Q6a/c reduce to device pins).
  **`hasFunc(name)`** "checks whether the specified callable function exists" — the
  guard `retire`/`revive` use before `callFunc` (§4). No OS-version note on the page;
  §8 Q6b pins it on the device and the guide records it against the 9.4 compile floor.
- **Observers (ifSGNodeField):** function-form `observeFieldScoped` callbacks run "on the
  thread that owns the observed node"; `unobserveFieldScoped` "removes the implicit
  connection state stored in the OBSERVING object" — the documented basis for Probe A4's
  immediate detach on observer-component death. `removeField` is documented (bound in
  SceneGraph.kt, still unpinned). `threadinfo()` reports
  `willRendezvousFromCurrentThread` — a spike-side diagnostic for the callFunc probes.

**Roku docs of record, third batch (`Component initialization order.html`, added
2026-09-04) — the init-order facts, now DOCUMENTED (device pins in §8 confirm, not
discover):**

- Order for a component instance: (1) its `<children>` nodes are created "and their
  fields are set to their initial values, either to a default value, or to the value
  specified in the XML markup"; (2) its own `<interface>` fields are created and
  initialized (default or `value=`); (3) its `init()` runs. Children are therefore fully
  created BEFORE the parent's init (§8 Q1).
- "For nodes that are defined in the `<children>` XML markup of the component file, the
  parent node is set AFTER the node is created, and init() is called." `getParent()` is
  therefore invalid inside init for XML-declared children as well as dynamic ones — the
  gate can never be satisfied from init for ANY creation path (§8 Q2/Q3).
- "Observer functions of fields that are set up in the init() function do not get called
  when those fields are initialized" — initial values never fire observers armed in init;
  the gate's read-value-first-then-observe rule (§4) is the documented idiom.
- "Field observer callback functions set up in init() cannot be guaranteed to have
  returned when the component is created using createObject() or createChild()" — the
  platform itself describes post-creation work as asynchronous; `onStart` is the
  sanctioned place for it.
- Markup values on `<children>` nodes "may be overridden as many as two times" — by the
  owner's `<interface>` field initialization (aliases) and by the owner's init(). The
  child's OWN init is not listed as an override source, which implies markup values land
  after the child's creation; §8 Q4 pins the order rather than relying on the implication.

**FIR:** the component-class predicate is PRIVATE to `FirBrsCreateComponentTypeChecker`
(`:85-92`); `FirBrsTaskStateNotFieldChecker.kt:61` skips fake-source (constructor-
parameter) properties on the recorded grounds that "components cannot take constructor
arguments anyway". Upstream `FirUninitializedEnumChecker.kt:79-150` does exactly the
init-block / property-initializer / lambda classification §7 needs via the
`containingDeclarations` stack. The diagnostic harness compares NAMES only and needs an
inverse message-template entry per new diagnostic (`AbstractBrsDiagnosticTest.kt:201-310`).

## 3. Public surface (package `kotlin.brs`, default-imported)

```kotlin
public abstract class ComponentBase {
    // existing: top, global, m, onKeyEvent

    /**
     * Fires once per ACTIVATION, on the render thread, as a child coroutine of the
     * component scope, after: init() has returned, every required input is set, and
     * every dependency registered during init has resolved. Earliest: the first pump
     * tick after init. Fires again after every revive(). Never launched for a class
     * whose hierarchy does not override it. Failure → the standard
     * "[kotlin.coroutines] Unhandled exception" line; the component stays alive.
     * retire() cancels an onStart still running.
     */
    protected open suspend fun onStart() {}

    /**
     * Runs synchronously inside retire(), BEFORE scope cancellation, so live state is
     * still readable. Launching here is pointless (the scope dies next); say so in KDoc.
     */
    protected open fun onStop() {}
}

/** Re-entrant; returns when the current activation's gate is open. Public for coroutines launched from observer handlers. */
public suspend fun ComponentBase.awaitReady()

/** Parent-side, render thread. Hops into the child; onStop → stdlib retire hooks → close exposed ScopeHost → cancel + reset scope → retired. Idempotent. Does NOT remove the node. */
public fun retire(node: RoSGNode)
public fun retire(component: ComponentBase)          // typed-handle overload; same BRS

/** Parent-side, render thread, after re-adding a retired node. Clears retired + once-flag, re-arms dependencies, relaunches the onStart driver. No-op on a live component. */
public fun revive(node: RoSGNode)
public fun revive(component: ComponentBase)

/** Test hooks (kotlin.test-adjacent, precedent: kotlinScopeWatchdogFires). */
public fun kotlinLifecycleWatchdogFires(): Int
public fun kotlinLifecycleWatchdogMillis(ms: Int)
```

**Constructor inputs:**

```kotlin
class AiringDetailsScreen(@SGStringField val airingId: String) : GroupComponent() {
    override suspend fun onStart() { /* airingId is set here */ }
}
val screen = AiringDetailsScreen("123")      // lowered: create + write + marker; typed as the class
top.getScene().appendChild(screen.asDynamic())   // or an app screen-stack `show(screen)`
```

- `val` is legal and expected; it is a Kotlin-side promise only (the platform can still
  write the field from outside — KDoc'd). Non-nullable types are fine: the field is
  guaranteed written before `onStart`.
- Ordinary `@SG var` fields keep their role as runtime-settable, observable inputs; the
  two coexist. Recyclable content is `var`; constructor inputs are identity.
- `createComponent<T>()` stays for input-less classes and is a FIR error when `T`
  declares required inputs (§7).
- Task components: a constructor parameter carrying an `@SG*Field` annotation is a FIR
  error (§7). The zero-argument constructor call is lowered for every concrete
  SceneGraph component kind (render, ContentNode, Task) — for tasks it is equivalent to
  `createComponent<T>()`.

**Typed layout builders (generated by kotlin-roku, §6):**

```kotlin
// generated next to the _Layout stubs
@SGComponentBuilder("AiringDetailsScreen")
fun LayoutBuilder.airingDetailsScreen(
    id: String, airingId: String,
    /* the standard attributes component() accepts today, all optional */
    init: ComponentBuilder.() -> Unit = {},
)
// parent layout — constants only; omission of airingId is an ordinary Kotlin compile error
sceneLayout { airingDetailsScreen(id = "details", airingId = "123") }
```

The raw `component("AiringDetailsScreen", id = "details") { attr("airingId", "123") }`
form keeps working and satisfies inputs the same way. `SGComponentBuilder` is a new
stdlib annotation in `kotlin.brs.scenegraph` (`@Target(FUNCTION)`, one `componentType:
String` argument) that the extractor keys on.

## 4. Runtime design: registry, gate, retire, revive

**One stdlib file** — `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt`
holds everything below: the attach, `awaitReady` and its name-registered marker observer
handler, the retire/revive impls, the registry object, and the public `retire`/`revive`
functions. One file because of the same-file include-closure law (Doorbells.kt
precedent): the attach call's recorded edge pulls the whole lifecycle into every
component's closure, and the observer handler must live beside its registration.

**Lifecycle registry** — an `internal object` singleton in that file (per component
instance via GetGlobalAA; ComponentMailbox precedent). State: `attached`, `retired`, `driverClaimed`, `activation: Int`
(monotonic; bumps on retire), `tickets: ArrayList<DependencyTicket>`, `parkedAwaiters`,
`retireHooks: ArrayList<() -> Unit>`, `watchdogArmedFor: Int`.

**`__kotlinComponentAttach(top, global)`** (`@BrsStatic`, bare-named, stdlib): the
FIRST statement of every concrete render component's `init()` — UNCONDITIONAL (replaces
the coroutine-scan-gated `__kotlinPumpAttach` injection). Performs `PumpScheduler.attach`
internally, sets `attached`. Cost: one call, two ref stores. Task components stay
excluded (their own thread; pump attach was already excluded).

**Inputs readiness.** Input-bearing classes carry one boolean XML field
`__kotlinInputsReady` (extractor-emitted, §5). `inputsReady() = !top.hasField(MARKER) ||
top.getField(MARKER) == true` — the stdlib needs no compiler flag: the field exists
exactly when the class declares inputs. The marker is written LAST by the
constructor-call lowering, and carried as the attribute `__kotlinInputsReady="true"`
on a static-layout child entry whose required inputs are all present as constants. Its
purpose is PROOF OF SANCTIONED CONSTRUCTION: a node created from raw BrightScript or
hand-written XML leaves it unset, the gate stays closed, and the watchdog names that
cause instead of `onStart` silently running on default values.

**The gate.** `awaitReady()` — one stdlib suspend function in the single-park
`ParkedContinuation` style (tail-delegation + `parked.finish()`):

1. render-context guard (guided ISE otherwise; runTask precedent);
2. `if (gateOpen()) return` — `gateOpen = !retired && inputsReady() && tickets.all { it.resolved }`;
3. else: arm the marker observer if the marker field exists and is false
   (`top.observeFieldScoped(MARKER, brsName(::onKotlinInputsReady))`, handler in the
   SAME file — include-closure law; read-value-first then observe, so XML-attribute
   timing (§8 Q4) is irrelevant); park in `parkedAwaiters`; arm the watchdog for this
   activation if not yet armed (`DelayTracker.register(watchdogMillis)`; fires ONE
   console line if still closed: `[kotlin.lifecycle] <Subtype>(id=<id>) not ready after
   30s — inputs <set|UNSET (created outside its Kotlin constructor?)>, unresolved:
   <labels>`; counter `kotlinLifecycleWatchdogFires()`; registry-miss no-op after the
   gate opens or the component retires);
4. wake paths: the marker observer fires, a ticket resolves, or `revive` runs — each
   re-checks `gateOpen()` and drains `parkedAwaiters` when open. Cancellation
   (`retire` → scope cancel) wakes a parked awaiter with `CancellationException`.

**The driver** (compiler-synthesized, §5): `fun __kotlinStartDriver() { if
(!kotlinLifecycleClaimDriver()) return; launch { awaitReady(); onStart() } }` — the
once-flag claim is SYNCHRONOUS and before the launch, so when a concrete base and its
concrete subclass both call the driver from their inits, only the first (the base's,
since base init runs first) launches; its body runs after the whole init cascade, when
the leaf's init has already attached the most-derived `onStart` slot → correct virtual
dispatch by construction.

**Retire** — `retire(node)` (parent side): `node.callFunc("__kotlinRetire")`. Inside the
child, the generated `__kotlinRetire` (§5) runs:

1. `if (retired) return` (idempotent);
2. `m.onStop_k_()` when the hierarchy overrides it (decided statically at generation;
   wrapped in try/catch → `[kotlin.lifecycle] onStop threw: <message>`; retire continues);
3. `retireHooks` run (spec 2 disarms doorbell observers here);
4. `ScopeHostHolder.state?.let { ScopeHostImpl(it).close() }` — `ScopeClosedException`
   reaches children (not a bare CE; hazard 13 of the runtime map);
5. `componentScope().coroutineContext[Job]!!.cancel()`; then `ComponentScopeHolder.scope
   = null` — the SCOPE SELF-HEALS: the next `launch {}` gets a fresh supervisor scope
   (an app that forgets `revive` gets working coroutines and a visibly missing
   `onStart`, not silent dead jobs);
6. `retired = true; activation++`.

Retire does NOT remove the node; the caller owns the tree. Its contract is Kotlin render
components only, enforced with the documented `hasFunc`: `retire`/`revive` check
`node.hasFunc("__kotlinRetire")` first and throw a guided ISE ("… is not a Kotlin render
component — retire/revive target components compiled from ComponentBase subclasses") when
it is absent (§8 Q6b pins `hasFunc` on the device). Self-retire (a component retiring its
own node) is documented as a direct same-thread `callFunc` and is pinned by §8 Q6c.

**Revive** — `revive(node)`: `node.callFunc("__kotlinRevive")`. Inside the child:

1. `if (!retired) return` (no-op on a live component);
2. `retired = false; driverClaimed = false; watchdogArmedFor = -1`;
3. every ticket → `resolved = false; ticket.rearm()` (spec 2's delegate drops its cached
   instance, walks again from the now-attached node, arms the doorbell on a miss —
   synchronous resolutions count immediately);
4. `<Class>___kotlinStartDriver_k_()` when the hierarchy overrides `onStart` (generated
   call).

Inputs are unchanged across the cycle (constructor `val`s are identity), so the marker
still holds. `awaitReady` parked from a coroutine launched during the retired window
re-checks on revive.

**The recycling law (documented in CLAUDE.md):** `retire(node)` → `removeChild` →
reconfigure `var` fields → `appendChild` → `revive(node)`. `init {}` is ONE-TIME
structural setup; per-activation work lives in `onStart`. Consequence for ScopeHandle:
`exposeScope` becomes re-callable AFTER retire (one OPEN host per component, not one
ever) — retire closes the host, `onStart` may expose again. This is a ScopeHandle law
amendment; Suite 8 must stay green and gains a re-expose test (§9).

## 5. Compiler changes

All in `compiler/ir/backend.brightscript/` unless noted.

1. **Unconditional attach** (T:995-1010 site): every render component that emits an
   `init()` — abstract user intermediates included, since they sit in the SceneGraph
   extends chain and the attach is idempotent — starts with
   `__kotlinComponentAttach(m.top, m.global)`; the `fileUsesCoroutines` gate is retired
   for this injection (delete the predicate if nothing else uses it). Task components
   excluded as today.
2. **Hierarchy override scan** (new helper in `BrsIntrinsics`): `hierarchyOverrides(
   irClass, hookName)` — walks user component supertypes up to (excluding) the stdlib
   bases, true if any declares a real (non-fake) override. Serves the `onStart` driver,
   the `onStop` call in `__kotlinRetire`, and the `onKeyEvent` wrapper fix.
3. **Driver synthesis** (new lowering `BrsComponentLifecycleLowering`, registered BEFORE
   the suspend-functions lowering so the lambda gets a state machine): for each concrete
   render component whose hierarchy overrides `onStart`, add a member
   `__kotlinStartDriver()` with body `if (!kotlinLifecycleClaimDriver()) return; launch
   { awaitReady(); onStart() }` (IR built against `kotlin.brs.launch`,
   `kotlin.brs.awaitReady`, and the class's `onStart` fake-override symbol — a normal
   dispatch-receiver call, emitted as the `m.onStart_ContinuationV_k_` slot call), and
   append a call to it as the LAST statement of init (after all user init blocks).
   Stdlib symbols resolve via `BrsSymbols.*OrNull` (absent in stdlib compilation).
4. **`super.f()` in components** (E, `visitCall` with `superQualifierSymbol != null` on
   a component receiver): emit a STATIC call to the base's mangled global
   (`Base_f_..._k_(args)` — exists because every component class emits its members as
   globals over the shared `m`); `createFunctionCall` records the dependency edge.
   Golden-pinned; the SharedService super fix is the template.
5. **`onKeyEvent` wrapper** (T:778-824): call the slot when `hierarchyOverrides(cls,
   "onKeyEvent")`, `return false` only when nothing in the hierarchy overrides.
6. **`__kotlinRetire` / `__kotlinRevive`** (new, beside `generateTaskMainFunction`
   T:826-965; XML `<function>` entries beside `__kotlinTaskMain` at BrsCompiler.kt:2073):
   per concrete render component, bare-named functions:
   `__kotlinRetire` = `[m.onStop_k_() in try/catch if hierarchyOverrides(onStop)]` +
   `__kotlinRetireImpl()`; `__kotlinRevive` = `__kotlinReviveImpl()` +
   `[<Class>___kotlinStartDriver_k_() if hierarchyOverrides(onStart)]`. The stdlib
   impls are `@BrsStatic` bare-named (pump-attach idiom) and their file is pulled into
   the include closure by the attach call's recorded edge (same file as
   `__kotlinComponentAttach`).
7. **Constructor-call lowering** (new `BrsComponentConstructorCallLowering`, phase 0.056
   in the call-site-rewrite family after runTask 0.055): an `IrConstructorCall` whose
   class is a CONCRETE SceneGraph component (`isSceneGraphComponent`) becomes an IR block
   `{ val n = brsCreateComponent<T>(); n.<input_i> = <arg_i> (IrSetField on the
   constructor-parameter property's backing field, receiver n — the backend's existing
   handle-receiver routing emits `n.field = v`); brsMarkInputsReady(n) [only when the
   class has inputs]; n }`. Abstract classes and unsubstituted type parameters are left
   alone (createComponent precedent) — the FIR side rejects them.
8. **Init emission skip** (T:1050-1091): properties with `fromPrimaryConstructor` emit NO
   initializer line (the value arrives via the creation write or XML attribute).
9. **Extractor** (X): (a) emit `<field id="__kotlinInputsReady" type="boolean"
   value="false"/>` for classes with ≥1 constructor-parameter `@SG` property, and record
   `requiredInputs: List<String>` on `BrsComponentInfo`; (b) recognize a call whose
   callee carries `@SGComponentBuilder(type)`: `id` param → id; params named after the
   target's `requiredInputs` → attributes; remaining params → standard attributes (by
   name, same table `component()` uses); the trailing lambda → `attr()`/children as
   today; (c) POST-EXTRACTION VALIDATION in BrsCompiler (after `preExtractedComponents`
   is complete, keyed by name): for every child entry whose type names a module
   component with `requiredInputs`, every required input must be present (builder param
   or `attr()`) AND compile-time constant → add `__kotlinInputsReady="true"` to the
   entry; else `reportError(element, "[BRS layout] component 'X' declared in <Owner>'s
   layout requires input 'airingId' as a compile-time constant — supply it in the builder
   call, or construct X in code")` — a new location-bearing ERROR twin of
   `reportWarning`. Types from dependency klibs have no `requiredInputs` (single-module
   closed world; recorded).
10. **`BrsStandardClassIds`**: `Callables.awaitReady/retire/revive`, `Annotations.
    SGComponentBuilder`, the lifecycle stdlib entry names.

## 6. Gradle plugin changes (`../kotlin-roku`, `GenerateLayoutStubsTask` or a sibling task)

- Scan `brsMain` sources for component class declarations with a CONSTRAINED grammar:
  `class <Name>(<params>) : <Base>(…)` where `<Base>` is a stdlib render base
  (`GroupComponent`/`SceneComponent`/`LayoutComponent`/…) or another module component,
  and `<params>` are `@SG<Kind>Field val <name>: <String|Int|Float|Boolean>` (node- and
  AA-typed inputs cannot be XML constants: a component with such a required input gets
  NO builder — construct it in code). Unparseable declarations → no builder + a Gradle
  warning naming the class (the compiler's extractor remains the source of truth: a
  wrong builder is a compile error at the call site, never a silent XML).
- Generate `<Module>ComponentBuilders.kt` beside the `_Layout` stubs: one
  `@SGComponentBuilder("<Name>") fun LayoutBuilder.<lowerCamelName>(id: String, <inputs
  as required params>, <standard attrs optional>, init: ComponentBuilder.() -> Unit =
  {})` per eligible component, body = `component("<Name>", id = id, <attrs>) {
  attr("<input>", <input>); …; init() }`. The body is never inspected by the extractor
  (it reads the CALL site), so the non-constant `attr(name, param)` inside is fine.
- Add the generated builder names to the id-extraction patterns so the parent's
  `_Layout` stub still gets an accessor for the child id. Accessor type stays `RoSGNode`
  (typed-handle accessors are out of scope).
- Plugin unit tests on fixture sources (parse → generated text).

## 7. FIR rule family (checkers.brs; all `@Suppress`-escapable; messages name the fix)

| Diagnostic | Severity | Fires on | Message shape |
|---|---|---|---|
| `BRS_COMPONENT_INPUT_READ_IN_INIT` | ERROR | a DIRECT read (dispatch receiver `this` of the owning component) of a constructor-parameter `@SG` property inside an `init {}` block or a sibling property initializer of that component | "Input ''{0}'' of component ''{1}'' is read during init: SceneGraph runs init() inside CreateObject, before any field is written, so this read sees the declared default. Read it in onStart() (fires once all inputs are set) or in a lambda/observer" |
| `BRS_CREATE_COMPONENT_HAS_INPUTS` | ERROR | `createComponent<T>()` where `T` declares ≥1 constructor input | "…declares required inputs ({1}); construct it with {0}(…) instead" |
| `BRS_TASK_CONSTRUCTOR_INPUT` | ERROR | an `@SG*Field`/`@BrsField` constructor parameter on a TaskComponent | "Task components take inputs through runTask<T> { field = value }; declare ''{0}'' as a var field" |

- R1 recipe: `FirPropertyAccessExpressionChecker`; property symbol `fromPrimaryConstructor`
  with an `@SG`/`@BrsField` annotation, owner class is a component
  (`BrsComponentTypes.isComponentClass`), dispatch receiver is `this` of the owner;
  context = `containingDeclarations.lastOrNull { init | property | function }`:
  `FirAnonymousInitializerSymbol` of the owner → REPORT; non-local `FirPropertySymbol` of
  the owner (a sibling initializer; a getter body shows `FirPropertyAccessorSymbol` and
  is NOT an initializer) → REPORT; any function symbol on top (lambda, method,
  constructor) → clean. DISCLOSED HOLE (documented, under-approximation convention): an
  inline lambda invoked synchronously in init (`run { airingId }`) classifies as
  deferred.
- `BrsComponentTypes` (new, `checkers.brs`): the component-class predicate extracted from
  `FirBrsCreateComponentTypeChecker`, with the MIRROR NOTE to
  `BrsIntrinsics.isSceneGraphComponent`/`BrsComponentExtractor.isComponent`
  (BrsSharedServiceTypes precedent; divergence lands in both in one commit).
- `FirBrsTaskStateNotFieldChecker`: REMOVE the fake-source skip (`:61`) and its KDoc
  justification — a plain constructor `val` on a task component now fires
  `BRS_TASK_STATE_NOT_FIELD`.
- Regenerate both diagnostic containers (CLAUDE.md recipe); add default messages; add the
  inverse templates to the harness map.

## 8. Pre-plan device spikes (raw BrightScript, flow-spike template)

One package `spikes/lifecycle-spike/probe-init/` (`manifest`, `source/main.brs`,
`components/*.xml|.brs`, `README.md` verdict contract, `deploy.sh`), verdict grammar
`[SPIKE] init.<q> PASS|FAIL k=v`, committed capture at the spike root. Run BEFORE the
API is fixed; stamp this section EXECUTED with the FINDINGS pointer. Facts only in
FINDINGS; design consequences here.

1. **Child-before-parent init** (XML-declared child) — DOCUMENTED (§2 third batch);
   a global-node log string written from both inits confirms on device.
2. **`getParent()` inside the XML-declared child's init** — DOCUMENTED invalid ("the
   parent node is set after the node is created, and init() is called"); confirm.
3. **`getParent()` inside a `CreateObject`-created child's init** (expected invalid),
   plus the `scene.createChild(subtype)` create-and-append variant.
4. **XML `<children>` attribute timing**: is a child's attribute value visible inside
   its own init (applied before) or only after? The docs imply "after" (the child's own
   init is not listed as an override source); pin it. Decides whether a body property's
   Kotlin initializer (init step 4) can clobber a layout-declared value — a separate
   pre-existing hazard to record either way.
5. **`getScene()` inside the init of a not-yet-attached node** (spec 2's terminal walk
   hop; also `top.getScene()` in handlers post-attach is already pinned).
   5b. **`getParent()` of a Scene's direct `<children>` child**: the Scene node itself, or
   one of the hidden framework elements ifSGNodeChildren describes? Compare with
   `getScene()` via `isSameNode`. Decides spec 2's walk-termination rule.
6. **callFunc render→render** (docs say synchronized, owner's thread and `m`): (a)
   parent calls a bare function in a child component — pin the direct same-thread
   execution and that `m` is the CHILD's; (b) `hasFunc("__kotlinRetire")` answers true
   on a Kotlin component and false on a plain node (the retire guard; also record the
   OS floor behaviour if the device offers a way to tell); (c) a component calling
   `callFunc` on ITS OWN node (self-retire) executes directly. `threadinfo()` lines as
   supporting evidence.
7. **Detach variants** (expected negative per A4): a child arms an observer on the
   PARENT's `change` field (documented operations `remove`/`set`/`clear`/`setall` are the
   removal shapes); after `removeChild`, does it fire while the parent KEEPS its
   reference? with an unscoped `observeField`? on a one-call `reparent()` (which
   operation, if any, is recorded)? Result decides only whether a backstop is offered;
   the design does not depend on it.

"A coroutine launched in a child's init runs after the parent's synchronous init" needs
the stdlib pump and is pinned in Suite 11, not the spike.

## 9. Verification

| Gate | Today | After |
|---|---|---|
| Golden file tests | 100 | +9 (below) |
| FIR diagnostic suite | 232 | +11 fixtures (listed below) |
| Stdlib device suite | 610 / 62 | +1 suite (lifecycle units) |
| rokuTest E2E | 104 active / 10 suites | +Suite 11 ComponentLifecycle (10 asserting + 1 informational, below) + 2 Suite 8 tests |
| `validateComponentIncludes` + test variant | strict, 0 | strict, 0 |

**Goldens** (`compiler/testData/codegen/brs/components/`): `ctorInputs` (field + no init
write + marker field); `ctorCallLowering` (creation block, incl. zero-arg and
ContentNode); `onStartDriver` (emitted only when overridden; inherited override;
concrete-base + leaf → driver in both inits, one claim); `superDispatchComponent`;
`onKeyEventInheritedWrapper`; `retireReviveEntries` (with/without `onStop`/`onStart`);
`builderCallExtraction` (typed builder → XML attrs + `__kotlinInputsReady="true"`);
`layoutRequiredInputError` (missing / non-constant → error text); `attachUnconditional`
(coroutine-free component gets the attach; `__kotlinPumpAttach` line gone).

**FIR fixtures** (`testsWithBrsStdLib/componentInputs/`): init-block read (fires),
sibling-initializer read (fires), lambda read in init (clean), read in `onStart`
(clean), read in a method (clean), `@Suppress` (clean), `createComponent` on an
input-bearing class (fires), the same under `@Suppress` (clean), task constructor input
(fires), the same under `@Suppress` (clean), plain constructor `val` on a task component
(`BRS_TASK_STATE_NOT_FIELD` fires).

**Stdlib unit suite** (runBlocking regime, `libraries/stdlib/brs/test/kotlin/`):
`awaitReady` fast path / park / wake on ticket resolve; driver claim once-flag; retire
idempotence + `retired` + holder reset (fresh scope after retire); revive no-op on live;
guided message texts; off-context ISEs.

**E2E Suite 11 ComponentLifecycle** (`tests/ComponentLifecycleTests.kt`; fixtures
`LifecycleParentProbe` — declares one child through a typed builder and constructs
another through a constructor call — `LifecycleChildProbe`, `LifecycleBaseProbe` +
`LifecycleLeafProbe` (concrete base + leaf), a dumb `LifecycleDumbProbe`; evidence via an
outliving blackboard node — init-time facts cannot go through @SG fields):

1. `onStart` fires for the layout-declared child AFTER the parent's init completed
   (pins the coroutine-after-parent-init ordering).
2. `onStart` fires exactly once for the concrete-base + leaf pair, and reaches the LEAF
   override.
3. `onStart` reads constructor inputs correctly — constructor-call path AND typed-builder
   constant path.
4. The dumb probe never launches anything (its blackboard beat stays zero).
5. `retire`: `onStop` ran; a parked `runTask` (SleepTask) was STOPped; a child's pending
   ScopeHandle request settled `ScopeClosedException`; a second `retire` is a no-op.
6. `retire` cancels an `onStart` still parked in `awaitReady`.
7. Watchdog counter increments for a node created by raw subtype string (marker never
   lands); the console line names the UNSET-inputs cause.
8. `super.onKeyEvent` chain dispatches to the base; an inherited `onKeyEvent` override
   is reached from a leaf without one.
9. Full recycle cycle: `retire → removeChild → set a var field → appendChild → revive`:
   `onStart` fires again, a coroutine launched after revive runs, and (with spec 2) a
   dependency re-resolves under a NEW parent — the spec-2 half is added when spec 2
   lands.
10. `exposeScope` after retire → revive → a child request is answered (Suite 8 gains
    `scopeReExposeAfterRetire` + `scopeRetireSettlesClosed`).
11. INFORMATIONAL timing line: create 1000 `LifecycleDumbProbe` instances; report ms
    (decision 10 evidence; never PASS/FAIL).

## 10. Documentation deliverables (CLAUDE.md)

- New section "Component lifecycle: onStart/onStop, retire/revive" — the contract, the
  gate, the recycling law, `init` vs `onStart` split, what retire cannot promise
  (unretired death is silent; task-side `finally` never runs on STOP), the watchdog
  line format, test hooks.
- New section "Constructor inputs" — syntax, `val` semantics, the init-read rule and its
  disclosed hole, creation via constructor call, typed builders + the constants-only
  law, the marker's meaning, task exclusion.
- Amend "Component Coroutine Scaffolding": the attach is unconditional; the helper-file
  hole is closed; `__kotlinPumpAttach` injection retired.
- Amend "ScopeHandle": `exposeScope` re-callable after retire (one OPEN host).
- Amend the SharedService design doc's decision 10 row with a "SUPERSEDED by
  2026-09-04-component-lifecycle-design.md" note (do not rewrite history; annotate).
- Gate table + suite table updates; `SceneComponent.kt` KDoc for `VideoItem()` becomes true.
- Roku pages in `../RokuDocs/` (added 2026-09-04, facts folded into §2): `Node.html`,
  `roSGNode.html`, `ifSGNodeChildren.html`, `ifSGNodeDict.html`, `ifSGNodeField.html`,
  `ifSGNodeFocus.html`, `ifSGNodeBoundingRect.html`, `ifSGNodeHttpAgentAccess.html`,
  `roSGNodeEvent.html`, `roSGScreenEvent.html`, `roHttpAgent.html`, `ifHttpAgent.html`.
  Also present: `Component initialization order.html`, `Creating custom components.html`,
  `SceneGraph compilation.html` (facts folded into §2). Nothing further is outstanding.
  The pages note that appending `.md` to a docs URL yields
  Markdown and `https://developer.roku.com/dev/llms.txt` is an index — untested against
  the bot block CLAUDE.md records.

## 11. Phasing

- **Phase 0 — spikes** (§8) → FINDINGS, this doc stamped, §2 facts updated.
- **Phase 1 — compiler fixes en route:** super dispatch (§5.4), `onKeyEvent` wrapper
  (§5.5), unconditional attach (§5.1) + stdlib `__kotlinComponentAttach`. Goldens +
  Suite 6/1 regressions green.
- **Phase 2 — hooks + registry + retire/revive:** stdlib registry, gate, watchdog,
  retire/revive impls; hierarchy scan; driver synthesis; `__kotlinRetire`/`__kotlinRevive`
  generation; `exposeScope` amendment. Stdlib units + Suite 11 tests 1-2, 4-8, 10.
- **Phase 3 — constructor inputs:** lowering, init skip, extractor (marker, builder
  recognition, post-extraction validation, error twin), FIR family + regen, plugin
  builders + tests. Goldens, FIR fixtures, Suite 11 tests 3, 9 (spec-1 half), 11.
- **Phase 4 — acceptance + docs:** flagship TestScreenChildLabel migrated to `onStart`
  as an INTERIM shape — it keeps the `screenRef` node field but drops the onChange
  handler, the manual launch and the re-fire guard, acquiring in `onStart` (which relies
  on the parent's synchronous publish-then-set in its init; spec 2's `by sharedService`
  removes that reliance and the field) — CLAUDE.md, gate table, memory/backlog sweep.

Process: `./rebuild.sh` only; device gates per phase; commit per phase on the feature
branch; gates never drop.

## 12. Risks

- **IR synthesis of a suspend lambda before the suspend lowering** is a new shape (the
  flow lift synthesizes whole classes, but post-FIR). Mitigation: golden-pinned; the
  driver body is three calls.
- **callFunc render→render from Kotlin is documented (synchronized, owner's thread and
  `m`) but unpinned** (§8 Q6). If the device disagrees, the fallback hop is a dedicated
  `__kotlinLifecycle` string field with a scoped observer — ASYNCHRONOUS, which forces the
  recycling law to insert a pump tick between `retire` and `removeChild` (A4). Spike first.
- **XML attribute timing** (§8 Q4) may reveal that init step 4 clobbers layout-declared
  values for body properties — a pre-existing bug this program would then record and fix
  (skip the init write when an XML attribute is present, or emit the attribute as the
  field's `value=` default).
- **Plugin grammar fragility.** The constrained grammar rejects rather than guesses;
  the compiler's extractor is the source of truth; a wrong or missing builder is a
  compile-time signal, never a silent XML.
- **Per-instance cost claim** — pinned by Suite 11 test 11 before the guide asserts it.
- **`exposeScope` law change** — Suite 8 (30 tests) must stay green; the amendment adds
  two tests rather than altering existing ones.
- **Include-closure edges** for the new stdlib entry points — `validateComponentIncludes`
  strict mode is the gate; the attach call's recorded edge is the mechanism.
- **Self-retire** (§8 Q6c) — if unsupported, `retire` from inside the component is a
  guided ISE; the app's screen stack owns the call anyway.

## 13. Interface to spec 2 (the `by sharedService { key }` delegate)

Internal API on the lifecycle registry (`kotlin.brs.lifecycle`, `@PublishedApi
internal`), consumed by spec 2:

```kotlin
internal class DependencyTicket(val label: String, val rearm: () -> Unit) { var resolved = false; fun resolve() }
internal fun kotlinLifecycleRegisterDependency(label: String, rearm: () -> Unit): DependencyTicket  // init-time only; after the driver fired → guided ISE
internal fun kotlinLifecycleOnRetire(hook: () -> Unit)   // runs after onStop, before scope cancellation
internal fun kotlinLifecycleIsRetired(): Boolean
public suspend fun ComponentBase.awaitReady()             // the same gate; re-entrant
```

**Ordering guarantee spec 2 relies on:** the attach is the FIRST init statement, so the
registry exists before any property initializer; property delegates initialize during
init (`provideDelegate` with the component as `thisRef` — `by lazy` already compiles
with `m` as `thisRef`), so every ticket is registered before the driver body runs on the
next pump tick. A delegate's FIRST resolution attempt therefore happens during init,
when the parent has not published and, for a dynamically created node, `getParent()` is
still invalid (§8 Q3): the attempt misses, the delegate arms on spec 2's global-node
doorbell, and the parent's publish rings it. Spec 2's walk must treat an invalid parent
as "not yet", never as an error. `ticket.resolve()` may run synchronously inside
registration. `rearm` runs on every revive: drop the cached instance, walk again from
the now-attached node, arm on a miss.

## 14. Out of scope (recorded)

- Visibility-based `onStart`/`onStop` semantics (observe `visible`) — the names are
  taken by the activation cycle; a visibility pair would be `onShow`/`onHide` later.
- A toolchain screen stack (`show(screen)`/`pop()`) that calls `retire`/`revive` itself —
  the natural next program; this spec's calls are what it would invoke.
- Detach-detection backstop unless §8 Q7 is positive.
- Typed `_Layout` accessors (handles typed as the component class) and method calls on
  handles (existing residual: slot call on a node crashes; no FIR guard).
- Cross-module component infos for the layout validation (single-module closed world;
  ScopeHandle/SharedService precedent).
- Builders for components with node-/AA-typed required inputs; ContentNode/Task builders.
- `removeField`-based cleanup of accumulated doorbell fields (bound, unpinned, unused).
- `@SGRequired` as an annotation — superseded: constructor parameters ARE the required
  inputs.

## 15. Public API summary (new/changed)

```kotlin
// package kotlin.brs (ComponentBase, SceneComponent.kt)
protected open suspend fun ComponentBase.onStart()      // member
protected open fun ComponentBase.onStop()               // member
public suspend fun ComponentBase.awaitReady()
public fun retire(node: RoSGNode); public fun retire(component: ComponentBase)
public fun revive(node: RoSGNode); public fun revive(component: ComponentBase)
public fun kotlinLifecycleWatchdogFires(): Int; public fun kotlinLifecycleWatchdogMillis(ms: Int)
// constructor inputs: `class X(@SGStringField val a: String) : GroupComponent()`; `X("v")` is a real call
// package kotlin.brs.scenegraph
public annotation class SGComponentBuilder(val componentType: String)
// generated by kotlin-roku per component: fun LayoutBuilder.<lowerCamel>(id, <inputs>, <attrs>, init)
// ScopeHandle amendment: exposeScope re-callable after retire (one OPEN host per component)
// FIR: BRS_COMPONENT_INPUT_READ_IN_INIT, BRS_CREATE_COMPONENT_HAS_INPUTS, BRS_TASK_CONSTRUCTOR_INPUT
```
