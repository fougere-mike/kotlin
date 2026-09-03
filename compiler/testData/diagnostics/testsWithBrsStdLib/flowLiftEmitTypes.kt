// BRS_TASK_EMIT_NOT_MARSHALLABLE — values emitted by the lifted upstream region
// cross the task→render boundary BY COPY as kind-tagged envelope AAs (flow-program
// spec §5, decision 2), and the spawnTask result crosses the same hop: types
// outside the marshallable set (primitives, String, Dynamic, external interfaces)
// arrive as method-less husks. The checker walks emit/emitAll argument types in
// the region and the spawnTask call's result type. The shipped idiom: emit parsed
// roAssociativeArrays task-side and map to domain types downstream of flowOn, on
// the render side.
//
// Disclosed hole (spec §7): generic T-typed emissions — a type-parameter type is
// unclassifiable, and the oracle under-approximates rather than false-positives.
import kotlin.brs.roku.RoAssociativeArray
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.emitAll
import kotlin.coroutines.flow.flow
import kotlin.coroutines.flow.flowOf
import kotlin.coroutines.flow.flowOn
import kotlin.coroutines.task.spawnTask

data class EmitBox(val value: Int)

// Case 1: the shipped idiom — parse task-side, emit plain AAs — CLEAN
fun aaEmissions(): Flow<RoAssociativeArray> = flow {
    val aa = RoAssociativeArray.create()
    aa.addReplace("id", 1)
    emit(aa)
}.flowOn(Dispatchers.Task)

// Case 2: primitive and String emissions — CLEAN
fun primitiveEmissions(): Flow<String> = flow {
    emit("ready")
    emit("done")
}.flowOn(Dispatchers.Task)

// Case 3: data-class emission (constructed inside the region, so the capture rule
// stays silent — the EMISSION is the violation) — ERROR
fun boxEmitRejected(): Flow<EmitBox> = flow {
    emit(<!BRS_TASK_EMIT_NOT_MARSHALLABLE!>EmitBox(1)<!>)
}.flowOn(Dispatchers.Task)

// Case 4: emitAll of a Flow of data classes — the element type crosses per
// emission — ERROR
fun boxSpliceRejected(): Flow<EmitBox> = flow {
    emitAll(<!BRS_TASK_EMIT_NOT_MARSHALLABLE!>flowOf(EmitBox(2))<!>)
}.flowOn(Dispatchers.Task)

// Case 5: emitAll of a marshallable-element flow — CLEAN
fun spliceOk(): Flow<Int> = flow {
    emit(0)
    emitAll(flowOf(1, 2))
}.flowOn(Dispatchers.Task)

// Case 6: spawnTask result outside the marshallable set — ERROR (same checker,
// same hop: the result envelope copies)
suspend fun listResultRejected(): List<Int> =
    <!BRS_TASK_EMIT_NOT_MARSHALLABLE!>spawnTask<!> { listOf(1, 2) }

// Case 7: spawnTask returning plain data — CLEAN
suspend fun aaResultOk(): RoAssociativeArray = spawnTask {
    val aa = RoAssociativeArray.create()
    aa.addReplace("k", "v")
    aa
}

// Case 8: DISCLOSED HOLE (spec §7) — generic T-typed emission: a type-parameter
// type is unclassifiable; the oracle treats it as marshallable rather than
// false-positive on instantiations like passthrough(42). CLEAN by design.
fun <T> passthroughHole(value: T): Flow<T> =
    flow { emit(value) }.flowOn(Dispatchers.Task)

// Case 9: DISCLOSED HOLE, spawnTask flavor — generic T result type. CLEAN by design.
suspend fun <T> supplyHole(value: T): T = spawnTask { value }

// Case 10: suppression escape — CLEAN (deliberate husk opt-in)
@Suppress("BRS_TASK_EMIT_NOT_MARSHALLABLE")
fun deliberateHuskEmission(): Flow<EmitBox> = flow {
    emit(EmitBox(3))
}.flowOn(Dispatchers.Task)
