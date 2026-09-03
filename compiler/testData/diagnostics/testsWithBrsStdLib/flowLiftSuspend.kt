// BRS_TASK_SUSPEND_IN_LIFTED — the lifted upstream world is SYNCHRONOUS by
// design (flow-program spec §5): the task thread has no coroutine pump, so a
// real suspension can never resume there. Blocking calls are the point of being
// on the task thread; the only suspend calls the region admits are emit and
// emitAll. The checker walks every call in the region (the flowOn upstream
// chain expression and all its literal lambdas) and flags any other resolved
// suspend callee. spawnTask needs no instance of this rule: its block is a
// NON-suspend function type, so the ordinary frontend already rejects
// suspension inside it.
//
// Disclosed hole (spec §7): a suspend function REFERENCE smuggled into an
// opaque callee — no suspend CALL is statically visible in the region, so the
// rule stays silent (the eventual invocation happens where the checker cannot
// see it; the un-pumped task side raises the guided runtime error instead).
import kotlin.coroutines.delay
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.emitAll
import kotlin.coroutines.flow.flow
import kotlin.coroutines.flow.flowOf
import kotlin.coroutines.flow.flowOn
import kotlin.coroutines.flow.map

fun blockingFetch(url: String): String = url

suspend fun suspendFetch(v: Int): Int = v

// Case 1: blocking non-suspend work in the region — CLEAN (the point of the lift)
fun blockingOk(): Flow<String> = flow {
    emit(blockingFetch("https://api.example"))
}.flowOn(Dispatchers.Task)

// Case 2: emit and emitAll — the two admitted suspend calls — CLEAN
fun emissionsOk(): Flow<Int> = flow {
    emit(0)
    emitAll(flowOf(1, 2))
}.flowOn(Dispatchers.Task)

// Case 3: delay upstream — ERROR (a real park can never resume task-side;
// delay-as-blocking-sleep is recorded backlog, spec §5)
fun delayRejected(): Flow<Int> = flow {
    <!BRS_TASK_SUSPEND_IN_LIFTED!>delay(100)<!>
    emit(1)
}.flowOn(Dispatchers.Task)

// Case 4: suspend call in an operator lambda of the chain — ERROR
fun operatorSuspendRejected(): Flow<Int> =
    flow { emit(1) }
        .map { v -> <!BRS_TASK_SUSPEND_IN_LIFTED!>suspendFetch(v)<!> }
        .flowOn(Dispatchers.Task)

// Case 5: DISCLOSED HOLE (spec §7) — a suspend function REFERENCE passed to an
// opaque callee inside the region: no suspend CALL is visible to the checker
// (the reference is not a call, and the callee that will invoke it is not
// resolvable to a suspend call site here). CLEAN by design.
fun opaque(f: suspend (Int) -> Int): Int = 0

fun fnRefHole(): Flow<Int> = flow {
    emit(opaque(::suspendFetch))
}.flowOn(Dispatchers.Task)

// Case 6: suppression escape — CLEAN (deliberate opt-in; the shim's guided
// runtime error names the law if the suspension actually parks)
@Suppress("BRS_TASK_SUSPEND_IN_LIFTED")
fun suppressedDelay(): Flow<Int> = flow {
    delay(1)
    emit(1)
}.flowOn(Dispatchers.Task)
