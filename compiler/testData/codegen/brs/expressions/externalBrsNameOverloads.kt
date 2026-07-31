// External-interface members must emit plain BrightScript method names:
// @BrsName overloads (the port forms of observeField/observeFieldScoped) emit
// the annotation's name, and native overloads (callFunc arities) emit the
// shared simple name — no _k_ suffix, no signature mangling in either case.

import kotlin.brs.Dynamic
import kotlin.brs.roku.ISGNodeField
import kotlin.brs.roku.RoMessagePort
import kotlin.brs.roku.RoSGNode

fun observePortForms(node: RoSGNode, port: RoMessagePort): Boolean {
    node.observeFieldPort("echo", port)
    return node.observeFieldScopedPort("input", port)
}

fun observeViaInterface(field: ISGNodeField, port: RoMessagePort): Boolean {
    return field.observeFieldPort("echo", port)
}

fun callFuncForms(node: RoSGNode): Dynamic? {
    node.callFunc("reset")
    return node.callFunc("setValue", node.getField("value"))
}
