// Expected: BRS_BRSCREATEOBJECT_INVALID_TYPE — "roarray" is rejected (canonical form is "roArray").
import kotlin.brs.BrsCreateObject

external interface MyArray {
    companion object {
        @BrsCreateObject("roarray")
        fun <!BRS_BRSCREATEOBJECT_INVALID_TYPE!>create<!>(): MyArray = definedExternally
    }
}
