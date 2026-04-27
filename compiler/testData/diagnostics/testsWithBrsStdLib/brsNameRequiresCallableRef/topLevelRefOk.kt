// Expected: no diagnostic — top-level function reference
import kotlin.brs.brsName

fun topLevelFun() {}

fun test() {
    val name = brsName(::topLevelFun)
}
