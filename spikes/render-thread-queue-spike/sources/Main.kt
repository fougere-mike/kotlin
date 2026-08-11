// render-thread-queue spike: characterize roRenderThreadQueue (Roku OS 15.0+)
// as the immediate-wakeup backend for the component coroutine PumpScheduler,
// plus two design-gating side checks that matter even without RTQ:
// GetGlobalAA identity across component instances, and whether unparented
// Timer nodes fire.
//
// Main-thread driver: creates the scene + two RtqProbe instances, drives them
// through command fields on a time script, port-observes their lastEvent
// mirrors, and prints SPIKE| verdicts.
package com.nuvyyo.roku

import kotlin.brs.BrsInline
import kotlin.brs.roku.*

// Port-form observe shim (same as port-observe-spike; still no stdlib API).
@BrsInline("return node.observeField(fieldName, port)")
private external fun observeFieldWithPort(node: RoSGNode, fieldName: String, port: RoMessagePort): Boolean

@BrsInline("return type(CreateObject(\"roRenderThreadQueue\"))")
private external fun rtqCreateTypeNameMain(): String

@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
private external fun rtqCreateMain(): Dynamic?

@BrsInline("return q.AddMessageHandler(channel, handlerName)")
private external fun rtqAddHandlerMain(q: Dynamic?, channel: String, handlerName: String): Dynamic?

private fun check(name: String, pass: Boolean, detail: String) {
    if (pass) {
        println("SPIKE|PASS|$name|$detail")
    } else {
        println("SPIKE|FAIL|$name|$detail")
    }
}

