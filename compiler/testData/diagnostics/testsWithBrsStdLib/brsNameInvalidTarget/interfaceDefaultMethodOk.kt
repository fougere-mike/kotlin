// Expected: no diagnostic — a default-implemented interface method is concrete and
// emits a module-level BRS function. Only `abstract` interface members are rejected.
import kotlin.brs.brsName

interface IConfig {
    fun describe(): String = "default"
}

fun test() {
    val name = brsName(IConfig::describe)
}
