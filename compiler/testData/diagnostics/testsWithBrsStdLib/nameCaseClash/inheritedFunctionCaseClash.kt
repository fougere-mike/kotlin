// Path B: user declares a function whose name case-collides with an inherited property.
// ComponentBase inherits `m: RoAssociativeArray`; user declares `fun M()`.
// Expected: BRS_NAME_CASE_CLASH on the user-declared function.

package test

import kotlin.brs.SceneComponent

class MyScreen : SceneComponent() {
    fun <!BRS_NAME_CASE_CLASH!>M<!>() {}
}
