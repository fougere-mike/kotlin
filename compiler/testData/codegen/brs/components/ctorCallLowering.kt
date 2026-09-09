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
        return screen.unsafeCast<RoSGNode>()
    }
}
