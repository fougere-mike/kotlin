// SharedService emission shape (Task 5 of the SharedService program):
// classes whose superclass chain reaches kotlin.brs.SharedService emit their
// methods EXTENSION-SHAPED — global functions taking the receiver as an
// explicit FIRST parameter named `m` (the existing extension convention;
// suspend members follow the suspend-extension ordering `(m, params...,
// _completion)`, scratch-verified 2026-08-14) — while their constructors
// attach thin forwarding WRAPPER slots (`<impl>__slot`, body `return
// <impl>(m, params...)`) so every existing slot-shaped call site keeps
// working until Task 6 rewrites call sites to direct static calls.
//
// Pins:
// - FixtureBaseG (abstract shared base): final member, abstract hook, and a
//   suspend member with a real state machine — the impl gains the receiver
//   param, the wrapper stays continuation-correct, and the coroutine class
//   (reaches CoroutineImpl, not SharedService) keeps its normal shape.
// - FixtureVmG (final shared leaf): override + own method + plain properties.
//   Simple val/var accessors (default accessor, backing field, final,
//   overriding nothing) are NOT treated: their m-reading globals stay
//   directly attached (data-class direct-read precedent) — this is also what
//   keeps the sharedFromLowering golden byte-identical. The non-trivial
//   accessor (custom getter `summary`) IS treated: extension-shaped + wrapper.
// - ControlPlainG (non-shared control): keeps today's slot-attached,
//   m-reading shape FOREVER — any drift here means the emission switch
//   leaked past the shared predicate.
// - __proto / __type / __id emission is identical for shared and non-shared
//   classes.
import kotlin.brs.SharedService

suspend fun tickG(): Int {
    return 1
}

abstract class FixtureBaseG : SharedService() {
    var baseTouches: Int = 0

    fun touchBase(): Int {
        baseTouches += 1
        return baseTouches
    }

    abstract fun hook(): String

    suspend fun slowTouch(): Int {
        val t = tickG()
        return baseTouches + t
    }
}

class FixtureVmG : FixtureBaseG() {
    var counter: Int = 0

    val summary: String
        get() = "vm:" + counter

    fun bump(): Int {
        counter += 1
        return counter
    }

    override fun hook(): String {
        return "vm:" + counter
    }
}

class ControlPlainG {
    var count: Int = 0

    val label: String
        get() = "c:" + count

    fun poke(): Int {
        count += 1
        return count
    }
}
