All research complete. Final reference map follows.

---

# DOMAIN 2 REFERENCE MAP: typed-task machinery end to end

## 1. TaskRunner.kt — the full runTask flow

File: `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`

**Public/exact signatures:**

```kotlin
public class TaskException(
    message: String,
    public val number: Int,          // BrightScript runtime error number (0 when unavailable)
    public val backtrace: Dynamic?,  // BrightScript backtrace array, if any
) : RuntimeException(message)                                              // L51-57

public suspend fun <T : TaskComponent> T.awaitCompletion(): T             // L204

@PublishedApi
internal suspend fun <T : TaskComponent> runTaskImpl(task: T, configure: T.() -> Unit): T  // L272-273

public suspend inline fun <reified T : TaskComponent> runTask(noinline configure: T.() -> Unit): T =
    runTaskImpl(createComponent<T>(), configure)                          // L306-307
```

**Protocol constants (L62-65):** `TASK_STATE_FIELD="kotlinTaskState"`, `TASK_ERROR_FIELD="kotlinTaskError"`, `TASK_ID_FIELD="kotlinTaskId"`, `TASK_CONTROL_FIELD="control"` (native Task lifecycle field).

**Node creation:** `createComponent<T>()` (`libraries/stdlib/brs/src/kotlin/brs/ComponentFactory.kt:27`) → `brsCreateComponent<T>()` stub (L35-37, body is `error(...)`) → lowered by codegen intrinsic to `CreateObject("roSGNode", "<ComponentName>")` (see §3). The typed handle IS the raw roSGNode: `@BrsInline("return task") private external fun taskNodeOf(task: TaskComponent): RoSGNode` (TaskRunner.kt:71-72).

**Input fields:** `task.configure()` (runTaskImpl L274) — plain `@SG*Field` property writes, which compile to direct node dot-assign on the handle (see §4 codegen routing).

**Arming order (runTaskImpl L274-279, device-proven):** configure → `task.kotlinTaskId = TaskRunner.allocateTaskId()` → `node.observeFieldScoped(TASK_STATE_FIELD, brsName(::onKotlinTaskStateChanged))` **BEFORE** `node.setField(TASK_CONTROL_FIELD, "RUN")` → `task.awaitCompletion()`. awaitCompletion arms again + fast-paths on already-terminal state (L222-231) so duplicate observes/deliveries are harmless; there is no missable window.

**Registry (L89-106):** `internal object TaskRunner` — `pending: mutableMapOf<Int, ParkedContinuation>`, `fun allocateTaskId(): Int` (ids start at 1; 0 = unassigned), `hasPending(taskId)`, `register(taskId, parked)`, `remove(taskId): ParkedContinuation?`.

**Observer callback (L118-161):** `internal fun onKotlinTaskStateChanged(event: RoSGNodeEvent)` — top-level, same file, registered by NAME (function-name observer form ⇒ must be in the caller component's include closure; the caller's calls into this file guarantee that via dependency recording). Guards: destroyed-node (`taskEventNodeOrNull`, L80-81 `@BrsInline("return event.getRoSGNode()")`), non-terminal states return, registry-miss drops, unobserve BEFORE resume, settle via `parked.tryResume(node)` / `parked.tryResumeException(taskExceptionFrom(node))` — resumption routes through the ContinuationInterceptor when present.

**TaskException construction (L164-181):** `taskExceptionFrom(node)` reads `kotlinTaskError` AA, lookups `"message"`/`"number"`/`"backtrace"`, defaults `"Task failed"`/0/null.

