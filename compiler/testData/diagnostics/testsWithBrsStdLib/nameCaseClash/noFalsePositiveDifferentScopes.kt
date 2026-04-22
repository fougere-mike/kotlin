// Test that a case-insensitive name match across DIFFERENT scopes (file top-level
// vs. class body) does NOT trigger BRS_NAME_CASE_CLASH. The two declarations
// resolve to distinct BrightScript identifiers (top-level `foo` vs.
// `m.Foo` on Container instances) — no collision.
// Expected: no BRS diagnostics.

val foo = 1

class Container {
    val Foo = 2
}
