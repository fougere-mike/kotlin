// Path C: @Suppress("BRS_INHERITED_NAME_CASE_CLASH") at the class declaration silences the diagnostic.
// No diagnostic expected.

package test

interface InterfaceA {
    fun bar(): String = "a"
}

interface InterfaceB {
    fun BAR(): Int = 1
}

@Suppress("BRS_INHERITED_NAME_CASE_CLASH")
class UserClass : InterfaceA, InterfaceB
