// FILE: a.kt
package my.app.alpha

import kotlin.brs.BrsStatic

@BrsStatic
fun foo(): String = "a"

// FILE: b.kt
package my.app.beta

import kotlin.brs.BrsStatic

@BrsStatic
fun foo(): Int = 1
