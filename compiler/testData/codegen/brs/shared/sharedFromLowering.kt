// sharedFrom<T> reified lowering — locks the acquire-side rewrite:
// (1) all four overloads rewrite to sharedAcquire(node, key, "<ClassName>", orNull)
//     with the class name resolved by the compiler (the same naming as __proto
//     heads and is-check type names),
// (2) keyless overloads derive the default key = the class name (the key and
//     typeName arguments are the same string constant), keyed overloads pass the
//     caller's key expression through unchanged,
// (3) shareOn is NOT rewritten (publish derives its default key from the runtime
//     __proto head instead — no reified type argument to resolve).
import kotlin.brs.SharedService
import kotlin.brs.shareOn
import kotlin.brs.sharedFrom
import kotlin.brs.sharedFromOrNull
import kotlin.brs.roku.RoSGNode

class GuideVm : SharedService() {
    var selectedDay: Int = 0
}

fun publish(node: RoSGNode, vm: GuideVm) {
    shareOn(node, vm)
}

fun acquireAll(node: RoSGNode): Int {
    val byType = sharedFrom<GuideVm>(node)
    val byKey = sharedFrom<GuideVm>(node, "guide")
    val orNullByType = sharedFromOrNull<GuideVm>(node)
    val orNullByKey = sharedFromOrNull<GuideVm>(node, "guide")
    var total = byType.selectedDay + byKey.selectedDay
    if (orNullByType != null) {
        total = total + orNullByType.selectedDay
    }
    if (orNullByKey != null) {
        total = total + orNullByKey.selectedDay
    }
    return total
}
