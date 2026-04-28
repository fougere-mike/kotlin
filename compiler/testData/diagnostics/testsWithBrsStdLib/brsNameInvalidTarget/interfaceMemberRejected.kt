// Expected: BRS_BRSNAME_INVALID_TARGET — interface members are abstract by default;
// no module-level BRS function is emitted for the abstract declaration.
import kotlin.brs.brsName

interface IHandler {
    fun onChange()
}

fun test() {
    val name = brsName(<!BRS_BRSNAME_INVALID_TARGET!>IHandler::onChange<!>)
}
