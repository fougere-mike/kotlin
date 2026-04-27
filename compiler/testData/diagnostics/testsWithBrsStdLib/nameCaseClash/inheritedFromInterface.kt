// Path B: user class implements an interface that provides a default `fun foo()`;
// user adds `fun FOO()` but does NOT override foo. foo is purely inherited from the interface.
// Expected: BRS_NAME_CASE_CLASH on the user-declared FOO function.

package test

interface Base {
    fun foo(): String = "default"
}

class UserClass : Base {
    fun <!BRS_NAME_CASE_CLASH!>FOO<!>(): Int = 1
}
