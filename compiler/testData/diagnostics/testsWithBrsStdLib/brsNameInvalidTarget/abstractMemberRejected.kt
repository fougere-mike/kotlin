// Expected: BRS_BRSNAME_INVALID_TARGET — explicit `abstract fun` in an abstract class.
import kotlin.brs.brsName

abstract class BaseHandler {
    abstract fun handle()
}

fun test() {
    val name = brsName(<!BRS_BRSNAME_INVALID_TARGET!>BaseHandler::handle<!>)
}
