package spike.shared

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic
import kotlin.brs.roku.RoArray
import kotlin.brs.typeOf

// Dynamic member access: characterization reads on possibly cloned/gutted AAs
// must not crash — a missing key reads as invalid instead of throwing.
@BrsInline("return obj[key]")
internal external fun getMember(obj: Any?, key: String): Dynamic?

@BrsInline("obj[key] = value")
internal external fun setMember(obj: Any?, key: String, value: Any?)

@BrsInline("return obj.keys()")
internal external fun keysOf(obj: Any?): RoArray

@BrsInline("return obj.lookup(key)")
internal external fun lookupOf(obj: Any?, key: String): Dynamic?

@BrsInline("return obj.doesExist(key)")
internal external fun keyExistsIn(obj: Any?, key: String): Boolean

@BrsInline("return obj.count()")
internal external fun countOf(obj: Any?): Int

// Native-exception-safe message read (FieldSemanticsProbe pattern): raw field
// access works for both native roExceptions and Kotlin Throwables.
@BrsInline("return e.message")
internal external fun rawMessage(e: Throwable): Dynamic?

@BrsInline("return n.subtype()")
internal external fun subtypeOf(n: Any?): String

// Bare `and` short-circuits on this device (CLAUDE.md, probe-verified
// 2026-08-11 on the same Roku Ultra), so the isSameNode call never sees an
// invalid or non-node receiver.
@BrsInline("return a <> invalid and b <> invalid and Type(a) = \"roSGNode\" and Type(b) = \"roSGNode\" and a.isSameNode(b)")
internal external fun isSameNodeSafe(a: Any?, b: Any?): Boolean

// The load-bearing Q1d probe: raw name-based dispatch on whatever arrived.
// Slot name matches the generated SharedVm_create (verified in build output).
@BrsInline("return obj.bump_k_()")
internal external fun callBumpDynamic(obj: Any?): Dynamic?

// Mirrors the stdlib PumpScheduler pattern: feature detection needs the
// invalid-on-older-OS result, so no @BrsCreateObject factory.
@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
internal external fun createRtqOrInvalid(): SpikeRtq?

external interface SpikeRtq {
    fun addMessageHandler(messageId: String, handler: String): Any?
    fun postMessage(messageId: String, data: Any?)
}

internal fun pf(ok: Boolean): String {
    if (ok) return "PASS"
    return "FAIL"
}

// Invalid-safe stringify (interpolating invalid is itself a runtime error).
internal fun valueOrInvalid(v: Dynamic?): String {
    if (v == null) return "invalid"
    return "$v"
}

// key:Type list of an AA-shaped value — shows exactly which slots (data vs
// function-valued) survived a hop. keys() hands back a copy, so shift-draining
// it is safe (Kotlin for-in over native roArray is not supported yet).
internal fun describeKeys(obj: Any?): String {
    if (obj == null) return "invalid"
    if (typeOf(obj) != "roAssociativeArray") return "<" + typeOf(obj) + ">"
    val ks = keysOf(obj)
    var out = ""
    while (ks.count() > 0) {
        val k = "${ks.shift()}"
        if (out != "") out = out + ","
        out = out + k + ":" + typeOf(lookupOf(obj, k))
    }
    return out
}
