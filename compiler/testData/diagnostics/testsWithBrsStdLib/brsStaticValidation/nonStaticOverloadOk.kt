// Expected: no diagnostic — overloads with NEITHER function annotated @BrsStatic
// are not the checker's concern. Sanity check: the checker only fires when at
// least two @BrsStatic-annotated functions share a name in the same scope.

fun format(value: String): String = value
fun format(value: Int): String = value.toString()
