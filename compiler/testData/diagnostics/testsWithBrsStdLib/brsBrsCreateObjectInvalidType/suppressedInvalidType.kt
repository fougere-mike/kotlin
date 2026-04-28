// Expected: no diagnostic — @Suppress silences BRS_BRSCREATEOBJECT_INVALID_TYPE.
import kotlin.brs.BrsCreateObject

external interface MyObj {
    companion object {
        @Suppress("BRS_BRSCREATEOBJECT_INVALID_TYPE")
        @BrsCreateObject("roWidget")
        fun create(): MyObj = definedExternally
    }
}
