// Test basic @BrsConstant functionality

// Local annotation definition for test (mirrors kotlin.brs.BrsConstant)
package kotlin.brs
annotation class BrsConstant

@BrsConstant
object Config {
    val API_VERSION = 1
    val BASE_URL = "https://api.example.com"
    val TIMEOUT_MS = 30 * 1000
    val DEBUG = false
}

fun getApiVersion(): Int = Config.API_VERSION
fun getBaseUrl(): String = Config.BASE_URL
fun getTimeoutMs(): Int = Config.TIMEOUT_MS
fun isDebug(): Boolean = Config.DEBUG
