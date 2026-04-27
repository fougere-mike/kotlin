// Path B negative: user adds members with names that don't case-collide with any inherited member.
// No diagnostic expected.

package test

import kotlin.brs.SceneComponent

class MyScreen : SceneComponent() {
    val title: String = "hello"
    fun activate(): Unit {}
}
