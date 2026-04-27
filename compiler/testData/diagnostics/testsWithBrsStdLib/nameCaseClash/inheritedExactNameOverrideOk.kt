// Path B negative: overriding an inherited member with the exact same effective name is NOT a clash.
// ComponentBase declares `onKeyEvent`; override has the same name. No diagnostic expected.

package test

import kotlin.brs.SceneComponent

class MyScreen : SceneComponent() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean = false
}
