// The condition of a do-while can legally reference variables declared in the
// loop BODY. The function-level `x` registers first, forcing collision
// renaming of the loop body's `x` (-> x_1): the emitted condition must read
// the RENAMED body variable, which requires the body to be transformed
// (registering unique names) BEFORE the condition. With condition-first
// ordering, the condition read falls back to the bare base name and silently
// reads the outer `x` instead.
fun doWhileConditionScope(seed: Int): Int {
    val x = seed + 10
    var total = x
    do {
        val x = total - seed
        total = total + 1
    } while (x < 0)
    return total
}
