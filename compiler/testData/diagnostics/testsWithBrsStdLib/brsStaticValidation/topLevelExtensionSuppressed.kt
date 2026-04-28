// Expected: no diagnostic — @Suppress silences BRS_STATIC_INVALID_TARGET. The new
// FirSimpleFunctionChecker uses the framework's automatic annotation-context push, so
// no manual isSuppressedByAnnotation call is needed (same pattern as
// FirBrsIntrinsicUserDefinedChecker).
import kotlin.brs.BrsStatic

class MyClass

@Suppress("BRS_STATIC_INVALID_TARGET")
@BrsStatic
fun MyClass.bar() {}
