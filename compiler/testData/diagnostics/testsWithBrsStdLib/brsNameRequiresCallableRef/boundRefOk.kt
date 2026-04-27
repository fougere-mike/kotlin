// Expected: no diagnostic — bound receiver reference (this::method)
import kotlin.brs.brsName

class MyComponent {
    fun onChanged() {}

    fun setup() {
        val name = brsName(this::onChanged)
    }
}
