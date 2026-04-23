// Expected: no diagnostic — subtype of RoSGNode is accepted by @SGNodeField
import kotlin.brs.SGNodeField
import kotlin.brs.roku.RoSGNode

class MyNode : RoSGNode

class MyComponent {
    @SGNodeField
    var x: MyNode? = null
}
