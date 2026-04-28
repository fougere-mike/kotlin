// Expected: BRS_STATIC_INVALID_TARGET — extension on an `object` is no safer; the
// extension function still lives at file top level and codegen drops the receiver.
// Distinct from the valid case `object MyObject { @BrsStatic fun bar() {} }`.
import kotlin.brs.BrsStatic

object MyObject

@BrsStatic
fun MyObject.<!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
