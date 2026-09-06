// The compiler-synthesized onStart driver (spec 2026-09-04-component-lifecycle §5.3):
// a class whose hierarchy overrides onStart gets a `__kotlinStartDriver` member
// (`if (!kotlinLifecycleClaimDriver()) return; launch { awaitReady(); onStart() }`)
// and an init-tail call to it. A concrete base + concrete leaf BOTH emit one (the
// claim makes the second a no-op at runtime); a component that never overrides
// onStart gets nothing.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

open class StartBase : GroupComponent() {
    @SGStringField
    var seen: String = ""

    override suspend fun onStart() {
        seen = "base"
    }
}

class StartLeaf : StartBase() {
    override suspend fun onStart() {
        seen = "leaf"
    }
}

class StartInherits : StartBase()

class NoStart : GroupComponent() {
    @SGStringField
    var label: String = ""
}
