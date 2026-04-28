import kotlin.brs.BrsComponent
import kotlin.brs.BrsExport

open class BaseComponent {
    @BrsExport
    fun handleTap() {}
}

@BrsComponent
class InheritedExportComponent : BaseComponent()
