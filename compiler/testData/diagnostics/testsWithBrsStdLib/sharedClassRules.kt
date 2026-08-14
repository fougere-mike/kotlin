// BRS_SHARED_CLASS_NOT_FINAL — a CONCRETE SharedService descendant declared `open`: static
// dispatch (Phase 3) enumerates the concrete leaves of a shared hierarchy, so concrete shared
// classes must be final. Abstract bases WITH members, overrides, and abstract hooks are LEGAL —
// the design's signature shape (spec §5: no member-free rule, no flat-hierarchy rule; the
// checker is purely structural).
// BRS_SHARED_FN_PROPERTY — a function-typed property anywhere in a SharedService hierarchy is
// a stored callback in the shared bag: the one shape static dispatch cannot rescue.
import kotlin.brs.SharedService

// Case 1 (the signature case): abstract base WITH state, a concrete member, and an abstract
// hook — CLEAN. This is exactly the shape the E2E hierarchy fixtures rely on.
abstract class ViewModel : SharedService() {
    var counter: Int = 0

    fun bump(): Int {
        counter = counter + 1
        return counter
    }

    abstract fun label(): String
}

// Case 2: final concrete subclass (Kotlin's default) — CLEAN
class GuideVm : ViewModel() {
    override fun label(): String = "guide"
}

// Case 3: open concrete subclass — ERROR
open class <!BRS_SHARED_CLASS_NOT_FINAL!>SettingsVm<!> : ViewModel() {
    override fun label(): String = "settings"
}

// Case 4: open concrete class directly on SharedService — ERROR (depth-1 walk)
open class <!BRS_SHARED_CLASS_NOT_FINAL!>DirectService<!> : SharedService() {
    fun ping(): Int = 1
}

// Case 5: deep chain abstract → abstract → final — CLEAN at every level (full transitive walk
// classifies MidBase and LeafVm as shared; neither is an open concrete class)
abstract class MidBase : ViewModel() {
    fun helper(): Int = 41
}

class LeafVm : MidBase() {
    override fun label(): String = "leaf"
}

// Case 6: open class OUTSIDE any shared hierarchy — CLEAN (non-shared classes are unaffected)
open class PlainOpen {
    fun anything(): Int = 0
}

// Case 7: fn-typed property on an ABSTRACT shared base — ERROR (fires anywhere in the hierarchy)
abstract class CallbackBase : SharedService() {
    var <!BRS_SHARED_FN_PROPERTY!>onChange<!>: (() -> Unit)? = null
}

// Case 8: fn-typed val on a final concrete subclass — ERROR
class CallbackVm : ViewModel() {
    val <!BRS_SHARED_FN_PROPERTY!>formatter<!>: (Int) -> String = { value -> "$value" }

    override fun label(): String = "callback"
}

// Case 9: suspend-fn-typed property — ERROR (suspend function types are function references too)
class SuspendCallbackVm : ViewModel() {
    var <!BRS_SHARED_FN_PROPERTY!>loader<!>: (suspend () -> Int)? = null

    override fun label(): String = "suspend"
}

// Case 10: constructor-val callback — ERROR (the likeliest real-world shape)
class CtorCallbackVm(val <!BRS_SHARED_FN_PROPERTY!>onDone<!>: () -> Unit) : ViewModel() {
    override fun label(): String = "ctor"
}

// Case 11: non-shared class with a fn property — CLEAN (the rule is scoped to shared hierarchies)
class PlainHolder {
    var onChange: (() -> Unit)? = null
}

// Case 12: ordinary data + methods on shared classes — CLEAN (that is the point of SharedService)
class StateVm : ViewModel() {
    var name: String = ""

    override fun label(): String = name
}

// Case 13: suppression escapes — CLEAN (deliberate, review-visible opt-outs)
@Suppress("BRS_SHARED_CLASS_NOT_FINAL")
open class DeliberateOpenVm : ViewModel() {
    override fun label(): String = "deliberate"
}

class DeliberateCallbackVm : ViewModel() {
    @Suppress("BRS_SHARED_FN_PROPERTY")
    var onSelect: (() -> Unit)? = null

    override fun label(): String = "opt-out"
}
