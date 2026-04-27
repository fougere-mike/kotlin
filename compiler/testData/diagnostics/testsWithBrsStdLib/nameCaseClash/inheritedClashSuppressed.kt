// Path B: @Suppress("BRS_NAME_CASE_CLASH") at the member site silences the inherited-clash diagnostic.
// No diagnostic expected.

package test

import kotlin.brs.SceneComponent

class MyScreen : SceneComponent() {
    @Suppress("BRS_NAME_CASE_CLASH")
    val Top: String = "hello"
}
