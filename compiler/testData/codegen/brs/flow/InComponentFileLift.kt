// A lift site inside a COMPONENT'S OWN file (Task 9b deliverable 2): the file
// basename IS the component name, so BrsCompiler.writeOutput routes the
// compiled file to components/InComponentFileLift/ — and the synthesized task
// component's include closure must reference it THERE. Pins:
// - the synthesized component's XML script tag routing the originating-file
//   dependency to pkg:/components/InComponentFileLift/InComponentFileLiftKt.brs
//   (the same routing rule writeOutput uses; pre-fix it said pkg:/source/ and
//   404'd on device), and
// - the deps.json entry carrying the component-dir-relative path
//   ("InComponentFileLift/InComponentFileLiftKt.brs") so the KGP script-tag
//   injector matches it against the staged component files instead of
//   dropping it.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.*

class InComponentFileLift : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        launch {
            flowOf("a", "b")
                .flowOn(Dispatchers.Task)
                .collect { v -> status = v }
        }
    }
}
