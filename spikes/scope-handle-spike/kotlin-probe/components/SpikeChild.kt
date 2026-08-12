package spike.probe

import kotlin.brs.Dynamic
import kotlin.brs.asDynamic
import kotlin.brs.brsName
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.typeOf
import spike.shared.SharedVm
import spike.shared.countOf
import spike.shared.createRtqOrInvalid
import spike.shared.describeKeys
import spike.shared.getMember
import spike.shared.isSameNodeSafe
import spike.shared.keyExistsIn
import spike.shared.lookupOf
import spike.shared.pf
import spike.shared.rawMessage
import spike.shared.subtypeOf
import spike.shared.valueOrInvalid

// The "child": builds a fresh SharedVm payload per channel, ships it, then
// re-reads its OWN references after the owner has inspected and mutated.
// Step machine + 2s watchdog mirror ProbeB (channel-matrix): a never-firing
// observer records explicit FAIL lines instead of a blank cell.
class SpikeChild : GroupComponent() {

    @SGNodeField
    var ownerNode: RoSGNode? = null

    @SGBooleanField(alwaysNotify = true)
    @BrsOnChange("onRunProbes")
    var runProbes: Boolean = false

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onAck")
    var ackBell: String = ""

    private var step: Int = 0
    private var expectedAck: String = ""
    private var currentVm: SharedVm? = null
    private var currentPayload: RoAssociativeArray? = null
    private var currentInner: RoAssociativeArray? = null
    private var stepTimer: RoSGNode? = null
    private var watchdog: RoSGNode? = null

    init {
        // setField("duration", Double) into a time field is the device-proven
        // stdlib pump-timer pattern (PumpScheduler.armTimerSeconds).
        val st = top.createChild("Timer")
        st.setField("repeat", false)
        st.setField("duration", 0.1)
        st.observeFieldScoped("fire", brsName(::onStepTimer))
        stepTimer = st

        val wd = top.createChild("Timer")
        wd.setField("repeat", false)
        wd.setField("duration", 2.0)
        wd.observeFieldScoped("fire", brsName(::onWatchdog))
        watchdog = wd
    }

    private fun onRunProbes(msg: RoSGNodeEvent) {
        if ("${msg.getData()}" != "true") return
        nextStep()
    }

    private fun nextStep() {
        watchdog?.setField("control", "stop")
        step = step + 1
        if (step == 1) {
            stepNodeField()
        } else if (step == 2) {
            stepCallFunc()
        } else if (step == 3) {
            stepRtq()
        } else {
            println("[SPIKE] END")
        }
    }

    private fun advance() {
        stepTimer?.setField("control", "start")
    }

    private fun onStepTimer(msg: RoSGNodeEvent) {
        nextStep()
    }

    private fun armWatchdog(ack: String) {
        expectedAck = ack
        val wd = watchdog
        if (wd != null) {
            wd.setField("control", "stop")
            wd.setField("control", "start")
        }
    }

    // Fresh payload per channel. The child keeps DIRECT references to the vm
    // and the nested inner AA — divergence checks read those, not the payload,
    // so RTQ's sender-side gutting can't blind them.
    private fun buildPayload(channel: String): RoAssociativeArray {
        val vm = SharedVm()
        vm.bump()
        val inner = RoAssociativeArray.create()
        inner.addReplace("marker", 1)
        val nested = RoAssociativeArray.create()
        nested.addReplace("inner", inner)
        val payload = RoAssociativeArray.create()
        payload.addReplace("marker", 1)
        payload.addReplace("kobj", vm)
        payload.addReplace("theFn", getMember(vm, "bump_k_"))
        payload.addReplace("nested", nested)
        currentVm = vm
        currentInner = inner
        currentPayload = payload
        // Sender-side truth before the hop: typed counter read + full slot map
        // + carried item 1 (Type() of the fn-valued key BEFORE posting).
        println("[SPIKE] q1d.$channel.pre vmType=${typeOf(vm)} counter=${vm.counter} vmKeys=${describeKeys(vm)}")
        println("[SPIKE] q1d.$channel.preFn keyExists=${pf(keyExistsIn(payload, "theFn"))} type=${typeOf(payload.lookup("theFn"))}")
        return payload
    }

