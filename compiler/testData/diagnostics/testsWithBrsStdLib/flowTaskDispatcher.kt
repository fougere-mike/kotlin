// BRS_FLOW_ON_INVALID_DISPATCHER — Dispatchers.Task is a compile-time token
// selecting the task lift: the lowering is chosen at compile time, so flowOn
// requires the LITERAL token at the call site (a runtime-chosen dispatcher
// cannot select a lowering), and the token is legal NOWHERE else (it is not a
// runtime dispatcher; its dispatch() is a guided-throw backstop). The rule
// fires in both directions: (a) a flowOn argument that is not literally
// Dispatchers.Task, and (b) a Dispatchers.Task reference outside flowOn-argument
// position. Dispatchers.IO in flowOn position additionally keeps its own
// blanket BRS_IO_DISPATCHER_UNSUPPORTED error — the two checkers stay
// independent (spec decisions 3/4/10).
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.builders.launch
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.CoroutineDispatcher
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.Flow
import kotlin.coroutines.flow.flowOf
import kotlin.coroutines.flow.flowOn

// CLEAN: the literal token in flowOn-argument position is the one legal shape.
fun clean(): Flow<Int> = flowOf(1, 2, 3).flowOn(Dispatchers.Task)

// flowOn with Dispatchers.IO: the flowOn rule fires on the non-Task argument,
// and the IO blanket rule fires on the same reference.
fun ioArgument(): Flow<Int> =
    flowOf(1).flowOn(<!BRS_FLOW_ON_INVALID_DISPATCHER!><!BRS_IO_DISPATCHER_UNSUPPORTED!>Dispatchers.IO<!><!>)

// flowOn with a variable holding the token: the aliasing site gets the reversed
// direction (Task outside flowOn-argument position), and the flowOn argument
// gets the literal-requirement direction — an alias cannot choose a
// compile-time lowering.
fun aliased(): Flow<Int> {
    val d: CoroutineDispatcher = <!BRS_FLOW_ON_INVALID_DISPATCHER!>Dispatchers.Task<!>
    return flowOf(1).flowOn(<!BRS_FLOW_ON_INVALID_DISPATCHER!>d<!>)
}

// flowOn(wrap(Dispatchers.Task)): deeper nesting is not a literal flowOn
// argument — the position half fires on the token (its direct enclosing call is
// wrap, not flowOn), and the argument half fires on the wrap(...) argument.
fun wrap(d: CoroutineDispatcher): CoroutineDispatcher = d

fun wrapped(): Flow<Int> =
    flowOf(1).flowOn(<!BRS_FLOW_ON_INVALID_DISPATCHER!>wrap(<!BRS_FLOW_ON_INVALID_DISPATCHER!>Dispatchers.Task<!>)<!>)

// launch(Dispatchers.Task): not a flowOn argument — the token is not a runtime
// dispatcher and cannot be launched on.
fun launched(scope: CoroutineScope) {
    scope.launch(<!BRS_FLOW_ON_INVALID_DISPATCHER!>Dispatchers.Task<!>) { }
}

// withContext(Dispatchers.Task): not a flowOn argument — one-shot background
// blocks go through spawnTask, not withContext.
suspend fun contextual(): Int =
    withContext(<!BRS_FLOW_ON_INVALID_DISPATCHER!>Dispatchers.Task<!>) { 42 }

// Suppressed: the error is suppressible (deliberate escapes hit the
// TaskTokenDispatcher guided-throw runtime backstop instead).
@Suppress("BRS_FLOW_ON_INVALID_DISPATCHER")
fun suppressed(): CoroutineDispatcher = Dispatchers.Task
