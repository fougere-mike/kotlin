// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@Suppress("BRS_STATIC_OVERLOAD")
@BrsStatic
fun bar(): String = "a"

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>bar<!>(): Int = 1
