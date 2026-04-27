// Expected: no diagnostic — qualified class method reference
import kotlin.brs.brsName

class MyClass {
    fun handler() {}
}

fun test() {
    val name = brsName(MyClass::handler)
}
