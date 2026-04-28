// Expected: no diagnostic — @Suppress silences BRS_BRSNAME_INVALID_TARGET at the call site.
import kotlin.brs.brsName

@Suppress("BRS_BRSNAME_INVALID_TARGET")
fun test() {
    fun inner() {}
    val name = brsName(::inner)
}
