package spike.probe

import kotlin.brs.roku.RoDeviceInfo
import kotlin.brs.scenegraph.LayoutDirection
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout
import spike.shared.lookupOf
import spike.shared.valueOrInvalid

// Declares SpikeOwner + SpikeChild via @SGLayout, then wires the node handles
// and fires the probes. Both children's observers/handlers are armed in their
// own inits, which run when the layout instantiates them — before this init's
// writes (arming-order law).
class SpikeScene : SceneComponent() {

    private val layout = SpikeScene_Layout(top)

    init {
        val di = RoDeviceInfo.create()
        // getOSVersion(), not getVersion(): the latter is deprecated and
        // returns a placeholder ("999.99E99999A" on this device).
        val osv = di.getOSVersion()
        println("[SPIKE] BEGIN model=${di.getModel()} os=${valueOrInvalid(lookupOf(osv, "major"))}.${valueOrInvalid(lookupOf(osv, "minor"))}.${valueOrInvalid(lookupOf(osv, "revision"))} build=${valueOrInvalid(lookupOf(osv, "build"))}")
        val owner = layout.spike_owner
        val child = layout.spike_child
        owner.setField("childNode", child)
        child.setField("ownerNode", owner)
        child.setField("runProbes", true)
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            layoutGroup(id = "spikeRoot", layoutDirection = LayoutDirection.horiz) {
                component("SpikeOwner", id = "spike_owner")
                component("SpikeChild", id = "spike_child")
            }
        }
    }
}
