# Component Constructor Inputs + Typed Layout Builders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `class Screen(@SGStringField val airingId: String) : GroupComponent()` a real, checked contract: constructor parameters are required inputs written by a lowered constructor call (`Screen("123")`) or by constants in a typed layout builder (`screen(id = "s", airingId = "123")`), readable from `onStart()` onward, with the ready marker, the init-read FIR rule, and the static-layout validation — the constructor-inputs half of spec 1. Requires plan A (`2026-09-04-component-lifecycle-core.md`) to be shipped: it consumes `awaitReady`'s inputs gate, `LIFECYCLE_INPUTS_READY_FIELD`, and Suite 11's fixtures.

**Architecture:** The extractor already emits the XML field for a constructor-parameter `@SG` property; this plan (1) stops init from materializing the parameter as an undefined identifier, (2) lowers component constructor calls to create + field writes + marker, (3) emits the marker field and validates static layouts against each target type's required inputs, (4) adds three FIR diagnostics, and (5) has the Gradle plugin generate one typed builder per component. Everything stays single-module closed-world (SharedService/ScopeHandle precedent).

**Tech Stack:** Kotlin K2 compiler fork (FIR checkers, IR lowering, IrToBrs transformer, extractor), BrightScript stdlib, kotlin-roku Gradle plugin (source generation), roku-test-app E2E on the Roku Ultra.

**Spec:** `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` — §1 decisions 3–5, §3 "Constructor inputs" and "Typed layout builders", §4 "Inputs readiness", §5.7–§5.10, §6, §7, §9 (goldens, FIR fixtures, Suite 11 tests 3/6/7/9), §10.

## Global Constraints