**awaitCompletion cancellation wiring (L237-255):** tail-delegating `suspendCoroutineUninterceptedOrReturn` block: `continuation.context.ensureActive()` entry check → `ParkedContinuation(continuation)` → `TaskRunner.register` → `jobImplOf(continuation.context[Job])?.invokeOnCancelRequest { TaskRunner.remove(taskId); node.unobserveFieldScoped(TASK_STATE_FIELD) }` (cleanup registered BEFORE `registerCallerCancel(parked, continuation.context)`) → `parked.finish()` last. Double-await guard throws `IllegalStateException("Task node (kotlinTaskId=$taskId) is already being awaited")` BEFORE arming (L219-221 — unobserving there would strip the first awaiter's observer).

**Ambient-context guard: runTask has NO runtime guard today** — the render-thread-component constraint is docs-only (L293-297). The guided-ISE precedent the Flow plan should copy is `shareOn` (`libraries/stdlib/brs/src/kotlin/brs/shared/SharedService.kt:165-170`): `if (PumpScheduler.hostTopOrNull() == null) throw IllegalStateException("shareOn must be called from a render-thread component context (like runTask)")` — detection primitive `PumpScheduler.hostTopOrNull(): RoSGNode?` (`libraries/stdlib/brs/src/kotlin/coroutines/pump/PumpScheduler.kt:119`); same pattern at `ComponentMailbox.kt:170` (ScopeHandle.run).

## 2. TaskComponent and the @SG annotation set

`libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt:209-244`:

```kotlin
@BrsSceneGraphComponent(extends = "Task")
public abstract class TaskComponent : ComponentBase() {
    @SGStringField(alwaysNotify = true)
    public var kotlinTaskState: String = ""            // "" running, then "done"/"error"; written LAST
    @SGAssocArrayField
    public var kotlinTaskError: RoAssociativeArray? = null  // {message, number, backtrace}; written BEFORE state
    @SGIntegerField
    public var kotlinTaskId: Int = 0                   // correlation id
    protected abstract fun run()                       // task thread; only @SG*Field state crosses
}
```

**Annotations** — all in `libraries/stdlib/brs/src/kotlin/brs/annotations.kt` (package `kotlin.brs`, `@Target(PROPERTY)`, BINARY retention): `SGStringField(alwaysNotify: Boolean = false, defaultValue: String = "")` L142; `SGIntegerField(..., defaultValue: Int = 0)` L155; `SGLongIntegerField` L168; `SGFloatField` L181; `SGDoubleField` L194; `SGBooleanField` L207; `SGArrayField(alwaysNotify: Boolean = false)` L219; `SGAssocArrayField(alwaysNotify)` L230; `SGNodeField(alwaysNotify: Boolean = false, nodeType: String = "")` L243; `SGFunctionField` L255; `SGUriField` L269; `SGTimeField` L281; `SGVector2DField` L294; `SGColorField(alwaysNotify, defaultValue)` L308. Legacy `BrsField(type, alwaysNotify, name, defaultValue, alias)` L97-103; `BrsOnChange(handler: String)` L114; `BrsSceneGraphComponent(extends: String = "Group")` L423-425; `BrsComponent(name, extends)` L324-327.

**Device-side run() dispatch:** compiler emits `sub __kotlinTaskMain()` (constant `KOTLIN_TASK_MAIN_FUNCTION_NAME`, `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsComponentInfo.kt:18`) wrapping `<Class>_run_k_()` in try/catch — error AA before state, state last (shape at `IrToBrsTransformer.generateTaskMainFunction`, see §3). Generated `init()` sets `m.top.functionName = "__kotlinTaskMain"`; the XML exports `<function name="__kotlinTaskMain"/>`. Roku's Task node runs `functionName` on the task thread when `control="RUN"`.

**Quarantine note:** `kotlin/coroutines/task/CoroutineTask.kt:48` `internal class CoroutineTask : ComponentBase()` — extends ComponentBase DIRECTLY, deliberately NOT a TaskComponent (excluded by `isTaskComponent`); part of the quarantined IO pipeline. Ignore.

## 3. THE KEY QUESTION — compilation chain, and what an IR-synthesized TaskComponent subclass would ride/miss

All in `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/`.

**Predicates (`BrsIntrinsics.kt`):**
- `taskComponentClass: IrClassSymbol?` L211-214 (via `BrsStandardClassIds.Components.TaskComponent`).
- `fun isSceneGraphComponent(irClass: IrClass): Boolean` L238-261 — BFS for `@BrsSceneGraphComponent` on class or any supertype.
- `fun isComponentBaseDeclaration(irClass: IrClass): Boolean` L274-277 — abstract + directly annotated (stdlib bases emit NO brs of their own).
- `fun isTaskComponent(irClass: IrClass): Boolean` L284-287 — hierarchy contains the TaskComponent symbol.
- `IrToBrsTransformer.isConcreteTaskComponent(irClass)` — `isTaskComponent && modality != ABSTRACT` (`transformers/irToBrs/IrToBrsTransformer.kt:830-833`).

**Pipeline order — `BrsCompiler.compile` (`BrsCompiler.kt:135-244`):**
1. **Pre-extraction (BEFORE lowering)** L153-164: `BrsComponentExtractor(context).extractComponent(irClass)` over `irModule.files` top-level `IrClass`es → `preExtractedComponents: Map<String name, BrsComponentInfo>`. Runs pre-lowering ONLY to preserve the @SGLayout DSL (comment L153-155).
2. **Lowering** L167: `BrsLoweringPhases.lower(irModule, context)` — mutates and returns the SAME module.
3. **Pass 1** L180-196: function manifest over `loweredModule.files` (`collectFunctionManifest`; output name = `File(file.path).nameWithoutExtension + "Kt.brs"`).
4. **Pass 2** L204-217: per file, `context.currentSourceFile = outputFileName`; `compileFile(file, transformer, context)` (L475-505) → `transformer.transformFile(irFile)` → render.
5. **Pass 3 (dedicated, after ALL files transform)** L222-238: `generateComponentOutputForFile(file, preExtractedComponents, context)` (L1839-1865) → per component: `computeComponentDependencies(outputFileName, context)` (L1875-1893: direct deps from `context.fileDependencies[outputFileName]`, merged with klib `dependencyFileDeps`, `resolveTransitiveDependencies` L1938-1958) → `generateComponentXmlContent` (L1967-2076) + `generateComponentDepsJson` (L1909-1927).

**Task-specific codegen (Pass 2, `IrToBrsTransformer.kt`):** `transformClassDeclarations` L562-628 routes SceneGraph components to `transformSceneGraphComponent` L660-731 (base declarations skipped L669-671). There: member functions emit as top-level mangled functions; `transformComponentInitBlock` (L967-…) prepends `m.top.functionName = "__kotlinTaskMain"` for concrete task components (L977-987) and EXCLUDES task components from `__kotlinPumpAttach` injection (L996); `generateTaskMainFunction(irClass): BrsSub?` L887-956 emits the try/catch wrapper iff `findLocalTaskRunImplementation` (L848-855: local, non-abstract, non-fake-override zero-arg `run`) exists — and calls `context.recordFunctionDependency(runName)` L894.

**XML content (`BrsCompiler.generateComponentXmlContent` L1967-2076):** interface fields from `BrsComponentInfo.fields`; `<function name="__kotlinTaskMain"/>` for concrete task components L2028-2033; script tags = sorted deps at `pkg:/source/<dep>` L2039-2053, then own script **hard-coded** as `pkg:/components/${component.name}/${component.name}Kt.brs` L2056.

**File routing (`BrsCompiler.writeOutput`, companion, L2148-2212):** a source file's .brs goes to `components/<Name>/` **only when the file's base name (minus "Kt.brs") exactly equals a component name** (L2160-2185); otherwise `source/`. XML + deps.json written to `components/<Name>/<Name>.xml` and `<Name>.deps.json` L2194-2210.

**deps recording (`BrsIrBackendContext.kt`):** `fileDependencies` L86, `currentSourceFile` L93, `functionManifest` L101, `fun recordFunctionDependency(functionName: String)` L144-180 (manifest lookup, warns on recording gaps, skips self-deps).

**runTask call-site rewrite (`lower/BrsRunTaskCallLowering.kt` L41-84, phase 0.055 registered `BrsLoweringPhases.kt:148`):** rewrites `runTask<T>(configure)` → `runTaskImpl(brsCreateComponent<T>(), configure)` (klib inline fns never inline at user sites). MUST run before `UpgradeCallableReferences` (configure still `IrFunctionExpression`) and before coroutine lowering (suspend call replaces suspend call). "Keep signature in sync with TaskRunner.kt" is a stated contract (TaskRunner.kt:259-264). The `brsCreateComponent` codegen intrinsic: `IrExpressionToBrsTransformer.kt:801-830` — matches `kotlin.brs.createComponent`/`brsCreateComponent`, emits `CreateObject("roSGNode", "<getBrsName(class)>")`, IR-level error backstop for non-concrete/non-component (L813-822), unsubstituted type param left alone.

**VERDICT for an IR-synthesized class extending TaskComponent (created by a lowering pass):**
- It **rides automatically**: Pass 1 manifest, Pass 2 transform (isSceneGraphComponent/isTaskComponent are supertype-walks — satisfied), `__kotlinTaskMain` wrapper + `functionName` init wiring (needs a locally-declared concrete `run()`), per-file dependency recording, Pass 3 transitive-closure resolution, and `writeOutput` — **because `BrsLoweringPhases.lower` returns the same mutated module, and Passes 1–3 iterate `loweredModule.files` AFTER lowering**, so a synthetic IrFile added during lowering is visited by all three passes.
- It **misses exactly one thing**: `preExtractedComponents` (built at BrsCompiler.kt:153-164, BEFORE lowering). Pass 3 filters classes by `context.getBrsName(it) in preExtractedComponents` (L1848-1850) → **no XML, no deps.json** for the synthesized component. Fix options: (a) have the lift register a `BrsComponentInfo` (plain data class, `BrsComponentInfo.kt:137-173`; `BrsFieldInfo` L178-224 needs only name/type/alwaysNotify — `irProperty` nullable) on the context and merge it into the map before Pass 3; or (b) run `extractComponent` post-lowering for synthesized classes — safe since they have no @SGLayout DSL (the only reason extraction is pre-lowering), but then synthesized `@SG*Field` annotations must exist as real `IrConstructorCall`s (the extractor matches annotations by SHORT NAME, `BrsComponentExtractor.findAnnotation` L550).
- **Two structural obligations regardless:** (1) synthesize ONE IrFile per component whose `path` base name equals the class's BRS name — otherwise `writeOutput` routes the .brs to `source/` while the XML points at `pkg:/components/<Name>/<Name>Kt.brs` (L2056) → device "Function is not defined"; (2) if the synthesized `run()` body writes output properties via IrSetField/accessor calls, those properties MUST carry real `@SG*Field` IrConstructorCall annotations, because codegen's m.top routing is annotation-gated: `hasInterfaceFieldAnnotation` (`transformers/irToBrs/brsTransformerUtils.kt:407`), consulted at `IrExpressionToBrsTransformer.kt:320-331` (getField), `367-382` (setField), and `1470-1541` (accessor-call path: self → `m.top.field`; other receiver → direct `<recv>.field` node access). Without the annotation the write lands on the m-scope AA and is clone-lost.
- FIR checkers never see IR-synthesized classes (FIR runs before the backend) — no BRS_TASK_STATE_NOT_FIELD interaction; the IR backstop in the createComponent intrinsic passes for a concrete component class.
- Phase slot precedent: the lift belongs alongside phases 0.055–0.057 — see `BrsScopeRunBlockLowering` (`lower/BrsScopeRunBlockLowering.kt`) as the lambda-lift template: `data class ScopeRunBlock(val requestName: String, val liftedFunction: IrSimpleFunction)` L69; lifted fn takes one `captures: RoAssociativeArray?` param and re-declares each capture at entry via `captures.lookup(name)` (L73-78); registry `BrsIrBackendContext.scopeRunBlocks: mutableMapOf<IrFile, MutableList<ScopeRunBlock>>` L333.

## 4. Kotlin type → SceneGraph field-type mapping

- **Backend (annotation → XML type):** `BrsComponentExtractor.sgFieldAnnotationTypes` (`BrsComponentExtractor.kt:281-296`) — annotation short name → `BrsFieldTypes` constant (`BrsComponentInfo.kt:244-258`: string/integer/longinteger/float/double/boolean/array/assocarray/node/function/uri/time/vector2d/color). Extraction path: `extractFields` L222-276 (own real props first, then `collectInheritedDeclarations<IrProperty>(stopAtUserComponents = true)` L160-184 — user-component ancestors skipped: their own XML declares those fields, re-declaring crashes node creation with "Attempt to add duplicate field"); `extractFieldFromTypeSafeAnnotation` L315-339 (`alwaysNotify`, `defaultValue` via `extractTypedDefaultValue` L344-380 — non-default values only, `nodeType` for SGNodeField); `findOnChangeHandler` L448-476 (@BrsOnChange or `on<Prop>Changed` convention, mangled via `context.getBrsName`).
- **Fallback (un-annotated @BrsField type inference):** `mapTypeToFieldType(type: IrType?)` L514-528 — Int/Short/Byte→integer, Long→longinteger, Float, Double, Boolean, String, `isArray()`→array, `isNodeType()`→node (component class, or name in {Node,Group,Task,ContentNode} or endsWith "Node", L533-543), else assocarray.
- **FIR-side mirror:** `core/compiler.common.brightscript/src/org/jetbrains/kotlin/name/BrsStandardClassIds.kt:330-345` — `Annotations.sgFieldAnnotationTypes: Map<ClassId, String>` (same 14 entries, ClassId-keyed). Marshallable-set correspondence for the Flow lift's capture fields: Int→SGIntegerField, Boolean→SGBooleanField, Double→SGDoubleField, String→SGStringField, RoArray→SGArrayField, RoAssociativeArray/Dynamic→SGAssocArrayField, node refs→SGNodeField. (No dedicated "dynamic" field type exists — assocarray is what `kotlinTaskError: RoAssociativeArray?` uses; note `mapTypeToFieldType`'s else-branch is also assocarray.)

