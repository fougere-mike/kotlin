// Expected: no diagnostic — @Suppress silences BRS_STATIC_INVALID_TARGET.
import kotlin.brs.BrsStatic

class Foo {
    @Suppress("BRS_STATIC_INVALID_TARGET")
    @BrsStatic
    fun bar() {}
}