fun main() {
    val dt = RoDateTime.create()
    dt.mark()
    println("===SPIKE_SENTINEL_${dt.asSeconds()}===")
    println("SPIKE|START|render-thread-queue-spike")

    val screen = RoSGScreen.create()
    val port = RoMessagePort.create()
    screen.setMessagePort(port)
    val scene = screen.createScene("RtqSpikeScene")
    screen.show()

    // Two instances of the same probe: both register the shared "spike.pump"
    // channel in init (the production PumpScheduler configuration).
    val probeA = scene.createChild("RtqProbe")
    probeA.setField("id", "probeA")
    val probeB = scene.createChild("RtqProbe")
    probeB.setField("id", "probeB")

    observeFieldWithPort(probeA, "lastEvent", port)
    observeFieldWithPort(probeB, "lastEvent", port)

    val clock = RoTimespan.create()
    clock.mark()

    // Time-script flags.
    var reportsRead = false
    var sentSelf = false
    var sentCross = false
    var sentEarly = false
    var sentLateReg = false
    var startedTask = false
    var sentReadGlobals = false
    var timersStarted = false
    var mainRtqDone = false

    // Observations.
    var gotSelf = false
    var gotCross = false
    var gotTask = false
    var gotEarly = false
    var eventCount = 0
    var timer1Fired = false
    var timer2Fired = false

    // Keep node handles alive for the whole loop.
    var taskNode: RoSGNode? = null
    var timer1: RoSGNode? = null
    var orphanGroup: RoSGNode? = null

    while (clock.totalMilliseconds() < 14000) {
        val ms = clock.totalMilliseconds()

        if (!reportsRead && ms > 700) {
            reportsRead = true
            println("SPIKE|INFO|initReportA|${probeA.getField("initReport")}")
            println("SPIKE|INFO|initReportB|${probeB.getField("initReport")}")
        }
        if (!sentSelf && ms > 1000) {
            sentSelf = true
            probeA.setField("command", "selfpost")
        }
        if (!sentCross && ms > 2200) {
            sentCross = true
            probeB.setField("command", "crosspost")
        }
        if (!sentEarly && ms > 3400) {
            sentEarly = true
            probeB.setField("command", "earlypost")
        }
        if (!sentLateReg && ms > 4000) {
            sentLateReg = true
            probeA.setField("command", "registerlate")
        }
        if (!startedTask && ms > 5200) {
            startedTask = true
            val t = RoSGNode.create("RtqSpikeTask")
            taskNode = t
            t.setField("control", "RUN")
        }
        if (!sentReadGlobals && ms > 6600) {
            sentReadGlobals = true
            probeA.setField("command", "readglobals")
            probeB.setField("command", "readglobals")
        }
        if (!timersStarted && ms > 7200) {
            timersStarted = true
            // Fully unparented Timer.
            val t1 = RoSGNode.create("Timer")
            t1.setField("id", "timer1")
            observeFieldWithPort(t1, "fire", port)
            t1.setField("duration", 0.4)
            t1.setField("control", "start")
            timer1 = t1
            // Timer child of a Group that is itself not in the scene.
            val g = RoSGNode.create("Group")
            orphanGroup = g
            val t2 = g.createChild("Timer")
            t2.setField("id", "timer2")
            observeFieldWithPort(t2, "fire", port)
            t2.setField("duration", 0.4)
            t2.setField("control", "start")
        }
        if (!mainRtqDone && ms > 9500) {
            mainRtqDone = true
            // Main-thread creation + registration attempt LAST so a hard
            // failure can't kill the core checks (doc says AddMessageHandler
            // is render-thread-only; what "only" means is what we probe).
            println("SPIKE|INFO|main_rtq_create|type=${rtqCreateTypeNameMain()}")
            try {
                val q = rtqCreateMain()
                if (q != null) {
                    val tok = rtqAddHandlerMain(q, "spike.main", "RtqMainNeverCalled")
                    println("SPIKE|INFO|main_rtq_addhandler|token=${typeOf(tok)}")
                } else {
                    println("SPIKE|INFO|main_rtq_addhandler|skipped-no-rtq-on-main")
                }
            } catch (e: Throwable) {
                println("SPIKE|INFO|main_rtq_addhandler|threw=${e.message}")
            }
        }

        val msg = port.waitMessage(50)
        if (msg == null) continue
        if (typeOf(msg) != "roSGNodeEvent") continue
        val ev = msg as RoSGNodeEvent
        val field = ev.getField()
        val dataStr = "${ev.getData()}"
        // getNode() returns the node's id STRING on device (port-observe-spike
        // finding) — usable directly for correlation.
        val nodeId = "${ev.getNode()}"
        if (field == "lastEvent") {
            eventCount++
            println("SPIKE|EVENT|lastEvent|#$eventCount|from=$nodeId|src=$dataStr|at=${clock.totalMilliseconds()}ms")
            if (dataStr == "A-self") gotSelf = true
            if (dataStr == "B-cross") gotCross = true
            if (dataStr == "task") gotTask = true
            if (dataStr == "B-early") gotEarly = true
        } else if (field == "fire") {
            println("SPIKE|EVENT|timer_fire|from=$nodeId|at=${clock.totalMilliseconds()}ms")
            if (nodeId == "timer1") timer1Fired = true
            if (nodeId == "timer2") timer2Fired = true
        }
    }

    println("SPIKE|LOOP_DONE|events=$eventCount|elapsed=${clock.totalMilliseconds()}ms")

    // Core verdict: the pump's immediate-wakeup path (render-thread self-post
    // delivered back to a render-thread handler that reached its component's
    // scope — the scope details are in the pump_handler INFO lines).
    check("rtq_selfpost_delivered", gotSelf, "render-thread PostMessage -> handler -> lastEvent mirror received=$gotSelf")

    // Design-shaping observations (INFO, not gating).
    println("SPIKE|INFO|crosspost_delivered|$gotCross")
    println("SPIKE|INFO|task_post_delivered|$gotTask")
    println("SPIKE|INFO|early_post_delivered_after_late_registration|$gotEarly")
    println("SPIKE|INFO|unparented_timer_fired|timer1=$timer1Fired|timer2=$timer2Fired")

    println("SPIKE|END")

    // Linger briefly so the capture gets everything before the app exits.
    val lingering = RoTimespan.create()
    lingering.mark()
    while (lingering.totalMilliseconds() < 3000) {
        val msg = port.waitMessage(100)
        if (msg != null && typeOf(msg) == "roSGScreenEvent") {
            val ev = msg as RoSGScreenEvent
            if (ev.isScreenClosed()) return
        }
    }
    screen.close()
}
