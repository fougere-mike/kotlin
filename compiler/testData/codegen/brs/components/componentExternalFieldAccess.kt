// Receiver-aware @SG*Field access on a NON-task component:
// self receiver -> m.top.field; any other instance -> <expr>.field,
// both inside the component and from top-level code.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class StatusCard : GroupComponent() {
    @SGStringField
    var label: String = ""

    fun refresh(other: StatusCard) {
        label = "self"
        other.label = label
        label = other.label
    }
}

fun syncLabels(a: StatusCard, b: StatusCard) {
    b.label = a.label
}
