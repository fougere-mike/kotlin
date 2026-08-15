// Dispatcher rungs into DATA-class leaves + the call-site simple-name render
// gate (Task 6 fix wave 2, findings D1 + D2).
//
// D1: a shared abstract base with hand-written open equals/hashCode (open via
// override) gets __proto dispatchers; a DATA-class leaf's GENERATED
// structural equals/hashCode override them, but generated data members have
// NO mangled global (the data-class emitters produce simple-named m-reading
// globals attached under the bare names). Before this fix the dispatcher
// rung emitted `DataLeafG_equals_AnyN_k_(recv, other)` — a nonexistent
// function ("Function is not defined" on device). Pinned here:
// - the DataLeafG rung dispatches through the leaf's own simple-named slot
//   attachment (`recv.equals(other)` — the leaf's runtime identity is exact
//   at that rung, proto-head matched; residual slot family, canary-watched);
// - the PlainLeafG rung (hand-written override) stays a direct static
//   (`PlainLeafG_equals_AnyN_k_(recv, other)`) — the control.
//
// D2: the call-site simple-name render heuristic is single-sourced:
// - equals/hashCode/toString render SIMPLE on EVERY class (hand-written
//   overrides attach a simple Any-alias next to the mangled slot;
//   non-overriding classes attach the Any defaults simple-ONLY — ControlEqG
//   and ControlBareG pin both, and kclassEquality already pins the KClass
//   shape); an isData gate here would break non-data receivers.
// - copy/componentN are DATA-CLASS generated names: gated on isData +
//   digit-checked (isDataClassGeneratedMemberName). A hand-written
//   componentFoo() on a plain data class now renders MANGLED at call sites
//   (p.componentFoo_I_k_(2)), matching its mangled emission + attachment —
//   the round trip is callable for the first time (it was emission-skipped
//   before the C1 wave and simple-rendered before this one). Canonical
//   copy/destructuring on the same class stay simple (drivePlainCanonical).
import kotlin.brs.SharedService

abstract class DispatchDataBaseG : SharedService() {
    override fun equals(other: Any?): Boolean {
        return other is DispatchDataBaseG
    }

    override fun hashCode(): Int {
        return 7
    }
}

data class DataLeafG(val x: Int) : DispatchDataBaseG()

class PlainLeafG : DispatchDataBaseG() {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return 3
    }
}

data class PlainPairG(val v: Int, val w: Int) {
    fun componentFoo(k: Int): Int {
        return v + k
    }
}

class ControlEqG {
    override fun equals(other: Any?): Boolean {
        return other is ControlEqG
    }

    override fun hashCode(): Int {
        return 5
    }
}

class ControlBareG

fun driveBaseEquals(a: DispatchDataBaseG, b: DispatchDataBaseG): Boolean {
    return a.equals(b)
}

fun driveBaseHash(a: DispatchDataBaseG): Int {
    return a.hashCode()
}

fun driveComponentFoo(p: PlainPairG): Int {
    return p.componentFoo(2)
}

fun drivePlainCanonical(p: PlainPairG): Int {
    val q = p.copy(3, 4)
    val (v, w) = q
    return v + w
}

fun driveControlEq(c: ControlEqG, d: ControlEqG): Boolean {
    return c.equals(d)
}

fun driveControlHash(c: ControlEqG): Int {
    return c.hashCode()
}

fun driveBareEq(c: ControlBareG, d: ControlBareG): Boolean {
    return c.equals(d)
}
