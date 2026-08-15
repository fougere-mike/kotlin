// Component include closure must fold PROJECT files' recorded deps transitively.
// This file compiles BEFORE HelperDepsVm.kt (the harness passes files sorted by
// name) — the order that used to drop the VM's own dependencies: deps.json was
// generated mid-transform, before HelperDepsVm.kt had recorded its deps, so the
// transitive walk saw HelperDepsVmKt.brs as a leaf. The pinned entry is
// ScopesKt.brs (coroutineScope) — reachable ONLY through the VM file; it was
// absent from deps.json and the XML <script> includes before the Pass 3 split.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class HelperDepsScreen : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        launch {
            status = HelperDepsVm().load()
        }
    }
}
