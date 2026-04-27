import kotlin.brs.BrsName

<!BRS_NAME_CASE_CLASH!>@BrsName("foo")
fun Alpha(): Int = 1<!>

<!BRS_NAME_CASE_CLASH!>@BrsName("FOO")
fun Beta(): Int = 2<!>
