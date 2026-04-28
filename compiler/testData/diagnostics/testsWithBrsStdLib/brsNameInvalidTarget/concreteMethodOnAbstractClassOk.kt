// Expected: no diagnostic — a concrete (non-abstract) member of an abstract class
// produces a stable module-level BRS function via getBrsName().
import kotlin.brs.brsName

abstract class BaseHandler {
    abstract fun handle()
    fun helper() {}
}

fun test() {
    val name = brsName(BaseHandler::helper)
}
