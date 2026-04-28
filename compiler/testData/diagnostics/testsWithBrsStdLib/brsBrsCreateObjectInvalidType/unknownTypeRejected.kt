// Expected: BRS_BRSCREATEOBJECT_INVALID_TYPE — "roWidget" is not a known type.
import kotlin.brs.BrsCreateObject

external interface MyWidget {
    companion object {
        @BrsCreateObject("roWidget")
        fun <!BRS_BRSCREATEOBJECT_INVALID_TYPE!>create<!>(): MyWidget = definedExternally
    }
}
