// Test that a class's primary constructor does NOT participate in case-clash
// grouping with same-lowercase methods inside its body. Constructors are
// invoked through the class name itself at BrightScript runtime — they don't
// have independent identities that can clash with sibling declarations.
//
// Specifically: `class Foo { fun foo() }` must NOT trip BRS_NAME_CASE_CLASH
// because the primary constructor's FIR symbol name is `Foo`, not `<init>`,
// and without the constructor filter our lowercase-grouping would put `Foo`
// (constructor) and `foo` (method) in the same group.
// Expected: no BRS diagnostics.

class Foo {
    fun foo() {}
}