    // Divergence read-back after the owner inspected+mutated. PASS on
    // identity/nested/refIdentity = the owner's write IS visible here (shared
    // structure); FAIL = independent copy. getMember/lookupOf reads survive
    // gutted or stripped objects (missing keys read as invalid).
    private fun checkAfterHop(channel: String) {
        // Guarded: a poisoned reference (e.g. post-RTQ-move state) must record
        // a THREW verdict, not kill the run before [SPIKE] END.
        try {
            val c = valueOrInvalid(getMember(currentVm, "counter"))
            val shared = (c == "99") || (c == "2")
            println("[SPIKE] q1d.$channel.identity ${pf(shared)} localCounter=$c")
            val im = valueOrInvalid(lookupOf(currentInner, "marker"))
            println("[SPIKE] q1d.$channel.nested ${pf(im == "2")} innerMarker=$im")
            val mk = valueOrInvalid(lookupOf(currentPayload, "marker"))
            println("[SPIKE] q1d.$channel.refIdentity ${pf(mk == "2")} marker=$mk")
        } catch (e: Throwable) {
            println("[SPIKE] q1d.$channel.checkAfterHop THREW ${valueOrInvalid(rawMessage(e))}")
        }
    }

    // ---- steps ----

    private fun stepNodeField() {
        val owner = ownerNode
        if (owner == null) {
            println("[SPIKE] q1d.nodeField.send FAIL owner-node-missing")
            println("[SPIKE] END")
            return
        }
        val payload = buildPayload("nodeField")
        armWatchdog("nodeField")
        owner.setField("mailbox", payload)
    }

    private fun stepCallFunc() {
        val owner = ownerNode
        if (owner == null) {
            println("[SPIKE] q1d.callFuncArg.send FAIL owner-node-missing")
            advance()
            return
        }
        val payload = buildPayload("callFuncArg")
        try {
            owner.callFunc("spikeCall", payload.asDynamic())
        } catch (e: Throwable) {
            println("[SPIKE] q1d.callFuncArg.call THREW ${valueOrInvalid(rawMessage(e))}")
        }
        // callFunc is synchronous: the owner has already inspected + mutated.
        checkAfterHop("callFuncArg")

        // Carried item 3: bare node both directions (not inside an AA).
        var nret: Dynamic? = null
        try {
            nret = owner.callFunc("nodeEcho", top.asDynamic())
        } catch (e: Throwable) {
            println("[SPIKE] barenode.callFuncRet call-THREW ${valueOrInvalid(rawMessage(e))}")
        }
        var ok = false
        var detail = " type=" + typeOf(nret)
        try {
            ok = isSameNodeSafe(nret, owner)
            if (typeOf(nret) == "roSGNode") {
                detail = detail + " subtype=" + subtypeOf(nret) + " id=" + valueOrInvalid(getMember(nret, "id"))
            }
        } catch (e: Throwable) {
            detail = detail + " threw:" + valueOrInvalid(rawMessage(e))
        }
        println("[SPIKE] barenode.callFuncRet ${pf(ok)}$detail")
        advance()
    }

    private fun stepRtq() {
        val q = createRtqOrInvalid()
        if (q == null) {
            println("[SPIKE] q1d.rtq.identity SKIP os<15")
            println("[SPIKE] q1d.rtq.nested SKIP os<15")
            println("[SPIKE] q1d.rtq.refIdentity SKIP os<15")
            advance()
            return
        }
        val payload = buildPayload("rtq")
        armWatchdog("rtq")
        q.postMessage("spike.kotlin", payload)
        // MOVE-semantics reach (Task 3 saw the sender's AA gutted at post
        // time): did the gutting reach the values the child still holds
        // direct references to — the vm object and the nested inner AA?
        try {
            println("[SPIKE] q1d.rtq.postState payloadKeys=${countOf(payload)} vmType=${typeOf(currentVm)} vmKeys=${describeKeys(currentVm)} vmCounter=${valueOrInvalid(getMember(currentVm, "counter"))} innerMarker=${valueOrInvalid(lookupOf(currentInner, "marker"))}")
        } catch (e: Throwable) {
            println("[SPIKE] q1d.rtq.postState THREW ${valueOrInvalid(rawMessage(e))}")
        }
    }

    // ---- post-hop plumbing ----

    private fun onAck(msg: RoSGNodeEvent) {
        val ch = "${msg.getData()}"
        if (ch == "") return
        if (ch != expectedAck) {
            println("[SPIKE] WARN unexpected-ack got=$ch expected=$expectedAck")
            return
        }
        watchdog?.setField("control", "stop")
        expectedAck = ""
        checkAfterHop(ch)
        advance()
    }

    private fun onWatchdog(msg: RoSGNodeEvent) {
        val ch = expectedAck
        if (ch == "") return
        expectedAck = ""
        println("[SPIKE] $ch.NO-ACK observer-or-handler-never-fired (2s timeout)")
        println("[SPIKE] q1d.$ch.identity FAIL no-delivery")
        println("[SPIKE] q1d.$ch.nested FAIL no-delivery")
        println("[SPIKE] q1d.$ch.refIdentity FAIL no-delivery")
        advance()
    }
}
