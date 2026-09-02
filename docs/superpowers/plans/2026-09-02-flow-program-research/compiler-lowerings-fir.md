# DOMAIN 3 REFERENCE MAP — IR lowerings + FIR checkers (Flow program plan)

All paths repo-relative to `/Users/Mike.Fougere/Documents/newt/git/Kotlin`.

## 1. The lambda-lift precedent: `BrsScopeRunBlockLowering`

`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsScopeRunBlockLowering.kt`

```kotlin
data class ScopeRunBlock(val requestName: String, val liftedFunction: IrSimpleFunction)   // L69
class BrsScopeRunBlockLowering(private val context: BrsIrBackendContext) : FileLoweringPass  // L105
```

Mechanism, piece by piece (all directly reusable for lifting a flowOn upstream / spawnTask block):

- **Call-shape detection** (L141–158, inside `irFile.transformChildrenVoid(object : IrElementTransformerVoid())`): match `callee.fqNameWhenAvailable == BrsStandardClassIds.Callables.scopeHandleRun.asSingleFqName()`, single value param of suspend-function type (`classFqName.startsWith("kotlin.coroutines.SuspendFunction")`, L183–184), argument must be `IrFunctionExpression` (literal lambda — non-literal falls through untouched; FIR rejects, suppressed calls hit the stdlib backstop ISE). Tracks `currentFunction` via `visitFunction` override (L133–139) so the rewrite can parent temp variables; a null enclosing function is `compilationException` (L157–158).
- **Naming** (L120–124, 160–165): request name `<fileFq>#<ordinal>` (1-based per file in transform order); lifted function name `__scopeBlock_<sanitizedFileName>_<ordinal>` where sanitize = `Regex("[^A-Za-z0-9_]") → "_"`.
- **Capture collection** — `private fun collectCaptures(lambda: IrSimpleFunction): List<Capture>` (L195–244): free-value analysis via `IrVisitorVoid` — every `IrGetValue`/`IrSetValue` whose symbol was not declared inside the lambda (params, locals, nested `IrValueParameter`s all recorded as declared), first-reference order; names from source (`<this>`/`$this*` → `"this"`, `$`→`_`, uniquified with `_2` suffixes).
- **Lifted function build** — `private fun buildLiftedFunction(functionName: String, lambda: IrSimpleFunction, captures: List<Capture>, capturesType: IrType, aaLookup: IrSimpleFunctionSymbol): IrSimpleFunction` (L252–331): `context.irFactory.buildFun { ... isSuspend = true; visibility = PRIVATE }`, one `captures: RoAssociativeArray?` param; per capture a local `val x = captures.lookup("x")` wrapped in `IrTypeOperator.IMPLICIT_CAST`; then the lambda body with `IrGetValue`/`IrSetValue` remapped to the locals and `IrReturn` retargeted from `lambda.symbol` to `lifted.symbol` (L306–325); finishes with `patchDeclarationParents(lifted)`.
- **Call-site rewrite** — `private fun buildRewrittenCall(...)` (L341–420): zero captures → direct `runLowered(name, invalid)` (`IrConstImpl.constNull`); with captures → `IrBlockImpl` that evaluates receiver into a temp (`IR_TEMPORARY_VARIABLE`, parented to `enclosing`), builds the AA via companion `RoAssociativeArray.create` + `addReplace(name, IrGetValueImpl(capture.symbol))` per capture, then calls `runLowered` — receiver-first evaluation preserved.
- **Registration of lift artifacts** (L167–168, 177–180): `context.scopeRunBlocks.getOrPut(irFile){...} += ScopeRunBlock(...)`; lifted functions appended to `irFile.declarations` AFTER the transform — they then ride ordinary top-level emission, the function manifest, and dependency recording for free.
- **Symbol prerequisites** (L112–118): `context.brsSymbols.scopeRunLoweredOrNull`, `roAssociativeArrayClass`, `iAssociativeArrayClass`; AA methods found by name on those classes (`create` on the companion, `lookup`/`addReplace` on `IAssociativeArray`). Lowering bails (`?: return`) if any missing — the stdlib-compilation-safe pattern.

## 2. The reified call-site rewrite precedent: `BrsSharedFromCallLowering`

`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsSharedFromCallLowering.kt` (104 lines total)

