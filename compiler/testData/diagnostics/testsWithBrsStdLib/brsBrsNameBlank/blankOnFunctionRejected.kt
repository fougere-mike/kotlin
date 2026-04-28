// Expected: BRS_BRSNAME_BLANK — @BrsName("") on a top-level function.
import kotlin.brs.BrsName

@BrsName("")
fun <!BRS_BRSNAME_BLANK!>foo<!>() {}
