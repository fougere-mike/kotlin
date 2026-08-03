// Expected: BRS_TASK_STATE_NOT_FIELD on results — vals are flagged too (v1 flag-all decision):
// a val holding a mutable object loses run()-side mutations exactly like a var reassignment,
// and the safe (read-only-constant) and unsafe (mutated-in-run) cases can't be cheaply told apart
import kotlin.brs.TaskComponent

class AccumulateTask : TaskComponent() {
    val <!BRS_TASK_STATE_NOT_FIELD!>results<!> = mutableListOf<String>()

    override fun run() {
        results.add("done")
    }
}
