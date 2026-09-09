// A typed layout builder call is extracted by parameter name (spec §5.9b): the
// child XML carries id, the constructor input as an attribute, the standard
// attribute, and — because every required input is a constant — the ready
// marker attribute __kotlinInputsReady="true". The builder below is hand-written
// in EXACTLY the shape the kotlin-roku plugin (GenerateLayoutStubsTask) generates:
// `fun LayoutBuilder.badge` — the bare lowerCamel of its component class — beside
// `class Badge` in the same package. The two names are equal after lowercasing,
// but @SGComponentBuilder functions are exempt from BRS_NAME_CASE_CLASH (R33):
// the class emits `Badge_*_k_` globals + the XML component `Badge`, the builder
// the mangled global `badge_rLayoutBuilder_…_k_` — distinct BRS identifiers. The
// extractor keys on @SGComponentBuilder, never on the function's name.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.scenegraph.ComponentBuilder
import kotlin.brs.scenegraph.LayoutBuilder
import kotlin.brs.scenegraph.SGComponentBuilder
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

class Badge(@SGStringField val label: String) : GroupComponent()

@SGComponentBuilder("Badge")
fun LayoutBuilder.badge(id: String, label: String, visible: Boolean? = null, init: ComponentBuilder.() -> Unit = {}) {
    component("Badge", id = id, visible = visible) {
        attr("label", label)
        init()
    }
}

class BadgeHost : GroupComponent() {
    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            badge(id = "hostBadge", label = "NEW", visible = true)
        }
    }
}
