// Suspend function REFERENCES as function values (device-pinned defect, Suite 8
// ScopeHandle bring-up): a suspend ::ref becomes a FUNCTION_REFERENCE_IMPL class
// WITHOUT the CoroutineImpl create/doResume treatment that LAMBDA_IMPL classes
// get — no create_..._k_ factory — so every starter that dispatches through
// create (startScopeHandlerN, startCoroutine) crashed at the first dispatch:
// "Member function not found" (&hf4) at ScopeHostImplKt.brs:294.
// This golden pins the generated class shape for a top-level ref and a
// bound-receiver member ref.
suspend fun echoUpper(s: String): String {
    return s + "!"
}

fun topLevelHandler(): suspend (String) -> String = ::echoUpper

class RefHolder {
    private suspend fun member(s: String): String {
        return s + "?"
    }

    fun boundHandler(): suspend (String) -> String = ::member
}