- Same build, device, prefix, and gate rules as plan A (`./rebuild.sh` only; `e2e:`/`plugin:` prefixes; a SEPARATE `brs-prebuilt:` commit for every stdlib source change; Co-Authored-By trailer; gates never drop; push only on Mike's word). Plan A's final gates are the baseline: goldens 104, FIR 232, stdlib 614/63, E2E 115/11.
- FIR diagnostics require the manual generator step after editing `FirBrsDiagnosticsList.kt`: `./gradlew :compiler:fir:checkers:checkers.brs:generateCheckersComponents --no-configuration-cache` then `./gradlew :compiler:fir:checkers:generateCheckersComponents --no-configuration-cache`; commit both generated files; add default messages AND the harness inverse-template entries (rendered form, single `'`).
- Mirror law: the FIR component predicate (`BrsComponentTypes`) and the backend predicates (`BrsIntrinsics.isSceneGraphComponent`, `BrsComponentExtractor.isComponent`) carry cross-referencing comments; a change lands in all in one commit.
- Task components take NO constructor inputs (FIR error); the zero-argument constructor call is lowered for every concrete component kind.
- Static layouts: required inputs must be compile-time constants; a missing or non-constant required input is a backend ERROR with a location, never the existing warning.
- Builders are generated only for components whose required inputs are all `String`/`Int`/`Float`/`Double`/`Boolean` (node- and AA-typed inputs cannot be XML constants: construct those in code).

---

## File structure

| File | Responsibility |
|---|---|
| `libraries/stdlib/brs/src/kotlin/brs/scenegraph/NodeEntry.kt` (modify) | `@SGComponentBuilder(componentType)` annotation |
| `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` (modify) | `kotlinLifecycleMarkInputsReady(node)` |
| `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` (modify) | `Annotations.SGComponentBuilder` |
| `compiler/.../brs/BrsComponentInfo.kt` (modify) | `requiredInputs` on `BrsComponentInfo`; `LayoutInputValidation` pure functions |
| `compiler/.../brs/BrsComponentExtractor.kt` (modify) | constructor-parameter detection, marker field, builder-call extraction |
| `compiler/.../brs/BrsCompiler.kt` (modify) | post-extraction layout validation + marker attribute |
| `compiler/.../brs/BrsIrBackendContext.kt` (modify) | `reportError(element, msg)` reuse (exists) |
| `compiler/.../irToBrs/IrToBrsTransformer.kt` (modify) | skip constructor-parameter initializers in init |
| `compiler/.../brs/lower/BrsComponentConstructorCallLowering.kt` (new) + `BrsLoweringPhases.kt` | the creation lowering (phase 0.0555) |
| `compiler/.../brs/BrsSymbols.kt` (modify) | `kotlinLifecycleMarkInputsReadyOrNull`, `roSGNodeClassOrNull` |
| `compiler/fir/checkers/checkers.brs/src/.../BrsComponentTypes.kt` (new) | shared FIR component predicate |
| `compiler/fir/checkers/checkers.brs/src/.../expression/FirBrsComponentInputEarlyReadChecker.kt` (new) | `BRS_COMPONENT_INPUT_READ_IN_INIT` |
| `compiler/fir/checkers/checkers.brs/src/.../expression/FirBrsCreateComponentTypeChecker.kt` (modify) | `BRS_CREATE_COMPONENT_HAS_INPUTS` |
| `compiler/fir/checkers/checkers.brs/src/.../declaration/FirBrsTaskConstructorInputChecker.kt` (new), `FirBrsTaskStateNotFieldChecker.kt` (modify) | `BRS_TASK_CONSTRUCTOR_INPUT`; skip removed |
| `compiler/fir/checkers/checkers-component-generator/.../FirBrsDiagnosticsList.kt`, gen files, `FirBrsErrorsDefaultMessages.kt`, `AbstractBrsDiagnosticTest.kt`, `BrsDiagnosticTests.kt`, `compiler/testData/diagnostics/testsWithBrsStdLib/componentInputs/*` | the FIR family |
| `compiler/testData/codegen/brs/components/{ctorInputs,ctorCallLowering,builderCallExtraction}.*` + `compiler/ir/backend.brightscript/test/.../LayoutInputValidationTest.kt` | goldens + a pure unit test |
| `../kotlin-roku/.../tasks/GenerateLayoutStubsTask.kt` (+ test) | builder generation + extended id patterns |
| `../roku-test-app/.../fixtures/LifecycleInputProbe.kt`, `LifecycleBuilderParentProbe.kt`; `tests/ComponentLifecycleTests.kt` | Suite 11 tests 3, 6, 7, 9-inputs |
| `CLAUDE.md`, `SceneComponent.kt` KDoc | docs |

---

### Task 1: Stdlib pieces — `@SGComponentBuilder` and the marker writer

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/brs/scenegraph/NodeEntry.kt` (after `@SGLayout`)
- Modify: `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt`
- Modify: `core/compiler.common.brightscript/src/org/jetbrains/kotlin/name/BrsStandardClassIds.kt` (`Annotations`)

**Interfaces:**
- Produces: `kotlin.brs.scenegraph.SGComponentBuilder(componentType: String)` annotation (Task 6 generates it, Task 3 keys on it); `@PublishedApi internal fun kotlinLifecycleMarkInputsReady(node: RoSGNode)` (Task 4's lowering calls it); `BrsStandardClassIds.Annotations.SGComponentBuilder`.

- [ ] **Step 1: Annotation**

In `NodeEntry.kt` after `SGLayout`:
```kotlin
/**
 * Marks a GENERATED typed layout builder (kotlin-roku `GenerateLayoutStubsTask`):
 * `fun LayoutBuilder.airingDetailsScreen(id, airingId, …)` declares a child of
 * component type [componentType] in a static layout. The compiler's layout
 * extractor keys on this annotation and reads the CALL SITE's arguments by
 * parameter name — `id`, the target's constructor inputs, then the standard
 * attributes `component()` accepts — so required inputs are ordinary required
 * Kotlin parameters and their values must be compile-time constants
 * (spec 2026-09-04-component-lifecycle §3/§5.9).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class SGComponentBuilder(val componentType: String)
```

- [ ] **Step 2: Marker writer**

In `ComponentLifecycle.kt` after `kotlinLifecycleClaimDriver`:
```kotlin
/**
 * Written LAST by the lowered constructor call of an input-bearing component
 * (after every input field write): proof of sanctioned construction. The gate
 * reads it; a node created from raw BrightScript or hand-written XML never gets
 * it, so onStart stays closed and the watchdog names that cause (spec §4).
 */
@PublishedApi
internal fun kotlinLifecycleMarkInputsReady(node: RoSGNode) {
    node.setField(LIFECYCLE_INPUTS_READY_FIELD, true)
}
```

- [ ] **Step 3: ClassId**

In `BrsStandardClassIds.Annotations`, next to the `SG*Field` ids:
```kotlin
        /** Generated typed layout builder marker (kotlin.brs.scenegraph.SGComponentBuilder). */
        @JvmField
        val SGComponentBuilder = ClassId(FqName("kotlin.brs.scenegraph"), Name.identifier("SGComponentBuilder"))
```
(mirror however `SGLayout`-adjacent ids in that file build a `kotlin.brs.scenegraph` ClassId; if none exist, this explicit form is fine.)

- [ ] **Step 4: Rebuild + commit**

Run: `./rebuild.sh` — success.
```bash
git add libraries/stdlib/brs/src core/compiler.common.brightscript
git commit -m "stdlib: @SGComponentBuilder annotation + kotlinLifecycleMarkInputsReady

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
git add libraries/stdlib/brs-prebuilt
git commit -m "brs-prebuilt: regenerate stdlib klib (SGComponentBuilder, marker writer)

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 2: Init emission — constructor-parameter properties get no initializer line (spec §5.8)

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsIntrinsics.kt` (helper)
- Modify: `IrToBrsTransformer.kt:1050-1091` (initializer loop)
- Create: `compiler/testData/codegen/brs/components/ctorInputs.kt` (+ `.brs.txt`); `@Test`

**Interfaces:**
- Produces: `BrsIntrinsics.isConstructorParameterProperty(property: IrProperty): Boolean` — true when the backing field's initializer is a read of a primary-constructor value parameter. Tasks 3 and 4 use it.

- [ ] **Step 1: Golden + test; capture the buggy output**

`compiler/testData/codegen/brs/components/ctorInputs.kt`:
```kotlin
// A constructor-parameter @SG property (spec 2026-09-04-component-lifecycle §3):
// the extractor emits the XML field as for any @SG property; init() must NOT
// emit `m.top.airingId = airingId` (an undefined identifier inside sub init()),
// and the type gains the boolean ready-marker field `__kotlinInputsReady`.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.SGIntegerField

class InputScreen(
    @SGStringField val airingId: String,
    @SGIntegerField val row: Int,
) : GroupComponent() {
    @SGStringField
    var status: String = "idle"

    override suspend fun onStart() {
        status = airingId + ":" + row
    }
}
```
Add `@Test fun ctorInputs() = runTest("components/ctorInputs.kt")`. `./run-compiler-tests.sh --update`; `grep -n "m.top.airingId = airingId\|m.top.row = row" compiler/testData/codegen/brs/components/ctorInputs.brs.txt` → the two bad lines are present (the defect).

- [ ] **Step 2: Helper**

In `BrsIntrinsics`:
```kotlin
    /**
     * True for a property whose backing field is initialized from a PRIMARY
     * CONSTRUCTOR value parameter — the `class X(@SGStringField val a: String)`
     * shape. SceneGraph components are runtime-instantiated, so such a property
     * is a REQUIRED INPUT: its value arrives through the lowered constructor
     * call's field write (or a static-layout constant), never through init().
     */
    fun isConstructorParameterProperty(property: IrProperty): Boolean {
        val init = property.backingField?.initializer?.expression as? IrGetValue ?: return false
        val owner = init.symbol.owner as? IrValueParameter ?: return false
        val constructor = owner.parent as? IrConstructor ?: return false
        return constructor.isPrimary
    }
```
Imports: `org.jetbrains.kotlin.ir.expressions.IrGetValue`, `org.jetbrains.kotlin.ir.declarations.IrValueParameter`, `org.jetbrains.kotlin.ir.declarations.IrConstructor`.

- [ ] **Step 3: Skip in the initializer loop**

In `transformComponentInitBlock`'s property loop, after the layout-class skip:
```kotlin
            // Constructor-parameter inputs (spec §5.8): the value is written onto the
            // node by the lowered constructor call (or a layout constant) — there is no
            // local `airingId` inside sub init() to read.
            if (context.intrinsics.isConstructorParameterProperty(property)) continue
```

- [ ] **Step 4: Regenerate + verify + commit**

`./run-compiler-tests.sh --update`; grep again → no `m.top.airingId = airingId` line; the XML still declares `<field id="airingId" type="string" />` and `<field id="row" type="integer" />` (the marker field arrives in Task 3, so re-update the golden then). `./run-compiler-tests.sh` — 105 pass.
```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs/components/ctorInputs.*
git commit -m "brs: constructor-parameter @SG inputs emit no init() initializer line

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 3: Extractor — required inputs, marker field, builder-call extraction, layout validation (spec §5.9)

**Files:**
- Modify: `BrsComponentInfo.kt` (`requiredInputs: List<String> = emptyList()` on `BrsComponentInfo`; new `LayoutInputValidation` object)
- Modify: `BrsComponentExtractor.kt` (`extractFields`, `extractComponent`, `extractNodeFromStatement`, new `extractBuilderComponentNode`)
- Modify: `BrsCompiler.kt` (after the pre-extraction loop)
- Create: `compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/LayoutInputValidationTest.kt`
- Create: `compiler/testData/codegen/brs/components/builderCallExtraction.kt` (+ `.brs.txt`); `@Test`; re-update `ctorInputs.brs.txt`

**Interfaces:**
- Produces: `BrsComponentInfo.requiredInputs`; `LayoutInputValidation.validate(nodes, requiredByType): List<LayoutInputFinding>` and `LayoutInputValidation.withInputMarkers(nodes, requiredByType): List<NodeEntryInfo>`; `NodeEntryInfo.attributes` carries `__kotlinInputsReady="true"` for satisfied input-bearing children.

- [ ] **Step 1: Pure validation functions + their unit test FIRST**

In `BrsComponentInfo.kt`:
```kotlin
/** One static-layout finding: [ownerComponent]'s layout declares [childType] at [childId] without constant [missingInput]. */
data class LayoutInputFinding(val ownerComponent: String, val childId: String, val childType: String, val missingInput: String) {
    fun message(): String =
        "[BRS layout] component '$childType' (id=\"$childId\") declared in $ownerComponent's layout requires input " +
            "'$missingInput' as a compile-time constant — supply it in the builder call (or attr(\"$missingInput\", …)), " +
            "or construct $childType in code"
}

/**
 * Static-layout input validation (spec §5.9c). Pure over the extracted model so
 * it is unit-testable without IR: [requiredByType] maps a module component's
 * name to its constructor-input names; a child whose type is not in the map
 * (a SceneGraph built-in or a dependency-klib component) is never checked.
 */
object LayoutInputValidation {
    const val READY_MARKER_ATTRIBUTE = "__kotlinInputsReady"

    fun validate(owner: String, nodes: List<NodeEntryInfo>, requiredByType: Map<String, List<String>>): List<LayoutInputFinding> {
        val findings = mutableListOf<LayoutInputFinding>()
        for (node in nodes) {
            val required = requiredByType[node.nodeType]
            if (required != null) {
                for (input in required) {
                    if (!node.attributes.containsKey(input)) {
                        findings.add(LayoutInputFinding(owner, node.id, node.nodeType, input))
                    }
                }
            }
            findings.addAll(validate(owner, node.children, requiredByType))
        }
        return findings
    }

    /** Adds `__kotlinInputsReady="true"` to every input-bearing child whose required inputs are all present. */
    fun withInputMarkers(nodes: List<NodeEntryInfo>, requiredByType: Map<String, List<String>>): List<NodeEntryInfo> =
        nodes.map { node ->
            val required = requiredByType[node.nodeType]
            val satisfied = required != null && required.isNotEmpty() && required.all { node.attributes.containsKey(it) }
            val attributes = if (satisfied) node.attributes + (READY_MARKER_ATTRIBUTE to "true") else node.attributes
            node.copy(attributes = attributes, children = withInputMarkers(node.children, requiredByType))
        }
}
```
And on `BrsComponentInfo` add `val requiredInputs: List<String> = emptyList()`.

`LayoutInputValidationTest.kt` (JUnit, same test source set as the golden tests):
```kotlin
package org.jetbrains.kotlin.ir.backend.brs.test

import org.jetbrains.kotlin.ir.backend.brs.LayoutInputValidation
import org.jetbrains.kotlin.ir.backend.brs.NodeEntryInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutInputValidationTest {
    private val required = mapOf("AiringDetailsScreen" to listOf("airingId"))

    @Test
    fun satisfiedChildGetsTheMarker() {
        val nodes = listOf(NodeEntryInfo("AiringDetailsScreen", "d", mapOf("airingId" to "123"), emptyList()))
        assertTrue(LayoutInputValidation.validate("Owner", nodes, required).isEmpty())
        val marked = LayoutInputValidation.withInputMarkers(nodes, required)
        assertEquals("true", marked[0].attributes["__kotlinInputsReady"])
    }

    @Test
    fun missingInputIsAFindingAndNoMarker() {
        val nodes = listOf(NodeEntryInfo("AiringDetailsScreen", "d", emptyMap(), emptyList()))
        val findings = LayoutInputValidation.validate("Owner", nodes, required)
        assertEquals(1, findings.size)
        assertEquals("airingId", findings[0].missingInput)
        assertTrue(findings[0].message().contains("construct AiringDetailsScreen in code"))
        assertTrue(LayoutInputValidation.withInputMarkers(nodes, required)[0].attributes.isEmpty())
    }

    @Test
    fun nestedChildrenAreCheckedAndUnknownTypesIgnored() {
        val inner = NodeEntryInfo("AiringDetailsScreen", "inner", emptyMap(), emptyList())
        val nodes = listOf(NodeEntryInfo("LayoutGroup", "g", emptyMap(), listOf(inner)), NodeEntryInfo("Label", "l", emptyMap(), emptyList()))
        val findings = LayoutInputValidation.validate("Owner", nodes, required)
        assertEquals(listOf("inner"), findings.map { it.childId })
    }
}
```
Run: `./gradlew :compiler:backend.brightscript:test --tests "*LayoutInputValidationTest*" --no-configuration-cache -Dorg.gradle.dependency.verification=off` → fails to compile until the object exists; then passes.

- [ ] **Step 2: Extractor — required inputs + marker field**

In `extractComponent`, after `val fields = extractFields(irClass)`:
```kotlin
        // Constructor-parameter @SG properties are REQUIRED INPUTS (spec §3): the type
        // also gains the boolean ready-marker field the lifecycle gate reads (§4).
        val requiredInputs = irClass.declarations.filterIsInstance<IrProperty>()
            .filter { !it.isFakeOverride && context.intrinsics.isConstructorParameterProperty(it) && extractTypeSafeField(it) != null }
            .map { it.name.asString() }
        val fieldsWithMarker = if (requiredInputs.isEmpty()) fields else fields + BrsFieldInfo(
            name = LayoutInputValidation.READY_MARKER_ATTRIBUTE,
            type = "boolean",
            defaultValue = "false",
            onChange = null,
            alwaysNotify = false,
        )
```
Use `fieldsWithMarker` where `fields` fed `mergeInterfaceFields`, and pass `requiredInputs = requiredInputs` into the `BrsComponentInfo(...)` construction. (Check `BrsFieldInfo`'s constructor for required params such as `nodeType`/`irProperty`; pass `null` for both.)

- [ ] **Step 3: Extractor — builder-call recognition**

In `extractNodeFromStatement`, before the `component` special case:
```kotlin
        // Generated typed builder (kotlin-roku): @SGComponentBuilder("Type") on the
        // callee. Extraction runs pre-lowering, so the builder BODY is never
        // inlined into view — read the CALL SITE by parameter name.
        val builderAnnotation = call.symbol.owner.getAnnotation(BrsStandardClassIds.Annotations.SGComponentBuilder.asSingleFqName())
        if (builderAnnotation != null) {
            val typeArg = builderAnnotation.getValueArgument(0) as? IrConst
            val componentType = typeArg?.value as? String
            if (componentType != null) return extractBuilderComponentNode(call, componentType)
        }
```
and the new method:
```kotlin
    /**
     * A typed-builder call: `id` → id; every other non-`init` parameter → an attribute
     * named after the parameter (a constructor input or a standard attribute — the
     * standard names go through mapParamToXmlAttribute, inputs pass verbatim).
     * Non-constant values are dropped with the existing warning; the post-extraction
     * validation upgrades a dropped REQUIRED input to an error.
     */
    private fun extractBuilderComponentNode(call: IrCall, componentType: String): NodeEntryInfo? {
        val function = call.symbol.owner
        var id: String? = null
        val attributes = mutableMapOf<String, String>()
        var children = emptyList<NodeEntryInfo>()
        for (i in 0 until call.valueArgumentsCount) {
            val param = function.valueParameters.getOrNull(i) ?: continue
            val paramName = param.name.asString()
            val arg = call.getValueArgument(i) ?: continue
            if (paramName == "init") {
                val lambdaBody = extractLambdaBody(arg)
                if (lambdaBody != null) {
                    val (attrs, childNodes) = extractComponentBuilderCalls(lambdaBody)
                    attributes.putAll(attrs)
                    children = childNodes
                }
                continue
            }
            val value = extractAttributeValue(arg, paramName)
            if (paramName == "id") {
                id = value
                continue
            }
            if (value != null) {
                attributes[mapParamToXmlAttribute(paramName)] = value
            } else if (resolvesToConst(arg) == null) {
                warnDroppedAttribute(paramName)
            }
        }
        val resolvedId = id ?: return null
        return NodeEntryInfo(nodeType = componentType, id = resolvedId, attributes = attributes, children = children)
    }
```
Verify `mapParamToXmlAttribute` returns an unknown name unchanged (add a `?: paramName` fallback if it is a strict table).

- [ ] **Step 4: Post-extraction validation in `BrsCompiler`**

Right after the pre-extraction loop (before `BrsLoweringPhases.lower`):
```kotlin
        // Static-layout inputs (spec §5.9c): every module component's required inputs
        // are known by name now; each owner's layout children of those types must carry
        // them as constants — ERROR otherwise — and satisfied children get the ready
        // marker attribute so the lifecycle gate opens at creation.
        val requiredByType = preExtractedComponents.mapValues { it.value.requiredInputs }.filterValues { it.isNotEmpty() }
        if (requiredByType.isNotEmpty()) {
            for ((name, info) in preExtractedComponents.toList()) {
                val layout = info.layout ?: continue
                for (finding in LayoutInputValidation.validate(name, layout.nodes, requiredByType)) {
                    context.reportError(info.irClass, finding.message())
                }
                val marked = layout.copy(nodes = LayoutInputValidation.withInputMarkers(layout.nodes, requiredByType))
                preExtractedComponents[name] = info.copy(layout = marked)
            }
        }
```

- [ ] **Step 5: Goldens**

`compiler/testData/codegen/brs/components/builderCallExtraction.kt` (two classes in one file; the builder is hand-written here exactly as the plugin would generate it):
```kotlin
// A typed layout builder call is extracted by parameter name (spec §5.9b): the
// child XML carries id, the constructor input as an attribute, the standard
// attribute, and — because every required input is a constant — the ready
// marker attribute __kotlinInputsReady="true".
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.scenegraph.ComponentBuilder
import kotlin.brs.scenegraph.LayoutBuilder
import kotlin.brs.scenegraph.SGComponentBuilder
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

class Badge(@SGStringField val label: String) : GroupComponent()

@SGComponentBuilder("Badge")
fun LayoutBuilder.badge(id: String, label: String, visible: Boolean? = null, init: ComponentBuilder.() -> Unit = {}) {
    component("Badge", id = id, visible = visible) {
        attr("label", label)
        init()
    }
}

class BadgeHost : GroupComponent() {
    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            badge(id = "hostBadge", label = "NEW", visible = true)
        }
    }
}
```
Add `@Test fun builderCallExtraction() = runTest("components/builderCallExtraction.kt")`. `./run-compiler-tests.sh --update`; verify in `builderCallExtraction.brs.txt`: `BadgeHost.xml` children contain `<Badge id="hostBadge" label="NEW" visible="true" __kotlinInputsReady="true"/>` (attribute order may differ) and `Badge.xml` declares `<field id="label" type="string" />` and `<field id="__kotlinInputsReady" type="boolean" value="false" />`. `ctorInputs.brs.txt` now also shows the marker field. `./run-compiler-tests.sh` — 106 pass; the JUnit validation test passes.

- [ ] **Step 6: Commit**

```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs
git commit -m "brs: required inputs + ready-marker field, typed-builder extraction, static-layout input validation

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 4: The constructor-call lowering (spec §5.7)

**Files:**
- Modify: `BrsSymbols.kt` (`kotlinLifecycleMarkInputsReadyOrNull`, `roSGNodeClassOrNull`)
- Create: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsComponentConstructorCallLowering.kt`
- Modify: `BrsLoweringPhases.kt` (register after `BrsRunTaskCallLowering`)
- Create: `compiler/testData/codegen/brs/components/ctorCallLowering.kt` (+ `.brs.txt`); `@Test`

**Interfaces:**
- Consumes: `brsCreateComponentOrNull` (exists), `kotlinLifecycleMarkInputsReady` (Task 1), `isConstructorParameterProperty` (Task 2).
- Produces: `Screen("123")` compiles to `CreateObject("roSGNode", "Screen")` + `n.airingId = "123"` + marker; `Screen()` (zero-arg, any concrete component kind) to bare `CreateObject`.

- [ ] **Step 1: Golden + test; capture the buggy output**

`compiler/testData/codegen/brs/components/ctorCallLowering.kt`:
```kotlin
// Component constructor calls are lowered (spec §5.7): create the node, write
// each constructor input onto it, write the ready marker LAST, yield the handle
// (typed as the class — the createComponent<T>() contract). Zero-arg calls on
// any concrete component kind are a bare CreateObject.
import kotlin.brs.GroupComponent
import kotlin.brs.ContentNodeComponent
import kotlin.brs.SGStringField
import kotlin.brs.SGIntegerField
import kotlin.brs.roku.RoSGNode

class DetailsScreen(
    @SGStringField val airingId: String,
    @SGIntegerField val row: Int,
) : GroupComponent()

class VideoItem : ContentNodeComponent()

class Navigator : GroupComponent() {
    fun open(id: String, row: Int): RoSGNode {
        val screen = DetailsScreen(id, row)
        val item = VideoItem()
        return screen.asDynamic()
    }
}
```
Add `@Test fun ctorCallLowering() = runTest("components/ctorCallLowering.kt")`. `--update` and confirm the defect: `DetailsScreen_create_Str_I_k_(id, row)` and `VideoItem_create_k_()` calls to functions that do not exist anywhere in the output.

- [ ] **Step 2: Symbols**

```kotlin
    /** kotlin.brs.kotlinLifecycleMarkInputsReady(node) — written last by the lowered component constructor call. */
    val kotlinLifecycleMarkInputsReadyOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinBrsFqn, "kotlinLifecycleMarkInputsReady")
    }

    /** kotlin.brs.roku.RoSGNode — the marker writer's parameter type. */
    val roSGNodeClassOrNull: IrClassSymbol? by lazy { findOptionalClass(FqName("kotlin.brs.roku"), "RoSGNode") }
```

- [ ] **Step 3: The lowering**

```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Component constructor calls → node creation (spec 2026-09-04-component-lifecycle §5.7).
 *
 * `DetailsScreen(id, row)` becomes
 *
 * ```
 * {
 *   val n = brsCreateComponent<DetailsScreen>()     // codegen intrinsic → CreateObject("roSGNode", "DetailsScreen")
 *   n.airingId = id                                   // IrSetField on the handle → `n.airingId = id`
 *   n.row = row
 *   kotlinLifecycleMarkInputsReady(n)                 // LAST — proof of sanctioned construction
 *   n
 * }
 * ```
 *
 * Constructor inputs are the primary-constructor properties (`isConstructorParameterProperty`);
 * each argument goes to the field of the property initialized from that parameter.
 * Components with no inputs lower to the bare create. Abstract classes are left alone
 * (FIR rejects them). Phase 0.0555 — alongside the runTask/sharedFrom call-site rewrites,
 * before any coroutine lowering. Today the same call compiles to `<Class>_create_…_k_`, a
 * function that is never emitted.
 */
class BrsComponentConstructorCallLowering(private val context: BrsIrBackendContext) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val brsCreateComponent = context.brsSymbols.brsCreateComponentOrNull ?: return
        val markReady = context.brsSymbols.kotlinLifecycleMarkInputsReadyOrNull ?: return
        val roSGNode = context.brsSymbols.roSGNodeClassOrNull ?: return

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)
                val constructor = expression.symbol.owner
                val irClass = constructor.parentAsClass
                if (!context.intrinsics.isSceneGraphComponent(irClass)) return expression
                if (irClass.modality == Modality.ABSTRACT) return expression
                if (!constructor.isPrimary) return expression

                val classType = irClass.defaultType
                val createCall = IrCallImpl(
                    expression.startOffset, expression.endOffset, classType, brsCreateComponent, typeArgumentsCount = 1,
                ).apply { putTypeArgument(0, classType) }

                val handle = buildVariable(
                    parent = null, startOffset = expression.startOffset, endOffset = expression.endOffset,
                    origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                    name = Name.identifier("__kotlinNewComponent"), type = classType,
                ).apply { initializer = createCall }
                fun handleRead() = IrGetValueImpl(expression.startOffset, expression.endOffset, classType, handle.symbol)

                val statements = mutableListOf<IrStatement>(handle)
                var wroteInput = false
                for ((index, parameter) in constructor.valueParameters.withIndex()) {
                    val property = irClass.declarations.filterIsInstance<IrProperty>().firstOrNull { p ->
                        val init = p.backingField?.initializer?.expression as? IrGetValue
                        init?.symbol == parameter.symbol
                    } ?: continue
                    val field = property.backingField ?: continue
                    val argument = expression.getValueArgument(index) ?: continue
                    statements.add(
                        IrSetFieldImpl(
                            expression.startOffset, expression.endOffset, field.symbol, handleRead(), argument,
                            context.irBuiltIns.unitType,
                        )
                    )
                    wroteInput = true
                }
                if (wroteInput) {
                    statements.add(
                        IrCallImpl(
                            expression.startOffset, expression.endOffset, context.irBuiltIns.unitType, markReady, typeArgumentsCount = 0,
                        ).apply {
                            putValueArgument(
                                0,
                                IrTypeOperatorCallImpl(
                                    expression.startOffset, expression.endOffset, roSGNode.defaultType,
                                    IrTypeOperator.IMPLICIT_CAST, roSGNode.defaultType, handleRead(),
                                )
                            )
                        }
                    )
                }
                statements.add(handleRead())
                return IrBlockImpl(expression.startOffset, expression.endOffset, classType, IrStatementOrigin.LAMBDA, statements)
            }
        })
    }
}
```
The temporary variable's parent is patched by the file transform (call `irFile.patchDeclarationParents()` after `transformChildrenVoid` if the backend requires parents on temporaries — `BrsScopeRunBlockLowering` uses `buildVariable` the same way; mirror its parent handling). If the backend's `IrSetField` emission for an `@SG` field on a non-self receiver needs the receiver typed as the component class (E:1497-1507 route), the `handleRead()` type above is exactly that.

- [ ] **Step 4: Register**

In `BrsLoweringPhases.kt` after `phases += BrsRunTaskCallLowering(context)`:
```kotlin
        // Phase 0.0555: component constructor-call rewrite
        // `Screen("123")` → { val n = brsCreateComponent<Screen>(); n.airingId = "123";
        // kotlinLifecycleMarkInputsReady(n); n } (spec 2026-09-04-component-lifecycle §5.7).
        // Before any coroutine lowering; the codegen intrinsic lowers the create.
        phases += BrsComponentConstructorCallLowering(context)
```

- [ ] **Step 5: Regenerate + verify + commit**

`./run-compiler-tests.sh --update`; in `ctorCallLowering.brs.txt` expect: `__kotlinNewComponent = CreateObject("roSGNode", "DetailsScreen")`, `__kotlinNewComponent.airingId = id`, `__kotlinNewComponent.row = row`, `kotlinLifecycleMarkInputsReady_RoSGNode_k_(__kotlinNewComponent)` (name per the manifest), and `CreateObject("roSGNode", "VideoItem")` with no marker; no `_create_` call remains. `./run-compiler-tests.sh` — 107 pass.
```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs/components/ctorCallLowering.*
git commit -m "brs: lower component constructor calls to CreateObject + input writes + ready marker

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 5: The FIR family (spec §7)

**Files:**
- Create: `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/brs/checkers/BrsComponentTypes.kt`
- Modify: `.../checkers/expression/FirBrsCreateComponentTypeChecker.kt` (use the shared predicate; add the inputs reason)
- Create: `.../checkers/expression/FirBrsComponentInputEarlyReadChecker.kt`
- Create: `.../checkers/declaration/FirBrsTaskConstructorInputChecker.kt`
- Modify: `.../checkers/declaration/FirBrsTaskStateNotFieldChecker.kt` (remove the fake-source skip + KDoc)
- Modify: `BrsExpressionCheckers.kt`, `BrsDeclarationCheckers.kt` (register)
- Modify: `compiler/fir/checkers/checkers-component-generator/.../FirBrsDiagnosticsList.kt` (new group) + regenerate `FirBrsErrors.kt` and `FirNonSuppressibleErrorNames.kt`
- Modify: `FirBrsErrorsDefaultMessages.kt`, `AbstractBrsDiagnosticTest.kt` (map), `BrsDiagnosticTests.kt`
- Create fixtures under `compiler/testData/diagnostics/testsWithBrsStdLib/componentInputs/`

**Interfaces:**
- Produces: `BrsComponentTypes.isComponentClass(symbol, session)`, `BrsComponentTypes.constructorInputs(symbol, session): List<Name>`; diagnostics `BRS_COMPONENT_INPUT_READ_IN_INIT(propertyName, className)`, `BRS_CREATE_COMPONENT_HAS_INPUTS(className, inputs)`, `BRS_TASK_CONSTRUCTOR_INPUT(propertyName, className)`.

- [ ] **Step 1: Fixtures FIRST (they fail until the checkers exist)**

Directory `compiler/testData/diagnostics/testsWithBrsStdLib/componentInputs/`, one file per case:

`initBlockRead.kt`:
```kotlin
// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the init-block read — init() runs inside
// CreateObject before any field is written, so the read sees the declared default.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        println(<!BRS_COMPONENT_INPUT_READ_IN_INIT!>airingId<!>)
    }
}
```
`siblingInitializerRead.kt`:
```kotlin
// Expected: BRS_COMPONENT_INPUT_READ_IN_INIT on the sibling property initializer's read.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    val title: String = "Airing " + <!BRS_COMPONENT_INPUT_READ_IN_INIT!>airingId<!>
}
```
`lambdaReadOk.kt`:
```kotlin
// Expected: clean — a read inside a lambda declared in init is deferred.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    private val describe: () -> String = { "Airing " + airingId }
    init {
        launch { println(airingId) }
    }
}
```
`onStartReadOk.kt`:
```kotlin
// Expected: clean — onStart runs after the gate; inputs are set.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    override suspend fun onStart() {
        println(airingId)
    }
}
```
`methodReadOk.kt`:
```kotlin
// Expected: clean — a method body is not init.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    fun describe(): String = "Airing " + airingId
}
```
`initReadSuppressed.kt`:
```kotlin
// Expected: clean — @Suppress opts out.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class Screen(@SGStringField val airingId: String) : GroupComponent() {
    init {
        @Suppress("BRS_COMPONENT_INPUT_READ_IN_INIT")
        println(airingId)
    }
}
```
`createComponentHasInputs.kt`:
```kotlin
// Expected: BRS_CREATE_COMPONENT_HAS_INPUTS — an input-bearing class must be constructed with its constructor.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.createComponent