## 5. FIR diagnostics precedent for the BRS_TASK_* family

- **Declarations list:** `compiler/fir/checkers/checkers-component-generator/src/org/jetbrains/kotlin/fir/checkers/generator/diagnostics/FirBrsDiagnosticsList.kt:172-184` — group `TYPED_TASKS by object : DiagnosticGroup("Typed task components")`: `BRS_TASK_STATE_NOT_FIELD by error<KtElement>(PositioningStrategy.DECLARATION_NAME) { parameter<String>("propertyName"); parameter<String>("className"); isSuppressible = true }` and `BRS_CREATE_COMPONENT_INVALID_TYPE by error<KtElement> { parameter<String>("functionName"); parameter<String>("actualType"); parameter<String>("reason"); isSuppressible = true }`. Regen procedure is in CLAUDE.md ("Regenerating FIR diagnostic containers") — generated `FirBrsErrors.kt` + aggregator both committed; messages added by hand to `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrorsDefaultMessages.kt` (entries at L209, L216).
- **`FirBrsTaskStateNotFieldChecker`** (`checkers.brs/src/.../checkers/declaration/FirBrsTaskStateNotFieldChecker.kt`, object at L54): `FirPropertyChecker(MppCheckerKind.Common)`, `context(context: CheckerContext, reporter: DiagnosticReporter) override fun check(declaration: FirProperty)`. Skips fake-source/delegated/no-backing-field; containing class must be concrete; task-derived test via `lookupSuperTypes(..., deep = true).any { it.lookupTag.classId == BrsStandardClassIds.Components.TaskComponent }` L70-71; annotation set = `sgFieldAnnotationTypes.keys + BrsField` L56-57. Registered `checkers/BrsDeclarationCheckers.kt:57`.
- **`FirBrsCreateComponentTypeChecker`** (`checkers/expression/FirBrsCreateComponentTypeChecker.kt`, object at L49): `FirFunctionCallChecker(MppCheckerKind.Common)`; callable set `BrsStandardClassIds.Callables.componentFactoryCallables = setOf(createComponent, brsCreateComponent, runTask)` (`BrsStandardClassIds.kt:516`; `Callables.runTask` L509 in `BASE_COROUTINES_TASK_PACKAGE`). Skips `ConeTypeParameterType` (reified forwarders); reasons: interface / abstract / not-a-component (FIR mirror of `BrsComponentExtractor.isComponent`, L85-92). Registered `checkers/BrsExpressionCheckers.kt:28`. **The new Flow callables (flowOn/spawnTask) can join `componentFactoryCallables`-style sets the same way; test fixtures live in `compiler/testData/diagnostics/testsWithBrsStdLib/`.**

