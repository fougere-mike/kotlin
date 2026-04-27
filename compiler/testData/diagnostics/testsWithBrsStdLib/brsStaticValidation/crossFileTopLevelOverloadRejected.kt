// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>foo<!>(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>foo<!>(): Int = 1
