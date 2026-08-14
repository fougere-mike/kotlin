// BRS_SCOPE_RESULT_NOT_DATA (warning) — scope request results cross the boundary BY
// COPY as plain data; a result type outside the marshallable set keeps its fields but
// loses all behavior (methods/equals/copy do not survive the copy). Fires on run{}
// block results AND on the R position of ScopeRequest/1/2 declarations.
//
// SEVERITY VERIFICATION (Task 6 Step 1, 2026-08-14): WARNING, not error. A scratch
// compile of `data class ProbePoint(val x: Int, val y: Int)` + `fun readX(p: ProbePoint)
// = p.x` through the current fat JAR emits `return p.x` — a DIRECT member read on the
// backing AA, NOT a getter call (data-class properties are plain AA keys; only methods
// are function-pointer slots). A husk result is therefore usable AS DATA — property
// reads work after the copy — so this steers behavioral state to the VM instead of
// hard-failing. Pinned by the dataClassPropertyRead golden
// (compiler/testData/codegen/brs/declarations/dataClassPropertyRead.kt).
import kotlin.brs.ScopeHandle
import kotlin.brs.ScopeRequest
import kotlin.brs.roku.RoAssociativeArray

data class ResBox(val value: Int)

fun makeNames(): List<String> = listOf("a", "b")

fun sideEffect() {}

// Case 1: data-class block result — WARNING (fields cross; equals/copy die)
suspend fun dataResultWarns(owner: ScopeHandle): ResBox {
    return owner.<!BRS_SCOPE_RESULT_NOT_DATA!>run<!> { ResBox(1) }
}

// Case 2: kotlin collection block result — WARNING
suspend fun listResultWarns(owner: ScopeHandle): List<String> {
    return owner.<!BRS_SCOPE_RESULT_NOT_DATA!>run<!> { makeNames() }
}

// Case 3: declaration-site R — WARNING (handlers for this request return husks)
object BadResultReq : <!BRS_SCOPE_RESULT_NOT_DATA!>ScopeRequest<ResBox><!>("BadResultReq")

// Case 4: marshallable results — CLEAN
suspend fun primitiveResultOk(owner: ScopeHandle): Int = owner.run { 41 + 1 }

suspend fun stringResultOk(owner: ScopeHandle): String = owner.run { "ok" }

suspend fun aaResultOk(owner: ScopeHandle): RoAssociativeArray {
    return owner.run {
        val out = RoAssociativeArray.create()
        out.addReplace("k", "v")
        out
    }
}

suspend fun unitResultOk(owner: ScopeHandle) {
    owner.run { sideEffect() }
}

// Case 5: marshallable declaration-site R — CLEAN
object GoodResultReq : ScopeRequest<Int>("GoodResultReq")

// Case 6: suppression escape (both surfaces) — CLEAN (deliberate data-only result)
@Suppress("BRS_SCOPE_RESULT_NOT_DATA")
suspend fun deliberateHuskResult(owner: ScopeHandle): ResBox = owner.run { ResBox(2) }

@Suppress("BRS_SCOPE_RESULT_NOT_DATA")
object DeliberateHuskReq : ScopeRequest<ResBox>("DeliberateHuskReq")
