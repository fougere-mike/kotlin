// The task lift, spawnTask form (flow-program spec §5, decision 3): a
// component awaiting spawnTask { <blocking work> } inside launch {}, with one
// hoisted capture. Pins:
// - the suspend-for-suspend call-site rewrite
//   (spawnTaskLifted("KotlinFlowTask_spawnTaskLift_1", <capturesAA>)),
// - the lifted block __spawnBlock_spawnTaskLift_1(captures) with the capture
//   re-declared from the AA and the block's value returned,
// - the synthesized component: six-field XML, extends="Task", __kotlinTaskMain
//   export, and run() handing m.top plus the block-forwarding lambda to
//   driveSpawnTask, and
// - the synthesized deps.json closing over the originating file + flow klib.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.coroutines.task.spawnTask

class SpawnTaskLift : GroupComponent() {
    @SGStringField
    var result: String = ""

    init {
        val base = "row"
        launch {
            val fetched = spawnTask { base + ":fetched" }
            result = fetched
        }
    }
}
