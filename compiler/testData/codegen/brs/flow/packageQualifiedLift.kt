// The task lift in a PACKAGE-QUALIFIED file (Task 9b deliverable 1): both lift
// surfaces in a realistic reverse-DNS package — the coverage class whose
// absence let the mangle length-fallback defect reach the device (Task 9: the
// no-package flowOnLift golden stayed under the 100-char cliff, so the hashed
// invoke mangles never appeared in any golden). Pins:
// - the synthesized names built from the file BASENAME + a short stable hash
//   of the package (never the full sanitized FQ), keeping ordinary members of
//   synthesized classes under the mangle cliff at realistic package depths,
// - the rebuilt upstream/spawn lambdas attaching the INTERFACE mangles the
//   flow runtime dispatches (invoke_AnyN_Continuation_k_ / invoke_AnyN_k_) —
//   an override-contract mangle is cross-module-stable and must never take
//   the hash fallback regardless of the implementing class's name length.
package com.example.app.screens.details.shelfgrid

import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.roku.RoAssociativeArray
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.flow.*
import kotlin.coroutines.task.spawnTask

class PackageQualifiedLift : GroupComponent() {
    @SGStringField
    var status: String = ""

    init {
        val label = "shelf"
        launch {
            flow {
                val row = RoAssociativeArray.create()
                row.addReplace("name", label)
                emit(row)
            }
                .map { row -> "mapped:" + label }
                .flowOn(Dispatchers.Task)
                .collect { v -> status = v }
            val fetched = spawnTask { label + ":fetched" }
            status = fetched
        }
    }
}
