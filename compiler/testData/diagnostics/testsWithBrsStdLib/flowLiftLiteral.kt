// BRS_FLOW_UPSTREAM_NOT_LITERAL — the task lift is a compile-time lowering
// (flow-program spec §5): at a flowOn(Dispatchers.Task) call site the compiler
// lifts the ENTIRE upstream chain expression into a per-call-site synthesized
// TaskComponent, and at a spawnTask call site it lifts the literal block. Only
// code literally visible at the call site can be lifted, so the flowOn receiver
// must be a LITERAL flow chain — every hop a flow-builder/operator call whose
// lambda arguments are literal lambdas, bottoming out at flow/flowOf/asFlow
// (combine continues through its Flow arguments) — and the spawnTask argument
// must be a literal lambda. A Flow-typed variable/parameter, a function call
// with an invisible body, or a stored lambda cannot be lifted. Suppressible:
// escapes hit the guided-throw runtime backstops in the un-lowered stubs.
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.asFlow
import kotlin.coroutines.flow.combine
import kotlin.coroutines.flow.filter
import kotlin.coroutines.flow.flow
import kotlin.coroutines.flow.flowOf
import kotlin.coroutines.flow.flowOn
import kotlin.coroutines.flow.map
import kotlin.coroutines.task.spawnTask

// Case 1: literal operator chain over a flow {} bottom — CLEAN
fun literalChain(): Flow<Int> =
    flow { emit(1) }
        .map { it * 2 }
        .filter { it > 0 }
        .flowOn(Dispatchers.Task)

// Case 2: the other builder bottoms — flowOf, asFlow — CLEAN
fun flowOfBottom(): Flow<Int> = flowOf(1, 2, 3).flowOn(Dispatchers.Task)

fun asFlowBottom(): Flow<Int> = listOf(1, 2).asFlow().flowOn(Dispatchers.Task)

// Case 3: combine — receiverless hop; the chain continues through its literal
// Flow arguments — CLEAN
fun combineBottom(): Flow<Int> =
    combine(flowOf(1), flowOf(2)) { a, b -> a + b }.flowOn(Dispatchers.Task)

// Case 4: Flow parameter flowOn'd — ERROR (its code is not visible at the call site)
fun parameterFlow(p: Flow<Int>): Flow<Int> =
    <!BRS_FLOW_UPSTREAM_NOT_LITERAL!>p<!>.flowOn(Dispatchers.Task)

// Case 5: Flow-typed local receiver — ERROR (declare the chain at the flowOn site)
fun storedFlow(): Flow<Int> {
    val base = flowOf(1)
    return <!BRS_FLOW_UPSTREAM_NOT_LITERAL!>base<!>.flowOn(Dispatchers.Task)
}

// Case 6: function-call receiver with an invisible body — ERROR
fun makeFlow(): Flow<Int> = flowOf(1)

fun calledFlow(): Flow<Int> =
    <!BRS_FLOW_UPSTREAM_NOT_LITERAL!>makeFlow()<!>.flowOn(Dispatchers.Task)

// Case 7: non-literal receiver deeper in the chain — the hop itself (map, literal
// lambda) is fine; the error lands on the opaque bottom — ERROR
fun midChain(p: Flow<Int>): Flow<Int> =
    <!BRS_FLOW_UPSTREAM_NOT_LITERAL!>p<!>.map { it + 1 }.flowOn(Dispatchers.Task)

// Case 8: whitelisted hop with a STORED lambda argument — ERROR (a function value
// has no body to lift)
fun storedLambda(): Flow<Int> {
    val transform: suspend (Int) -> Int = { it }
    return flowOf(1).map(<!BRS_FLOW_UPSTREAM_NOT_LITERAL!>transform<!>).flowOn(Dispatchers.Task)
}

// Case 9: combine with one non-literal Flow argument — ERROR on that argument
fun combineNonLiteral(p: Flow<Int>): Flow<Int> =
    combine(flowOf(1), <!BRS_FLOW_UPSTREAM_NOT_LITERAL!>p<!>) { a, b -> a + b }.flowOn(Dispatchers.Task)

// Case 10: spawnTask with a literal lambda — CLEAN
suspend fun spawnLiteral(): Int = spawnTask { 41 + 1 }

// Case 11: spawnTask with a stored function value — ERROR (spawnTask wording)
suspend fun spawnStored(): Int {
    val block = { 42 }
    return spawnTask(<!BRS_FLOW_UPSTREAM_NOT_LITERAL!>block<!>)
}

// Case 12: spawnTask with a function reference — ERROR (no literal body at the site)
fun answer(): Int = 42

suspend fun spawnRef(): Int = spawnTask(<!BRS_FLOW_UPSTREAM_NOT_LITERAL!>::answer<!>)

// Case 13: suppression escape — CLEAN (deliberate opt-in to the runtime backstop ISE)
@Suppress("BRS_FLOW_UPSTREAM_NOT_LITERAL")
fun deliberateOpaque(p: Flow<Int>): Flow<Int> = p.flowOn(Dispatchers.Task)