class Screen(@SGStringField val airingId: String) : GroupComponent()

class Host : GroupComponent() {
    fun open() {
        createComponent<<!BRS_CREATE_COMPONENT_HAS_INPUTS!>Screen<!>>()
    }
}
```
`createComponentHasInputsSuppressed.kt`: same body with `@Suppress("BRS_CREATE_COMPONENT_HAS_INPUTS")` on `open()` and no marker.

`taskConstructorInput.kt`:
```kotlin
// Expected: BRS_TASK_CONSTRUCTOR_INPUT — task inputs are configured via runTask<T> { }.
import kotlin.brs.TaskComponent
import kotlin.brs.SGIntegerField

class FetchTask(@SGIntegerField val <!BRS_TASK_CONSTRUCTOR_INPUT!>count<!>: Int) : TaskComponent() {
    override fun run() {}
}
```
`taskConstructorInputSuppressed.kt`: same with `@Suppress("BRS_TASK_CONSTRUCTOR_INPUT")` on the class, no marker.

`taskPlainConstructorVal.kt`:
```kotlin
// Expected: BRS_TASK_STATE_NOT_FIELD — the constructor-parameter skip is gone: a plain
// constructor val on a task is m-state and is lost across the task-thread clone.
import kotlin.brs.TaskComponent