## GOTCHAS

1. **Extraction-before-lowering is the ONE gap** for synthesized components: `preExtractedComponents` (BrsCompiler.kt:153-164) is built pre-lowering and Pass 3 filters on it (L1848-1850). Everything else (manifest, transform, task wrapper, deps, writeOutput) runs post-lowering and picks up synthetic files automatically.
2. **File-name = component-name is load-bearing twice**: `writeOutput` routing (L2160-2173) AND the hard-coded own-script URI in XML (L2056). One synthetic IrFile per synthesized component, named exactly after the class, or the packaged app 404s its own script.
3. **`hasInterfaceFieldAnnotation` gates m.top routing at codegen** (brsTransformerUtils.kt:407; used at IrExpressionToBrsTransformer.kt:325-331/372-382/1499/1511) — synthesized capture/output properties need real IR annotation constructor calls, not just extractor-side field infos, or the task-side writes silently land on the cloned m-scope AA.
4. **runTask has no runtime ambient-context guard** — the "guided ISE" precedent is shareOn/ScopeHandle.run via `PumpScheduler.hostTopOrNull()`; the Flow v1 context law needs to ADD one, not copy one from runTask.
5. **Lift-lowering phase constraints**: must run before `UpgradeCallableReferences` (lambda still `IrFunctionExpression` with implicit captures) and before all coroutine lowering (suspend-for-suspend replacement) — slots 0.055–0.057 in `BrsLoweringPhases.kt:140-165`; `BrsScopeRunBlockLowering` is the lifting template (captures re-declared from a `captures` AA param).
6. **Arming order is a law**: observer BEFORE `control="RUN"` (runTaskImpl L276-278); observers attached after a task-thread write never see it. Also unobserve-before-resume in the callback (L148-150).
7. **Double-await guard placement**: bail BEFORE arming (TaskRunner.kt:213-221) — `unobserveFieldScoped` removes ALL of this scope's observers on the field, so disarming on the error path would strand the first awaiter. Any new await-shaped primitive sharing a field observer has the same trap.
8. **`kotlinTaskState` is `@SGStringField(alwaysNotify = true)`** — repeated terminal writes re-fire; the observer tolerates duplicates by registry-miss. The Flow envelope field should follow suit (each write must deliver).
9. **Task components are excluded from `__kotlinPumpAttach`** (IrToBrsTransformer.kt:996) and `run()` is synchronous by design — no `launch {}`/pump on the task thread; the synthesized `run()` must evaluate the upstream chain synchronously.
10. **Inherited-field XML dedup**: `stopAtUserComponents` (BrsComponentExtractor.kt:160-184, 253-273) — a synthesized component extending TaskComponent directly inherits the three `kotlinTask*` protocol fields into its own XML (stdlib bases emit no XML); but if the plan ever derives one task from another USER task, re-declaring inherited fields crashes node creation ("Attempt to add duplicate field", device-proven Suite 4).
11. **`__kotlinTaskMain` emission is per-declaring-class** (findLocalTaskRunImplementation, L848-855 + doc L835-846): the wrapper lands next to the `run()` declaration and reaches leaves via SceneGraph XML script inheritance — a synthesized class must DECLARE its own concrete `run()` to get its own wrapper.
12. **Dependency recording is warning-only in the compiler** (`recordFunctionDependency` L161-174); the hard gate is KGP `validateComponentIncludes` (strict, 0 findings). A synthesized file whose emitted calls resolve through the Pass-1 manifest records cleanly; anything bypassing `BrsFunctionCall`-creation-time recording is a silent include hole.
13. **BrsModuleCompilationResult carries everything the KGP consumes** (`BrsCompiler.kt:65-102`): `outputs`, `componentXml`, `componentDepsJson`, `functionManifest`, `fileDependencies` — a synthesized component's artifacts must appear in `componentXml`/`componentDepsJson` maps (Pass 3) to exist downstream; there is no other channel.