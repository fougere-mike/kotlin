// Expected: no diagnostic — literal-required-narrow policy: dynamic (non-constant) type arg is silently skipped
import kotlin.brs.roku.RoSGNode

fun computeType(): String = "string"

fun test(node: RoSGNode) {
    node.addField("x", computeType(), false)
}
