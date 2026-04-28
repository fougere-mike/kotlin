// NOTE: The onChange attribute in the generated XML currently emits
// "InheritedOnChangeComponent_onTitleChanged_k_" (subclass-prefixed) even though
// the actual BrightScript function is "BaseComponent_onTitleChanged_k_" (base-class-prefixed).
// This is a known name-caching issue in getBrsName. The golden captures current output;
// fix the mismatch in a follow-up before expecting this to work on a real Roku device.
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
