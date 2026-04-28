// Expected: BRS_STATIC_INVALID_TARGET — @BrsStatic on a top-level extension whose
// receiver is a regular class. Codegen would emit a bare BRS function name with
// no receiver disambiguator, silently colliding with any other `bar` extension.
import kotlin.brs.BrsStatic

class MyClass

@BrsStatic
fun MyClass.<!BRS_STATIC_INVALID_TARGET!>bar<!>() {}