class FetchTask(val <!BRS_TASK_STATE_NOT_FIELD!>retries<!>: Int) : TaskComponent() {
    override fun run() {}
}
```
Register eleven `@Test` methods in `BrsDiagnosticTests.kt` (`runTest("componentInputs/<file>.kt")` each). Run `./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests*" --no-configuration-cache -Dorg.gradle.dependency.verification=off` → the new tests fail (unknown diagnostic names).

- [ ] **Step 2: Declare the diagnostics + regenerate**

In `FirBrsDiagnosticsList.kt`, a new group after `TYPED_TASKS`:
```kotlin
    val COMPONENT_INPUTS by object : DiagnosticGroup("Component constructor inputs") {
        val BRS_COMPONENT_INPUT_READ_IN_INIT by error<KtElement> {
            parameter<String>("propertyName")
            parameter<String>("className")
            isSuppressible = true
        }
        val BRS_CREATE_COMPONENT_HAS_INPUTS by error<KtElement> {
            parameter<String>("className")
            parameter<String>("inputs")
            isSuppressible = true
        }
        val BRS_TASK_CONSTRUCTOR_INPUT by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("propertyName")
            parameter<String>("className")
            isSuppressible = true
        }
    }
```
Run both generator commands (Global Constraints). Messages in `FirBrsErrorsDefaultMessages.kt` (import the three factories):
```kotlin
        map.put(
            BRS_COMPONENT_INPUT_READ_IN_INIT,
            "Input ''{0}'' of component ''{1}'' is read during init: SceneGraph runs init() inside CreateObject, before any " +
                "field is written, so this read sees the declared default. Read it in onStart() (fires once all inputs are set) " +
                "or in a lambda/observer.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_CREATE_COMPONENT_HAS_INPUTS,
            "''{0}'' declares required inputs ({1}); construct it with {0}(…) so every input is written before onStart(), " +
                "instead of createComponent<{0}>().",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_TASK_CONSTRUCTOR_INPUT,
            "Task components take inputs through runTask<{1}> '{' field = value '}'; declare ''{0}'' as a var field instead of a " +
                "constructor parameter.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
```
(the `'{'`/`'}'` escapes keep MessageFormat from parsing braces.) Harness map entries (rendered form):
```kotlin
        "BRS_COMPONENT_INPUT_READ_IN_INIT" to
            "Input '{0}' of component '{1}' is read during init: SceneGraph runs init() inside CreateObject, before any " +
                "field is written, so this read sees the declared default. Read it in onStart() (fires once all inputs are set) " +
                "or in a lambda/observer.",
        "BRS_CREATE_COMPONENT_HAS_INPUTS" to
            "'{0}' declares required inputs ({1}); construct it with {0}(…) so every input is written before onStart(), " +
                "instead of createComponent<{0}>().",
        "BRS_TASK_CONSTRUCTOR_INPUT" to
            "Task components take inputs through runTask<{1}> { field = value }; declare '{0}' as a var field instead of a " +
                "constructor parameter.",
```

