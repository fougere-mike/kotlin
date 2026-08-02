// Deep transitive dependency test - locks the component <script> include closure.
// Exercises every dependency-recording path fixed in Phase 1b:
//   - HashMap + String.hashCode()  -> hash machinery incl. __kotlin_stringHashCode helper
//   - StringBuilder                -> StringBuilderBrsKt
//   - cross-file enum entry access -> CharCategory_initEntries (CharCategoryBrsKt)
//   - Char/Int ranges              -> corrected primitive-extension names (RangeOperatorsBrsKt)
//                                     + transitive PrimitiveIteratorsKt via the klib file-deps graph
//   - stdlib superclass constructor -> Exception create chain (ExceptionsKt)
//   - unbound function reference + brsName intrinsic
//   - arrayOf / arrayOfNulls intrinsics
import kotlin.brs.BrsComponent
import kotlin.brs.SGStringField
import kotlin.brs.brsName
import kotlin.text.CharCategory
import kotlin.text.StringBuilder

class DeepDepsError(message: String) : Exception(message)

fun describeChar(c: Char): String {
    return if (c.category == CharCategory.LOWERCASE_LETTER) "lower" else "other"
}

fun sumRange(): Int {
    var total = 0
    for (i in 1..5) {
        total += i
    }
    return total
}

fun onTick(value: Int): Int = value + 1

@BrsComponent
class DeepDepsProbe {
    @SGStringField
    val summary: String = ""

    fun compute(): String {
        val counts = HashMap<String, Int>()
        counts["alpha"] = 1
        counts["beta"] = 2
        val sb = StringBuilder()
        for (c in 'a'..'c') {
            sb.append(describeChar(c))
        }
        sb.append(counts.size)
        sb.append(sumRange())
        sb.append("alpha".hashCode())
        val names = arrayOf("x", "y")
        val slots = arrayOfNulls<Any>(3)
        sb.append(names.size + slots.size)
        val tick = ::onTick
        sb.append(tick(41))
        sb.append(brsName(::onTick))
        if (sb.length == 0) {
            throw DeepDepsError("empty summary")
        }
        return sb.toString()
    }
}
