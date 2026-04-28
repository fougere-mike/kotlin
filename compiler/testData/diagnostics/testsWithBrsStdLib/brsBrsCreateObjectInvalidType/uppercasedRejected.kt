// Expected: BRS_BRSCREATEOBJECT_INVALID_TYPE — "ROARRAY" is rejected (exact canonical match required).
import kotlin.brs.BrsCreateObject

external interface MyArray {
    companion object {
        @BrsCreateObject("ROARRAY")
        fun <!BRS_BRSCREATEOBJECT_INVALID_TYPE!>create<!>(): MyArray = definedExternally
    }
}
