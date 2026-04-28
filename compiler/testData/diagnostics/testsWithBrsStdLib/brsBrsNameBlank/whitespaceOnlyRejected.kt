// Expected: BRS_BRSNAME_BLANK — @BrsName with whitespace-only strings.
import kotlin.brs.BrsName

@BrsName("   ")
fun <!BRS_BRSNAME_BLANK!>spaces<!>() {}

@BrsName("\t\n")
fun <!BRS_BRSNAME_BLANK!>tabs<!>() {}
