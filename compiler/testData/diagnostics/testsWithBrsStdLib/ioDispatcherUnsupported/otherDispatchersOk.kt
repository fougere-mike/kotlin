// Expected: no diagnostics — only Dispatchers.IO is flagged; Main, Default,
// and Unconfined are honest about what they do.
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.dispatchers.Dispatchers

fun scopes(): List<CoroutineScope> = listOf(
    CoroutineScope(Dispatchers.Main),
    CoroutineScope(Dispatchers.Default),
    CoroutineScope(Dispatchers.Unconfined),
)