```kotlin
class BrsSharedFromCallLowering(private val context: BrsIrBackendContext) : FileLoweringPass  // L42
```

- Match by `expression.symbol.owner.fqNameWhenAvailable` against `BrsStandardClassIds.Callables.sharedFrom/.sharedFromOrNull.asSingleFqName()` (L56–60).
- Reified type arg: `expression.typeArguments.getOrNull(0)`; `serviceType.classOrNull?.owner ?: return expression` — **calls with an unsubstituted type parameter are left alone** (a user-written reified forwarder body), mirroring the createComponent intrinsic (L61–62).
- Class-name string via `context.getBrsName(serviceClass)` (L64) — the SAME string as `__proto` heads and `is`-check names.
- Rebuilds an `IrCallImpl` on `context.brsSymbols.sharedAcquireOrNull` carrying the type arg + const args. Nullability mismatch between site `T` and target `T?` is deliberately tolerated (untyped backend, L78–81).
- Simpler sibling: `BrsRunTaskCallLowering.kt` (L41–84) — rewrites `runTask<T>(configure)` → `runTaskImpl(brsCreateComponent<T>(), configure)`; the type arg moves onto a synthetic `brsCreateComponent<T>()` `IrCallImpl` which the codegen intrinsic lowers to `CreateObject("roSGNode", name)`. **This is the exact template for `spawnTask` / `flowOn` node creation**: `context.brsSymbols.runTaskImplOrNull` + `brsCreateComponentOrNull` (L48–49). "One suspend call replaces another, before any coroutine lowering."

## 3. Lowering pipeline registration

`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsLoweringPhases.kt`

```kotlin
object BrsLoweringPhases {                                     // L37
    fun lower(module: IrModuleFragment, context: BrsIrBackendContext): IrModuleFragment  // L42
```

- Phase 0 (L53–87): klib-aware inlining (`BrsInlineFunctionResolver` + `FunctionInlining`) — **cannot inline klib functions at user call sites**; that is why every reified stdlib entry point needs a call-site rewrite pass.
- The rewrite-pass slot the Flow lifts belong in — `phases += ...` in order:
  - L130 `BrsIntrinsicLowering` (0.05)
  - L138 `BrsMultiCatchLowering` (0.052)
  - **L148 `BrsRunTaskCallLowering` (0.055)**
  - **L157 `BrsSharedFromCallLowering` (0.056)**
  - **L165 `BrsScopeRunBlockLowering` (0.057)** ← a `BrsFlowOnLowering`/`BrsSpawnTaskLowering` slots here, BEFORE:
  - L182 `UpgradeCallableReferences` (0.1) — after this, lambdas are no longer `IrFunctionExpression` with implicit captures; the lift MUST run before it.
  - L172–174: `BrsIOWorkerExtractionLowering` gated on `!context.isStdlibCompilation`.
- Coroutine lowerings are last (L343–378): `BrsSuspendFunctionsLoweringWrapper` (user code only, gated on `context.brsSymbols.coroutineSymbols.areCoroutineSymbolsAvailable`), then the continuation-parameter lowerings 16.5.2–16.5.4. **Continuation params change mangled names AFTER phase 0.05x** — nothing at rewrite time may bake in a suspend function's final BRS name (see §7 gotcha).
- Execution: `for (phase in phases) for (file in module.files) phase.lower(file)` (L380–384) — every pass is a `FileLoweringPass`.
- **Why `BrsSharedDispatchLowering` is NOT a pipeline pass** — header at `lower/BrsSharedDispatchLowering.kt` L47–88: (a) its static targets have no IR symbols (receiver param injected at EMISSION in `IrToBrsTransformer.transformFunction`); (b) suspend mangles (`_ContinuationI_k_`) only exist after phase 16.5.x; (c) `external` stubs would skip `recordFunctionDependency`. Its pieces live where the names live: registry built in `BrsCompiler.compile` L202 (`context.sharedDispatchRegistry = BrsSharedDispatchRegistry.build(loweredModule, context)`, after the manifest pass so name uniquification order is untouched), call-site classification in `IrExpressionToBrsTransformer`, dispatcher emission next to the declaring impl. Relevant precedent if any Flow piece needs post-continuation-mangle names.