- [ ] **Step 3: `BrsComponentTypes` (shared predicate + inputs)**

```kotlin
package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * FIR-side component predicates. MIRROR NOTE: [isComponentClass] mirrors the backend's
 * `BrsComponentExtractor.isComponent` / `BrsIntrinsics.isSceneGraphComponent` (module
 * boundary forbids importing them — BrsSharedServiceTypes precedent); a change to what
 * counts as a component lands in all three in one commit. [constructorInputs] mirrors
 * `BrsIntrinsics.isConstructorParameterProperty` + the extractor's `requiredInputs`.
 */
object BrsComponentTypes {
    val fieldAnnotationIds: Set<ClassId> =
        BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys + BrsStandardClassIds.Annotations.BrsField

    fun isComponentClass(classSymbol: FirRegularClassSymbol, session: FirSession): Boolean {
        if (classSymbol.hasAnnotation(BrsStandardClassIds.Annotations.BrsComponent, session)) return true
        return lookupSuperTypes(classSymbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.toRegularClassSymbol(session)?.hasAnnotation(BrsStandardClassIds.Annotations.BrsSceneGraphComponent, session) == true }
    }

    fun isTaskComponentClass(classSymbol: FirRegularClassSymbol, session: FirSession): Boolean =
        lookupSuperTypes(classSymbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.lookupTag.classId == BrsStandardClassIds.Components.TaskComponent }

    fun isConstructorInput(property: FirPropertySymbol, session: FirSession): Boolean =
        property.fromPrimaryConstructor && property.annotations.any { it.toAnnotationClassId(session) in fieldAnnotationIds }

    /** The class's own constructor-parameter @SG properties, in declaration order. */
    @OptIn(DirectDeclarationsAccess::class)
    fun constructorInputs(classSymbol: FirRegularClassSymbol, session: FirSession): List<Name> =
        classSymbol.declarationSymbols.filterIsInstance<FirPropertySymbol>()
            .filter { isConstructorInput(it, session) }
            .map { it.name }
}
```
Replace `FirBrsCreateComponentTypeChecker.isComponentClass` with `BrsComponentTypes.isComponentClass`, and insert the inputs check BEFORE the existing `val reason = when { … else -> return }` block (that block returns on a clean type, so anything after it never runs for the classes this rule targets):
```kotlin
        // Input-bearing classes must be constructed through their constructor so every
        // input is written before onStart (spec §3); runTask keeps its configure lambda.
        val inputs = BrsComponentTypes.constructorInputs(classSymbol, session)
        if (inputs.isNotEmpty() && callableId != BrsStandardClassIds.Callables.runTask) {
            reporter.reportOn(
                typeProjection.source ?: expression.source,
                FirBrsErrors.BRS_CREATE_COMPONENT_HAS_INPUTS,
                classSymbol.classId.shortClassName.asString(),
                inputs.joinToString(", ") { it.asString() },
            )
            return
        }
```

