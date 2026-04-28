// Expected: no diagnostic — @Suppress silences BRS_BRSNAME_BLANK.
import kotlin.brs.BrsName

@Suppress("BRS_BRSNAME_BLANK")
@BrsName("")
fun foo() {}
