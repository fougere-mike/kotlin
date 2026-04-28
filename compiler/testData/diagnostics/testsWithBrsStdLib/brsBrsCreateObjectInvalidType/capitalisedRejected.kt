// Expected: BRS_BRSCREATEOBJECT_INVALID_TYPE — "RoArray" is rejected (case policy: exact canonical match).
import kotlin.brs.BrsCreateObject

external interface MyArray {
    companion object {
        @BrsCreateObject("RoArray")
        fun <!BRS_BRSCREATEOBJECT_INVALID_TYPE!>create<!>(): MyArray = definedExternally
    }
}
