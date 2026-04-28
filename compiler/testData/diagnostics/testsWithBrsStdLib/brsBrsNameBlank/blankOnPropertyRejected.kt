// Expected: BRS_BRSNAME_BLANK — @BrsName("") on a class property.
import kotlin.brs.BrsName

class Container {
    @BrsName("")
    val <!BRS_BRSNAME_BLANK!>x<!> = 1
}
