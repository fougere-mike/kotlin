// BRS_TASK_CAPTURE_UNMARSHALLABLE / BRS_TASK_CAPTURE_MUTATION_LOST — the lifted
// region (the flowOn(Dispatchers.Task) upstream chain / the spawnTask block) is
// evaluated on a Roku Task thread; every free value it references crosses the
// render→task boundary BY COPY as a typed capture field. Types outside the
// marshallable set (primitives, String, Dynamic, external interfaces) lose all
// behavior in that copy: function values die entirely, class instances —
// including the enclosing `this`, captured implicitly by any member access —
// survive only as method-less data husks. The fix the message names: hoist the
// value to a local first (val x = <expr>) so the marshallable RESULT of the
// read crosses instead of the instance. Writes to captured vars land on the
// task-side copy and never reach the caller (warning, scope-rule mirror).
//
// Disclosed hole (spec §7): a value pre-erased to Dynamic passes — Dynamic is
// an external interface, inside the marshallable set, so a data class hiding
// behind it escapes. kotlin.Any is NOT part of that hole on this rule family:
// the positive-list oracle rejects Any (it is a regular class), so an
// Any-erased value is flagged — see anyRejected below.
import kotlin.brs.Dynamic
import kotlin.brs.asDynamic
import kotlin.brs.roku.RoAssociativeArray
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.flow
import kotlin.coroutines.flow.flowOn
import kotlin.coroutines.task.spawnTask

data class CapBox(val value: Int)

// Case 1: marshallable captures — primitives, String, external interfaces, Dynamic — CLEAN
fun marshallableOk(aa: RoAssociativeArray, dyn: Dynamic): Flow<String> {
    val count = 3
    val label = "shelf"
    return flow {
        aa.addReplace(label, dyn)
        emit(label + count)
    }.flowOn(Dispatchers.Task)
}

// Case 2: data-class capture — ERROR (methods die crossing; the husk is not the value)
fun dataClassCaptureRejected(): Flow<Int> {
    val box = CapBox(7)
    return flow { emit(<!BRS_TASK_CAPTURE_UNMARSHALLABLE!>box<!>.value) }.flowOn(Dispatchers.Task)
}

// Case 3: kotlin collection capture — ERROR
fun listCaptureRejected(items: List<Int>): Flow<Int> =
    flow { emit(<!BRS_TASK_CAPTURE_UNMARSHALLABLE!>items<!>.size) }.flowOn(Dispatchers.Task)

// Case 4: function-value capture — ERROR (function values cannot cross at all)
fun functionCaptureRejected(): Flow<Int> {
    val supplier: () -> Int = { 9 }
    return flow { emit(<!BRS_TASK_CAPTURE_UNMARSHALLABLE!>supplier<!>()) }.flowOn(Dispatchers.Task)
}

// Case 5: implicit-`this` capture via an instance-property read in a
// class-declared chain — ERROR. The property read captures the enclosing
// instance; it crosses as a husk and every later member access dies task-side.
class StreamVm {
    private val baseUrl: String = "https://api.example"

    fun stream(): Flow<String> =
        flow { emit(<!BRS_TASK_CAPTURE_UNMARSHALLABLE!>baseUrl<!>) }.flowOn(Dispatchers.Task)

    // Case 6: the hoist fix from the message — val x = <expr> outside the region,
    // so the marshallable String crosses instead of `this` — CLEAN
    fun streamHoisted(): Flow<String> {
        val url = baseUrl
        return flow { emit(url) }.flowOn(Dispatchers.Task)
    }
}

// Case 7: construction moved INSIDE the region — nothing crosses — CLEAN
fun constructionInsideOk(): Flow<Int> = flow {
    val local = CapBox(4)
    emit(local.value)
}.flowOn(Dispatchers.Task)

// Case 8: spawnTask block capture — same rule, same boundary — ERROR
suspend fun spawnCaptureRejected(): Int {
    val box = CapBox(1)
    return spawnTask { <!BRS_TASK_CAPTURE_UNMARSHALLABLE!>box<!>.value }
}

// Case 9: assignment to a captured var in the flowOn region — WARNING (the write
// lands on the task-side copy; emit the value instead)
fun mutationLost(): Flow<Int> {
    var total = 0
    return flow {
        <!BRS_TASK_CAPTURE_MUTATION_LOST!>total<!> = total + 1
        emit(total)
    }.flowOn(Dispatchers.Task)
}

// Case 10: assignment to a captured var in a spawnTask block — WARNING (return
// the value from the block instead)
suspend fun spawnMutationLost(): Int {
    var hits = 0
    return spawnTask {
        <!BRS_TASK_CAPTURE_MUTATION_LOST!>hits<!> = hits + 1
        hits
    }
}

// Case 11: DISCLOSED HOLE (spec §7) — a value pre-erased to Dynamic crosses
// unflagged: Dynamic is an external interface, inside the marshallable set, so
// the data class hiding behind it escapes the rule. CLEAN by design.
fun erasedToDynamicHole(): Flow<Dynamic> {
    val hidden: Dynamic = CapBox(3).asDynamic()
    return flow { emit(hidden) }.flowOn(Dispatchers.Task)
}

// Case 12: Any is NOT the erasure hole here — the positive-list oracle rejects
// kotlin.Any, so the capture AND the emission are both flagged.
fun anyRejected(): Flow<Any> {
    val boxed: Any = CapBox(4)
    return flow { emit(<!BRS_TASK_CAPTURE_UNMARSHALLABLE!><!BRS_TASK_EMIT_NOT_MARSHALLABLE!>boxed<!><!>) }.flowOn(Dispatchers.Task)
}

// Case 13: suppression escape — CLEAN (deliberate husk opt-in)
@Suppress("BRS_TASK_CAPTURE_UNMARSHALLABLE")
fun deliberateHusk(): Flow<Int> {
    val box = CapBox(8)
    return flow { emit(box.value) }.flowOn(Dispatchers.Task)
}
