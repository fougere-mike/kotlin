package spike.probe

import kotlin.brs.Dynamic
import kotlin.brs.brsName
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.typeOf
import spike.shared.SharedVm
import spike.shared.callBumpDynamic
import spike.shared.createRtqOrInvalid
import spike.shared.describeKeys
import spike.shared.getMember
import spike.shared.isSameNodeSafe
import spike.shared.keyExistsIn
import spike.shared.lookupOf
import spike.shared.pf
import spike.shared.rawMessage
import spike.shared.setMember
import spike.shared.subtypeOf
import spike.shared.valueOrInvalid

// The "owner" analog: receives the Q1d payload on every channel and runs the
// same inspection body. All arrival points are armed here — declared-field
// observers via @BrsOnChange (generated init) and the RTQ handler in init,
// before SpikeScene wires anything (arming-order law).
class SpikeOwner : GroupComponent() {

    // nodeField channel arrival point (declared AA field, dot-assign parity
    // with setField is total on declared fields).
    @SGAssocArrayField
    @BrsOnChange("onMailbox")
    var mailbox: RoAssociativeArray? = null

    // Wired by SpikeScene so acks and the bare-node compare never rely on
    // findNode timing.
    @SGNodeField
    var childNode: RoSGNode? = null

    init {
        val q = createRtqOrInvalid()
        if (q != null) {
            val tok = q.addMessageHandler("spike.kotlin", brsName(::onRtqPayload))
            println("[SPIKE] rtq.register token=${typeOf(tok)}")
        } else {
            println("[SPIKE] rtq.register SKIP os<15 (CreateObject returned invalid)")
        }
    }

    private fun onMailbox(msg: RoSGNodeEvent) {
        // Skip the one initial default-value fire from the generated init().
        val p = mailbox ?: return
        if (p.count() == 0) return
        inspectPayload(p, "nodeField")
        ringChild("nodeField")
    }

    @BrsExport
    @BrsName("spikeCall")
    fun spikeCall(payload: RoAssociativeArray?): String {
        // Synchronous channel: the child checks divergence right after
        // callFunc returns, so no ack bell for this one.
        inspectPayload(payload, "callFuncArg")
        return "ok"
    }

    // Carried item 3: bare node as the callFunc arg (NOT inside an AA), bare
    // node returned. The child prints the return-direction verdict.
    @BrsExport
    @BrsName("nodeEcho")
    fun nodeEcho(arg: Dynamic?): RoSGNode {
        var ok = false
        var detail = " type=" + typeOf(arg)
        try {
            ok = isSameNodeSafe(arg, childNode)
            if (typeOf(arg) == "roSGNode") {
                detail = detail + " subtype=" + subtypeOf(arg) + " id=" + valueOrInvalid(getMember(arg, "id"))
            }
        } catch (e: Throwable) {
            detail = detail + " threw:" + valueOrInvalid(rawMessage(e))
        }
        println("[SPIKE] barenode.callFuncArg ${pf(ok)}$detail")
        return top
    }

    // rtq channel arrival point (ifRenderThreadQueue handler signature:
    // handler(data, msgInfo), runs in this component's script context).
    private fun onRtqPayload(data: RoAssociativeArray?, msgInfo: Dynamic?) {
        inspectPayload(data, "rtq")
        ringChild("rtq")
    }

    private fun ringChild(channel: String) {
        val c = childNode
        if (c != null) {
            c.setField("ackBell", channel)
        } else {
            println("[SPIKE] WARN childNode-missing cannot-ack $channel")
        }
    }

    // The Q1d inspection body, identical at every channel's arrival point.
    private fun inspectPayload(payload: RoAssociativeArray?, channel: String) {
        if (payload == null) {
            println("[SPIKE] q1d.$channel.arrived FAIL payload-invalid")
            return
        }
        val kobj = payload.lookup("kobj")

        // (a) what did the Kotlin object arrive as, and which slots survived?
        println("[SPIKE] q1d.$channel.arrived kobjType=${typeOf(kobj)} kobjKeys=${describeKeys(kobj)}")

        // Carried item 1 (receiver side): the fn-valued payload key.
        println("[SPIKE] q1d.$channel.fnKey keyExists=${pf(keyExistsIn(payload, "theFn"))} type=${typeOf(payload.lookup("theFn"))}")

        // Does the clone still pass Kotlin's own type check? (as? compiles to
        // an __proto walk; __proto is plain data and should survive the copy.)
        val anyObj: Any? = kobj
        val cast = anyObj as? SharedVm
        println("[SPIKE] q1d.$channel.cast ${pf(cast != null)} (as? SharedVm on the arrived value)")

        // (b) method dispatch on the arrived object. Typed call when the cast
        // passed, raw name-based dispatch otherwise — both compile to
        // obj.bump_k_(), the AA-slot call the clone may no longer carry.
        if (cast != null) {
            try {
                val n = cast.bump()
                println("[SPIKE] q1d.$channel.method PASS via=typed bumpReturned=$n counterAfter=${valueOrInvalid(getMember(kobj, "counter"))}")
            } catch (e: Throwable) {
                println("[SPIKE] q1d.$channel.method FAIL via=typed threw:${valueOrInvalid(rawMessage(e))}")
            }
        } else {
            try {
                val n = callBumpDynamic(kobj)
                println("[SPIKE] q1d.$channel.method PASS via=dynamic bumpReturned=${valueOrInvalid(n)} counterAfter=${valueOrInvalid(getMember(kobj, "counter"))}")
            } catch (e: Throwable) {
                println("[SPIKE] q1d.$channel.method FAIL via=dynamic threw:${valueOrInvalid(rawMessage(e))}")
            }
        }

        // (c) owner-side writes: clone state, nested-AA marker (carried item
        // 2), and the top-level marker (Task 3 continuity). The child re-reads
        // its own references afterwards.
        try {
            setMember(kobj, "counter", 99)
            setMember(payload, "marker", 2)
            val inner = lookupOf(payload.lookup("nested"), "inner")
            setMember(inner, "marker", 2)
            println("[SPIKE] q1d.$channel.ownerMutate ok counter=${valueOrInvalid(getMember(kobj, "counter"))} innerMarker=${valueOrInvalid(lookupOf(inner, "marker"))}")
        } catch (e: Throwable) {
            println("[SPIKE] q1d.$channel.ownerMutate THREW ${valueOrInvalid(rawMessage(e))}")
        }
    }
}
