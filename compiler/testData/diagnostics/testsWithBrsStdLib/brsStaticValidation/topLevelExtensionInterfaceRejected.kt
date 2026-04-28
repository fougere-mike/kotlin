// Expected: BRS_STATIC_INVALID_TARGET — extension on an interface receiver.
import kotlin.brs.BrsStatic

interface MyInterface

@BrsStatic
fun MyInterface.<!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