- [ ] **Step 4: The init-read checker**

```kotlin
package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsComponentTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousInitializerSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

/**
 * BRS_COMPONENT_INPUT_READ_IN_INIT (spec 2026-09-04-component-lifecycle §7): a DIRECT
 * read of a component's constructor-parameter @SG property, through `this` of that
 * component, inside one of ITS init blocks or a sibling property initializer. SceneGraph
 * runs init() inside CreateObject before any field write, so the read sees the default.
 *
 * Context classification (FirUninitializedEnumChecker precedent): the nearest
 * containing init block / property / function on the checker's declaration stack.
 * An anonymous initializer of the owner, or a non-local sibling property of the owner
 * (its initializer) → REPORT. Any function on top — a lambda, a method, a constructor,
 * a property ACCESSOR body — → deferred/legal. DISCLOSED HOLE (under-approximation, the
 * marshallability-checker convention): an inline lambda invoked synchronously in init
 * (`run { airingId }`) classifies as deferred.
 */
object FirBrsComponentInputEarlyReadChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        val session = context.session
        val property = expression.calleeReference.toResolvedCallableSymbol() as? FirPropertySymbol ?: return
        if (!BrsComponentTypes.isConstructorInput(property, session)) return
        val owner = property.getContainingClassSymbol() as? FirRegularClassSymbol ?: return
        if (!BrsComponentTypes.isComponentClass(owner, session)) return

        // Only the component's OWN read: `other.airingId` on a handle is a node field read.
        val receiver = expression.dispatchReceiver
        if (receiver != null && receiver !is FirThisReceiverExpression) return
        if (receiver is FirThisReceiverExpression &&
            receiver.calleeReference.boundSymbol?.let { (it as? FirRegularClassSymbol)?.classId } != owner.classId
        ) return

        val accessedContext = context.containingDeclarations.lastOrNull {
            it is FirAnonymousInitializerSymbol || it is FirPropertySymbol || it is FirFunctionSymbol<*>
        } ?: return

        val inInit = when (accessedContext) {
            is FirAnonymousInitializerSymbol -> accessedContext.getContainingClassSymbol() == owner
            is FirPropertySymbol ->
                !accessedContext.isLocal && accessedContext != property &&
                    accessedContext.getContainingClassSymbol() == owner
            else -> false
        }
        if (!inInit) return

        reporter.reportOn(
            expression.source,
            FirBrsErrors.BRS_COMPONENT_INPUT_READ_IN_INIT,
            property.name.asString(),
            owner.classId.shortClassName.asString(),
        )
    }
}
```
Register in `BrsExpressionCheckers.propertyAccessExpressionCheckers`.

- [ ] **Step 5: Task constructor input checker + skip removal**

```kotlin
package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsComponentTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

/**
 * BRS_TASK_CONSTRUCTOR_INPUT (spec §7): a constructor-parameter @SG property on a
 * TaskComponent — task inputs are configured through `runTask<T> { field = value }`;
 * constructor inputs are a render-component contract (v1).
 */
object FirBrsTaskConstructorInputChecker : FirPropertyChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        if (!BrsComponentTypes.isConstructorInput(declaration.symbol, session)) return
        val containingClass = context.containingDeclarations.lastOrNull() as? FirRegularClassSymbol ?: return
        if (!BrsComponentTypes.isTaskComponentClass(containingClass, session)) return
        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_TASK_CONSTRUCTOR_INPUT,
            declaration.name.asString(),
            containingClass.classId.shortClassName.asString(),
        )
    }
}
```
Register in `BrsDeclarationCheckers.propertyCheckers`. In `FirBrsTaskStateNotFieldChecker`, delete `if (declaration.source?.kind is KtFakeSourceElementKind) return` (and its import) and replace the KDoc bullet's "synthetic/fake-source properties (e.g. constructor-parameter properties — … cannot take constructor arguments anyway)" with "Constructor-parameter properties ARE checked (components take constructor inputs since the lifecycle program); an @SG-annotated one is a task-input error (FirBrsTaskConstructorInputChecker), a plain one is m-state and fires here."

- [ ] **Step 6: Run the suite, rebuild, commit**

