import kotlin.brs.BrsComponent
import kotlin.brs.SGStringField

open class BaseComponent {
    @SGStringField
    val title: String = ""
}

@BrsComponent
class InheritedFieldComponent : BaseComponent()
