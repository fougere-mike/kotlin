import kotlin.brs.BrsComponent
import kotlin.brs.BrsField
import kotlin.brs.SGIntegerField

open class BaseComponent {
    @BrsField(type = "string")
    open val score: Int = 0
}

@BrsComponent
class OverriddenAnnotationComponent : BaseComponent() {
    @SGIntegerField
    override val score: Int = 0
}