`./gradlew :compiler:fir:checkers:checkers.brs:test --tests "*BrsDiagnosticTests*" --no-configuration-cache -Dorg.gradle.dependency.verification=off` → 243 pass. Then `./rebuild.sh` (steps 8/9 compile kotlin-test-brs and flow against the new rules — if either fires, `@Suppress` at the declaration site as the guide prescribes). Commit:
```bash
git add compiler/fir core/compiler.common.brightscript compiler/testData/diagnostics
git commit -m "fir: component input rules — init-read, createComponent-has-inputs, task-constructor-input; shared BrsComponentTypes

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 6: kotlin-roku — generate typed builders (spec §6)

**Files (kotlin-roku):**
- Modify: `src/main/kotlin/com/example/roku/gradle/tasks/GenerateLayoutStubsTask.kt`
- Modify/Create: `src/test/kotlin/com/example/roku/gradle/tasks/GenerateLayoutStubsTaskTest.kt` (add cases; create if absent)

**Interfaces:**
- Produces: `<stubOutputDir>/<pkg path>/ComponentBuilders.kt` per package with one `@SGComponentBuilder("<Class>") fun LayoutBuilder.<lowerCamel>(id, <inputs>, <standard attrs>, init)` per eligible component; id-extraction patterns extended with the generated builder names.

- [ ] **Step 1: Tests first**

Add to the task's test class (mirroring an existing `extractNodeIds` case):
```kotlin
    @Test
    fun parsesComponentDeclarationWithInputs() {
        val src = """
            package app.screens
            import kotlin.brs.GroupComponent
            import kotlin.brs.SGStringField
            import kotlin.brs.SGIntegerField
            class AiringDetailsScreen(@SGStringField val airingId: String, @SGIntegerField(alwaysNotify = true) val row: Int) : GroupComponent()
            class Plain : GroupComponent()
        """.trimIndent()
        val infos = GenerateLayoutStubsTask.extractComponentInfos(src)
        assertEquals(listOf("AiringDetailsScreen", "Plain"), infos.map { it.className })
        assertEquals(listOf("airingId" to "String", "row" to "Int"), infos[0].inputs)
        assertEquals("airingDetailsScreen", infos[0].builderName)
    }

    @Test
    fun skipsIneligibleComponents() {
        val src = """
            package app
            import kotlin.brs.GroupComponent
            import kotlin.brs.SGNodeField
            import kotlin.brs.roku.RoSGNode
            class NodeInput(@SGNodeField val owner: RoSGNode?) : GroupComponent()
            class Label : GroupComponent()
        """.trimIndent()
        val infos = GenerateLayoutStubsTask.extractComponentInfos(src)
        assertTrue(infos.none { it.className == "NodeInput" })     // node-typed input: construct in code
        assertTrue(infos.none { it.className == "Label" })         // name collides with a built-in DSL method
    }

    @Test
    fun rendersABuilder() {
        val info = GenerateLayoutStubsTask.ComponentInfo("app.screens", "AiringDetailsScreen", listOf("airingId" to "String"))
        val text = GenerateLayoutStubsTask.renderBuilder(info)
        assertTrue(text.contains("@SGComponentBuilder(\"AiringDetailsScreen\")"))
        assertTrue(text.contains("fun LayoutBuilder.airingDetailsScreen(id: String, airingId: String, translation: Vector2D? = null"))
        assertTrue(text.contains("attr(\"airingId\", airingId)"))
    }

    @Test
    fun builderCallsYieldNodeIds() {
        val src = """sceneLayout { airingDetailsScreen(id = "details", airingId = "123") }"""
        assertEquals(listOf("details"), GenerateLayoutStubsTask.extractNodeIds(src, setOf("airingDetailsScreen")))
    }
