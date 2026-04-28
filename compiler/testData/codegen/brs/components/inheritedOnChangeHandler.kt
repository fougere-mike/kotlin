import kotlin.brs.BrsComponent
import kotlin.brs.SGStringField
import kotlin.brs.BrsOnChange

open class BaseComponent {
    fun onTitleChanged() {}
}

@BrsComponent
class InheritedOnChangeComponent : BaseComponent() {
    @SGStringField
    @BrsOnChange("onTitleChanged")
    val title: String = ""
}
