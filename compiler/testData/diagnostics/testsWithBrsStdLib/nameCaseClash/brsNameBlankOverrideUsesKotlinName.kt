import kotlin.brs.BrsName

<!BRS_NAME_CASE_CLASH!>@BrsName("")
fun Foo(): Int = 1<!>

<!BRS_NAME_CASE_CLASH!>fun foo(): String = "x"<!>
