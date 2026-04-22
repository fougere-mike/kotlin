// Test that a top-level class named `SubList` with a method `subList` in its
// body does NOT trigger BRS_NAME_CASE_CLASH on the class. The class is a
// file-level declaration and the method is a class-body declaration — they
// live in different scopes.
// Expected: no BRS diagnostics.

class SubList {
    fun subList(): Int = 0
}
