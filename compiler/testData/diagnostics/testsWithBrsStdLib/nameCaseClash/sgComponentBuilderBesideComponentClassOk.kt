// R33 (spec 2026-09-04-component-lifecycle §6): the kotlin-roku plugin generates one
// typed layout builder per component as `fun LayoutBuilder.<lowerCamel>` beside the
// PascalCase component class — `badge` beside `Badge`, in the same package. The two
// source names are equal after lowercasing, but the runtime clash is not real: the
// class emits `Badge_*_k_` globals plus the XML component `Badge`; the builder emits
// the mangled global `badge_rLayoutBuilder_…_k_`. @SGComponentBuilder functions are
// therefore exempt from BRS_NAME_CASE_CLASH grouping in both directions (as the
// file's own member and as a package-scope peer of the class).
//
// The exemption is keyed on the annotation, not on the LayoutBuilder receiver: the
// un-annotated `chip` beside `Chip` below is the negative control and still clashes.
// Expected: no diagnostic on Badge/badge; BRS_NAME_CASE_CLASH on both Chip and chip.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.scenegraph.ComponentBuilder
import kotlin.brs.scenegraph.LayoutBuilder
import kotlin.brs.scenegraph.SGComponentBuilder

class Badge(@SGStringField val label: String) : GroupComponent()

@SGComponentBuilder("Badge")
fun LayoutBuilder.badge(id: String, label: String, visible: Boolean? = null, init: ComponentBuilder.() -> Unit = {}) {
    component("Badge", id = id, visible = visible) {
        attr("label", label)
        init()
    }
}

class <!BRS_NAME_CASE_CLASH!>Chip<!> : GroupComponent()

fun LayoutBuilder.<!BRS_NAME_CASE_CLASH!>chip<!>(id: String) {
    component("Chip", id = id)
}
