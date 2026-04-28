// Expected: no diagnostic — non-blank @BrsName arguments are valid.
import kotlin.brs.BrsName

@BrsName("validName")
fun fooKotlin() {}

@BrsName("a")
class FooClass

@BrsName("longerNameWithDigits123")
fun bar() {}
