// init { } blocks in PLAIN (non-component) classes: statements must be emitted into
// the generated _create constructor, interleaved with property initializers in
// DECLARATION ORDER (Kotlin semantics), after the super-constructor call.

// (a) init block mutating a property
class Counter(start: Int) {
    var count: Int = start

    init {
        count = count + 1
    }
}

// (b) init block with control flow: guard that throws
class Guarded(val step: Int) {
    init {
        if (step == 0) throw IllegalArgumentException("Step must be non-zero.")
    }

    val magnitude: Int = if (step < 0) -step else step
}

// (c) TWO init blocks interleaved with property initializers — declaration order is:
//     trace = "p1"  →  init#1  →  doubled = seed * 2  →  init#2
class Ordered(seed: Int) {
    var trace: String = "p1"

    init {
        trace = trace + ":i1"
    }

    val doubled: Int = seed * 2

    init {
        trace = trace + ":i2:" + doubled
    }
}

// (d) subclass whose superclass has an init block: super's init runs first
open class BaseWithInit {
    var log: String = "base-prop"

    init {
        log = log + ":base-init"
    }
}

class DerivedWithInit : BaseWithInit() {
    init {
        log = log + ":derived-init"
    }
}

fun useAll(): String {
    val c = Counter(41)
    val g = Guarded(-3)
    val o = Ordered(7)
    val d = DerivedWithInit()
    return c.count.toString() + "|" + g.magnitude + "|" + o.trace + "|" + d.log
}
