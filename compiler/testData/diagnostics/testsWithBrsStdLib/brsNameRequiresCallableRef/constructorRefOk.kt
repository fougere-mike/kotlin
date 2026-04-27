// Expected: no diagnostic — constructor reference (::SomeClass)
// IR handles IrConstructor via generateBrsFunctionName; this is a valid use case.
import kotlin.brs.brsName

class SomeClass

fun test() {
    val name = brsName(::SomeClass)
}
