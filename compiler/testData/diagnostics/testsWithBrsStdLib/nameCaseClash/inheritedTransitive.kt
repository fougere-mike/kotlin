// Path B: three-level hierarchy: GrandBase → MidClass → UserClass.
// The clash is between a user-declared member and a grandparent member.
// Expected: BRS_NAME_CASE_CLASH on the user-declared member.

package test

abstract class GrandBase {
    open fun process(): Unit {}
}

abstract class MidClass : GrandBase() {
    fun helper(): Unit {}
}

class UserClass : MidClass() {
    fun <!BRS_NAME_CASE_CLASH!>Process<!>(): Int = 1
}
