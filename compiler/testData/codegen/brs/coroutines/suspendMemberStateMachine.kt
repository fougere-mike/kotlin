// Suspend MEMBER function of a plain class whose body needs a real state
// machine (non-tail suspend call) and reads `this` state after entry — the
// VM-facade shape (ScopeVmFixture.echoLowered). Pins the COROUTINE class
// constructor's captured-instance parameter handling: the ctor parameter is
// literally named `<this>` (a moved receiver), and its store must emit
// `this.__this = __this` — NOT the self-alias `this.__this = this` that the
// constructor-body `<this>` rendering rule produced before the 2026-08-14 fix
// (the coroutine then aliased ITSELF as the instance, and every dispatch on
// it crashed with "Member function not found", &hf4, on device).
suspend fun fetchNumber(x: Int): Int {
    return x + 1
}

class TaggedFetcher(private val label: String) {
    suspend fun tagged(x: Int): String {
        val n = fetchNumber(x)
        return label + n
    }
}