```
Run `cd ../kotlin-roku && ./gradlew test --tests '*GenerateLayoutStubsTask*'` → compile failures (new API).

- [ ] **Step 2: Parsing + rendering**

In the companion object:
```kotlin
        internal data class ComponentInfo(val packageName: String, val className: String, val inputs: List<Pair<String, String>>) {
            val builderName: String get() = className.replaceFirstChar { it.lowercase() }
        }

        private val renderBases = setOf("GroupComponent", "SceneComponent", "LayoutComponent", "RectangleComponent")
        private val builderInputTypes = setOf("String", "Int", "Float", "Double", "Boolean")
        private val classHeaderPattern = Regex("""(?:open\s+)?class\s+(\w+)\s*(?:\(([^()]*(?:\([^()]*\)[^()]*)*)\))?\s*:\s*(\w+)\s*\(""")
        private val inputParamPattern = Regex("""@(SG\w+Field)(?:\([^)]*\))?\s+val\s+(\w+)\s*:\s*(\w+)""")

        /**
         * Constrained grammar for component declarations: `class X(<@SG…Field val a: T, …>) : Base()`
         * where Base is a stdlib render base or another component in the same source set (two
         * passes). A class is ELIGIBLE for a builder when every constructor parameter is an
         * @SG-annotated val of String/Int/Float/Double/Boolean (node/AA inputs cannot be XML
         * constants — construct those in code) and its lowerCamel name does not collide with a
         * built-in DSL method. The compiler's extractor is the source of truth: a wrong builder
         * is a compile error at its call site, never a silent XML.
         */
        internal fun extractComponentInfos(content: String): List<ComponentInfo> {
            val packageName = Regex("""package\s+([\w.]+)""").find(content)?.groupValues?.get(1) ?: ""
            val headers = classHeaderPattern.findAll(content).toList()
            val knownComponents = mutableSetOf<String>()
            repeat(2) {
                for (m in headers) if (m.groupValues[3] in renderBases || m.groupValues[3] in knownComponents) knownComponents.add(m.groupValues[1])
            }
            val result = mutableListOf<ComponentInfo>()
            for (m in headers) {
                val className = m.groupValues[1]
                if (className !in knownComponents) continue
                val params = m.groupValues[2].trim()
                val inputs = mutableListOf<Pair<String, String>>()
                var eligible = true
                if (params.isNotEmpty()) {
                    val pieces = params.split(Regex(""",(?![^(]*\))""")).map { it.trim() }.filter { it.isNotEmpty() }
                    for (piece in pieces) {
                        val im = inputParamPattern.matchEntire(piece)
                        if (im == null || im.groupValues[3] !in builderInputTypes) { eligible = false; break }
                        inputs.add(im.groupValues[2] to im.groupValues[3])
                    }
                }
                if (!eligible) continue
                val info = ComponentInfo(packageName, className, inputs)
                if (info.builderName in builderMethods || info.builderName == "component") continue
                result.add(info)
            }
            return result
        }

        internal fun renderBuilder(info: ComponentInfo): String {
            val inputParams = info.inputs.joinToString("") { (n, t) -> ", $n: $t" }
            val attrs = info.inputs.joinToString("\n") { (n, _) -> "        attr(\"$n\", $n)" }
            return """
                |@SGComponentBuilder("${info.className}")
                |fun LayoutBuilder.${info.builderName}(id: String$inputParams, translation: Vector2D? = null, rotation: Float? = null, scale: Vector2D? = null, scaleRotateCenter: Vector2D? = null, opacity: Float? = null, visible: Boolean? = null, inheritParentOpacity: Boolean? = null, inheritParentTransform: Boolean? = null, clippingRect: Vector4D? = null, renderGroup: Boolean? = null, focusable: Boolean? = null, renderPass: Int? = null, init: ComponentBuilder.() -> Unit = {}) {
                |    component("${info.className}", id, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass) {
                |$attrs
                |        init()
                |    }
                |}
                |""".trimMargin()
        }

        /** Node-id extraction now also matches the generated builder names (id = "…" form). */
        internal fun extractNodeIds(content: String, builderNames: Set<String>): List<String> {
            val nodeIds = extractNodeIds(content).toMutableList()
            if (builderNames.isNotEmpty()) {
                val pattern = Regex("""\b(${builderNames.joinToString("|")})\s*\([^)]*id\s*=\s*"([^"]+)"""")
                pattern.findAll(content).forEach { val id = it.groupValues[2]; if (id !in nodeIds) nodeIds.add(id) }
            }
            return nodeIds
        }
```
In `generateStubs()`: pass 1 over `allKotlinFiles` → `extractComponentInfos` collected per package; write one `<pkgPath>/ComponentBuilders.kt` per package (header: `package <pkg>`, imports `kotlin.brs.scenegraph.*`), containing each `renderBuilder(info)`; pass 2 → layout stubs as today but calling `extractNodeIds(content, allBuilderNames)` inside `extractLayoutInfo` (thread the set through). Log `Generated <n> component builders`.

- [ ] **Step 3: Tests green, republish, commit**

`./gradlew test --tests '*GenerateLayoutStubsTask*'` → pass. `cd ../roku-test-app && ./rebuild-all.sh --plugin`.
```bash
cd ../kotlin-roku && git add src && git commit -m "plugin: generate @SGComponentBuilder typed layout builders per component; builder ids feed layout stubs

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 7: E2E — Suite 11 input tests (spec §9 tests 3, 6, 7, 9-inputs)

**Files (roku-test-app):**
- Create: `fixtures/LifecycleInputProbe.kt`, `fixtures/LifecycleBuilderParentProbe.kt`
- Modify: `tests/ComponentLifecycleTests.kt`

- [ ] **Step 1: Fixtures**

`LifecycleInputProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.kotlinLifecycleWatchdogFires
import kotlin.brs.kotlinLifecycleWatchdogMillis
import kotlin.brs.roku.RoSGNodeEvent

// Input-bearing probe: `airingId` is a constructor input (written by the lowered
// constructor call or a layout constant, never by init). onStart logs it; ops
// report the per-component watchdog counter and the log.
class LifecycleInputProbe(@SGStringField val airingId: String) : GroupComponent() {

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onOpChanged")
    var op: String = ""

    @SGStringField(alwaysNotify = true)
    var outcome: String = ""

    init {
        kotlinLifecycleWatchdogMillis(400)
    }

    override suspend fun onStart() {
        lifecycleLog(global, "input-start:$airingId")
    }

    private fun onOpChanged(msg: RoSGNodeEvent) {
        val runOp = "${msg.getData()}"
        if (runOp == "report") {
            outcome = "${global.getField("__lifecycleLog")}"
            global.setField("__lifecycleLog", "")
        } else if (runOp == "watchdog") {
            outcome = "fires:${kotlinLifecycleWatchdogFires()}"
        }
    }
}
```
`LifecycleBuilderParentProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

// Declares an input-bearing child through the plugin-generated typed builder
// with a CONSTANT input: the XML child carries airingId="from-layout" and the
// ready-marker attribute, so the child's onStart fires at creation.
class LifecycleBuilderParentProbe : GroupComponent() {

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onOpChanged")
    var op: String = ""

    @SGStringField(alwaysNotify = true)
    var outcome: String = ""

    private fun onOpChanged(msg: RoSGNodeEvent) {
        if ("${msg.getData()}" == "report") {
            outcome = "${global.getField("__lifecycleLog")}"
            global.setField("__lifecycleLog", "")
        }
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            lifecycleInputProbe(id = "fixed", airingId = "from-layout")
        }
    }
}
```

- [ ] **Step 2: Tests (append inside `suite("ComponentLifecycle")`)**

```kotlin
        testAsync("constructorInputsReadableInOnStartViaConstructorCall") {
            val parent = installParent(scene)
            val probe = LifecycleInputProbe("a1")            // lowered: create + write + marker
            LifecycleExtras.adoptNode(scene, nodeOf(probe))
            delay(300)
            assertEquals("input-start:a1;", "${roundTrip(nodeOf(probe), "op", "report", "outcome")}")
        }

        testAsync("constructorInputsFromTypedBuilderConstant") {
            installParent(scene)
            val host = LifecycleExtras.adopt(scene, "LifecycleBuilderParentProbe")
            delay(300)
            assertEquals("input-start:from-layout;", "${roundTrip(host, "op", "report", "outcome")}")
        }

        testAsync("rawCreatedInputComponentStaysClosedUntilMarkerAndWatchdogFires") {
            installParent(scene)
            val raw = LifecycleExtras.adopt(scene, "LifecycleInputProbe")   // raw subtype string: no marker
            delay(700)                                                     // > 400ms watchdog
            assertEquals("", "${roundTrip(raw, "op", "report", "outcome")}")
            assertEquals("fires:1", "${roundTrip(raw, "op", "watchdog", "outcome")}")
            raw.setField("__kotlinInputsReady", true)                      // the gate's wake path
            delay(300)
            assertEquals("input-start:;", "${roundTrip(raw, "op", "report", "outcome")}")
        }

        testAsync("retireCancelsOnStartParkedInAwaitReady") {
            installParent(scene)
            val raw = LifecycleExtras.adopt(scene, "LifecycleInputProbe")
            delay(200)
            retire(raw)
            raw.setField("__kotlinInputsReady", true)
            delay(300)
            assertEquals("", "${roundTrip(raw, "op", "report", "outcome")}")   // retired: no onStart
        }

        testAsync("constructorInputsSurviveRetireRevive") {
            installParent(scene)
            val probe = LifecycleInputProbe("keep")
            val node = LifecycleExtras.adoptNode(scene, nodeOf(probe))
            delay(300)
            roundTrip(node, "op", "report", "outcome")
            retire(node)
            scene.removeChild(node)
            scene.appendChild(node)
            revive(node)
            delay(300)
            assertEquals("input-start:keep;", "${roundTrip(node, "op", "report", "outcome")}")
        }
```
Add to `LifecycleExtras`: `fun adoptNode(scene: RoSGNode, node: RoSGNode): RoSGNode { scene.appendChild(node); nodes.add(node); return node }`, and the file needs the `nodeOf` bridge (`@BrsInline("return component") private external fun nodeOf(component: ComponentBase): RoSGNode`) plus imports for `LifecycleInputProbe`, `ComponentBase`, `BrsInline`.

- [ ] **Step 3: Run on device, commit**

```bash
cd ../roku-test-app && ./rebuild-all.sh --all && ./run-device-tests.sh
```
Expected: Suite 11 reports 14 passed (9 + 5); everything else unchanged; include validators strict 0. The watchdog console line `[kotlin.lifecycle] LifecycleInputProbe(id=…) not ready after 0s — inputs UNSET (created outside its Kotlin constructor?), unresolved: ` appears once in the capture (400ms override renders as 0s).
```bash
git add src && git commit -m "e2e: Suite 11 constructor-input tests — constructor call, typed builder, raw-creation gate + watchdog, retire while parked, inputs survive revive

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 8: Documentation + gates (spec §10)

**Files:**
- Modify: `CLAUDE.md` (new "Constructor inputs and typed layout builders" section after the lifecycle section; suite row; gate table); `SceneComponent.kt` KDoc for `ContentNodeComponent` (`VideoItem()` now real); the memory file.

- [ ] **Step 1: Write the section**

Cover: the syntax and what it means (required inputs; `val` is a Kotlin-side promise; non-null OK); construction via the constructor call and what it lowers to; `createComponent<T>()` is an error on input-bearing classes; task components take no constructor inputs; static layouts through the generated builders (required inputs are required parameters; constants only; the raw `component()` + `attr()` form still works; missing/non-constant required input is a compile ERROR with the exact message shape); the ready marker's meaning and the watchdog's UNSET-inputs clause; the init-read rule with its disclosed inline-lambda hole; recyclable content is `var`, inputs are identity; the plugin's constrained grammar and which classes get no builder (node/AA inputs, DSL-name collisions). Key files table: extractor, `LayoutInputValidation`, the two lowerings, `BrsComponentTypes`, the three checkers, `GenerateLayoutStubsTask`.

- [ ] **Step 2: Gates + counts**

Golden 107; FIR 243; Suite 11 row gains the five tests; E2E `120 active / 11 suites`; stdlib unchanged from plan A.

- [ ] **Step 3: Commit + memory**

```bash
git add CLAUDE.md libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt
git commit -m "docs: constructor inputs + typed layout builders shipped; gates 107/243/614-63/120-11

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```
Memory: plan B SHIPPED; spec 1 complete; spec 2 (scoped SharedService lookup) is next and consumes §13.

---

## Self-review

- **Spec coverage:** §3 constructor inputs → Tasks 2, 4, 5; §3 typed builders → Tasks 1, 3, 6; §4 inputs readiness (marker, gate wake, watchdog UNSET clause) → Tasks 1, 3, 4, 7; §5.7 → Task 4; §5.8 → Task 2; §5.9 → Task 3; §5.10 → Tasks 1, 5; §6 → Task 6; §7 → Task 5; §9 goldens (ctorInputs, ctorCallLowering, builderCallExtraction; the layout-error case is the pure unit test `LayoutInputValidationTest` because the golden harness has no expected-error mode) → Tasks 2–4; §9 FIR fixtures (11) → Task 5; §9 Suite 11 tests 3, 6, 7, 9-inputs → Task 7; §10 → Task 8.
- **Placeholders:** none; each code step is complete or names the exact precedent line to mirror.
- **Type consistency:** `kotlinLifecycleMarkInputsReady(node: RoSGNode)` (Task 1) = `kotlinLifecycleMarkInputsReadyOrNull` (Task 4); `LIFECYCLE_INPUTS_READY_FIELD`/`READY_MARKER_ATTRIBUTE` both `"__kotlinInputsReady"`; `isConstructorParameterProperty` (Task 2) used by Tasks 3–4; `BrsComponentTypes.isConstructorInput` (Task 5) mirrors it; `requiredInputs` (Task 3) drives validation and the marker; builder names `lowerCamel(className)` in Task 6 match the `lifecycleInputProbe(...)` call in Task 7 and the `@SGComponentBuilder` annotation Task 3 keys on.
