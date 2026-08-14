// BRS_SCOPE_CAPTURE_UNMARSHALLABLE — ScopeHandle.run { } captures cross the component
// boundary BY COPY as plain data (BrsScopeRunBlockLowering marshals every free value of
// the block into an roAssociativeArray). Types outside the marshallable set (primitives,
// String, Dynamic, external interfaces) lose all behavior in that copy: function values
// die entirely, class instances — including the enclosing `this`, captured implicitly by
// any member access — survive only as method-less data husks. One report per captured
// value, at its first reference in the block.
import kotlin.brs.Dynamic
import kotlin.brs.GroupComponent
import kotlin.brs.ScopeHandle
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scopeHandleOf

data class CapBox(val value: Int)

// Case 1: marshallable captures (primitives, String, external interfaces, Dynamic) — CLEAN
suspend fun marshallableOk(
    owner: ScopeHandle,
    node: RoSGNode,
    aa: RoAssociativeArray,
    arr: RoArray,
    dyn: Dynamic,
): Int {
    val count = 3
    val label = "shelf"
    return owner.run {
        aa.addReplace(label, dyn)
        aa.addReplace("node", node)
        arr.count() + count
    }
}

// Case 2: kotlin collection capture — ERROR (methods die crossing; size read would crash owner-side)
suspend fun listCaptureRejected(owner: ScopeHandle): Int {
    val items: List<Int> = listOf(1, 2, 3)
    return owner.run { <!BRS_SCOPE_CAPTURE_UNMARSHALLABLE!>items<!>.size }
}

// Case 3: data-class capture — ERROR
suspend fun dataClassCaptureRejected(owner: ScopeHandle): Int {
    val box = CapBox(7)
    return owner.run { <!BRS_SCOPE_CAPTURE_UNMARSHALLABLE!>box<!>.value }
}

// Case 4: function-type capture — ERROR (function values cannot cross at all)
suspend fun functionCaptureRejected(owner: ScopeHandle): Int {
    val supplier: () -> Int = { 9 }
    return owner.run { <!BRS_SCOPE_CAPTURE_UNMARSHALLABLE!>supplier<!>() }
}

// Case 5: component-`this` capture — ERROR. The member call captures the component
// instance implicitly; it crosses as an m-AA husk and the method call dies owner-side.
class CaptureProbe : GroupComponent() {
    private fun helperValue(): Int = 7

    init {
        launch {
            val owner = scopeHandleOf(top)
            val viaMember = owner.run { <!BRS_SCOPE_CAPTURE_UNMARSHALLABLE!>helperValue()<!> }
            println("" + viaMember)
        }
    }
}

// Case 6: construction moved INSIDE the block — CLEAN (nothing crosses)
suspend fun constructionInsideOk(owner: ScopeHandle): Int {
    return owner.run {
        val local = listOf(4, 5)
        local.size
    }
}

// Case 7: suppression escape — CLEAN (deliberate husk opt-in)
@Suppress("BRS_SCOPE_CAPTURE_UNMARSHALLABLE")
suspend fun deliberateHusk(owner: ScopeHandle): Int {
    val box = CapBox(8)
    return owner.run { box.value }
}