## 4. Stdlib-symbol recognition (what the plan extends)

**`core/compiler.common.brightscript/src/org/jetbrains/kotlin/name/BrsStandardClassIds.kt`** — FIR-and-backend-shared IDs:
- Packages L14–18: `BASE_BRS_PACKAGE`(kotlin.brs), `BASE_BRS_INTERNAL_PACKAGE`, `BASE_BRS_ROKU_PACKAGE`, `BASE_BRS_COROUTINES_PACKAGE`(kotlin.brs.coroutines), `BASE_COROUTINES_TASK_PACKAGE`(kotlin.coroutines.task).
- `object Components` (TaskComponent L388), `object Scope` L408, `object Shared` L429.
- `object Callables` L443: `runTask` L509 (`"runTask".callableId(BASE_COROUTINES_TASK_PACKAGE)`), `componentFactoryCallables` L516 (set consumed by `FirBrsCreateComponentTypeChecker`), `scopeHandleRun` L527 (`CallableId(Scope.scopeHandle, Name.identifier("run"))` — the member-callable pattern), `scopeRunLowered` L534, `setFieldCallables` L552 / `callFuncCallables` L562 / `asDynamic`/`unsafeCast` L570/573 (copy-channel checker plumbing), `sharedFrom` L586.
- Helpers L632–640: `"name".brsId()` / `.brsInternalId()` / `.brsRokuId()` / `.brsCoroutinesId()` / `.callableId(pkg)`. **New Flow ids go here** (e.g. a `Flow` ClassId, `flowOn`/`spawnTask` CallableIds, a `Dispatchers.Task` CallableId).

**`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsSymbols.kt`** — lazy, missing-tolerant symbol lookups. The pattern to copy (all `by lazy` with `findOptionalFunction`/`findOptionalClass`, L53–58):
```kotlin
val runTaskImplOrNull: IrSimpleFunctionSymbol? by lazy { findOptionalFunction(BrsStandardClassIds.BASE_COROUTINES_TASK_PACKAGE, "runTaskImpl") }  // L317
val brsCreateComponentOrNull: IrSimpleFunctionSymbol? by lazy { findOptionalFunction(BrsStandardClassIds.BASE_BRS_PACKAGE, "brsCreateComponent") } // L325
val sharedAcquireOrNull: ... // L336;  scopeRunLoweredOrNull: L346;  iAssociativeArrayClass: L354;  roAssociativeArrayClass: L447
```
**`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsIntrinsics.kt`** — class predicates:
```kotlin
fun isSharedServiceClass(irClass: IrClass): Boolean          // L141 (supertype walk to Shared.sharedService)
fun sharedServiceClassOrNull(type: IrType): IrClass?         // L161
fun isSceneGraphComponent(irClass: IrClass): Boolean         // L238
fun isTaskComponent(irClass: IrClass): Boolean               // L284
fun isNativeIterable(type: IrType): Boolean                  // L369
fun returnsNativeArrayIterator(function: IrSimpleFunction): Boolean  // L384
```
`taskComponentClass: IrClassSymbol?` L211. Predicate roots are the model for `isFlowType`/`isStateFlowClass` if needed backend-side.

**Dispatchers actual location** (matters for the `Dispatchers.Task` token): `libraries/stdlib/brs/src/kotlin/coroutines/dispatchers/Dispatchers.kt` — `package kotlin.coroutines.dispatchers` (L6), `public object Dispatchers` L37, `val Default` L51, `val Main` L65, `val IO` L95. The spec (§3) says "kotlin.coroutines" — the REAL package is `kotlin.coroutines.dispatchers`; `FirBrsIODispatcherChecker` already pins `ClassId(FqName("kotlin.coroutines.dispatchers"), "Dispatchers")` (L40–42). `Dispatchers.Task` will live on this object; the new FIR checker must use the same ClassId.

