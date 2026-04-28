// Expected: no diagnostic — canonical lowercase type names are valid.
import kotlin.brs.BrsCreateObject

external interface MyArray {
    companion object {
        @BrsCreateObject("roArray")
        fun create(): MyArray = definedExternally
    }
}

external interface MyMap {
    companion object {
        @BrsCreateObject("roAssociativeArray")
        fun create(): MyMap = definedExternally
    }
}
