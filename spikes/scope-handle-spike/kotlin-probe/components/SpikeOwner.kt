package spike.probe

import kotlin.brs.Dynamic
import kotlin.brs.brsName
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.typeOf
import spike.shared.SharedVm
import spike.shared.callBumpDynamic
import spike.shared.createRoUtils
import spike.shared.createRtqOrInvalid
import spike.shared.describeKeys
import spike.shared.getMember
import spike.shared.getRefOn
import spike.shared.isSameNodeSafe
import spike.shared.keyExistsIn
import spike.shared.lookupOf
import spike.shared.moveFromFieldOn
import spike.shared.pf
import spike.shared.rawMessage
import spike.shared.setMember
import spike.shared.setRefRet
import spike.shared.setRefStmt
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

    // Addendum (OS 15 reference APIs) state: direct references to the
    // SetRef'd payloads, so shared-identity checks read the owner's OWN
    // objects, plus the refStash observer fire counter.
    private var refAA: RoAssociativeArray? = null
    private var refInner: RoAssociativeArray? = null
    private var refVm: SharedVm? = null
    private var refStashFires: Int = 0

    init {
        val q = createRtqOrInvalid()
        if (q != null) {
            val tok = q.addMessageHandler("spike.kotlin", brsName(::onRtqPayload))
            println("[SPIKE] rtq.register token=${typeOf(tok)}")
        } else {
            println("[SPIKE] rtq.register SKIP os<15 (CreateObject returned invalid)")
        }
        // Addendum fields: runtime-added AA fields for the OS 15 reference
        // APIs (SetRef targets + Move* box). refStash observer armed HERE,
        // before any SetRef, so the doc's observer-silence claim is testable.
        top.addField("refStash", "assocarray", true)
        top.addField("vmStash", "assocarray", true)
        top.addField("moveBox", "assocarray", true)
        top.observeFieldScoped("refStash", brsName(::onRefStashChanged))
    }

    // Fires for ordinary writes to refStash; the doc claims SetRef does NOT
    // notify. The expected fire carries the {ping:1} AA written by
    // verifySetRef's plain setField, and rings the child's ack bell.
    private fun onRefStashChanged(msg: RoSGNodeEvent) {
        refStashFires = refStashFires + 1
        val d = msg.getData()
        var isPing = false
        if (typeOf(d) == "roAssociativeArray") {
            isPing = valueOrInvalid(lookupOf(d, "ping")) == "1"
        }
        if (isPing) {
            println("[SPIKE] setref.observerOnSetField PASS fires=$refStashFires")
            ringChild("setrefObs")
        } else {
            println("[SPIKE] setref.observerFire UNEXPECTED n=$refStashFires dataType=${typeOf(d)} keys=${describeKeys(d)}")
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

    // ---- OS 15 reference-API addendum arrival points (all called by the
    // child over callFunc; both components live on the render thread, which
    // is the only thread SetRef/GetRef are legal on) ----

    @BrsExport
    @BrsName("setupSetRef")
    fun setupSetRef(arg: Dynamic?): String {
        val inner = RoAssociativeArray.create()
        inner.addReplace("marker", 1)
        val nested = RoAssociativeArray.create()
        nested.addReplace("inner", inner)
        val aa = RoAssociativeArray.create()
        aa.addReplace("marker", 1)
        aa.addReplace("nested", nested)
        refAA = aa
        refInner = inner
        refStashFires = 0
        // The doc's SetRef signature says void but its Return Value section
        // says Boolean — capture whichever the device implements.
        try {
            val r = setRefRet(top, "refStash", aa)
            println("[SPIKE] setref.set ret=${valueOrInvalid(r)}")
        } catch (e: Throwable) {
            setRefStmt(top, "refStash", aa)
            println("[SPIKE] setref.set ret=void (function-form threw:${valueOrInvalid(rawMessage(e))})")
        }
        // Direct same-thread identity check on the owner's own node.
        try {
            val u = createRoUtils()
            if (u != null) {
                println("[SPIKE] setref.ownerIsSameObject ${pf(u.isSameObject(refAA, getRefOn(top, "refStash")))}")
            } else {
                println("[SPIKE] setref.ownerIsSameObject SKIP roUtils-invalid")
            }
        } catch (e: Throwable) {
            println("[SPIKE] setref.ownerIsSameObject THREW ${valueOrInvalid(rawMessage(e))}")
        }
        return "ok"
    }

    @BrsExport
    @BrsName("verifySetRef")
    fun verifySetRef(arg: Dynamic?): String {
        // The child has mutated the GetRef'd object (marker=2 top-level and
        // nested). Re-read the owner's DIRECT references.
        val m1 = valueOrInvalid(lookupOf(refAA, "marker"))
        val m2 = valueOrInvalid(lookupOf(refInner, "marker"))
        println("[SPIKE] setref.sharedTopLevel ${pf(m1 == "2")} ownerMarker=$m1")
        println("[SPIKE] setref.sharedNested ${pf(m2 == "2")} ownerInnerMarker=$m2")
        try {
            val u = createRoUtils()
            if (u != null) {
                println("[SPIKE] setref.isSameObjectAfterMutate ${pf(u.isSameObject(refAA, getRefOn(top, "refStash")))}")
            }
        } catch (e: Throwable) {
            println("[SPIKE] setref.isSameObjectAfterMutate THREW ${valueOrInvalid(rawMessage(e))}")
        }
        // Doc claim: SetRef does not notify observers. Everything so far
        // (SetRef + GetRefs + mutations) should have produced zero fires.
        println("[SPIKE] setref.observerSilentOnSetRef ${pf(refStashFires == 0)} fires=$refStashFires")
        // Control: an ORDINARY setField on the same field must fire the
        // observer (async — the handler rings the child's setrefObs bell).
        val ping = RoAssociativeArray.create()
        ping.addReplace("ping", 1)
        top.setField("refStash", ping)
        return "ok"
    }

    @BrsExport
    @BrsName("setupSetRefVm")
    fun setupSetRefVm(arg: Dynamic?): String {
        val vm = SharedVm()
        vm.bump()
        refVm = vm
        println("[SPIKE] setrefVm.pre vmType=${typeOf(vm)} counter=${vm.counter} vmKeys=${describeKeys(vm)}")
        try {
            val r = setRefRet(top, "vmStash", vm)
            println("[SPIKE] setrefVm.set ret=${valueOrInvalid(r)}")
        } catch (e: Throwable) {
            setRefStmt(top, "vmStash", vm)
            println("[SPIKE] setrefVm.set ret=void (function-form threw:${valueOrInvalid(rawMessage(e))})")
        }
        return "ok"
    }

    @BrsExport
    @BrsName("verifySetRefVm")
    fun verifySetRefVm(arg: Dynamic?): String {
        // If the child's bump() on the GetRef'd object dispatched AND the
        // object is genuinely shared, the owner's direct ref reads counter=2.
        val c = valueOrInvalid(getMember(refVm, "counter"))
        println("[SPIKE] setrefVm.sharedState ${pf(c == "2")} ownerCounter=$c")
        return "ok"
    }

    @BrsExport
    @BrsName("moveVerify")
    fun moveVerify(arg: Dynamic?): String {
        try {
            val got = moveFromFieldOn(top, "moveBox")
            val fieldAfter = top.getField("moveBox")
            println("[SPIKE] move.from gotKeys=${describeKeys(got)} fieldAfterType=${typeOf(fieldAfter)}")
        } catch (e: Throwable) {
            println("[SPIKE] move.from THREW ${valueOrInvalid(rawMessage(e))}")
        }
        return "ok"
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
