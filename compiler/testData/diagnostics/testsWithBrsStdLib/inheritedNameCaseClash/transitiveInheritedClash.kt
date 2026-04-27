// Path B + Path C: transitive hierarchy. BaseA declares concrete `fun foo()`.
// MidClass extends BaseA and adds `fun FOO()` — MidClass itself gets Path B (own FOO vs inherited foo).
// UserClass extends MidClass but declares neither. Both foo and FOO are inherited.
// Expected:
//   BRS_NAME_CASE_CLASH on MidClass.FOO (Path B: own-declared FOO clashes with inherited foo)
//   BRS_INHERITED_NAME_CASE_CLASH on UserClass (Path C: inherits both foo and FOO)

package test

open class BaseA {
    fun foo(): String = "a"
}

open class MidClass : BaseA() {
    fun <!BRS_NAME_CASE_CLASH!>FOO<!>(): Int = 1
}

<!BRS_INHERITED_NAME_CASE_CLASH!>class UserClass<!> : MidClass()
