package spike.shared

// THE Q1d payload: a plain compiled Kotlin class. At runtime this is an
// roAssociativeArray whose method/accessor slots are function-valued keys
// (bump_k_, __get_counter, ...) — exactly the thing Task 3 proved gets
// stripped on every cross-component hop.
class SharedVm {
    var counter: Int = 0
    fun bump(): Int {
        counter += 1
        return counter
    }
}
