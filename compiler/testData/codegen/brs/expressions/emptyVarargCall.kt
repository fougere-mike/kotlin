// Empty-vararg call sites. A vararg argument absent at the call site has no
// callee-side default: the callee must receive the EMPTY ARRAY [], never
// invalid (joinParts() previously compiled to joinParts_Arr_k_(invalid), so
// the callee's first index/dot into the parameter was a device crash).
// Object varargs only: primitive varargs (e.g. vararg Int -> IntArray) have a
// separate pre-existing call-site/consumption protocol mismatch.
fun joinParts(vararg parts: String): String {
    var out = ""
    for (p in parts) {
        out = out + p
    }
    return out
}

fun callBoth(): String {
    return joinParts() + joinParts("a", "b")
}
