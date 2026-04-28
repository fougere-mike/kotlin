// Expected: BRS_STATIC_INVALID_TARGET — nullable receiver. The receiver-name renderer
// suffixes "?" but the diagnostic still fires.
import kotlin.brs.BrsStatic

class MyClass

@BrsStatic
fun MyClass?.<!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
