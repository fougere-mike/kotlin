// FILE: a.kt
package my.app

@Suppress("BRS_NAME_CASE_CLASH")
fun Foo(): Int = 1

// FILE: b.kt
package my.app

<!BRS_NAME_CASE_CLASH!>fun foo(): String = "x"<!>