**Stdlib runtime signatures the lifts target** (`libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`):
```kotlin
public suspend inline fun <reified T : TaskComponent> runTask(noinline configure: T.() -> Unit): T  // L306
@PublishedApi internal suspend fun <T : TaskComponent> runTaskImpl(task: T, configure: T.() -> Unit): T  // L273
public suspend fun <T : TaskComponent> T.awaitCompletion(): T  // L204 (arm observer BEFORE control=RUN, L273-280)
```
ScopeApi lowered-entry pattern (`libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt`):
```kotlin
internal suspend fun <R> ScopeHandle.runLowered(name: String, captures: RoAssociativeArray?): R  // L280
```
A `flowOnImpl`/`spawnTaskImpl` should follow this `internal` + rewrite-target shape (keep signature in sync with the lowering — the runTask KDoc convention, BrsRunTaskCallLowering.kt L33–34).

## 5. The task-component emission machinery the synthesized components ride

**`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrToBrsTransformer.kt`**:
- `fun transformFile(irFile: IrFile): BrsProgram` L95; sets `genCtx.currentFileUsesCoroutines = fileUsesCoroutines(irFile)` L112 and `genCtx.currentFileCallsExposeScope = fileCallsExposeScope(irFile)` L116.
- `private fun fileUsesCoroutines(irFile)` L242–285 (matches `kotlin.coroutines.*` fqNames, CoroutineImpl supertypes, `kotlin.brs.launch`/`componentScope`; KNOWN HOLE: helper-file indirection, KDoc L237–240). `fileCallsExposeScope` L301–318 (same hole, KDoc L291–299).
- `private fun isConcreteTaskComponent(irClass)` L830–833 (`intrinsics.isTaskComponent` + not ABSTRACT).
- `private fun findLocalTaskRunImplementation(irClass): IrSimpleFunction?` L848–855 — real non-abstract `run()` with no params declared in THIS class.
- `private fun generateTaskMainFunction(irClass): BrsSub?` L887–956 — emits `sub __kotlinTaskMain()` wrapping `<Class>_run_k_()` in try/catch; error AA written before state; `m.top.kotlinTaskState` LAST; calls `context.recordFunctionDependency(runName)` L894. Device-proven shape documented L862–885. **A per-site flowOn task component that subclasses TaskComponent with a real `run()` gets all of this for free.**
- `private fun transformComponentInitBlock(irClass, constructor, layoutInfo)` L967+: task components get `m.top.functionName = "__kotlinTaskMain"` first (L977–987); `__kotlinPumpAttach(m.top, m.global)` for coroutine-using files, **task components excluded** (L996–1009); scope binding-table injection L1020–1041 (registers an EMPTY `BrsAALiteral` in `context.pendingScopeBindingTables`, filled later — the deferred-fill pattern if Flow ever needs an init-time table; per-site synthesis deliberately avoids needing one).
- Constants: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsComponentInfo.kt` L18 `KOTLIN_TASK_MAIN_FUNCTION_NAME = "__kotlinTaskMain"`, L25 `KOTLIN_TASK_STATE_FIELD = "kotlinTaskState"`, L26 `KOTLIN_TASK_ERROR_FIELD = "kotlinTaskError"`, L34 `KOTLIN_SCOPE_BINDINGS_FIELD`.
- createComponent/brsCreateComponent codegen intrinsic: `transformers/irToBrs/IrExpressionToBrsTransformer.kt` ~L803–830 — matches both names, emits `CreateObject("roSGNode", "<Name>")`, reports the IR-level backstop error for non-concrete classes.

**Component extraction** — `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsComponentExtractor.kt`:
```kotlin
fun extractComponent(irClass: IrClass): BrsComponentInfo?   // L52 (null if not a component)
fun isComponent(irClass: IrClass): Boolean                  // L105 (@BrsComponent or SG base in hierarchy)
```
`BrsComponentInfo` (`BrsComponentInfo.kt` L137–173): `irClass, name, extendsComponent, fields: List<BrsFieldInfo>, exports, additionalScripts, layout`. `BrsFieldInfo` L178–224: `name, type, defaultValue, onChange, alwaysNotify, alias, nodeType, irProperty, irField` — a synthesized component's capture/output fields can be described by constructing `BrsComponentInfo`/`BrsFieldInfo` directly instead of via @SG annotations.

## 6. BrsCompiler passes — where synthesized components must join

`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsCompiler.kt`, `fun compile(irModule: IrModuleFragment): BrsModuleCompilationResult` L135:

1. **Pre-extraction BEFORE lowering** L153–164: `preExtractedComponents[name] = componentExtractor.extractComponent(irClass)` for every top-level IrClass — done pre-lowering only to preserve the @SGLayout DSL.
2. `BrsLoweringPhases.lower(irModule, context)` L167 — synthesized flowOn/spawnTask components are born here, so they are **absent from `preExtractedComponents`**.
3. **Pass 1** L180–196: `collectFunctionManifest(file, outputFileName, context, functionManifest)` per file (output name = `File(file.path).nameWithoutExtension + "Kt.brs"`, L185); `context.functionManifest.putAll(...)` L196. Dispatch registry L202.
4. **Pass 2** L204–220: per file `context.currentSourceFile = outputFileName`; `compileFile(file, transformer, context)` (L475–505) = `transformer.transformFile` → `populateScopeBindingTables(irFile, context)` (L533–570; the SUBSET-closure KDoc L507–532 — entry names resolved from CURRENT declarations because the coroutine pipeline changed the mangle, L546–562) → `program.render()`.
5. **Pass 3 (the dedicated pass)** L222–238: per file `generateComponentOutputForFile(file, preExtractedComponents, context)` (L1839–1865) — finds component classes in the LOWERED file by `context.getBrsName(it) in preExtractedComponents` (L1848–1850), computes `dependencies = computeComponentDependencies(outputFileName, context)` ONCE per file (L1855; fn at L1875–1893: `context.fileDependencies[outputFileName]` + merged `dependencyFileDeps` klib graph → `resolveTransitiveDependencies` L1938–1958), then `generateComponentXmlContent(componentInfo, context, dependencies)` (L1967+) + `generateComponentDepsJson(name, dependencies)` (L1909–1927, `{"version":1,"component":...,"dependencies":[...]}` sorted).

**Dependency recording** — `BrsIrBackendContext.kt`: `fileDependencies` L86, `currentSourceFile` L93, `functionManifest` L101, `fun recordFunctionDependency(functionName: String)` L144–180 (manifest lookup, native-builtin allowlist L116–129, warn-once on gaps, self-dep filtered L177). Also on context: `scopeRunBlocks` L333, `pendingScopeBindingTables` L343, `sharedDispatchRegistry` L355, `fun getBrsName(irClass): String` L429 / `(irFunction)` L438 (uniquifying caches L268/L279), `componentClasses` L284.

**How synthesized task components join Pass 3**: they exist only post-lowering, so the plan must bridge — either (a) after `BrsLoweringPhases.lower` returns (BrsCompiler L167), run `componentExtractor.extractComponent` over synthesized IrClasses (safe: no @SGLayout DSL to break, which was the only reason for pre-lowering extraction) and merge into `preExtractedComponents`, or (b) have the lowering register ready-made `BrsComponentInfo`s on the context (a `synthesizedComponents` map, `scopeRunBlocks` precedent) merged before the Pass 3 loop. Either way, appending the synthesized IrClass to the ORIGINATING `irFile.declarations` (ScopeRunBlock precedent L177–180) makes name-matching in `generateComponentOutputForFile` (L1848) and manifest/dep recording work unchanged, and gives the component the originating file's include closure — per-site self-containment for free.

## 7. FIR checker infrastructure

**Diagnostics list (regen input):** `compiler/fir/checkers/checkers-component-generator/src/org/jetbrains/kotlin/fir/checkers/generator/diagnostics/FirBrsDiagnosticsList.kt` — `object BRS_DIAGNOSTICS_LIST : DiagnosticList("FirBrsErrors")` (L15). Groups are `val NAME by object : DiagnosticGroup("...")` blocks; entries `val BRS_X by error<KtElement>([PositioningStrategy.DECLARATION_NAME]) { parameter<String>("p"); isSuppressible = true }` or `by warning<KtElement>`. ScopeHandle group L134–153 is the exact shape for the new `FLOW`/`TASK_LIFT` group; `BRS_IO_DISPATCHER_UNSUPPORTED` lives in the `IO_WORKER` group L76–78. Warnings (`BRS_SCOPE_CAPTURE_MUTATION_LOST` L143) omit `isSuppressible`.

**Regen** (manual; CLAUDE.md-documented): `./gradlew :compiler:fir:checkers:checkers.brs:generateCheckersComponents --no-configuration-cache` then `:compiler:fir:checkers:generateCheckersComponents --no-configuration-cache`. Generated outputs, both committed:
- `compiler/fir/checkers/checkers.brs/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrors.kt` (`KtDiagnosticFactory0/1/2` vals, e.g. `BRS_IO_DISPATCHER_UNSUPPORTED` L44)
- `compiler/fir/checkers/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/FirNonSuppressibleErrorNames.kt`

**Messages:** `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrorsDefaultMessages.kt` — `map.put(DIAG, "template", CommonRenderers.STRING, ...)`. **`BRS_IO_DISPATCHER_UNSUPPORTED` message at L98–103** ("...Use runTask<T> for background work.") — this is the string the plan edits to "use flowOn(Dispatchers.Task), spawnTask {}, or runTask<T>". Gotcha in-file (L166–167): parameterized messages go through MessageFormat — a bare `{` is a parse error that poisons the whole renderer map; literal quotes are `''`.

**Registration:** `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/brs/checkers/BrsExpressionCheckers.kt` — `object BrsExpressionCheckers : ExpressionCheckers()` with `functionCallCheckers` (L22–33), `propertyAccessExpressionCheckers` (L35–38, where `FirBrsIODispatcherChecker` sits — and where a `Dispatchers.Task`-position checker goes), `tryExpressionCheckers`. Declaration checkers: sibling `BrsDeclarationCheckers.kt`. Wired via `fun FirSessionConfigurator.registerBrsCheckers()` in `compiler/fir/entrypoint/src/org/jetbrains/kotlin/fir/checkers/CheckersContainers.kt` L68–71 (`useCheckers(...)` + `registerDiagnosticContainers(FirBrsErrors)`), invoked from `compiler/fir/entrypoint/src/org/jetbrains/kotlin/fir/session/FirBrsSessionFactory.kt` L78. New checker objects only need adding to the container object — no session plumbing.

**Checker templates end-to-end:**
- Call-shape gate: `checkers.brs/src/.../expression/FirBrsScopeBlockChecker.kt` — `object ... : FirFunctionCallChecker(MppCheckerKind.Common)`, `context(context: CheckerContext, reporter: DiagnosticReporter) override fun check(expression: FirFunctionCall)`; resolves callee via `expression.calleeReference.toResolvedCallableSymbol()`, matches `callee.callableId == BrsStandardClassIds.Callables.scopeHandleRun` (L43), unwraps `FirWrappedArgumentExpression`, literal-lambda test = `argument is FirAnonymousFunctionExpression` (L48–52). Template for `BRS_FLOW_UPSTREAM_NOT_LITERAL` / `BRS_FLOW_ON_INVALID_DISPATCHER` (arg-position half).
- Capture walk: `expression/FirBrsScopeCaptureChecker.kt` (L72–195) — full free-value analysis over `FirAnonymousFunction` with `FirVisitorVoid`: `declared` set fed by `visitProperty`/`visitValueParameter`/`visitReceiverParameter`; captures found via `visitPropertyAccessExpression` (FirValueParameterSymbol, or FirPropertySymbol where `symbol.isLocal`) and `visitThisReceiverExpression` (implicit `this` — `calleeReference.boundSymbol`, L153–162); mutation via `visitVariableAssignment` + `unwrapLValue()` (L180–190); one report per symbol, per-write for mutations. **The direct template for `BRS_TASK_CAPTURE_UNMARSHALLABLE` (incl. implicit this) and `BRS_TASK_CAPTURE_MUTATION_LOST`; extend the same walk with a suspend-call visitor for `BRS_TASK_SUSPEND_IN_LIFTED`.**
- Property-access token gate: `expression/FirBrsIODispatcherChecker.kt` — `FirPropertyAccessExpressionChecker(MppCheckerKind.Common)`, matches `CallableId(ClassId(FqName("kotlin.coroutines.dispatchers"), "Dispatchers"), "IO")` (L40–42); identity-based (aliases flagged at aliasing site only, KDoc L34–36). Template for the reversed half of `BRS_FLOW_ON_INVALID_DISPATCHER` ("Task anywhere except a flowOn arg") — that direction needs enclosing-context awareness (`context.callsOrAssignments` / containing elements on `CheckerContext`).
- Marshallability oracle: `checkers.brs/src/.../BrsScopeMarshallability.kt` — `internal object BrsScopeMarshallability` L47, `fun isMarshallable(type: ConeKotlinType, session: FirSession): Boolean` L49, `fun render(type: ConeKotlinType): String` L58. Reuse for `BRS_TASK_EMIT_NOT_MARSHALLABLE` and the capture rule (the spec says same set).

**Fixture tests:**
- Harness: `compiler/fir/checkers/checkers.brs/test/org/jetbrains/kotlin/fir/analysis/brs/AbstractBrsDiagnosticTest.kt` — fixtures at `compiler/testData/diagnostics/testsWithBrsStdLib/` (L41), `<!DIAG_NAME!>expr<!>` markers (L43), multi-file via `// FILE: name.kt` (L78–90), compiles stripped source with `K2BrsCompiler` against `libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib`, matches by NAME resolved through the **`diagnosticRenderedMessages` inverse map (L192–272)** — **every new diagnostic MUST also add its rendered template there (single `'`, not `''`), or verification reports `<unknown>`**. Warnings are verified only when they resolve to a known template (L311–313). IR-backstop noise is filtered via `ignoredMessagePatterns` L278–300 (add a pattern if the Flow lowering emits `[IR] `-prefixed backstops).
- Test methods: `BrsDiagnosticTests.kt` — plain `@Test fun x() { runTest("subdir/fixture.kt") }`. Fixture style example: `compiler/testData/diagnostics/testsWithBrsStdLib/scopeCaptureUnmarshallable.kt` (top-of-file rationale comment, positive CLEAN cases + marked negative cases). Gate: 227 tests.

## 8. Golden tests

- Harness: `compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/AbstractBrsGoldenFileTest.kt` — `TEST_DATA_ROOT = compiler/testData/codegen/brs` (L39); `protected fun runTest(testPath: String)` L49 (input `x.kt`, golden sibling `x.brs.txt`); `protected fun runMultiFileTest(testDirPath: String)` L72 (all `.kt` in dir, **sorted by name** — pins order-sensitive behavior; golden is sibling `<dir>.brs.txt`). Output capture includes **`.brs`, `.xml`, AND `.deps.json`** (L174–177) — synthesized component XML + deps land in goldens automatically. Update: `-PupdateGoldenFiles=true` (system prop `kotlin.test.update.golden.files`, L41), or `./run-compiler-tests.sh --update`.
- Tests: `test/.../BrsGoldenFileTests.kt` — one-liner `@Test fun name() = runTest("scopehandle/runBlockLowering.kt")`. Existing precedents to mirror: `runBlockLowering` L281, `bindingTableInjection` L284, `sharedFromLowering` L292, multi-file `projectHelperTransitiveDeps() = runMultiFileTest("components/projectHelperTransitiveDeps")` L203. testData dirs: `components/ coroutines/ scopehandle/ shared/ ...` — a `flow/` dir is the natural home.
- Gradle: testData is a tracked input — `compiler/ir/backend.brightscript/build.gradle.kts` L44 `inputs.dir(rootDir.resolve("compiler/testData/codegen/brs"))`.
- Gate: 79 executed goldens (raw `grep -c "@Test"` = 80; one commented-out brsName golden).

## GOTCHAS (things that would trip a plan written without them)

1. **Pre-extraction runs BEFORE lowering** (BrsCompiler.kt L153–167): a task component synthesized by a lowering is invisible to `preExtractedComponents` and gets NO XML/deps.json unless the plan adds a post-lowering extraction/merge step (see §6). `generateComponentOutputForFile` matches by `getBrsName(irClass) in preExtractedComponents` — both the IrClass in the lowered file AND the map entry are required.
2. **Never bake a suspend function's BRS name at rewrite time**: continuation lowerings (phase 16.5.x) change the mangle (`_ContinuationI_k_`). `populateScopeBindingTables` (BrsCompiler.kt L546–562) re-resolves names from CURRENT declarations by unique Kotlin name; `BrsSharedDispatchLowering`'s header explains why an early IrCall retarget to synthesized stubs is impossible. The synthesized `run()` is NON-suspend (spec law) — that dodges the mangle problem for the task side, but any suspend rewrite target must be a real stdlib symbol (`runLowered`/`runTaskImpl` pattern).
3. **Lift passes must run BEFORE `UpgradeCallableReferences`** (BrsLoweringPhases.kt L182) — after it, lambdas are `IrRichFunctionReference`, captures explicit, `IrFunctionExpression` gone; the whole capture-collection approach in §1 stops working.
4. **klib inline functions never inline at user call sites** (`BrsInlineFunctionResolver`) — every reified/lifting entry point (`flowOn` receiver chain, `spawnTask`) needs a call-site rewrite pass; the compiled stdlib wrapper body with unsubstituted `T` must be left alone (the `classOrNull == null → return expression` guard, BrsRunTaskCallLowering L57).
5. **Symbol lookups must be `OrNull` + bail** (`?: return` at the top of `lower()`): the stdlib itself is compiled by the same backend, and during stdlib compilation the symbols being defined may not resolve. Also gate user-only passes on `!context.isStdlibCompilation` if they must not touch stdlib code (L172 precedent).
6. **`recordFunctionDependency` is emission-time and manifest-keyed** (BrsIrBackendContext L144): anything emitted as a raw `BrsFunctionCall` outside the normal transformer path must record its dependency itself (generateTaskMainFunction L894 precedent), or the include closure silently misses the file (warning only; KGP `validateComponentIncludes` is the hard gate, strict-0).
7. **The `Dispatchers` object's real package is `kotlin.coroutines.dispatchers`**, not `kotlin.coroutines` as the spec §3 abbreviates — reuse `FirBrsIODispatcherChecker`'s ClassId (L40–42) for the `Task` token checker, and put `Task` on that object (stdlib internal sites suppress `BRS_IO_DISPATCHER_UNSUPPORTED` today; a `Task` val needs no suppression since the new rule targets non-flowOn POSITIONS).
8. **New diagnostics require FOUR files**: FirBrsDiagnosticsList.kt entry + both regenerated containers (committed) + FirBrsErrorsDefaultMessages.kt message — **plus the test harness's `diagnosticRenderedMessages` map in AbstractBrsDiagnosticTest.kt (L192)**, which is easy to forget and fails with `<unknown>` mismatches. Rebuild step 7 (kotlin-test-brs compile check) will catch a new checker firing on real kotlin.test source — fix with declaration-site `@Suppress`.
9. **Message templates go through MessageFormat**: bare `{`/`}` in a parameterized message poisons the entire renderer map (FirBrsErrorsDefaultMessages L166–167); code snippets in messages ("spawnTask {}") need care — the current `BRS_SCOPE_CAPTURE_MUTATION_LOST` deliberately avoids braces. Non-parameterized messages (like BRS_IO_DISPATCHER_UNSUPPORTED today) don't hit MessageFormat, but adding a `{0}` param changes that.
10. **`FileLoweringPass` ordinals and file-scan gates are per-FILE**: `fileUsesCoroutines`/`fileCallsExposeScope` KNOWN HOLE (helper-file indirection) — per-site flowOn synthesis avoids the binding-table subset trap by design, but the pump-attach for the COLLECTOR side still rides `fileUsesCoroutines` on the collector's file (a `flowOn` collect call names `kotlin.coroutines.*` symbols, so the scan catches it — worth a golden pin, `componentNoCoroutinesNoPumpAttach` precedent).
11. **Task components are excluded from pump attach** (IrToBrsTransformer L996) and their init gets `m.top.functionName = "__kotlinTaskMain"` first (L977) — synthesized components extending TaskComponent inherit both behaviors only if `isConcreteTaskComponent` is true (concrete, non-abstract, `isTaskComponent` supertype walk).
12. **Multi-file goldens compile in sorted-name order** (AbstractBrsGoldenFileTest L77–78) — name fixture files deliberately when pinning order-dependent behavior (e.g. a flowOn call site in a file that transforms after its helper).
13. `BrsIOWorkerExtractionLowering` (quarantined pipeline) still runs for user code (BrsLoweringPhases L172–174) — if the plan retires `withContext(IO)` messaging, that pass and `FirBrsIOWorkerCaptureChecker` are adjacent but separate; only the `BRS_IO_DISPATCHER_UNSUPPORTED` message changes per spec decision 3.