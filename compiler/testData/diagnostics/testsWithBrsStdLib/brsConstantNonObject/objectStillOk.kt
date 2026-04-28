// Expected: no diagnostic — @BrsConstant on an object declaration is the legitimate use case.
import kotlin.brs.BrsConstant

@BrsConstant
object Config {
    val API_VERSION = 1
    val TIMEOUT_MS = 30 * 1000
}
