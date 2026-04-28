import kotlin.brs.BrsComponent
import kotlin.brs.SGStringField

open class BaseComponent {
    @SGStringField
    open val title: String = ""
}

@BrsComponent
class OverriddenFieldComponent : BaseComponent() {
    override val title: String = "default"
}
