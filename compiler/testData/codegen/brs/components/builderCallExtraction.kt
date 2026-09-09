// A typed layout builder call is extracted by parameter name (spec §5.9b): the
// child XML carries id, the constructor input as an attribute, the standard
// attribute, and — because every required input is a constant — the ready
// marker attribute __kotlinInputsReady="true". The builder below is hand-written
// in the shape the kotlin-roku plugin (GenerateLayoutStubsTask) generates; the
// extractor keys on @SGComponentBuilder, never on the function's name. NOTE the
// name: a builder called `badge` next to `class Badge` in the same package is a
// BRS_NAME_CASE_CLASH error (top-level names equal after lowercasing), so a
// generated builder must not be the bare lowerCamel of its component class.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.scenegraph.ComponentBuilder
import kotlin.brs.scenegraph.LayoutBuilder
import kotlin.brs.scenegraph.SGComponentBuilder
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

class Badge(@SGStringField val label: String) : GroupComponent()

@SGComponentBuilder("Badge")
fun LayoutBuilder.badgeComponent(id: String, label: String, visible: Boolean? = null, init: ComponentBuilder.() -> Unit = {}) {
    component("Badge", id = id, visible = visible) {
        attr("label", label)
        init()
    }
}

class BadgeHost : GroupComponent() {
    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            badgeComponent(id = "hostBadge", label = "NEW", visible = true)
        }
    }
}
