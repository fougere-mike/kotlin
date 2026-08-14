// SharedService static dispatch (Task 6 of the SharedService program): call
// sites whose receiver is a shared class compile WITHOUT reading fn slots —
// direct static calls to the extension-shaped impls (receiver FIRST), or
// generated `__proto`-name dispatchers for open/abstract members reached
// through a non-final static type. Shapes pinned here:
// - direct final call (leaf-typed `vm.bump()` → DispatchVmG_bump_k_(vm)) and
//   fake-override resolution (leaf-typed `vm.touchBase()` → the BASE's impl);
// - final member via base type (`b.touchBase()` → declaring class's impl);
// - open/abstract member via base type (`b.hook("h")`, `b.describe()`) →
//   Base_<mangle>__dispatch(recv, args...): reads recv.__proto[0], if-chains
//   over the compilation's concrete descendants in source-name order
//   (DispatchSvcG before DispatchVmG), non-overriding descendants route to
//   the declaring impl, else-arm throws the guided closed-world error;
// - the same open member via the FINAL leaf's static type (`vm.hook("x")`)
//   → direct static to the override (no dispatcher hop);
// - base-internal `this.hook()` (template method) → dispatcher on `m`;
// - `super.describe()` inside the override → direct static to the SUPERCLASS
//   impl (pre-Task-6 this emitted a self-recursive slot call);
// - suspend member through the dispatcher (`b.slowHook(n)`) → the state
//   machine calls Base_slowHook_I_ContinuationI_k___dispatch(b, n, m):
//   receiver first, `_completion` LAST, threaded transparently to the impl;
// - abstract base with NO concrete descendants (OrphanBaseG.ghost) → the
//   dispatcher body is ONLY the guided error;
// - simple val/var access on shared receivers → DIRECT member reads/writes
//   (`vm.counter = 5`, `b.baseTouches` — no accessor-slot calls);
// - non-trivial accessors: final (`vm.summary`) → static accessor call;
//   open (`b.label`) → accessor dispatcher;
// - residuals stay on TODAY's paths byte-for-byte: interface-typed receiver
//   (`t.tagOf()`) keeps the slot call, Any-typed receiver (`x.toString()`)
//   keeps the pre-existing Any path;
// - ControlOpenG/ControlLeafG (non-shared virtual hierarchy): slot dispatch
//   forever — any drift here means the rewrite leaked past the shared
//   predicate.
import kotlin.brs.SharedService

interface TaggedG {
    fun tagOf(): String
}

suspend fun pauseG(): Int {
    return 1
}

abstract class DispatchBaseG : SharedService() {
    var baseTouches: Int = 0

    fun touchBase(): Int {
        baseTouches += 1
        return baseTouches
    }

    abstract fun hook(tag: String): String

    open fun describe(): String {
        return "base"
    }

    fun template(): String {
        return "T:" + hook("t")
    }

    abstract suspend fun slowHook(n: Int): Int

    open val label: String
        get() = "base"
}

class DispatchSvcG : DispatchBaseG() {
    override fun hook(tag: String): String {
        return tag + ":svc"
    }

    override suspend fun slowHook(n: Int): Int {
        return n
    }
}

class DispatchVmG : DispatchBaseG(), TaggedG {
    var counter: Int = 0

    val summary: String
        get() = "vm:" + counter

    fun bump(): Int {
        counter += 1
        return counter
    }

    override fun hook(tag: String): String {
        return tag + ":vm:" + counter
    }

    override fun describe(): String {
        return super.describe() + ":vm"
    }

    override suspend fun slowHook(n: Int): Int {
        val t = pauseG()
        return counter + n + t
    }

    override val label: String
        get() = "vm"

    override fun tagOf(): String {
        return "tag:vm"
    }
}

abstract class OrphanBaseG : SharedService() {
    abstract fun ghost(): Int
}

open class ControlOpenG {
    open fun poke(): Int {
        return 1
    }
}

class ControlLeafG : ControlOpenG() {
    override fun poke(): Int {
        return 2
    }
}

fun driveDirect(vm: DispatchVmG): Int {
    return vm.bump() + vm.touchBase()
}

fun driveBaseFinal(b: DispatchBaseG): Int {
    return b.touchBase()
}

fun driveHook(b: DispatchBaseG): String {
    return b.hook("h")
}

fun driveLeafHook(vm: DispatchVmG): String {
    return vm.hook("x")
}

fun driveTemplate(b: DispatchBaseG): String {
    return b.template()
}

fun driveDescribe(b: DispatchBaseG): String {
    return b.describe()
}

suspend fun driveSlow(b: DispatchBaseG, n: Int): Int {
    return b.slowHook(n)
}

suspend fun driveSlowChained(b: DispatchBaseG, n: Int): Int {
    return b.slowHook(n) + 1
}

fun driveOrphan(o: OrphanBaseG): Int {
    return o.ghost()
}

fun driveProps(vm: DispatchVmG, b: DispatchBaseG): Int {
    vm.counter = 5
    return vm.counter + b.baseTouches
}

fun driveAccessors(vm: DispatchVmG, b: DispatchBaseG): String {
    return vm.summary + b.label
}

fun driveResidualInterface(t: TaggedG): String {
    return t.tagOf()
}

fun driveResidualAny(x: Any): String {
    return x.toString()
}

fun driveControl(c: ControlOpenG): Int {
    return c.poke()
}
