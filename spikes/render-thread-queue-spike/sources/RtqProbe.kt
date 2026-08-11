package com.nuvyyo.roku.components.rtqprobe

import kotlin.brs.BrsInline
import kotlin.brs.roku.*

// roRenderThreadQueue spike probe (render thread side).
//
// The stdlib has NO roRenderThreadQueue API yet — @BrsInline shims splice the
// raw BrightScript calls at the call site. Do NOT promote these to stdlib API
// here; Phase 1 adds the real interface informed by this spike's findings.
//
// BOTH RtqProbe instances register a handler for the SAME channel id
// ("spike.pump") from init — deliberately mirroring the production
// PumpScheduler configuration, where every component's scheduler registers the
// shared pump channel. One post to the channel then answers three questions at
// once: how many handler invocations fire (channel identity / multi-handler),
// and in which script context each runs (the m/GetGlobalAA probes).
@BrsInline("return type(CreateObject(\"roRenderThreadQueue\"))")
private external fun rtqCreateTypeName(): String

@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
private external fun rtqCreate(): Dynamic?

@BrsInline("return q.AddMessageHandler(channel, handlerName)")
private external fun rtqAddHandler(q: Dynamic?, channel: String, handlerName: String): Dynamic?

@BrsInline("q.PostMessage(channel, data)")
private external fun rtqPost(q: Dynamic?, channel: String, data: Any?)

@BrsInline("return GetGlobalAA()")
private external fun globalAA(): RoAssociativeArray

@BrsInline("return FormatJson(v)")
private external fun toJson(v: Any?): String

// m-scope probes: these splice INTO the enclosing generated function body, so
// when called from the message handler they report whatever script context the
// platform invokes the handler in.
@BrsInline("return type(m)")
private external fun scopeMType(): String

@BrsInline("return type(m.top)")
private external fun scopeMTopType(): String

@BrsInline("return m.top.subtype()")
private external fun scopeMTopSubtype(): String

@BrsInline("return m.top.id")
private external fun scopeMTopId(): String

class RtqProbe : GroupComponent() {

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onCommand")
    var command: String = ""

    // Mirrors every handler receipt to the main-thread driver (port-observed).
    @SGStringField(alwaysNotify = true)
    var lastEvent: String = ""

    @SGStringField
    var initReport: String = ""

    private var rtq: Dynamic? = null
    private var instanceNum: Int = 0
    private val clock: RoTimespan = RoTimespan.create()
    private var postAtMs: Int = -1

    init {
        clock.mark()

        // GetGlobalAA identity markers (check: globalaa_identity). If the AA
        // is shared per render thread, the second instance sees counter=1 and
        // bumps it to 2; if per-instance, both see a fresh AA and stay at 1.
        val gaa = globalAA()
        var n = 1
        val prev = gaa.lookup("spike_counter")
        if (prev != null) {
            n = (prev as Int) + 1
        }
        gaa.addReplace("spike_counter", n)
        gaa.addReplace("spike_marker_$n", "present")
        instanceNum = n

        // roRenderThreadQueue creation on the render thread (check: rtq_create).
        val rtqType = rtqCreateTypeName()
        val q = rtqCreate()
        rtq = q

        // m.global validity at init time (needed for the session-wide backend
        // cache in the production design).
        val globalType = typeOf(global)

        // Handler registration from component init (check: rtq_handler_registration).
        var tokenType = "no-rtq"
        if (q != null) {
            val token = rtqAddHandler(q, "spike.pump", brsName(::onRtqMessage))
            tokenType = typeOf(token)
        }

        val ok = rtqType == "roRenderThreadQueue" && tokenType != "no-rtq" &&
            tokenType != "Invalid" && tokenType != "<uninitialized>"
        if (ok) {
            println("SPIKE|PASS|rtq_create_and_register|instance=$n|rtqType=$rtqType|token=$tokenType|mGlobal=$globalType")
        } else {
            println("SPIKE|FAIL|rtq_create_and_register|instance=$n|rtqType=$rtqType|token=$tokenType|mGlobal=$globalType")
        }
        initReport = "rtqType=$rtqType|token=$tokenType|mGlobal=$globalType|counter=$n"
    }

    private fun onCommand(msg: RoSGNodeEvent) {
        val cmd = "${msg.getData()}"
        val myId = "${top.getField("id")}"
        val q = rtq
        if (cmd == "selfpost") {
            postAtMs = clock.totalMilliseconds()
            val payload = RoAssociativeArray.create()
            payload.addReplace("src", "A-self")
            rtqPost(q, "spike.pump", payload)
            println("SPIKE|INFO|selfpost_sent|id=$myId|at=${postAtMs}ms")
        } else if (cmd == "crosspost") {
            val payload = RoAssociativeArray.create()
            payload.addReplace("src", "B-cross")
            rtqPost(q, "spike.pump", payload)
            println("SPIKE|INFO|crosspost_sent|id=$myId")
        } else if (cmd == "earlypost") {
            // Post to a channel NOBODY has registered yet (check: rtq_post_before_handler).
            val payload = RoAssociativeArray.create()
            payload.addReplace("src", "B-early")
            rtqPost(q, "spike.early", payload)
            println("SPIKE|INFO|earlypost_sent|id=$myId")
        } else if (cmd == "registerlate") {
            val token = rtqAddHandler(q, "spike.early", brsName(::onRtqMessage))
            println("SPIKE|INFO|late_registration|id=$myId|token=${typeOf(token)}")
        } else if (cmd == "readglobals") {
            val gaa = globalAA()
            println("SPIKE|INFO|gaa_identity|id=$myId|instanceProp=$instanceNum|counter=${gaa.lookup("spike_counter")}|marker1=${gaa.doesExist("spike_marker_1")}|marker2=${gaa.doesExist("spike_marker_2")}")
        }
    }

    // Message handler, signature per RokuDocs ifRenderThreadQueue:
    //   sub Handler(data, msgInfo)
    // Defensive: if the platform invokes this in a foreign script context, the
    // m probes / property reads / field write will misbehave — that outcome is
    // exactly what the spike measures, so catch and report instead of crashing.
    private fun onRtqMessage(data: Dynamic?, msgInfo: Dynamic?) {
        try {
            val at = clock.totalMilliseconds()
            val mType = scopeMType()
            val topType = scopeMTopType()
            var subtype = "n/a"
            var nodeId = "n/a"
            if (topType == "roSGNode") {
                subtype = scopeMTopSubtype()
                nodeId = scopeMTopId()
            }
            var src = "no-data"
            if (data != null) {
                src = "${(data as RoAssociativeArray).lookup("src")}"
            }
            var latency = -1
            if (postAtMs >= 0 && src == "A-self") {
                latency = at - postAtMs
            }
            println("SPIKE|INFO|pump_handler|src=$src|latencyMs=$latency|mType=$mType|topType=$topType|subtype=$subtype|nodeId=$nodeId|instanceProp=$instanceNum")
            println("SPIKE|INFO|pump_msginfo|src=$src|info=${toJson(msgInfo)}")
            lastEvent = src
        } catch (e: Throwable) {
            println("SPIKE|INFO|pump_handler_error|${e.message}")
        }
    }
}
