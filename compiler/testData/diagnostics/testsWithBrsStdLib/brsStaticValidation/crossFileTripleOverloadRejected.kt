// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): Int = 1

// FILE: c.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>greet<!>(): Boolean = true
