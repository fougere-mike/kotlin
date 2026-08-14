// Shared DATA classes under static dispatch (Task 6 fix wave, finding C1):
// the data-class emitters generate equals/hashCode/toString/copy/componentN
// as SIMPLE-NAMED m-reading globals and skip same-named members BY NAME —
// there is no extension-shaped impl for them. Call sites must therefore stay
// on the simple-named slot shapes the data-class ctor attaches
// (isSharedExtensionShapedFunction excludes them via
// isDataClassGeneratedMemberName); before this fix they compiled to
// nonexistent mangled statics (SharedDataVmG_copy_I_Str_k_(vm, ...)) —
// "Function is not defined" on device.
//
// Pins:
// - copy (full + named-arg/default), destructuring (component1/component2),
//   explicit componentN, .equals, ==, hashCode: all on today's data-class
//   paths, byte-for-byte the same shapes a NON-shared data class gets.
// - HAND-WRITTEN members are still treated (extension-shaped + wrapper +
//   static call): scale(factor) is also the param-carrying WRAPPER pin the
//   Task 5 review deferred (slot body forwards the param:
//   `return SharedDataVmG_scale_I_k_(m, factor)`).
// - Property access (ctor param `n`, body property `hits`) stays direct
//   member access, including self-access inside scale().
//
// NOTE (backlog'd, pre-existing and broader than SharedService): data-class
// constructors do not chain the superclass ctor — no SharedService base-field
// init, no base __proto entry. `data class X : SharedService()` is PARTIALLY
// supported until that gap closes; this golden pins call-site shapes only.
import kotlin.brs.SharedService

data class SharedDataVmG(val n: Int, val tag: String) : SharedService() {
    var hits: Int = 0

    fun scale(factor: Int): Int {
        hits += 1
        return n * factor
    }
}

fun driveCopy(vm: SharedDataVmG): SharedDataVmG {
    return vm.copy(5, "x")
}

fun driveCopyDefaults(vm: SharedDataVmG): SharedDataVmG {
    return vm.copy(n = 7)
}

fun driveDestructure(vm: SharedDataVmG): String {
    val (a, b) = vm
    return b + a
}

fun driveComponentExplicit(vm: SharedDataVmG): Int {
    return vm.component1()
}

fun driveEqualsCall(a: SharedDataVmG, b: SharedDataVmG): Boolean {
    return a.equals(b)
}

fun driveEqEq(a: SharedDataVmG, b: SharedDataVmG): Boolean {
    return a == b
}

fun driveHash(vm: SharedDataVmG): Int {
    return vm.hashCode()
}

fun driveHandWritten(vm: SharedDataVmG): Int {
    return vm.scale(3)
}

fun driveDataProps(vm: SharedDataVmG): Int {
    vm.hits = 2
    return vm.n + vm.hits
}
