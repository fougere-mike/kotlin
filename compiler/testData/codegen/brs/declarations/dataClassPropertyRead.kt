// Pins the shape BRS_SCOPE_RESULT_NOT_DATA's WARNING severity depends on (Task 6
// Step 1 verification, 2026-08-14): simple `val` property access on a data class
// compiles to a DIRECT member read (`p.x` — a plain AA key read), not a getter call.
// Data-class properties are AA keys; only methods are function-pointer slots. A scope
// request result that crossed the boundary as a method-less data husk therefore stays
// readable as data — which is why RESULT_NOT_DATA warns (share behavior via the VM)
// instead of erroring. If this golden ever shows a getter CALL here, that severity
// decision must be revisited.
data class ProbePoint(val x: Int, val y: Int)

fun readX(p: ProbePoint): Int {
    return p.x
}

fun readBoth(p: ProbePoint): Int {
    val a = p.x
    val b = p.y
    return a + b
}
