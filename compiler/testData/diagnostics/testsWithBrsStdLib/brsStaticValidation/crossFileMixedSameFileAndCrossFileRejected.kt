// FILE: a.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: String): String = x

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: Int): Int = x

// FILE: b.kt
package my.app

import kotlin.brs.BrsStatic

@BrsStatic
fun <!BRS_STATIC_OVERLOAD!>process<!>(x: Boolean): Boolean = x
