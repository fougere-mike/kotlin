// Expected: no diagnostic — RoArray, RoAssociativeArray, and Dynamic are whitelisted
// serializable types (they are native BrightScript types that survive Task boundaries).
import kotlin.coroutines.builders.withContext
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.Dynamic

suspend fun run() {
    val arr: RoArray = RoArray.create(0, true)
    val assoc: RoAssociativeArray = RoAssociativeArray.create()
    val dyn: Dynamic? = null
    withContext(Dispatchers.IO) {
        println(arr)
        println(assoc)
        println(dyn)
    }
}
