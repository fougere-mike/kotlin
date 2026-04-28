import kotlin.brs.BrsComponent
import kotlin.brs.SGStringField
import kotlin.brs.BrsOnChange

open class BaseComponent {
    @SGStringField
    @BrsOnChange("onTitleChanged")
    val title: String = ""

    fun onTitleChanged() {}
}

@BrsComponent
class InheritedOnChangeComponent : BaseComponent()
