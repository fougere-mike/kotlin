// Expected: no diagnostic — @BrsComponent on the class itself makes it a component even
// without a @BrsSceneGraphComponent base (mirrors BrsComponentExtractor.isComponent)
import kotlin.brs.BrsComponent
import kotlin.brs.ComponentBase
import kotlin.brs.createComponent

@BrsComponent(extends = "Group")
class CustomWidget : ComponentBase()

fun make(): CustomWidget = createComponent<CustomWidget>()
