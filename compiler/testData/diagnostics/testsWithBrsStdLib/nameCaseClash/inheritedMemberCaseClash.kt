// Path B: user-declared member whose effective BRS name collides (case-insensitive) with
// an inherited member. Expected: BRS_NAME_CASE_CLASH on the user-declared member.
// Canonical example: SceneComponent inherits `top: RoSGNode`; user declares `val Top`.

package test

import kotlin.brs.SceneComponent

class MyScreen : SceneComponent() {
    val <!BRS_NAME_CASE_CLASH!>Top<!>: String = "hello"
}
