// A plain class property that merely SHARES a component-scope property name
// (`top`) and is read from INSIDE a component method must keep ordinary
// accessor emission (marker.__get_top()) — never be hijacked to m.top. Locks
// the resolved-parent guard (isComponentBaseScopeGetter) on the component-
// scope special case: before the 2026-08-14 tightening, the in-component gate
// keyed on the NAME alone (any getter named top/global/m, any receiver), so
// this read emitted m.top — the component's node instead of the value.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class TopNamedHolder(val top: String) {
    val globalTag: String
        get() = "tag:" + top
}

class PlainTopNameNoHijack : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        val marker = TopNamedHolder("plain")
        status = marker.top + ":" + marker.globalTag
    }
}
