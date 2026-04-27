// Path C: user class implements two interfaces that each contribute a concrete (non-abstract)
// member that case-collides with the other. The user declares neither clashing member —
// both come from base interfaces. With concrete (default) implementations the user doesn't
// need to provide overrides.
// Expected: BRS_INHERITED_NAME_CASE_CLASH on the user class declaration.

package test

interface InterfaceA {
    fun foo(): String = "a"
}

interface InterfaceB {
    fun FOO(): Int = 1
}

<!BRS_INHERITED_NAME_CASE_CLASH!>class UserClass<!> : InterfaceA, InterfaceB
