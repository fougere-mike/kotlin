// Component constructor calls are lowered (spec §5.7): create the node, write
// each constructor input onto it, write the ready marker LAST, yield the handle
// (typed as the class — the createComponent<T>() contract). Zero-arg calls on
// any concrete component kind are a bare CreateObject.
//
// Every input write must be a PLAIN node-field set on the created handle
// (`n.airingId = id`), never the component-self `.top` route — in EVERY position
// the lowered block can land in, because codegen has two visitSetField sites
// (expression vs statement position) that share one routing decision.
import kotlin.brs.GroupComponent
import kotlin.brs.ContentNodeComponent
import kotlin.brs.SGStringField
import kotlin.brs.SGIntegerField
import kotlin.brs.SGNodeField
import kotlin.brs.awaitReady
import kotlin.brs.roku.RoSGNode

class DetailsScreen(
    @SGStringField val airingId: String,
    @SGIntegerField val row: Int,
) : GroupComponent()

class VideoItem : ContentNodeComponent()

// A node-typed input: the nested case constructs its argument inline.
class Outer(@SGNodeField val child: RoSGNode?) : GroupComponent()

class Navigator : GroupComponent() {
    private var held: DetailsScreen? = null

    // Local initializer in a plain body — the EXPRESSION-site visitSetField path.
    fun open(id: String, row: Int): RoSGNode {
        val screen = DetailsScreen(id, row)
        val item = VideoItem()
        return screen.unsafeCast<RoSGNode>()
    }

    // (a) argument position and property-write position — STATEMENT-site paths.
    fun attach(id: String, row: Int) {
        top.appendChild(DetailsScreen(id, row).unsafeCast<RoSGNode>())
        held = DetailsScreen(id, row)
    }

    // (b) nested: the inner call's block is hoisted ahead of the outer input write;
    // the per-call-site temp names keep the outer handle intact.
    fun nest(id: String, row: Int): RoSGNode {
        val outer = Outer(DetailsScreen(id, row).unsafeCast<RoSGNode>())
        return outer.unsafeCast<RoSGNode>()
    }

    // (c) THE flagship shape: a local constructed, then live across a suspension.
    // BrsLiveLocalsTransformer hoists it to a coroutine field, so the initializer
    // block becomes the VALUE of an IrSetField — statement-site path inside the
    // state machine. Both the suspend-member and the launch-lambda forms.
    suspend fun openAsync(id: String, row: Int): RoSGNode {
        val screen = DetailsScreen(id, row)
        awaitReady()
        top.appendChild(screen.unsafeCast<RoSGNode>())
        return screen.unsafeCast<RoSGNode>()
    }

    fun openLater(id: String, row: Int) {
        launch {
            val screen = DetailsScreen(id, row)
            awaitReady()
            top.appendChild(screen.unsafeCast<RoSGNode>())
        }
    }

    // (d) discarded statement position: the block's trailing handle read is dropped.
    fun fireAndForget(id: String, row: Int) {
        DetailsScreen(id, row)
    }
}
