// arrayOf / arrayOfNulls compilation shape.
// kotlin.arrayOf and kotlin.arrayOfNulls resolve from builtins metadata and have no BRS
// source implementation - the compiler intrinsifies them:
//   arrayOf(a, b, c) -> [a, b, c]
//   arrayOf()        -> []
//   arrayOfNulls(n)  -> __kotlin_arrayOfNulls(n)
// (Previously they compiled to arrayOf_Arr_k_ / arrayOfNulls_I_k_, which were defined
// nowhere in any packaged .brs - a guaranteed device crash on first call.)
fun makeInts(): Array<Int> {
    return arrayOf(1, 2, 3)
}

fun makeEmpty(): Array<String> {
    return arrayOf()
}

fun makeSlots(): Array<Any?> {
    return arrayOfNulls(4)
}

fun totalSize(): Int {
    return makeInts().size + makeEmpty().size + makeSlots().size
}
