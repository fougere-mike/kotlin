// Expected: BRS_BRSCREATEOBJECT_INVALID_TYPE — empty string is not a valid type name.
import kotlin.brs.BrsCreateObject

external interface MyObj {
    companion object {
        @BrsCreateObject("")
        fun <!BRS_BRSCREATEOBJECT_INVALID_TYPE!>create<!>(): MyObj = definedExternally
    }
}
