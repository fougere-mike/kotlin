// Path C negative: user class inherits from two interfaces, but all inherited members in each
// lowercase bucket have IDENTICAL effective names (same casing). No clash — identical names
// are overloads, handled upstream by REDECLARATION/CONFLICTING_OVERLOADS, not by this checker.
// No diagnostic expected.

package test

interface InterfaceA {
    fun bar(): String = "a"
}

interface InterfaceB {
    fun bar(): Int = 1
}

// Both interfaces provide `bar` with the same casing — same-name overloads, not case clash.
// Even though this may be a redeclaration conflict, BRS_INHERITED_NAME_CASE_CLASH does NOT fire
// (the checker only reports when distinctNames.size >= 2 with different casings).
class UserClass : InterfaceA, InterfaceB {
    override fun bar(): String = "override"
}
